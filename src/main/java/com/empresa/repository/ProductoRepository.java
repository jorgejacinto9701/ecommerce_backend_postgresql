package com.empresa.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.empresa.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

	/**
	 * Catalogo publico con filtros opcionales (0 = sin filtro).
	 * La categoria filtra por la elegida o por sus hijas (2 niveles).
	 * precioDesde IS NOT NULL deja fuera productos sin variantes activas.
	 */
	@Query("select x from Producto x "
			+ "join x.categoria c "
			+ "left join c.categoriaPadre cp "
			+ "left join x.marca m "
			+ "left join x.especie e "
			+ "where x.estado = 1 and x.precioDesde is not null "
			+ "and (upper(x.nombre) like upper(:var_fil) or upper(m.nombre) like upper(:var_fil)) "
			+ "and (:var_cat = 0 or c.idCategoria = :var_cat or cp.idCategoria = :var_cat) "
			+ "and (:var_marca = 0 or m.idMarca = :var_marca) "
			+ "and (:var_esp = 0 or e.idEspecie = :var_esp) "
			+ "and x.precioDesde >= :var_min and x.precioDesde <= :var_max")
	public List<Producto> buscaCatalogo(@Param("var_fil") String filtro, @Param("var_cat") int idCategoria,
			@Param("var_marca") int idMarca, @Param("var_esp") int idEspecie,
			@Param("var_min") BigDecimal precioMin, @Param("var_max") BigDecimal precioMax, Pageable pageable);

	@Query("Select x from Producto x where upper(x.nombre) like upper(:var_fil)")
	public List<Producto> listaProductoAdmin(@Param("var_fil") String filtro, Pageable pageable);

}
