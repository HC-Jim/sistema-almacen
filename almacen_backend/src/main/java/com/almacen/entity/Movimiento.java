package com.almacen.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de un movimiento de inventario: compra (ingreso), venta (egreso)
 * o traslado interno entre almacenes. Los campos propios de cada tipo quedan
 * en null cuando no aplican.
 */
@Entity
@Table(name = "movimientos")
@Getter
@Setter
@NoArgsConstructor
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoMovimiento tipo;

    /** Correlativo interno por tipo: CO-000001, VE-000001, TR-000001. */
    @Column(nullable = false, unique = true, length = 20)
    private String correlativo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Almacén del que sale la mercadería (venta y traslado). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_origen_id")
    private Almacen almacenOrigen;

    /** Almacén al que ingresa la mercadería (compra y traslado). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_destino_id")
    private Almacen almacenDestino;

    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(length = 255)
    private String observacion;

    // ---- Compra ----
    @Column(name = "orden_compra", length = 30)
    private String ordenCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_compra", length = 15)
    private TipoCompra tipoCompra;

    @Column(length = 120)
    private String proveedor;

    // ---- Venta ----
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", length = 15)
    private TipoComprobante tipoComprobante;

    /** Número del comprobante, por ejemplo B001-00000001. */
    @Column(name = "numero_comprobante", unique = true, length = 20)
    private String numeroComprobante;

    @Column(name = "cliente_dni", length = 8)
    private String clienteDni;

    @Column(name = "cliente_nombre", length = 120)
    private String clienteNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", length = 20)
    private MedioPago medioPago;

    /** Compra: total al costo. Venta: total cobrado. Traslado: valor al costo promedio. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimientoDetalle> detalles = new ArrayList<>();

    public void agregarDetalle(MovimientoDetalle detalle) {
        detalle.setMovimiento(this);
        detalles.add(detalle);
    }
}
