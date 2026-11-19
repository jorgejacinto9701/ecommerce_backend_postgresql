package com.empresa.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.empresa.entity.Usuario;
import com.empresa.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

	private final UsuarioRepository usuarioRepository;

	@Override
	public List<Usuario> listaUsuario() {
		return usuarioRepository.findAll();
	}

}
