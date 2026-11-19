package com.empresa.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtTokenFilter extends OncePerRequestFilter {

	private final static Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

	@Autowired
	JwtProvider jwtProvider;

	@Autowired
	UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String token = getToken(req);

			if (token != null && jwtProvider.validateToken(token)) {
				Claims claims = jwtProvider.getClaims(token);

				if (JwtProvider.TIPO_CLIENTE.equals(claims.get("tipo"))) {
					// Cliente de la tienda: principal = idCliente, unica autoridad = CLIENTE
					Integer idCliente = (Integer) claims.get("idCliente");
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
							idCliente, null, List.of(new SimpleGrantedAuthority(JwtProvider.TIPO_CLIENTE)));
					SecurityContextHolder.getContext().setAuthentication(auth);
				} else {
					// Administrador / vendedor
					UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			}
		} catch (Exception e) {
			logger.error("fail en el método doFilter " + e.getMessage());
		}
		filterChain.doFilter(req, res);
	}

	private String getToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer "))
			return header.substring(7);
		return null;
	}
}
