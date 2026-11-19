package com.empresa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empresa.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

	public List<Categoria> findByEstado(int estado);

}
