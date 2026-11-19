package com.empresa.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.empresa.dto.CheckoutDto;
import com.empresa.dto.ClienteLoginDto;
import com.empresa.entity.Categoria;
import com.empresa.entity.Cliente;
import com.empresa.entity.DataCatalogo;
import com.empresa.entity.Especie;
import com.empresa.entity.Marca;
import com.empresa.entity.Pedido;
import com.empresa.entity.Producto;
import com.empresa.entity.Reclamo;
import com.empresa.entity.Ubigeo;
import com.empresa.security.JwtProvider;
import com.empresa.service.CategoriaService;
import com.empresa.service.ClienteService;
import com.empresa.service.DataCatalogoService;
import com.empresa.service.PedidoService;
import com.empresa.service.ProductoService;
import com.empresa.service.TiendaCatalogoService;
import com.empresa.util.AppSettings;
import com.empresa.util.NegocioException;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints de la tienda. Son PUBLICOS salvo registraPedido y misPedidos,
 * que exigen el JWT del cliente (ver SecurityConfig); el cliente sale del token.
 */
@RestController
@RequestMapping("/url/tienda")
@RequiredArgsConstructor
public class TiendaController {

	private final ProductoService productoService;
	private final CategoriaService categoriaService;
	private final ClienteService clienteService;
	private final PedidoService pedidoService;
	private final TiendaCatalogoService tiendaCatalogoService;
	private final DataCatalogoService dataCatalogoService;
	private final JwtProvider jwtProvider;

	// ============ CATALOGO ============

	@ResponseBody
	@GetMapping("/listaCategoria")
	public List<Categoria> listaCategoria() {
		return categoriaService.listaActivos();
	}

	@ResponseBody
	@GetMapping("/listaMarca")
	public List<Marca> listaMarca() {
		return tiendaCatalogoService.listaMarca();
	}

	@ResponseBody
	@GetMapping("/listaEspecie")
	public List<Especie> listaEspecie() {
		return tiendaCatalogoService.listaEspecie();
	}

