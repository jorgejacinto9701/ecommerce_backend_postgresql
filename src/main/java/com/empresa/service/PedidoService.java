package com.empresa.service;

import java.util.HashMap;
import java.util.List;

import com.empresa.dto.CheckoutDto;
import com.empresa.entity.Pedido;

public interface PedidoService {

	public abstract HashMap<String, Object> registraPedido(int idCliente, CheckoutDto checkout);
	public abstract List<Pedido> listaPedidoPorCliente(int idCliente);
	public abstract List<Pedido> listaPedidoTodos();
	public abstract Pedido actualizaEstado(int idPedido, int idEstado);
	public abstract Pedido confirmaPago(int idPedido);

}
