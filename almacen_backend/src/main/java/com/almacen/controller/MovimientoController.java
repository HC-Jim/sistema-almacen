package com.almacen.controller;

import com.almacen.config.Roles;
import com.almacen.dto.MovimientoResponse;
import com.almacen.entity.TipoMovimiento;
import com.almacen.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Consulta de movimientos. Cada rol solo ve los tipos de su área (ver {@link Roles#lectores}). */
@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final InventarioService inventarioService;

    @GetMapping
    public List<MovimientoResponse> listar(
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) Long almacenId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth) {
        exigirLectura(tipo, auth);
        return inventarioService.listar(tipo, almacenId, desde, hasta);
    }

    @GetMapping("/{id}")
    public MovimientoResponse obtener(@PathVariable Long id, Authentication auth) {
        MovimientoResponse movimiento = inventarioService.obtener(id);
        exigirLectura(movimiento.tipo(), auth);
        return movimiento;
    }

    private static void exigirLectura(TipoMovimiento tipo, Authentication auth) {
        Set<String> permitidos = Roles.lectores(tipo);
        boolean puede = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.startsWith("ROLE_") && permitidos.contains(a.substring(5)));
        if (!puede) {
            throw new AccessDeniedException("No tiene permiso para consultar estos movimientos");
        }
    }
}
