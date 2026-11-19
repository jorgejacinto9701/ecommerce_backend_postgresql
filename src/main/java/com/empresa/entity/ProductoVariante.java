package com.empresa.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

/**
 * Presentacion vendible de un producto (ej: bolsa 3 kg, talla M).
 * El precio y el stock viven aqui, no en Producto. Los precios incluyen IGV.
 */
@Getter
@Setter
@Entity
@Table(name = "producto_variante")
public class ProductoVariante {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idVariante;

	private String sku;
	private String presentacion;
	private BigDecimal precio;
	private BigDecimal precioOferta;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date ofertaDesde;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date ofertaHasta;

	private int stock;
	private String lote;

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date fechaVencimiento;

	private int estado;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idproducto", nullable = false)
	private Producto producto;

	/** Precio que se cobra hoy: la oferta si esta vigente, si no el precio regular. */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public BigDecimal getPrecioVigente() {
		if (precioOferta != null && precioOferta.compareTo(precio) < 0) {
			Date ahora = new Date();
			boolean desdeOk = ofertaDesde == null || !ofertaDesde.after(ahora);
			boolean hastaOk = ofertaHasta == null || !ofertaHasta.before(ahora);
			if (desdeOk && hastaOk) {
				return precioOferta;
			}
		}
		return precio;
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isEnOferta() {
		return precio != null && getPrecioVigente().compareTo(precio) < 0;
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public Integer getIdProducto() {
		return producto == null ? null : producto.getIdProducto();
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getNombreProducto() {
		return producto == null ? null : producto.getNombre();
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getImagenId() {
		return producto == null ? null : producto.getImagenId();
	}

}
