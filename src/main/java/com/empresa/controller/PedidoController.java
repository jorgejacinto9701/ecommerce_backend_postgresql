package com.empresa.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.empresa.entity.Pedido;
import com.empresa.service.PedidoService;
import com.empresa.util.NegocioException;

import lombok.RequiredArgsConstructor;

/**
 * Gestion de pedidos para el ADMINISTRADOR (requiere JWT).
 */
@RestController
@RequestMapping("/url/pedido")
@RequiredArgsConstructor
public class PedidoController {

	private final PedidoService pedidoService;

	@ResponseBody
	@GetMapping("/listaPedido")
	public List<Pedido> listaPedido() {
		return pedidoService.listaPedidoTodos();
	}

	@ResponseBody
	@PutMapping("/actualizaEstado/{idPedido}/{idEstado}")
	public HashMap<String, Object> actualizaEstado(
			@PathVariable int idPedido,
			@PathVariable int idEstado) {
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
	public HashMap<String, Object> confirmaPago(@PathVariable int idPedido) {
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
