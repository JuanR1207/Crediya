package co.com.bancolombia.jpa.transaccion;

import co.com.bancolombia.model.transaccion.gateways.TransaccionGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Implementación del puerto de transacciones. Todo lo que el caso de uso ejecute aquí
 * (validar duplicados + guardar) ocurre en una sola transacción: si algo falla, se hace rollback.
 */
@Slf4j
@Component
public class JpaTransaccionAdapter implements TransaccionGateway {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> T ejecutarEnTransaccion(Supplier<T> operacion) {
        log.debug("Iniciando transacción");
        T resultado = operacion.get();
        log.debug("Transacción lista para commit");
        return resultado;
    }
}
