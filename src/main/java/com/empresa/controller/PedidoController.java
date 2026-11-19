package com.empresa.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.empresa.entity.Pedido;
import com.empresa.service.PedidoService;
import com.empresa.util.AppSettings;
import com.empresa.util.NegocioException;

/**
 * Gestion de pedidos para el ADMINISTRADOR (requiere JWT).
 */
@RestController
@RequestMapping("/url/pedido")
public class PedidoController {

	@Autowired
	private PedidoService pedidoService;

	@ResponseBody
	@GetMapping("/listaPedido")
	public List<Pedido> listaPedido() {
		return pedidoService.listaPedidoTodos();
	}

	@ResponseBody
	@PutMapping("/actualizaEstado/{idPedido}/{idEstado}")
	public HashMap<String, Object> actualizaEstado(
			@PathVariable("idPedido") int idPedido,
			@PathVariable("idEstado") int idEstado) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		try {
			Pedido objSalida = pedidoService.actualizaEstado(idPedido, idEstado);
			if (objSalida != null) {
				mapSalida.put("mensaje", "Se actualizó el estado del pedido ==> " + objSalida.getNumeroPedido());
				mapSalida.put("data", objSalida);
			} else {
				mapSalida.put("mensaje", "El pedido no existe");
			}
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		}
		return mapSalida;
	}

	/** Confirma el pago pendiente (Yape, Plin, contra entrega) tras verificar el voucher o el cobro. */
	@ResponseBody
	@PutMapping("/confirmaPago/{idPedido}")
	public HashMap<String, Object> confirmaPago(@PathVariable("idPedido") int idPedido) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		try {
			Pedido objSalida = pedidoService.confirmaPago(idPedido);
			if (objSalida != null) {
				mapSalida.put("mensaje", "Se confirmó el pago del pedido ==> " + objSalida.getNumeroPedido());
				mapSalida.put("data", objSalida);
			} else {
				mapSalida.put("mensaje", "El pedido no existe");
			}
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		}
		return mapSalida;
	}

}
