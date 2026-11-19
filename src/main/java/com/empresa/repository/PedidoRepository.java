package com.empresa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.empresa.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

	@Query("Select x from Pedido x where x.cliente.idCliente = :var_cli order by x.idPedido desc")
	public List<Pedido> listaPedidoPorCliente(@Param("var_cli") int idCliente);

	@Query("Select x from Pedido x order by x.idPedido desc")
	public List<Pedido> listaPedidoTodos();

}
