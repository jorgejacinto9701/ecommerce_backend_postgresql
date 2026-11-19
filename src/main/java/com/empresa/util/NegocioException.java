package com.empresa.util;

/** Error de regla de negocio: se informa al cliente y se revierte la transaccion. */
public class NegocioException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NegocioException(String mensaje) {
		super(mensaje);
	}

}
