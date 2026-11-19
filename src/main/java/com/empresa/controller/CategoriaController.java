package com.empresa.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.empresa.entity.Categoria;
import com.empresa.service.CategoriaService;
import com.empresa.util.AppSettings;
import com.empresa.util.NegocioException;

/**
 * CRUD de categorias para el ADMINISTRADOR (requiere JWT).
 */
@RestController
@RequestMapping("/url/categoria")
public class CategoriaController {

	@Autowired
	private CategoriaService categoriaService;

	@ResponseBody
	@GetMapping("/listaCategoria")
	public List<Categoria> listaCategoria() {
		return categoriaService.listaTodos();
	}

	@ResponseBody
	@PostMapping("/registraCategoria")
	public HashMap<String, Object> registraCategoria(@RequestBody Categoria objCategoria) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		objCategoria.setIdCategoria(0);
		objCategoria.setEstado(AppSettings.ACTIVO);
		try {
			Categoria objSalida = categoriaService.insertaActualizaCategoria(objCategoria);
			mapSalida.put("mensaje", "Se registró la categoría ==> " + objSalida.getDescripcion());
			mapSalida.put("data", objSalida);
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		}
		return mapSalida;
	}

}
