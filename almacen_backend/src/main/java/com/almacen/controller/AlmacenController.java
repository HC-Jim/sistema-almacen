package com.almacen.controller;

import com.almacen.dto.AlmacenResponse;
import com.almacen.repository.AlmacenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenRepository almacenRepository;

    @GetMapping
    public List<AlmacenResponse> listar() {
        return almacenRepository.findAllByOrderByCodigoAsc().stream().map(AlmacenResponse::from).toList();
    }
}
