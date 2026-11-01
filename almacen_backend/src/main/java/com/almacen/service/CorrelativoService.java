package com.almacen.service;

import com.almacen.entity.Correlativo;
import com.almacen.repository.CorrelativoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Genera números correlativos sin saltos ni repeticiones (bloquea la fila mientras dura la transacción). */
@Service
@RequiredArgsConstructor
public class CorrelativoService {

    public static final String COMPRA = "COMPRA";
    public static final String VENTA = "VENTA";
    public static final String TRASLADO = "TRASLADO";

    private final CorrelativoRepository correlativoRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public String siguiente(String clave) {
        Correlativo correlativo = correlativoRepository.findForUpdate(clave)
                .orElseThrow(() -> new IllegalStateException("No está configurado el correlativo " + clave));
        correlativo.setUltimo(correlativo.getUltimo() + 1);
        return correlativo.getPrefijo() + "-"
                + String.format("%0" + correlativo.getDigitos() + "d", correlativo.getUltimo());
    }
}
