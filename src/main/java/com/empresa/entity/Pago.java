package com.empresa.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pago")
public class Pago {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idPago;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date fechaRegistro = new Date();

	private BigDecimal monto;

	// Tarjeta enmascarada, ej: **** **** **** 1111
	private String numeroTarjeta;

	private String codigoAutorizacion;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idmetodopago", nullable = false)
	private DataCatalogo metodoPago;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idestadopago", nullable = false)
	private DataCatalogo estadoPago;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idpedido", nullable = false)
	private Pedido pedido;

}
