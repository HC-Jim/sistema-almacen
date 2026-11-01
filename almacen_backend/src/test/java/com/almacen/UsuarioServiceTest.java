package com.almacen;

import com.almacen.dto.UsuarioRequest;
import com.almacen.dto.UsuarioUpdateRequest;
import com.almacen.entity.Usuario;
import com.almacen.exception.ReglaNegocioException;
import com.almacen.repository.UsuarioRepository;
import com.almacen.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final UsuarioService service = new UsuarioService(repository, encoder);

    private Usuario admin;

    @BeforeEach
    void setUp() {
        admin = usuario(1L, "admin", "ADMIN", "admin123");
        when(repository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Usuario usuario(Long id, String username, String rol, String password) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername(username);
        u.setNombre(username);
        u.setRol(rol);
        u.setPassword(encoder.encode(password));
        return u;
    }

    @Test
    void creaUsuarioConContrasenaCifrada() {
        var creado = service.crear(new UsuarioRequest(" ana ", "Ana Pérez", "COMERCIAL", "clave-de-ana"));

        assertThat(creado.username()).isEqualTo("ana");
        assertThat(creado.activo()).isTrue();
    }

    @Test
    void noPermiteUsuarioDuplicado() {
        when(repository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(new UsuarioRequest("admin", "X", "COMERCIAL", "12345678")))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void adminNoPuedeDesactivarseASiMismo() {
        when(repository.countByRolAndActivoTrue("ADMIN")).thenReturn(2L);

        assertThatThrownBy(() -> service.actualizar(1L, new UsuarioUpdateRequest("Admin", "ADMIN", false), "admin"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(admin.isActivo()).isTrue();
    }

    @Test
    void debeQuedarAlMenosUnAdministradorActivo() {
        when(repository.countByRolAndActivoTrue("ADMIN")).thenReturn(1L);

        assertThatThrownBy(() -> service.actualizar(1L, new UsuarioUpdateRequest("Admin", "COMERCIAL", true), "otro"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("al menos un administrador");
    }

    @Test
    void desactivaYRestableceContrasenaDeOtroUsuario() {
        Usuario vendedor = usuario(2L, "juan", "COMERCIAL", "clave-juan-1");
        when(repository.findById(2L)).thenReturn(Optional.of(vendedor));

        var actualizado = service.actualizar(2L, new UsuarioUpdateRequest("Juan", "COMERCIAL", false), "admin");
        service.restablecerPassword(2L, "clave-juan-2");

        assertThat(actualizado.activo()).isFalse();
        assertThat(encoder.matches("clave-juan-2", vendedor.getPassword())).isTrue();
    }
}
