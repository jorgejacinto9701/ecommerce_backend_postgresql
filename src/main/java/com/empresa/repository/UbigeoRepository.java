package com.empresa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.empresa.entity.Ubigeo;

public interface UbigeoRepository extends JpaRepository<Ubigeo, Integer> {

	@Query("select distinct x.departamento from Ubigeo x where x.estado = 1 order by x.departamento")
	public List<String> listaDepartamento();

	@Query("select distinct x.provincia from Ubigeo x where x.estado = 1 and x.departamento = :var_dep order by x.provincia")
	public List<String> listaProvincia(@Param("var_dep") String departamento);

	@Query("select x from Ubigeo x where x.estado = 1 and x.departamento = :var_dep and x.provincia = :var_prov order by x.distrito")
	public List<Ubigeo> listaDistrito(@Param("var_dep") String departamento, @Param("var_prov") String provincia);

}
