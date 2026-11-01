package com.almacen;

import com.almacen.config.SecurityConfig;
import com.almacen.controller.CompraController;
import com.almacen.controller.MovimientoController;
import com.almacen.controller.ProductoController;
import com.almacen.controller.TrasladoController;
import com.almacen.controller.VentaController;
import com.almacen.repository.UsuarioRepository;
import com.almacen.service.InventarioService;
import com.almacen.service.ProductoService;
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
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cada área solo accede a sus operaciones: comercial (precios y ventas), compras y almacén. */
@WebMvcTest(controllers = {CompraController.class, VentaController.class, TrasladoController.class,
        MovimientoController.class, ProductoController.class})
@Import(SecurityConfig.class)
class RolesSeguridadTest {

    private static final ResultMatcher PERMITIDO = status().isOk();
    private static final ResultMatcher PROHIBIDO = status().isForbidden();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private InventarioService inventarioService;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private String token(String rol) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .subject("usuario")
                .claim("scope", rol)
                .build();
        return "Bearer " + jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private void verificarGet(String rol, String url, ResultMatcher esperado) throws Exception {
        mockMvc.perform(get(url).header("Authorization", token(rol))).andExpect(esperado);
    }

    private void verificarPrecio(String rol, ResultMatcher esperado) throws Exception {
        mockMvc.perform(patch("/api/productos/1/precio").header("Authorization", token(rol))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"precio\": 10}"))
                .andExpect(esperado);
    }

    @Test
    void comercialManejaPreciosYVentas() throws Exception {
        verificarPrecio("COMERCIAL", PERMITIDO);
        verificarGet("COMERCIAL", "/api/ventas", PERMITIDO);
        verificarGet("COMERCIAL", "/api/movimientos?tipo=VENTA", PERMITIDO);
        verificarGet("COMERCIAL", "/api/compras", PROHIBIDO);
        verificarGet("COMERCIAL", "/api/traslados", PROHIBIDO);
        verificarGet("COMERCIAL", "/api/movimientos", PROHIBIDO);
        mockMvc.perform(post("/api/productos").header("Authorization", token("COMERCIAL"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"X\",\"nombre\":\"X\"}"))
                .andExpect(PROHIBIDO);
    }

    @Test
    void comprasRegistraComprasPeroNoCambiaPrecios() throws Exception {
        verificarGet("COMPRAS", "/api/compras", PERMITIDO);
        verificarGet("COMPRAS", "/api/movimientos?tipo=COMPRA", PERMITIDO);
        verificarGet("COMPRAS", "/api/ventas", PROHIBIDO);
        verificarGet("COMPRAS", "/api/traslados", PROHIBIDO);
        verificarPrecio("COMPRAS", PROHIBIDO);
    }

    @Test
    void almacenRegistraTrasladosYConsultaTodosLosMovimientos() throws Exception {
        verificarGet("ALMACEN", "/api/traslados", PERMITIDO);
        verificarGet("ALMACEN", "/api/movimientos", PERMITIDO);
        verificarGet("ALMACEN", "/api/compras", PROHIBIDO);
        verificarGet("ALMACEN", "/api/ventas", PROHIBIDO);
        verificarPrecio("ALMACEN", PROHIBIDO);
    }

    @Test
    void adminPuedeTodo() throws Exception {
        verificarPrecio("ADMIN", PERMITIDO);
        for (String url : new String[]{"/api/ventas", "/api/compras", "/api/traslados", "/api/movimientos"}) {
            verificarGet("ADMIN", url, PERMITIDO);
        }
    }

    @Test
    void todosConsultanProductos() throws Exception {
        for (String rol : new String[]{"ADMIN", "COMERCIAL", "COMPRAS", "ALMACEN"}) {
            verificarGet(rol, "/api/productos", PERMITIDO);
        }
    }
}
