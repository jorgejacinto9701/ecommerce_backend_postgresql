package com.empresa.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.empresa.dto.CheckoutDto;
import com.empresa.dto.ItemCheckoutDto;
import com.empresa.dto.ResultadoPagoDto;
import com.empresa.entity.Cliente;
import com.empresa.entity.DataCatalogo;
import com.empresa.entity.Pago;
import com.empresa.entity.Pedido;
import com.empresa.entity.PedidoHasProducto;
import com.empresa.entity.PedidoHasProductoPK;
import com.empresa.entity.ProductoVariante;
import com.empresa.entity.Ubigeo;
import com.empresa.repository.ClienteRepository;
import com.empresa.repository.DataCatalogoRepository;
import com.empresa.repository.PagoRepository;
import com.empresa.repository.PedidoHasProductoRepository;
import com.empresa.repository.PedidoRepository;
import com.empresa.repository.ProductoVarianteRepository;
import com.empresa.repository.UbigeoRepository;
import com.empresa.util.AppSettings;
import com.empresa.util.NegocioException;

import lombok.extern.apachecommons.CommonsLog;

@Service
@CommonsLog
public class PedidoServiceImpl implements PedidoService {

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private PedidoHasProductoRepository detalleRepository;

	@Autowired
	private PagoRepository pagoRepository;

	@Autowired
	private ProductoVarianteRepository varianteRepository;

	@Autowired
	private UbigeoRepository ubigeoRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private DataCatalogoRepository dataCatalogoRepository;

	@Autowired
	private PasarelaPagoService pasarelaPagoService;

	// Envio gratis desde este monto (0 = desactivado)
	@Value("${tienda.envio-gratis-desde:0}")
	private BigDecimal envioGratisDesde;

