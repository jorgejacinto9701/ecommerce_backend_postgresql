package com.empresa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "especie")
public class Especie {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idEspecie;
	private String nombre;
	private int estado;

}
