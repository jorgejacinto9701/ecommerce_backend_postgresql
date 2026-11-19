package com.empresa.util;

import java.math.BigDecimal;

public class AppSettings {

	// Catálogos
	public static final int CATALOGO_01_ESTADO_DE_PEDIDO = 1;
	public static final int CATALOGO_02_METODO_DE_PAGO = 2;
	public static final int CATALOGO_03_ESTADO_DE_PAGO = 3;
	public static final int CATALOGO_04_TIPO_COMPROBANTE = 4;
	public static final int CATALOGO_05_ETAPA_DE_VIDA = 5;
	public static final int CATALOGO_06_TAMANO_DE_RAZA = 6;

	// Estados de Pedido (data_catalogo)
	public static final int ESTADO_PEDIDO_REGISTRADO = 1;
	public static final int ESTADO_PEDIDO_PAGADO = 2;
	public static final int ESTADO_PEDIDO_EN_PREPARACION = 3;
	public static final int ESTADO_PEDIDO_ENVIADO = 4;
	public static final int ESTADO_PEDIDO_ENTREGADO = 5;
	public static final int ESTADO_PEDIDO_ANULADO = 6;

	// Métodos de Pago (data_catalogo)
	public static final int METODO_PAGO_TARJETA = 7;
	public static final int METODO_PAGO_YAPE = 8;
	public static final int METODO_PAGO_PLIN = 9;
	public static final int METODO_PAGO_CONTRA_ENTREGA = 10;

	// Estados de Pago (data_catalogo)
	public static final int ESTADO_PAGO_APROBADO = 11;
	public static final int ESTADO_PAGO_RECHAZADO = 12;
	public static final int ESTADO_PAGO_PENDIENTE = 13;

	// Tipos de comprobante (data_catalogo)
	public static final int COMPROBANTE_BOLETA = 14;
	public static final int COMPROBANTE_FACTURA = 15;

	// Tipos de entrega
	public static final String ENTREGA_DELIVERY = "DELIVERY";
	public static final String ENTREGA_RECOJO = "RECOJO";

	// IGV Perú: los precios de la tienda YA lo incluyen (base imponible = precio / 1.18)
	public static final BigDecimal IGV_FACTOR = new BigDecimal("1.18");

	// Boletas por montos mayores a este valor (S/) deben consignar el DNI del comprador
	public static final BigDecimal BOLETA_LIMITE_SIN_DOCUMENTO = new BigDecimal("700.00");

	public static final int ACTIVO = 1;
	public static final int INACTIVO = 0;
}
