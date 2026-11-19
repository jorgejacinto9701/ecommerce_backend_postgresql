package com.empresa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultadoPagoDto {

	private boolean aprobado;
	private String codigoAutorizacion;
	private String tarjetaEnmascarada;
	private String mensaje;

}
