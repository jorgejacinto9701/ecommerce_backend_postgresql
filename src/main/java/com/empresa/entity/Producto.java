package com.empresa.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "producto")
public class Producto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idProducto;
	private String nombre;
	private String descripcion;
	private String ingredientes;

	// clave del archivo en Cloudflare R2 (ej: productos/<uuid>.jpg)
	private String imagenId;

	// Precio regular minimo entre las variantes activas. Se recalcula al
	// cambiar variantes; sirve para filtrar y ordenar el catalogo.
	private BigDecimal precioDesde;

	private int estado;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date fechaRegistro;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date fechaActualizacion;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "categoriaPadre" })
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "idcategoria", nullable = false)
	private Categoria categoria;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idmarca")
	private Marca marca;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idespecie")
	private Especie especie;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "catalogo" })
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idetapavida")
	private DataCatalogo etapaVida;

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "catalogo" })
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idtamanoraza")
	private DataCatalogo tamanoRaza;

	// Solo variantes activas (la baja es logica). Se gestionan con sus propios endpoints.
	@SQLRestriction("estado = 1")
	@OneToMany(mappedBy = "producto", fetch = FetchType.EAGER)
	@OrderBy("precio ASC")
	private List<ProductoVariante> variantes = new ArrayList<ProductoVariante>();

}
