package com.almacen.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/hola")
public class HolaController {

    @GetMapping
    public Map<String, Object> saludar(@RequestParam(defaultValue = "Mundo") String nombre) {
        return Map.of(
                "mensaje", "¡Hola " + nombre + " desde Spring Boot!",
                "fecha", LocalDateTime.now().toString()
        );
    }
}
