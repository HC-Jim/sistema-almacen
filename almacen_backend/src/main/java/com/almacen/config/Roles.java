package com.almacen.config;

import com.almacen.entity.TipoMovimiento;

import java.util.Set;

/**
 * Roles del sistema:
 * - ADMIN: acceso total y administración de usuarios.
 * - COMERCIAL (área comercial): actualiza precios de venta y registra ventas.
 * - COMPRAS (logística): registra compras y mantiene productos.
 * - ALMACEN (logística): mantiene productos, registra traslados y consulta movimientos.
 */
public final class Roles {

    public static final String ADMIN = "ADMIN";
    public static final String COMERCIAL = "COMERCIAL";
    public static final String COMPRAS = "COMPRAS";
    public static final String ALMACEN = "ALMACEN";

    /** Expresión para validar el rol en los formularios de usuario. */
    public static final String PATRON = "ADMIN|COMERCIAL|COMPRAS|ALMACEN";

    private Roles() {
    }

    /** Roles que pueden consultar un tipo de movimiento (null = todos los tipos a la vez). */
    public static Set<String> lectores(TipoMovimiento tipo) {
        return switch (tipo) {
            case null -> Set.of(ADMIN, ALMACEN);
            case COMPRA -> Set.of(ADMIN, COMPRAS, ALMACEN);
            case VENTA -> Set.of(ADMIN, COMERCIAL, ALMACEN);
            case TRASLADO -> Set.of(ADMIN, ALMACEN);
        };
    }
}
