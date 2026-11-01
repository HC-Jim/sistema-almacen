package com.almacen.config;

import com.almacen.entity.Almacen;
import com.almacen.entity.Correlativo;
import com.almacen.entity.Producto;
import com.almacen.entity.Stock;
import com.almacen.entity.TipoComprobante;
import com.almacen.entity.Usuario;
import com.almacen.repository.AlmacenRepository;
import com.almacen.repository.CorrelativoRepository;
import com.almacen.repository.ProductoRepository;
import com.almacen.repository.StockRepository;
import com.almacen.repository.UsuarioRepository;
import com.almacen.service.CorrelativoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Crea los datos mínimos al arrancar: 2 almacenes, las series de correlativos y el usuario administrador. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AlmacenRepository almacenRepository;
    private final CorrelativoRepository correlativoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final StockRepository stockRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        crearAlmacenes();
        crearCorrelativos();
        crearAdministrador();
        asignarStockInicial();
    }

    private void crearAlmacenes() {
        if (almacenRepository.count() == 0) {
            almacenRepository.save(new Almacen(null, "ALM01", "Almacén Principal", null));
            almacenRepository.save(new Almacen(null, "ALM02", "Almacén Sucursal 2", null));
            log.info("Se crearon los almacenes ALM01 y ALM02");
        }
    }

    private void crearCorrelativos() {
        crearSiFalta(new Correlativo(CorrelativoService.COMPRA, "CO", 6));
        crearSiFalta(new Correlativo(CorrelativoService.VENTA, "VE", 6));
        crearSiFalta(new Correlativo(CorrelativoService.TRASLADO, "TR", 6));
        for (TipoComprobante tipo : TipoComprobante.values()) {
            crearSiFalta(new Correlativo(tipo.name(), tipo.getSerie(), 8));
        }
    }

    private void crearSiFalta(Correlativo correlativo) {
        if (!correlativoRepository.existsById(correlativo.getClave())) {
            correlativoRepository.save(correlativo);
        }
    }

    private void crearAdministrador() {
        if (usuarioRepository.findByUsername(adminUsername).isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNombre("Administrador");
            admin.setRol("ADMIN");
            usuarioRepository.save(admin);
            log.info("Se creó el usuario administrador '{}'", adminUsername);
        }
    }

    /**
     * Productos registrados antes de manejar almacenes tienen stock total pero ningún stock por almacén:
     * ese stock se asigna al almacén principal para que las cifras cuadren.
     */
    private void asignarStockInicial() {
        Almacen principal = almacenRepository.findAllByOrderByCodigoAsc().getFirst();
        for (Producto producto : productoRepository.findAll()) {
            if (producto.getStock() > 0 && !stockRepository.existsByProductoId(producto.getId())) {
                Stock stock = new Stock(principal, producto);
                stock.setCantidad(producto.getStock());
                stockRepository.save(stock);
                log.info("Stock inicial de {} asignado a {}", producto.getCodigo(), principal.getCodigo());
            }
        }
    }
}
