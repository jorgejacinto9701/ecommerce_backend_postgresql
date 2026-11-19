package com.empresa.security;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.empresa.entity.Cliente;
import com.empresa.entity.Opcion;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.PostConstruct;

@Component
public class JwtProvider {
    private final static Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    /** Valor de desarrollo de application.properties. NUNCA usarlo en produccion. */
    private static final String SECRET_DE_DESARROLLO = "iuZG60TkLJRWvuCO3EsJ4u2TVwLs5VbUo0O3eVvn98e1zbBiVz0NgfJvZsFjcUDk6ESTLbscbWHn2ydUOTHDxA==";

    /** Claim que distingue el token de un CLIENTE de la tienda del token de un usuario administrador. */
    public static final String TIPO_CLIENTE = "CLIENTE";

    // Clave HS512 en Base64 (jjwt 0.9.x). Se define con la variable de entorno JWT_SECRET.
    @Value("${app.jwt.secret}")
    private String secret;

    // Duracion del token en segundos
    @Value("${app.jwt.expiration:36000}")
    private int expiration;

    @PostConstruct
    void avisaSecretoDeDesarrollo() {
        if (SECRET_DE_DESARROLLO.equals(secret)) {
            logger.warn(">>> JWT: se esta usando el secreto de DESARROLLO. Define la variable de entorno JWT_SECRET antes de publicar.");
        }
    }

    // Metodo original (lo dejamos para compatibilidad)
    public String generateToken(Authentication authentication){
        UsuarioPrincipal usuarioPrincipal = (UsuarioPrincipal) authentication.getPrincipal();
        return generateTokenWithClaims(usuarioPrincipal);
    }

    // Token del administrador/vendedor: incluye roles y opciones de menu
    public String generateTokenWithClaims(UsuarioPrincipal usuarioPrincipal){
        Map<String, Object> claims = new HashMap<>();

        claims.put("idUsuario", usuarioPrincipal.getIdUsuario());
        claims.put("nombreCompleto", usuarioPrincipal.getNombreCompleto());
        claims.put("login", usuarioPrincipal.getLogin());

        List<String> rolesNombres = usuarioPrincipal.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList());
        claims.put("roles", rolesNombres);

        if (usuarioPrincipal.getOpciones() != null) {
        	List<Opcion> lstOpciones = usuarioPrincipal.getOpciones();
            claims.put("opciones", lstOpciones);
        }

        return construye(claims, usuarioPrincipal.getUsername());
    }

    // Token del CLIENTE de la tienda: solo identifica al cliente (idCliente) y su tipo
    public String generateTokenCliente(Cliente cliente){
        Map<String, Object> claims = new HashMap<>();
        claims.put("tipo", TIPO_CLIENTE);
        claims.put("idCliente", cliente.getIdCliente());
        return construye(claims, "cliente:" + cliente.getIdCliente());
    }

    private String construye(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + expiration * 1000L))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Claims getClaims(String token){
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public String getNombreUsuarioFromToken(String token){
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token){
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        }catch (MalformedJwtException e){
            logger.error(">>> validateToken >>> token mal formado");
        }catch (UnsupportedJwtException e){
            logger.error(">>> validateToken >>> token no soportado");
        }catch (ExpiredJwtException e){
            logger.error(">>> validateToken >>> token expirado");
        }catch (IllegalArgumentException e){
            logger.error(">>> validateToken >>> token vacío");
        }catch (SignatureException e){
            logger.error(">>> validateToken >>> fail en la firma");
        }
        return false;
    }
}
