package com.empresa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TarjetaDto {

	private String numero;       // 16 digitos
	private String titular;
	private String expiracion;   // MM/yy
	private String cvv;          // 3 o 4 digitos

}