	/**
	 * Registra el pedido. El cliente sale del JWT y los precios, del catalogo
	 * en la base de datos: lo unico que se acepta del navegador es la variante y la cantidad.
	 *
	 * Orden: 1) validar, 2) reservar stock de forma atomica, 3) cobrar (si es tarjeta),
	 * 4) grabar pedido y pago. Si algo falla antes de grabar, se revierte la transaccion
	 * (el stock vuelve) y, si ya se habia cobrado, se revierte tambien el cobro.
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public HashMap<String, Object> registraPedido(int idCliente, CheckoutDto checkout) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		String codigoCobro = null;
		try {
			// ---------- 1. Validaciones ----------
			Optional<Cliente> optCliente = clienteRepository.findById(idCliente);
			if (optCliente.isEmpty() || optCliente.get().getEstado() != AppSettings.ACTIVO) {
				throw new NegocioException("El cliente no está habilitado para comprar");
			}
			if (checkout == null || checkout.getItems() == null || checkout.getItems().isEmpty()) {
				throw new NegocioException("El carrito está vacío");
			}

			boolean esDelivery = !AppSettings.ENTREGA_RECOJO.equals(checkout.getTipoEntrega());
			Ubigeo ubigeo = null;
			String direccion = null;
			if (esDelivery) {
				if (checkout.getIdUbigeo() == null) {
					throw new NegocioException("Selecciona el distrito de entrega");
				}
				Optional<Ubigeo> optUbigeo = ubigeoRepository.findById(checkout.getIdUbigeo());
				if (optUbigeo.isEmpty() || optUbigeo.get().getEstado() != AppSettings.ACTIVO) {
					throw new NegocioException("No hacemos envíos al distrito seleccionado");
				}
				ubigeo = optUbigeo.get();
				direccion = checkout.getDireccionEnvio() == null ? "" : checkout.getDireccionEnvio().trim();
				if (direccion.length() < 5) {
					throw new NegocioException("Ingresa la dirección de entrega");
				}
			} else {
				direccion = "Recojo en tienda";
			}

			int idMetodo = checkout.getIdMetodoPago();
			if (idMetodo != AppSettings.METODO_PAGO_TARJETA && idMetodo != AppSettings.METODO_PAGO_YAPE
					&& idMetodo != AppSettings.METODO_PAGO_PLIN && idMetodo != AppSettings.METODO_PAGO_CONTRA_ENTREGA) {
				throw new NegocioException("Método de pago no válido");
			}
			boolean esTarjeta = idMetodo == AppSettings.METODO_PAGO_TARJETA;

			// Se agrupan lineas repetidas y se ordenan por id para evitar bloqueos cruzados entre compras simultaneas
			Map<Integer, Integer> cantidades = new TreeMap<Integer, Integer>();
			for (ItemCheckoutDto it : checkout.getItems()) {
				if (it == null || it.getCantidad() < 1) {
					throw new NegocioException("Cantidad no válida en el carrito");
				}
				cantidades.merge(it.getIdVariante(), it.getCantidad(), Integer::sum);
			}

			// ---------- 2. Precios desde la BD + reserva ATOMICA de stock ----------
			BigDecimal totalProductos = BigDecimal.ZERO;
			List<PedidoHasProducto> detalles = new ArrayList<PedidoHasProducto>();
			for (Map.Entry<Integer, Integer> e : cantidades.entrySet()) {
				Optional<ProductoVariante> optVar = varianteRepository.findById(e.getKey());
				if (optVar.isEmpty() || optVar.get().getEstado() != AppSettings.ACTIVO
						|| optVar.get().getProducto().getEstado() != AppSettings.ACTIVO) {
					throw new NegocioException("Un producto del carrito ya no está disponible");
				}
				ProductoVariante v = optVar.get();
				int filas = varianteRepository.descuentaStock(v.getIdVariante(), e.getValue());
				if (filas == 0) {
					throw new NegocioException("Stock insuficiente para: " + v.getProducto().getNombre()
							+ " (" + v.getPresentacion() + ")");
				}
				BigDecimal precio = v.getPrecioVigente();
				totalProductos = totalProductos.add(precio.multiply(new BigDecimal(e.getValue())));

				PedidoHasProducto d = new PedidoHasProducto();
				PedidoHasProductoPK pk = new PedidoHasProductoPK();
				pk.setIdVariante(v.getIdVariante());
				d.setPedidoHasProductoPK(pk);
				d.setPrecio(precio);
				d.setCantidad(e.getValue());
				d.setVariante(v);
				detalles.add(d);
			}

			// ---------- 3. Montos (precios con IGV incluido) ----------
			BigDecimal costoEnvio = BigDecimal.ZERO;
			if (esDelivery) {
				costoEnvio = ubigeo.getTarifaEnvio();
				if (envioGratisDesde != null && envioGratisDesde.signum() > 0
						&& totalProductos.compareTo(envioGratisDesde) >= 0) {
					costoEnvio = BigDecimal.ZERO;
				}
			}
			BigDecimal total = totalProductos.add(costoEnvio).setScale(2, RoundingMode.HALF_UP);
			BigDecimal subTotal = total.divide(AppSettings.IGV_FACTOR, 2, RoundingMode.HALF_UP);
			BigDecimal igv = total.subtract(subTotal);

			// ---------- 4. Comprobante ----------
			String numeroDocumento = checkout.getNumeroDocumento() == null ? "" : checkout.getNumeroDocumento().trim();
			String razonSocial = checkout.getRazonSocial() == null ? "" : checkout.getRazonSocial().trim();
			if (checkout.getIdTipoComprobante() == AppSettings.COMPROBANTE_FACTURA) {
				if (!numeroDocumento.matches("(10|15|17|20)\\d{9}")) {
					throw new NegocioException("Para factura ingresa un RUC válido de 11 dígitos");
				}
				if (razonSocial.isEmpty()) {
					throw new NegocioException("Para factura ingresa la razón social");
				}
			} else if (checkout.getIdTipoComprobante() == AppSettings.COMPROBANTE_BOLETA) {
				razonSocial = null;
				if (!numeroDocumento.isEmpty() && !numeroDocumento.matches("\\d{8}")) {
					throw new NegocioException("El DNI debe tener 8 dígitos");
				}
				if (numeroDocumento.isEmpty() && total.compareTo(AppSettings.BOLETA_LIMITE_SIN_DOCUMENTO) > 0) {
					throw new NegocioException("Para boletas mayores a S/ 700 debes ingresar tu DNI");
				}
			} else {
				throw new NegocioException("Tipo de comprobante no válido");
			}

			// ---------- 5. Cobro con tarjeta (antes de grabar) ----------
			ResultadoPagoDto resultadoPago = null;
			if (esTarjeta) {
				resultadoPago = pasarelaPagoService.procesarPago(checkout.getTarjeta(), total);
				if (!resultadoPago.isAprobado()) {
					throw new NegocioException("Pago rechazado: " + resultadoPago.getMensaje());
				}
				codigoCobro = resultadoPago.getCodigoAutorizacion();
			}

			// ---------- 6. Grabar pedido, detalle y pago ----------
			Pedido pedido = new Pedido();
			pedido.setCliente(optCliente.get());
			pedido.setSubTotal(subTotal);
			pedido.setIgv(igv);
			pedido.setCostoEnvio(costoEnvio);
			pedido.setTotal(total);
			pedido.setTipoEntrega(esDelivery ? AppSettings.ENTREGA_DELIVERY : AppSettings.ENTREGA_RECOJO);
			pedido.setDireccionEnvio(direccion);
			pedido.setReferenciaEnvio(esDelivery ? checkout.getReferenciaEnvio() : null);
			pedido.setUbigeo(ubigeo);
			pedido.setNumeroDocumento(numeroDocumento.isEmpty() ? null : numeroDocumento);
			pedido.setRazonSocial(razonSocial);
			pedido.setTipoComprobante(dataCatalogoRepository.getReferenceById(checkout.getIdTipoComprobante()));
			pedido.setEstado(dataCatalogoRepository.getReferenceById(
					esTarjeta ? AppSettings.ESTADO_PEDIDO_PAGADO : AppSettings.ESTADO_PEDIDO_REGISTRADO));
			Pedido cabecera = pedidoRepository.save(pedido);
			cabecera.setNumeroPedido(String.format("PED-%06d", cabecera.getIdPedido()));
			cabecera = pedidoRepository.save(cabecera);

			for (PedidoHasProducto d : detalles) {
				d.getPedidoHasProductoPK().setIdPedido(cabecera.getIdPedido());
				d.setPedido(cabecera);
				detalleRepository.save(d);
			}
			cabecera.setDetallesPedido(detalles);

			Pago pago = new Pago();
			pago.setMonto(total);
			pago.setPedido(cabecera);
			pago.setMetodoPago(dataCatalogoRepository.getReferenceById(idMetodo));
			if (esTarjeta) {
				pago.setEstadoPago(dataCatalogoRepository.getReferenceById(AppSettings.ESTADO_PAGO_APROBADO));
				pago.setNumeroTarjeta(resultadoPago.getTarjetaEnmascarada());
				pago.setCodigoAutorizacion(resultadoPago.getCodigoAutorizacion());
			} else {
				pago.setEstadoPago(dataCatalogoRepository.getReferenceById(AppSettings.ESTADO_PAGO_PENDIENTE));
			}
			pagoRepository.save(pago);

			log.info(">>> registraPedido >>> " + cabecera.getNumeroPedido());
			mapSalida.put("mensaje", "Se registró el Pedido ==> " + cabecera.getNumeroPedido());
			mapSalida.put("data", cabecera);
			mapSalida.put("pago", pago);
			return mapSalida;

		} catch (NegocioException e) {
			if (codigoCobro != null) {
				pasarelaPagoService.reversaPago(codigoCobro);
			}
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			mapSalida.clear();
			mapSalida.put("mensaje", e.getMessage());
			return mapSalida;
		} catch (RuntimeException e) {
			// Error inesperado despues de cobrar: se devuelve el dinero y se relanza para revertir la transaccion
			if (codigoCobro != null) {
				pasarelaPagoService.reversaPago(codigoCobro);
			}
			throw e;
		}
	}

	@Override
	public List<Pedido> listaPedidoPorCliente(int idCliente) {
		return pedidoRepository.listaPedidoPorCliente(idCliente);
	}

	@Override
	public List<Pedido> listaPedidoTodos() {
		return pedidoRepository.listaPedidoTodos();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Pedido actualizaEstado(int idPedido, int idEstado) {
		if (idEstado < AppSettings.ESTADO_PEDIDO_REGISTRADO || idEstado > AppSettings.ESTADO_PEDIDO_ANULADO) {
			throw new NegocioException("Estado de pedido no válido");
		}
		Optional<Pedido> opt = pedidoRepository.findById(idPedido);
		if (opt.isEmpty()) {
			return null;
		}
		Pedido pedido = opt.get();
		int estadoActual = pedido.getEstado().getIdDataCatalogo();

		if (estadoActual == AppSettings.ESTADO_PEDIDO_ANULADO && idEstado != AppSettings.ESTADO_PEDIDO_ANULADO) {
			throw new NegocioException("Un pedido anulado no se puede reactivar; el cliente debe hacer un pedido nuevo");
		}

		// Si se anula el pedido, se repone el stock (una sola vez)
		if (idEstado == AppSettings.ESTADO_PEDIDO_ANULADO && estadoActual != AppSettings.ESTADO_PEDIDO_ANULADO) {
			for (PedidoHasProducto d : pedido.getDetallesPedido()) {
				varianteRepository.reponeStock(d.getPedidoHasProductoPK().getIdVariante(), d.getCantidad());
			}
		}

		pedido.setEstado(dataCatalogoRepository.getReferenceById(idEstado));
		return pedidoRepository.save(pedido);
	}

	/** Confirma manualmente un pago pendiente (Yape, Plin, contra entrega): pago Aprobado y pedido Pagado. */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Pedido confirmaPago(int idPedido) {
		Optional<Pedido> opt = pedidoRepository.findById(idPedido);
		if (opt.isEmpty()) {
			return null;
		}
		Pedido pedido = opt.get();
		if (pedido.getEstado().getIdDataCatalogo() == AppSettings.ESTADO_PEDIDO_ANULADO) {
			throw new NegocioException("El pedido está anulado");
		}
		boolean huboPendiente = false;
		for (Pago pago : pagoRepository.listaPagoPorPedido(idPedido)) {
			if (pago.getEstadoPago().getIdDataCatalogo() == AppSettings.ESTADO_PAGO_PENDIENTE) {
				pago.setEstadoPago(dataCatalogoRepository.getReferenceById(AppSettings.ESTADO_PAGO_APROBADO));
				pagoRepository.save(pago);
				huboPendiente = true;
			}
		}
		if (!huboPendiente) {
			throw new NegocioException("El pedido no tiene pagos pendientes");
		}
		if (pedido.getEstado().getIdDataCatalogo() == AppSettings.ESTADO_PEDIDO_REGISTRADO) {
			pedido.setEstado(dataCatalogoRepository.getReferenceById(AppSettings.ESTADO_PEDIDO_PAGADO));
			pedido = pedidoRepository.save(pedido);
		}
		return pedido;
	}

}
