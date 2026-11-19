package com.empresa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empresa.entity.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {

	public List<Marca> findByEstadoOrderByNombre(int estado);

	public boolean existsByNombreIgnoreCase(String nombre);

}