	/**
	 * Catalogo con filtros. 0 = sin filtro. orden: recientes | precio_asc | precio_desc | nombre
	 */
	@ResponseBody
	@GetMapping("/listaProducto/{filtro}")
	public List<Producto> listaProducto(
			@PathVariable String filtro,
			@RequestParam(defaultValue = "0", required = false) int idCategoria,
			@RequestParam(defaultValue = "0", required = false) int idMarca,
			@RequestParam(defaultValue = "0", required = false) int idEspecie,
			@RequestParam(required = false) BigDecimal precioMin,
			@RequestParam(required = false) BigDecimal precioMax,
			@RequestParam(defaultValue = "recientes", required = false) String orden,
			@RequestParam(defaultValue = "0", required = false) int page,
			@RequestParam(defaultValue = "8", required = false) int size) {

		Sort sort;
		switch (orden) {
		case "precio_asc":
			sort = Sort.by(Sort.Direction.ASC, "precioDesde");
			break;
		case "precio_desc":
			sort = Sort.by(Sort.Direction.DESC, "precioDesde");
			break;
		case "nombre":
			sort = Sort.by(Sort.Direction.ASC, "nombre");
			break;
		default:
			sort = Sort.by(Sort.Direction.DESC, "idProducto");
		}
		// Tope al tamano de pagina para que nadie pida el catalogo entero de una vez
		Pageable paginable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 48), sort);

		String var_filtro = filtro.equals("todos") ? "%" : "%" + filtro + "%";
		BigDecimal min = precioMin == null ? BigDecimal.ZERO : precioMin;
		BigDecimal max = precioMax == null ? new BigDecimal("99999999") : precioMax;
		return productoService.buscaCatalogo(var_filtro, idCategoria, idMarca, idEspecie, min, max, paginable);
	}

	@ResponseBody
	@GetMapping("/producto/{id}")
	public Producto buscaProducto(@PathVariable int id) {
		Optional<Producto> opt = productoService.buscaProducto(id);
		// Solo productos activos y con presentaciones disponibles
		if (opt.isPresent() && opt.get().getEstado() == AppSettings.ACTIVO && opt.get().getPrecioDesde() != null) {
			return opt.get();
		}
		return null;
	}

	// ============ APOYO A CHECKOUT ============

	@ResponseBody
	@GetMapping("/listaMetodoPago")
	public List<DataCatalogo> listaMetodoPago() {
		return dataCatalogoService.listaDataCatalogo(AppSettings.CATALOGO_02_METODO_DE_PAGO);
	}

	@ResponseBody
	@GetMapping("/listaTipoComprobante")
	public List<DataCatalogo> listaTipoComprobante() {
		return dataCatalogoService.listaDataCatalogo(AppSettings.CATALOGO_04_TIPO_COMPROBANTE);
	}

	@ResponseBody
	@GetMapping("/ubigeo/departamentos")
	public List<String> listaDepartamento() {
		return tiendaCatalogoService.listaDepartamento();
	}

	@ResponseBody
	@GetMapping("/ubigeo/provincias")
	public List<String> listaProvincia(@RequestParam String departamento) {
		return tiendaCatalogoService.listaProvincia(departamento);
	}

	@ResponseBody
	@GetMapping("/ubigeo/distritos")
	public List<Ubigeo> listaDistrito(
			@RequestParam String departamento,
			@RequestParam String provincia) {
		return tiendaCatalogoService.listaDistrito(departamento, provincia);
	}

	// ============ CLIENTE ============

	@ResponseBody
	@PostMapping("/registraCliente")
	public HashMap<String, Object> registraCliente(@RequestBody Cliente objCliente) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		if (objCliente.getCorreo() == null || objCliente.getCorreo().trim().isEmpty()
				|| objCliente.getPassword() == null || objCliente.getPassword().length() < 8) {
			mapSalida.put("mensaje", "Ingresa un correo y una contraseña de al menos 8 caracteres");
			return mapSalida;
		}
		Optional<Cliente> optExiste = clienteService.buscaPorCorreo(objCliente.getCorreo());
		if (optExiste.isPresent()) {
			mapSalida.put("mensaje", "El correo " + objCliente.getCorreo() + " ya está registrado");
			return mapSalida;
		}
		objCliente.setIdCliente(0);
		Cliente objSalida = clienteService.registraCliente(objCliente);
		if (objSalida != null) {
			mapSalida.put("mensaje", "Se registró el cliente ==> " + objSalida.getNombreCompleto());
			mapSalida.put("data", objSalida);
		} else {
			mapSalida.put("mensaje", "Error en el registro del cliente");
		}
		return mapSalida;
	}

	/** Login del cliente: devuelve sus datos y el JWT que debe enviar en checkout y "mis pedidos". */
	@ResponseBody
	@PostMapping("/loginCliente")
	public HashMap<String, Object> loginCliente(@RequestBody ClienteLoginDto loginDto) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		Cliente objCliente = clienteService.login(loginDto.getCorreo(), loginDto.getPassword());
		if (objCliente != null) {
			mapSalida.put("mensaje", "Bienvenido " + objCliente.getNombreCompleto());
			mapSalida.put("data", objCliente);
			mapSalida.put("token", jwtProvider.generateTokenCliente(objCliente));
		} else {
			mapSalida.put("mensaje", "Correo o contraseña incorrectos");
		}
		return mapSalida;
	}

	// ============ CHECKOUT / PEDIDOS (requieren JWT de cliente) ============

	@ResponseBody
	@PostMapping("/registraPedido")
	public HashMap<String, Object> registraPedido(@RequestBody CheckoutDto checkout, Authentication authentication) {
		int idCliente = (Integer) authentication.getPrincipal();
		return pedidoService.registraPedido(idCliente, checkout);
	}

	@ResponseBody
	@GetMapping("/misPedidos")
	public List<Pedido> misPedidos(Authentication authentication) {
		int idCliente = (Integer) authentication.getPrincipal();
		return pedidoService.listaPedidoPorCliente(idCliente);
	}

	// ============ LIBRO DE RECLAMACIONES ============

	@ResponseBody
	@PostMapping("/registraReclamo")
	public HashMap<String, Object> registraReclamo(@RequestBody Reclamo objReclamo) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		try {
			Reclamo objSalida = tiendaCatalogoService.registraReclamo(objReclamo);
			mapSalida.put("mensaje", "Se registró tu hoja de reclamación " + objSalida.getNumeroHoja());
			mapSalida.put("data", objSalida);
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		}
		return mapSalida;
	}

}
