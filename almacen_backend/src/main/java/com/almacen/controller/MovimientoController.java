package com.almacen.controller;

import com.almacen.dto.MovimientoResponse;
import com.almacen.entity.TipoMovimiento;
import com.almacen.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Consulta de todos los movimientos (compras, ventas y traslados). */
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return inventarioService.listar(tipo, almacenId, desde, hasta);
    }

    @GetMapping("/{id}")
    public MovimientoResponse obtener(@PathVariable Long id) {
        return inventarioService.obtener(id);
    }
}
