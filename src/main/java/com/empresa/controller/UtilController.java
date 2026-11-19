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
import com.empresa.entity.DataCatalogo;
import com.empresa.entity.Especie;
import com.empresa.entity.Marca;
import com.empresa.entity.Usuario;
import com.empresa.service.CategoriaService;
import com.empresa.service.DataCatalogoService;
import com.empresa.service.TiendaCatalogoService;
import com.empresa.service.UsuarioService;
import com.empresa.util.AppSettings;
import com.empresa.util.NegocioException;

@RestController
@RequestMapping("/url/util")
public class UtilController {

	@Autowired
	private DataCatalogoService dataCatalogoService;

	@Autowired
	private CategoriaService categoriaService;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private TiendaCatalogoService tiendaCatalogoService;

	@GetMapping("/listaCategoria")
	@ResponseBody
	public List<Categoria> listaCategoria() {
		return categoriaService.listaActivos();
	}

	@GetMapping("/listaEstadoPedido")
	@ResponseBody
	public List<DataCatalogo> listaEstadoPedido() {
		return dataCatalogoService.listaDataCatalogo(AppSettings.CATALOGO_01_ESTADO_DE_PEDIDO);
	}

	@GetMapping("/listaMetodoPago")
	@ResponseBody
	public List<DataCatalogo> listaMetodoPago() {
		return dataCatalogoService.listaDataCatalogo(AppSettings.CATALOGO_02_METODO_DE_PAGO);
	}

	@GetMapping("/listaEstadoPago")
	@ResponseBody
	public List<DataCatalogo> listaEstadoPago() {
		return dataCatalogoService.listaDataCatalogo(AppSettings.CATALOGO_03_ESTADO_DE_PAGO);
	}

	@GetMapping("/listaMarca")
	@ResponseBody
	public List<Marca> listaMarca() {
		return tiendaCatalogoService.listaMarca();
	}

	@PostMapping("/registraMarca")
	@ResponseBody
	public HashMap<String, Object> registraMarca(@RequestBody Marca objMarca) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		try {
			Marca objSalida = tiendaCatalogoService.registraMarca(objMarca);
			mapSalida.put("mensaje", "Se registró la marca ==> " + objSalida.getNombre());
			mapSalida.put("data", objSalida);
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		}
		return mapSalida;
	}

	@GetMapping("/listaEspecie")
	@ResponseBody
	public List<Especie> listaEspecie() {
		return tiendaCatalogoService.listaEspecie();
	}

	@GetMapping("/listaEtapaVida")
	@ResponseBody
	public List<DataCatalogo> listaEtapaVida() {
		return dataCatalogoService.listaDataCatalogo(AppSettings.CATALOGO_05_ETAPA_DE_VIDA);
	}

	@GetMapping("/listaTamanoRaza")
	@ResponseBody
	public List<DataCatalogo> listaTamanoRaza() {
		return dataCatalogoService.listaDataCatalogo(AppSettings.CATALOGO_06_TAMANO_DE_RAZA);
	}

	@GetMapping("/listaUsuario")
	@ResponseBody
	public List<Usuario> listaUsuario() {
		return usuarioService.listaUsuario();
	}

}
