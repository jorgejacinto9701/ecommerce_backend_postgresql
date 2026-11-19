package com.empresa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empresa.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

	public Optional<Cliente> findByCorreo(String correo);

}
