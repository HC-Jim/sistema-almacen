package com.almacen.entity;

/** Comprobante de venta. Cada tipo lleva su propia serie y correlativo. */
public enum TipoComprobante {
    FACTURA("F001"),
    BOLETA("B001"),
    TICKET("T001");

    private final String serie;

    TipoComprobante(String serie) {
        this.serie = serie;
    }

    public String getSerie() {
        return serie;
    }
}
