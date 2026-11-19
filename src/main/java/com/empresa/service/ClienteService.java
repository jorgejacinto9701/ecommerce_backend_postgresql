package com.empresa.service;

import java.util.Optional;

import com.empresa.entity.Cliente;

public interface ClienteService {

	public abstract Cliente registraCliente(Cliente obj);
	public abstract Optional<Cliente> buscaPorCorreo(String correo);
	public abstract Optional<Cliente> buscaCliente(int idCliente);
	public abstract Cliente login(String correo, String password);

}
