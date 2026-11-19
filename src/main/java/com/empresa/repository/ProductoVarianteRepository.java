package com.empresa.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.empresa.entity.ProductoVariante;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Integer> {

	public boolean existsBySku(String sku);

	public boolean existsBySkuAndIdVarianteNot(String sku, int idVariante);

	/**
	 * Descuento ATOMICO de stock: solo actualiza si alcanza el stock.
	 * Devuelve las filas afectadas (0 = stock insuficiente). Evita la carrera
	 * "validar y luego descontar" cuando dos clientes compran lo ultimo a la vez.
	 */
	@Transactional
	@Modifying
	@Query("update ProductoVariante v set v.stock = v.stock - :var_cantidad where v.idVariante = :var_id and v.stock >= :var_cantidad")
	public int descuentaStock(@Param("var_id") int idVariante, @Param("var_cantidad") int cantidad);

	@Transactional
	@Modifying
	@Query("update ProductoVariante v set v.stock = v.stock + :var_cantidad where v.idVariante = :var_id")
	public int reponeStock(@Param("var_id") int idVariante, @Param("var_cantidad") int cantidad);

	@Query("select min(v.precio) from ProductoVariante v where v.producto.idProducto = :var_prod and v.estado = 1")
	public BigDecimal precioMinimo(@Param("var_prod") int idProducto);

	@Query("select v from ProductoVariante v where v.producto.idProducto = :var_prod and v.estado = 1")
	public List<ProductoVariante> listaActivasPorProducto(@Param("var_prod") int idProducto);

}
