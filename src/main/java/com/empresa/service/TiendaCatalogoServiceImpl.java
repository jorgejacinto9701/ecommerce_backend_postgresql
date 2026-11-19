package com.empresa.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.empresa.entity.Especie;
import com.empresa.entity.Marca;
import com.empresa.entity.Reclamo;
import com.empresa.entity.Ubigeo;
import com.empresa.repository.EspecieRepository;
import com.empresa.repository.MarcaRepository;
import com.empresa.repository.ReclamoRepository;
import com.empresa.repository.UbigeoRepository;
import com.empresa.util.AppSettings;
import com.empresa.util.NegocioException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TiendaCatalogoServiceImpl implements TiendaCatalogoService {

	private final MarcaRepository marcaRepository;
	private final EspecieRepository especieRepository;
	private final UbigeoRepository ubigeoRepository;
	private final ReclamoRepository reclamoRepository;

	@Override
	public List<Marca> listaMarca() {
		return marcaRepository.findByEstadoOrderByNombre(AppSettings.ACTIVO);
	}

	@Override
	public Marca registraMarca(Marca obj) {
		if (obj.getNombre() == null || obj.getNombre().trim().length() < 2) {
			throw new NegocioException("El nombre de la marca es obligatorio");
		}
		obj.setNombre(obj.getNombre().trim());
		if (marcaRepository.existsByNombreIgnoreCase(obj.getNombre())) {
			throw new NegocioException("La marca " + obj.getNombre() + " ya existe");
		}
		obj.setIdMarca(0);
		obj.setEstado(AppSettings.ACTIVO);
		return marcaRepository.save(obj);
	}

	@Override
	public List<Especie> listaEspecie() {
		return especieRepository.findByEstadoOrderByNombre(AppSettings.ACTIVO);
	}

	@Override
	public List<String> listaDepartamento() {
		return ubigeoRepository.listaDepartamento();
	}

	@Override
	public List<String> listaProvincia(String departamento) {
		return ubigeoRepository.listaProvincia(departamento);
	}

	@Override
	public List<Ubigeo> listaDistrito(String departamento, String provincia) {
		return ubigeoRepository.listaDistrito(departamento, provincia);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Reclamo registraReclamo(Reclamo obj) {
		exige(obj.getNombres(), "nombres");
		exige(obj.getApellidos(), "apellidos");
		exige(obj.getNumeroDocumento(), "número de documento");
		exige(obj.getDomicilio(), "domicilio");
		exige(obj.getCorreo(), "correo");
		exige(obj.getDescripcionBien(), "identificación del bien contratado");
		exige(obj.getDetalle(), "detalle del reclamo o queja");
		exige(obj.getPedidoConsumidor(), "pedido del consumidor");
		if (!"PRODUCTO".equals(obj.getTipoBien()) && !"SERVICIO".equals(obj.getTipoBien())) {
			throw new NegocioException("Indica si el reclamo es sobre un producto o un servicio");
		}
		if (!"RECLAMO".equals(obj.getTipoReclamo()) && !"QUEJA".equals(obj.getTipoReclamo())) {
			throw new NegocioException("Indica si es un reclamo o una queja");
		}
		if (obj.getTipoDocumento() == null || !obj.getTipoDocumento().matches("DNI|CE|PASAPORTE|RUC")) {
			throw new NegocioException("Tipo de documento no válido");
		}
		obj.setIdReclamo(0);
		obj.setFechaRegistro(new Date());
		Reclamo guardado = reclamoRepository.save(obj);
		guardado.setNumeroHoja(String.format("LR-%06d", guardado.getIdReclamo()));
		return reclamoRepository.save(guardado);
	}

	private void exige(String valor, String campo) {
		if (valor == null || valor.trim().isEmpty()) {
			throw new NegocioException("Falta completar: " + campo);
		}
	}

}
