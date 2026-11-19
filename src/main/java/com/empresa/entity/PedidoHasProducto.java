package com.empresa.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pedido_has_producto")
public class PedidoHasProducto {

	@EmbeddedId
	private PedidoHasProductoPK pedidoHasProductoPK;

	// Precio unitario cobrado (con IGV), tomado de la BD al momento de comprar
	private BigDecimal precio;
	private int cantidad;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "idpedido", nullable = false, insertable = false, updatable = false)
	private Pedido pedido;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@ManyToOne
	@JoinColumn(name = "idvariante", nullable = false, insertable = false, updatable = false)
	private ProductoVariante variante;

}
