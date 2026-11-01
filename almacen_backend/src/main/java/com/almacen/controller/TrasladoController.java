package com.almacen.controller;

import com.almacen.dto.MovimientoResponse;
import com.almacen.dto.TrasladoRequest;
import com.almacen.entity.TipoMovimiento;
import com.almacen.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/traslados")
@RequiredArgsConstructor
public class TrasladoController {

    private final InventarioService inventarioService;

    @GetMapping
    public List<MovimientoResponse> listar(
            @RequestParam(required = false) Long almacenId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return inventarioService.listar(TipoMovimiento.TRASLADO, almacenId, desde, hasta);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse registrar(@Valid @RequestBody TrasladoRequest request, Authentication auth) {
        return inventarioService.registrarTraslado(request, auth.getName());
    }
}
