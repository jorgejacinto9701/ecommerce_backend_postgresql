package com.empresa.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Cuerpo del checkout. El cliente NO se envia (sale del JWT) y los precios
 * tampoco (se leen de la base de datos).
 */
@Getter
@Setter
public class CheckoutDto {

	private List<ItemCheckoutDto> items;

	private String tipoEntrega;        // DELIVERY | RECOJO
	private String direccionEnvio;
	private String referenciaEnvio;
	private Integer idUbigeo;          // obligatorio si DELIVERY

	private int idTipoComprobante;     // data_catalogo: 14 Boleta, 15 Factura
	private String numeroDocumento;    // DNI (boleta) o RUC (factura)
	private String razonSocial;        // obligatorio si Factura

	private int idMetodoPago;          // data_catalogo: 7=Tarjeta, 8=Yape, 9=Plin, 10=Contra entrega
	private TarjetaDto tarjeta;        // obligatorio solo si idMetodoPago = Tarjeta

}
