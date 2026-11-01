package com.almacen.service;

import com.almacen.dto.UsuarioRequest;
import com.almacen.dto.UsuarioResponse;
import com.almacen.dto.UsuarioUpdateRequest;
import com.almacen.entity.Usuario;
import com.almacen.exception.RecursoNoEncontradoException;
import com.almacen.exception.ReglaNegocioException;
import com.almacen.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Alta de usuarios, activación/desactivación y cambio de contraseñas (siempre cifradas con BCrypt). */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    public static final String ADMIN = "ADMIN";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByOrderByUsernameAsc().stream().map(UsuarioResponse::from).toList();
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        String username = request.username().trim();
        if (usuarioRepository.existsByUsername(username)) {
            throw new ReglaNegocioException("Ya existe el usuario " + username);
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNombre(request.nombre().trim());
        usuario.setRol(request.rol());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    /**
     * Cambia nombre, rol y estado. Un administrador no puede quitarse a sí mismo el rol ni desactivarse,
     * y siempre debe quedar al menos un administrador activo.
     */
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request, String usernameActual) {
        Usuario usuario = buscar(id);
        boolean dejaDeSerAdmin = esAdminActivo(usuario) && (!ADMIN.equals(request.rol()) || !request.activo());
        if (dejaDeSerAdmin) {
            if (usuario.getUsername().equals(usernameActual)) {
                throw new ReglaNegocioException("No puede quitarse el rol de administrador ni desactivar su propio usuario");
            }
            if (usuarioRepository.countByRolAndActivoTrue(ADMIN) <= 1) {
                throw new ReglaNegocioException("Debe quedar al menos un administrador activo");
            }
        }
        usuario.setNombre(request.nombre().trim());
        usuario.setRol(request.rol());
        usuario.setActivo(request.activo());
        return UsuarioResponse.from(usuario);
    }

    /** El administrador asigna una nueva contraseña (por ejemplo, si el usuario la olvidó). */
    @Transactional
    public void restablecerPassword(Long id, String nueva) {
        buscar(id).setPassword(passwordEncoder.encode(nueva));
    }

    /** El usuario conectado cambia su propia contraseña; debe confirmar la actual. */
    @Transactional
    public void cambiarPassword(String username, String actual, String nueva) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (!passwordEncoder.matches(actual, usuario.getPassword())) {
            throw new ReglaNegocioException("La contraseña actual es incorrecta");
        }
        if (passwordEncoder.matches(nueva, usuario.getPassword())) {
            throw new ReglaNegocioException("La nueva contraseña debe ser distinta de la actual");
        }
        usuario.setPassword(passwordEncoder.encode(nueva));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private static boolean esAdminActivo(Usuario u) {
        return ADMIN.equals(u.getRol()) && u.isActivo();
    }
}
