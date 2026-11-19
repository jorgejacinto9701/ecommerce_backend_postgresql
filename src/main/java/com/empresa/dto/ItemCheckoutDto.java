package com.empresa.dto;

import lombok.Getter;
import lombok.Setter;

/** Una linea del carrito: SOLO identifica la variante y la cantidad; el precio lo decide el servidor. */
@Getter
@Setter
public class ItemCheckoutDto {

	private int idVariante;
	private int cantidad;

}
