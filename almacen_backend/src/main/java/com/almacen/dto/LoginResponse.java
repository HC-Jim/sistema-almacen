package com.almacen.dto;

import java.time.Instant;

public record LoginResponse(String token, Instant expiraEn, String username, String nombre, String rol) {
}
