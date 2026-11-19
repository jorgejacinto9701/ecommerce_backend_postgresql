package com.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empresa.entity.PedidoHasProducto;
import com.empresa.entity.PedidoHasProductoPK;

public interface PedidoHasProductoRepository extends JpaRepository<PedidoHasProducto, PedidoHasProductoPK> {

}
