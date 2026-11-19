package com.empresa.service;

import java.util.List;

import com.empresa.entity.Especie;
import com.empresa.entity.Marca;
import com.empresa.entity.Reclamo;
import com.empresa.entity.Ubigeo;

/** Datos de apoyo de la tienda: marcas, especies, ubigeo/envio y libro de reclamaciones. */
public interface TiendaCatalogoService {

	public abstract List<Marca> listaMarca();
	public abstract Marca registraMarca(Marca obj);
	public abstract List<Especie> listaEspecie();

	public abstract List<String> listaDepartamento();
	public abstract List<String> listaProvincia(String departamento);
	public abstract List<Ubigeo> listaDistrito(String departamento, String provincia);

	public abstract Reclamo registraReclamo(Reclamo obj);

}
