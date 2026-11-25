package co.com.bancolombia.jpa.transaccion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JpaTransaccionAdapterTest {

    private final JpaTransaccionAdapter adapter = new JpaTransaccionAdapter();

    @Test
    void debeRetornarResultadoDeLaOperacion() {
        assertEquals("ok", adapter.ejecutarEnTransaccion(() -> "ok"));
    }

    @Test
    void debePropagarExcepcionesDeLaOperacion() {
        assertThrows(IllegalStateException.class, () -> adapter.ejecutarEnTransaccion(() -> {
            throw new IllegalStateException("falla");
        }));
    }
}
