package com.almacen;

import com.almacen.config.SecurityConfig;
import com.almacen.controller.AuthController;
import com.almacen.controller.UsuarioController;
import com.almacen.dto.UsuarioResponse;
import com.almacen.repository.UsuarioRepository;
import com.almacen.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica con tokens JWT reales que el claim "scope" se traduce en roles y que /api/usuarios es solo para ADMIN. */
@WebMvcTest(controllers = {UsuarioController.class, AuthController.class})
@Import(SecurityConfig.class)
class UsuarioSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private String token(String username, String rol) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .subject(username)
                .claim("scope", rol)
                .build();
        return "Bearer " + jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    @Test
    void adminPuedeListarUsuarios() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(new UsuarioResponse(1L, "admin", "Administrador", "ADMIN", true)));

        mockMvc.perform(get("/api/usuarios").header("Authorization", token("admin", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void comercialNoPuedeAdministrarUsuarios() throws Exception {
        String comercial = token("juan", "COMERCIAL");

        mockMvc.perform(get("/api/usuarios").header("Authorization", comercial))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/usuarios").header("Authorization", comercial)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"otro","nombre":"Otro","rol":"ADMIN","password":"12345678"}"""))
                .andExpect(status().isForbidden());
        verify(usuarioService, never()).crear(any());
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void crearUsuarioValidaRolYLongitudDeContrasena() throws Exception {
        mockMvc.perform(post("/api/usuarios").header("Authorization", token("admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ana","nombre":"Ana","rol":"JEFE","password":"corta"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.rol").exists())
                .andExpect(jsonPath("$.errores.password").exists());
    }
}
