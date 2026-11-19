package com.empresa.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.empresa.entity.Producto;
import com.empresa.entity.ProductoVariante;

public interface ProductoService {

	public abstract List<Producto> buscaCatalogo(String filtro, int idCategoria, int idMarca, int idEspecie,
			BigDecimal precioMin, BigDecimal precioMax, Pageable pageable);
	public abstract List<Producto> listaProductoAdmin(String filtro, Pageable pageable);
	public abstract Optional<Producto> buscaProducto(int idProducto);

	/** Registra el producto con las variantes que traiga en su lista (al menos una). */
	public abstract Producto registraProducto(Producto obj);
	/** Actualiza los datos del producto; las variantes se gestionan aparte. */
	public abstract Producto actualizaProducto(Producto obj);
	public abstract boolean eliminaProducto(int idProducto);

	public abstract ProductoVariante registraVariante(int idProducto, ProductoVariante obj);
	public abstract ProductoVariante actualizaVariante(ProductoVariante obj);
	public abstract boolean eliminaVariante(int idVariante);

}
