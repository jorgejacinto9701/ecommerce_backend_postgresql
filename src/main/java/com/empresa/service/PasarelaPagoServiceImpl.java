package com.empresa.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.empresa.dto.ResultadoPagoDto;
import com.empresa.dto.TarjetaDto;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Simulador de pasarela de pagos (fines academicos).
 *
 * Reglas:
 *  - Numero de tarjeta: 13 a 16 digitos y debe pasar el algoritmo de Luhn.
 *  - Expiracion: formato MM/yy y no debe estar vencida.
 *  - CVV: 3 o 4 digitos.
 *  - Tarjeta de prueba APROBADA:  4111 1111 1111 1111
 *  - Tarjeta de prueba RECHAZADA: 4000 0000 0000 0002 (fondos insuficientes)
 */
@Service
@CommonsLog
public class PasarelaPagoServiceImpl implements PasarelaPagoService {

	private static final String TARJETA_RECHAZADA = "4000000000000002";

	@Override
	public ResultadoPagoDto procesarPago(TarjetaDto tarjeta, BigDecimal monto) {
		ResultadoPagoDto resultado = new ResultadoPagoDto();
		resultado.setAprobado(false);

		if (tarjeta == null) {
			resultado.setMensaje("Datos de tarjeta no enviados");
			return resultado;
		}

		String numero = tarjeta.getNumero() == null ? "" : tarjeta.getNumero().replaceAll("[\\s-]", "");
		log.info(">>> procesarPago >>> monto: " + monto);

		if (!numero.matches("\\d{13,16}") || !validaLuhn(numero)) {
			resultado.setMensaje("Número de tarjeta inválido");
			return resultado;
		}
		if (tarjeta.getCvv() == null || !tarjeta.getCvv().matches("\\d{3,4}")) {
			resultado.setMensaje("CVV inválido");
			return resultado;
		}
		if (!validaExpiracion(tarjeta.getExpiracion())) {
			resultado.setMensaje("Tarjeta vencida o fecha de expiración inválida (use MM/yy)");
			return resultado;
		}
		if (monto == null || monto.signum() <= 0) {
			resultado.setMensaje("Monto inválido");
			return resultado;
		}
		if (TARJETA_RECHAZADA.equals(numero)) {
			resultado.setMensaje("Pago rechazado por el banco emisor: fondos insuficientes");
			return resultado;
		}

		// Pago aprobado: se genera codigo de autorizacion
		String codigo = "AUT-" + new SimpleDateFormat("yyMMddHHmmss").format(new Date())
				+ "-" + (1000 + new Random().nextInt(9000));
		resultado.setAprobado(true);
		resultado.setCodigoAutorizacion(codigo);
		resultado.setTarjetaEnmascarada("**** **** **** " + numero.substring(numero.length() - 4));
		resultado.setMensaje("Pago aprobado");
		log.info(">>> procesarPago >>> " + resultado.getMensaje() + " >>> " + codigo);
		return resultado;
	}

	@Override
	public void reversaPago(String codigoAutorizacion) {
		// Simulador: en una pasarela real aqui se llama a la API de anulacion/reembolso.
		log.warn(">>> reversaPago >>> cobro revertido: " + codigoAutorizacion);
	}

	/** Algoritmo de Luhn: valida que el numero de tarjeta sea coherente. */
	private boolean validaLuhn(String numero) {
		int suma = 0;
		boolean alterna = false;
		for (int i = numero.length() - 1; i >= 0; i--) {
			int digito = numero.charAt(i) - '0';
			if (alterna) {
				digito = digito * 2;
				if (digito > 9) digito = digito - 9;
			}
			suma = suma + digito;
			alterna = !alterna;
		}
		return suma % 10 == 0;
	}

	/** Valida formato MM/yy y que la tarjeta no este vencida. */
	private boolean validaExpiracion(String expiracion) {
		if (expiracion == null || !expiracion.matches("(0[1-9]|1[0-2])/\\d{2}")) {
			return false;
		}
		int mes = Integer.parseInt(expiracion.substring(0, 2));
		int anio = 2000 + Integer.parseInt(expiracion.substring(3));

		java.util.Calendar hoy = java.util.Calendar.getInstance();
		int mesActual = hoy.get(java.util.Calendar.MONTH) + 1;
		int anioActual = hoy.get(java.util.Calendar.YEAR);

		return (anio > anioActual) || (anio == anioActual && mes >= mesActual);
	}

}
