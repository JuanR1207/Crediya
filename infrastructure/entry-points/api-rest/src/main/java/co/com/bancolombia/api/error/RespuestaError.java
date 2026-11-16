package co.com.bancolombia.api.error;

import java.util.List;

public record RespuestaError(
        String codigo,
        String mensaje,
        List<String> detalles,
        String ruta,
        String timestamp
) {
}
