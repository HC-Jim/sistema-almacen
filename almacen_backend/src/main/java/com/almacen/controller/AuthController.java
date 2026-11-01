package com.almacen.controller;

import com.almacen.dto.LoginRequest;
import com.almacen.dto.LoginResponse;
import com.almacen.entity.Usuario;
import com.almacen.exception.RecursoNoEncontradoException;
import com.almacen.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.horas:8}")
    private long horasValidez;

    /** Valida usuario y contraseña; si son correctos devuelve un token JWT. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username().trim(), request.password()));
        Usuario usuario = buscar(auth.getName());

        Instant ahora = Instant.now();
        Instant expira = ahora.plus(Duration.ofHours(horasValidez));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("almacen-backend")
                .issuedAt(ahora)
                .expiresAt(expira)
                .subject(usuario.getUsername())
                .claim("nombre", usuario.getNombre())
                .claim("scope", usuario.getRol())
                .build();
        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        return new LoginResponse(token, expira, usuario.getUsername(), usuario.getNombre(), usuario.getRol());
    }

    /** Datos del usuario dueño del token. */
    @GetMapping("/me")
    public Map<String, String> me(Authentication auth) {
        Usuario usuario = buscar(auth.getName());
        return Map.of("username", usuario.getUsername(), "nombre", usuario.getNombre(), "rol", usuario.getRol());
    }

    private Usuario buscar(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
