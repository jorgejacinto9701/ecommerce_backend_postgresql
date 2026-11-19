package com.empresa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empresa.entity.Especie;

public interface EspecieRepository extends JpaRepository<Especie, Integer> {

	public List<Especie> findByEstadoOrderByNombre(int estado);

}
