package com.empresa.service;

import java.math.BigDecimal;

import com.empresa.dto.ResultadoPagoDto;
import com.empresa.dto.TarjetaDto;

/**
 * Pasarela de pagos. La implementacion por defecto es un SIMULADOR
 * academico; para produccion se puede crear otra implementacion
 * (Culqi, MercadoPago, Niubiz, Stripe) respetando esta interfaz.
 */
public interface PasarelaPagoService {

	public abstract ResultadoPagoDto procesarPago(TarjetaDto tarjeta, BigDecimal monto);

	/** Revierte (anula) un cobro ya aprobado. Se usa si despues del cobro falla el registro del pedido. */
	public abstract void reversaPago(String codigoAutorizacion);

}
