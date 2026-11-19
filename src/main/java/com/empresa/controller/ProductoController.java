package com.empresa.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.empresa.entity.Producto;
import com.empresa.entity.ProductoVariante;
import com.empresa.service.ProductoService;
import com.empresa.util.NegocioException;

/**
 * CRUD de productos y presentaciones (variantes) para el personal (requiere JWT de Administrador o Vendedor).
 */
@RestController
@RequestMapping("/url/producto")
public class ProductoController {

	@Autowired
	private ProductoService productoService;

	@ResponseBody
	@GetMapping("/listaProducto/{filtro}")
	public List<Producto> listaProducto(
			@PathVariable("filtro") String filtro,
			@RequestParam(name = "page", defaultValue = "0", required = false) int page,
			@RequestParam(name = "size", defaultValue = "10", required = false) int size) {
		Pageable paginable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
				Sort.by(Sort.Direction.DESC, "idProducto"));
		return productoService.listaProductoAdmin(filtro.equals("todos") ? "%" : "%" + filtro + "%", paginable);
	}

	@ResponseBody
	@PostMapping("/registraProducto")
	public HashMap<String, Object> registraProducto(@RequestBody Producto objProducto) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		try {
			Producto objSalida = productoService.registraProducto(objProducto);
			mapSalida.put("mensaje", "Se registró el producto ==> " + objSalida.getIdProducto());
			mapSalida.put("data", objSalida);
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		}
		return mapSalida;
	}

	@ResponseBody
	@PutMapping("/actualizaProducto")
	public HashMap<String, Object> actualizaProducto(@RequestBody Producto objProducto) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		try {
			Producto objSalida = productoService.actualizaProducto(objProducto);
			if (objSalida == null) {
				mapSalida.put("mensaje", "El producto no existe");
			} else {
				mapSalida.put("mensaje", "Se actualizó el producto ==> " + objSalida.getIdProducto());
				mapSalida.put("data", objSalida);
			}
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		}
		return mapSalida;
	}

	@ResponseBody
	@PutMapping("/eliminaProducto/{id}")
	public HashMap<String, Object> eliminaProducto(@PathVariable("id") int id) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		if (productoService.eliminaProducto(id)) {
			mapSalida.put("mensaje", "Se eliminó el producto ==> " + id);
		} else {
			mapSalida.put("mensaje", "El producto no existe");
		}
		return mapSalida;
	}

	// ============ PRESENTACIONES (variantes) ============

	@ResponseBody
	@PostMapping("/registraVariante/{idProducto}")
	public HashMap<String, Object> registraVariante(@PathVariable("idProducto") int idProducto,
			@RequestBody ProductoVariante objVariante) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		try {
			ProductoVariante objSalida = productoService.registraVariante(idProducto, objVariante);
			if (objSalida == null) {
				mapSalida.put("mensaje", "El producto no existe");
			} else {
				mapSalida.put("mensaje", "Se registró la presentación ==> " + objSalida.getPresentacion());
				mapSalida.put("data", objSalida);
			}
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		}
		return mapSalida;
	}

	@ResponseBody
	@PutMapping("/actualizaVariante")
	public HashMap<String, Object> actualizaVariante(@RequestBody ProductoVariante objVariante) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		try {
			ProductoVariante objSalida = productoService.actualizaVariante(objVariante);
			if (objSalida == null) {
				mapSalida.put("mensaje", "La presentación no existe");
			} else {
				mapSalida.put("mensaje", "Se actualizó la presentación ==> " + objSalida.getPresentacion());
				mapSalida.put("data", objSalida);
			}
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		}
		return mapSalida;
	}

	@ResponseBody
	@PutMapping("/eliminaVariante/{id}")
	public HashMap<String, Object> eliminaVariante(@PathVariable("id") int id) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		if (productoService.eliminaVariante(id)) {
			mapSalida.put("mensaje", "Se eliminó la presentación ==> " + id);
		} else {
			mapSalida.put("mensaje", "La presentación no existe");
		}
		return mapSalida;
	}

}
