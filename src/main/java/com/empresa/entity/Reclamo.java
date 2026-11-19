package com.empresa.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

/** Hoja del Libro de Reclamaciones virtual. */
@Getter
@Setter
@Entity
@Table(name = "reclamo")
public class Reclamo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idReclamo;

	private String numeroHoja;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date fechaRegistro;

	private String nombres;
	private String apellidos;
	private String tipoDocumento;   // DNI | CE | PASAPORTE | RUC
	private String numeroDocumento;
	private String domicilio;
	private String telefono;
	private String correo;
	private String tipoBien;        // PRODUCTO | SERVICIO
	private BigDecimal montoReclamado;
	private String descripcionBien;
	private String tipoReclamo;     // RECLAMO | QUEJA
	private String detalle;
	private String pedidoConsumidor;
	private String numeroPedido;

}
