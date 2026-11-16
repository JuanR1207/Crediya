package co.com.bancolombia.model.transaccion.gateways;

import java.util.function.Supplier;

/**
 * Puerto para ejecutar una operación dentro de una transacción.
 * El dominio no conoce Spring: la implementación con @Transactional vive en el adaptador JPA.
 */
public interface TransaccionGateway {

    <T> T ejecutarEnTransaccion(Supplier<T> operacion);
}
