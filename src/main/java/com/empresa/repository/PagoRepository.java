package com.empresa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.empresa.entity.Pago;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

	@Query("Select x from Pago x where x.pedido.idPedido = :var_ped")
	public List<Pago> listaPagoPorPedido(@Param("var_ped") int idPedido);

}
