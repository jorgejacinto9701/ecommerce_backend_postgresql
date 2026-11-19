package com.empresa.service;

import java.util.List;

import com.empresa.entity.Categoria;

public interface CategoriaService {

	public abstract List<Categoria> listaActivos();
	public abstract List<Categoria> listaTodos();
	public abstract Categoria insertaActualizaCategoria(Categoria obj);

}
