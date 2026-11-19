package com.empresa.entity;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cliente")
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idCliente;
	private String nombres;
	private String apellidos;
	private String dni;
	private String correo;
	private String telefono;
	private String direccion;

	// Solo se acepta en la entrada (registro); nunca se expone en el JSON de salida
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	private int estado;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date fechaRegistro;

	@JsonIgnore
	@OneToMany(mappedBy = "cliente")
	private List<Pedido> pedidos;

	public String getNombreCompleto() {
		if (nombres != null && apellidos != null) {
			return nombres.concat(" ").concat(apellidos);
		} else {
			return "";
		}
	}
}
