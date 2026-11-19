package com.empresa.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

/**
 * Todos los montos incluyen IGV: total = productos + envio;
 * subTotal = base imponible (total / 1.18); igv = total - subTotal.
 */
@Getter
@Setter
@Entity
@Table(name = "pedido")
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idPedido;

	private String numeroPedido;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date fechaRegistro = new Date();

	private BigDecimal subTotal;
	private BigDecimal igv;
	private BigDecimal costoEnvio;
	private BigDecimal total;

	// DELIVERY | RECOJO
	private String tipoEntrega;
	private String direccionEnvio;
	private String referenciaEnvio;

	// Datos para el comprobante electronico (boleta: DNI; factura: RUC + razon social)
	private String numeroDocumento;
	private String razonSocial;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "catalogo" })
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idtipocomprobante", nullable = false)
	private DataCatalogo tipoComprobante;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idubigeo")
	private Ubigeo ubigeo;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idestado", nullable = false)
	private DataCatalogo estado;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idcliente", nullable = false)
	private Cliente cliente;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pedido")
	private List<PedidoHasProducto> detallesPedido;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pedido")
	private List<Pago> pagos;

}
