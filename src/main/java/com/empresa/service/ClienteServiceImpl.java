package com.empresa.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.empresa.entity.Cliente;
import com.empresa.repository.ClienteRepository;
import com.empresa.util.AppSettings;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

	private final ClienteRepository repository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Cliente registraCliente(Cliente obj) {
		obj.setPassword(passwordEncoder.encode(obj.getPassword()));
		obj.setEstado(AppSettings.ACTIVO);
		obj.setFechaRegistro(new Date());
		return repository.save(obj);
	}

	@Override
	public Optional<Cliente> buscaPorCorreo(String correo) {
		return repository.findByCorreo(correo);
	}

	@Override
	public Optional<Cliente> buscaCliente(int idCliente) {
		return repository.findById(idCliente);
	}

	@Override
	public Cliente login(String correo, String password) {
		Optional<Cliente> opt = repository.findByCorreo(correo);
		if (opt.isPresent() && opt.get().getEstado() == AppSettings.ACTIVO
				&& passwordEncoder.matches(password, opt.get().getPassword())) {
			return opt.get();
		}
		return null;
	}

}
