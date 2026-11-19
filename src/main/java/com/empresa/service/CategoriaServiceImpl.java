package com.empresa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.empresa.entity.Categoria;
import com.empresa.repository.CategoriaRepository;
import com.empresa.util.AppSettings;
import com.empresa.util.NegocioException;

@Service
public class CategoriaServiceImpl implements CategoriaService {

	@Autowired
	private CategoriaRepository repository;

	@Override
	public List<Categoria> listaActivos() {
		return repository.findByEstado(AppSettings.ACTIVO);
	}

	@Override
	public List<Categoria> listaTodos() {
		return repository.findAll();
	}

	@Override
	public Categoria insertaActualizaCategoria(Categoria obj) {
		if (obj.getDescripcion() == null || obj.getDescripcion().trim().length() < 3) {
			throw new NegocioException("La descripción de la categoría es obligatoria");
		}
		// Se permiten 2 niveles: el padre debe existir y ser una categoria raiz
		if (obj.getCategoriaPadre() != null && obj.getCategoriaPadre().getIdCategoria() > 0) {
			Optional<Categoria> padre = repository.findById(obj.getCategoriaPadre().getIdCategoria());
			if (padre.isEmpty()) {
				throw new NegocioException("La categoría padre no existe");
			}
			if (padre.get().getCategoriaPadre() != null) {
				throw new NegocioException("Solo se permiten 2 niveles de categorías");
			}
			obj.setCategoriaPadre(padre.get());
		} else {
			obj.setCategoriaPadre(null);
		}
		return repository.save(obj);
	}

}
