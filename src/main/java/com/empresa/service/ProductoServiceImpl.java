package com.empresa.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.empresa.entity.Producto;
import com.empresa.entity.ProductoVariante;
import com.empresa.repository.ProductoRepository;
import com.empresa.repository.ProductoVarianteRepository;
import com.empresa.util.AppSettings;
import com.empresa.util.NegocioException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

	private final ProductoRepository repository;
	private final ProductoVarianteRepository varianteRepository;

	@Override
	public List<Producto> buscaCatalogo(String filtro, int idCategoria, int idMarca, int idEspecie,
			BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
		return repository.buscaCatalogo(filtro, idCategoria, idMarca, idEspecie, precioMin, precioMax, pageable);
	}

	@Override
	public List<Producto> listaProductoAdmin(String filtro, Pageable pageable) {
		return repository.listaProductoAdmin(filtro, pageable);
	}

	@Override
	public Optional<Producto> buscaProducto(int idProducto) {
		return repository.findById(idProducto);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Producto registraProducto(Producto obj) {
		List<ProductoVariante> nuevas = obj.getVariantes() == null
				? new ArrayList<ProductoVariante>() : new ArrayList<ProductoVariante>(obj.getVariantes());
		if (nuevas.isEmpty()) {
			throw new NegocioException("Registra al menos una presentación (precio y stock)");
		}
		validaDatosProducto(obj);

		obj.setIdProducto(0);
		obj.setVariantes(new ArrayList<ProductoVariante>());
		obj.setPrecioDesde(null);
		obj.setEstado(AppSettings.ACTIVO);
		obj.setFechaRegistro(new Date());
		obj.setFechaActualizacion(new Date());
		Producto guardado = repository.save(obj);

		for (ProductoVariante v : nuevas) {
			preparaVariante(v, guardado);
			varianteRepository.save(v);
		}
		recalculaPrecioDesde(guardado);
		return guardado;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Producto actualizaProducto(Producto obj) {
		Optional<Producto> opt = repository.findById(obj.getIdProducto());
		if (opt.isEmpty()) {
			return null;
		}
		validaDatosProducto(obj);
		Producto actual = opt.get();
		actual.setNombre(obj.getNombre());
		actual.setDescripcion(obj.getDescripcion());
		actual.setIngredientes(obj.getIngredientes());
		actual.setImagenId(obj.getImagenId());
		actual.setCategoria(obj.getCategoria());
		actual.setMarca(obj.getMarca() != null && obj.getMarca().getIdMarca() > 0 ? obj.getMarca() : null);
		actual.setEspecie(obj.getEspecie() != null && obj.getEspecie().getIdEspecie() > 0 ? obj.getEspecie() : null);
		actual.setEtapaVida(obj.getEtapaVida() != null && obj.getEtapaVida().getIdDataCatalogo() > 0 ? obj.getEtapaVida() : null);
		actual.setTamanoRaza(obj.getTamanoRaza() != null && obj.getTamanoRaza().getIdDataCatalogo() > 0 ? obj.getTamanoRaza() : null);
		actual.setFechaActualizacion(new Date());
		return repository.save(actual);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean eliminaProducto(int idProducto) {
		Optional<Producto> opt = repository.findById(idProducto);
		if (opt.isEmpty()) {
			return false;
		}
		// Eliminacion logica
		Producto p = opt.get();
		p.setEstado(AppSettings.INACTIVO);
		p.setFechaActualizacion(new Date());
		repository.save(p);
		return true;
	}

	// ============ VARIANTES ============

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProductoVariante registraVariante(int idProducto, ProductoVariante obj) {
		Optional<Producto> opt = repository.findById(idProducto);
		if (opt.isEmpty()) {
			return null;
		}
		preparaVariante(obj, opt.get());
		ProductoVariante guardada = varianteRepository.save(obj);
		recalculaPrecioDesde(opt.get());
		return guardada;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProductoVariante actualizaVariante(ProductoVariante obj) {
		Optional<ProductoVariante> opt = varianteRepository.findById(obj.getIdVariante());
		if (opt.isEmpty()) {
			return null;
		}
		ProductoVariante actual = opt.get();
		validaVariante(obj);
		if (varianteRepository.existsBySkuAndIdVarianteNot(obj.getSku().trim(), actual.getIdVariante())) {
			throw new NegocioException("El SKU " + obj.getSku() + " ya existe");
		}
		actual.setSku(obj.getSku().trim());
		actual.setPresentacion(obj.getPresentacion().trim());
		actual.setPrecio(obj.getPrecio());
		actual.setPrecioOferta(obj.getPrecioOferta());
		actual.setOfertaDesde(obj.getOfertaDesde());
		actual.setOfertaHasta(obj.getOfertaHasta());
		actual.setStock(obj.getStock());
		actual.setLote(obj.getLote());
		actual.setFechaVencimiento(obj.getFechaVencimiento());
		ProductoVariante guardada = varianteRepository.save(actual);
		recalculaPrecioDesde(actual.getProducto());
		return guardada;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean eliminaVariante(int idVariante) {
		Optional<ProductoVariante> opt = varianteRepository.findById(idVariante);
		if (opt.isEmpty()) {
			return false;
		}
		ProductoVariante v = opt.get();
		v.setEstado(AppSettings.INACTIVO);
		varianteRepository.save(v);
		recalculaPrecioDesde(v.getProducto());
		return true;
	}

	// ============ UTILITARIOS ============

	private void validaDatosProducto(Producto obj) {
		if (obj.getNombre() == null || obj.getNombre().trim().length() < 3) {
			throw new NegocioException("El nombre del producto es obligatorio (mínimo 3 caracteres)");
		}
		if (obj.getCategoria() == null || obj.getCategoria().getIdCategoria() <= 0) {
			throw new NegocioException("Selecciona la categoría");
		}
	}

	private void validaVariante(ProductoVariante v) {
		if (v.getSku() == null || v.getSku().trim().isEmpty()) {
			throw new NegocioException("El SKU es obligatorio");
		}
		if (v.getPresentacion() == null || v.getPresentacion().trim().isEmpty()) {
			throw new NegocioException("La presentación es obligatoria (ej: Bolsa 3 kg, Talla M)");
		}
		if (v.getPrecio() == null || v.getPrecio().signum() <= 0) {
			throw new NegocioException("El precio debe ser mayor a cero");
		}
		if (v.getPrecioOferta() != null && v.getPrecioOferta().compareTo(v.getPrecio()) >= 0) {
			throw new NegocioException("El precio de oferta debe ser menor al precio regular");
		}
		if (v.getOfertaDesde() != null && v.getOfertaHasta() != null && v.getOfertaHasta().before(v.getOfertaDesde())) {
			throw new NegocioException("La fecha fin de la oferta es anterior a la de inicio");
		}
		if (v.getStock() < 0) {
			throw new NegocioException("El stock no puede ser negativo");
		}
	}

	private void preparaVariante(ProductoVariante v, Producto producto) {
		validaVariante(v);
		if (varianteRepository.existsBySku(v.getSku().trim())) {
			throw new NegocioException("El SKU " + v.getSku() + " ya existe");
		}
		v.setIdVariante(0);
		v.setSku(v.getSku().trim());
		v.setPresentacion(v.getPresentacion().trim());
		v.setProducto(producto);
		v.setEstado(AppSettings.ACTIVO);
	}

	/** Deja en producto.precioDesde el menor precio regular entre sus variantes activas (null si no hay). */
	private void recalculaPrecioDesde(Producto producto) {
		varianteRepository.flush();
		BigDecimal minimo = varianteRepository.precioMinimo(producto.getIdProducto());
		producto.setPrecioDesde(minimo);
		repository.save(producto);
	}

}
