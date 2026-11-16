package co.com.bancolombia.api.error;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.TipoError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Convierte cualquier excepción en una respuesta JSON controlada,
 * para que el consumidor del API nunca reciba trazas ni mensajes inesperados.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    static final String CODIGO_CUERPO_INVALIDO = "GEN-400";
    static final String CODIGO_ERROR_INTERNO = "GEN-500";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<RespuestaError> manejarNegocio(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = statusPara(ex.getTipoError());
        log.warn("Error de negocio {} en {}: {} {}", ex.getTipoError().getCodigo(), request.getRequestURI(),
                ex.getMessage(), ex.getDetalles());
        return construir(status, ex.getTipoError().getCodigo(), ex.getMessage(), ex.getDetalles(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> manejarCuerpoInvalido(HttpMessageNotReadableException ex,
                                                                HttpServletRequest request) {
        log.warn("Cuerpo de petición ilegible en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.BAD_REQUEST, CODIGO_CUERPO_INVALIDO,
                "El cuerpo de la petición no es válido",
                List.of("Verifique que el JSON esté bien formado y que los tipos de datos sean correctos "
                        + "(ej. salario_base numérico y fecha_nacimiento con formato yyyy-MM-dd)"),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarGeneral(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorSpring) {
            HttpStatusCode status = errorSpring.getStatusCode();
            log.warn("Error HTTP {} en {}: {}", status.value(), request.getRequestURI(), ex.getMessage());
            return construir(status, "GEN-" + status.value(), mensajePara(status), List.of(), request);
        }
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, CODIGO_ERROR_INTERNO,
                "Ocurrió un error inesperado, intente nuevamente más tarde", List.of(), request);
    }

    static HttpStatus statusPara(TipoError tipoError) {
        return switch (tipoError) {
            case CORREO_YA_REGISTRADO, DOCUMENTO_YA_REGISTRADO, USUARIO_DUPLICADO -> HttpStatus.CONFLICT;
            case CUERPO_REQUERIDO, DATOS_INVALIDOS -> HttpStatus.BAD_REQUEST;
        };
    }

    private static String mensajePara(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> "El recurso solicitado no existe";
            case 405 -> "Método HTTP no permitido para este recurso";
            case 406 -> "Tipo de respuesta no soportado";
            case 415 -> "Tipo de contenido no soportado, use application/json";
            default -> "No fue posible procesar la petición";
        };
    }

    private static ResponseEntity<RespuestaError> construir(HttpStatusCode status, String codigo, String mensaje,
                                                            List<String> detalles, HttpServletRequest request) {
        RespuestaError cuerpo = new RespuestaError(codigo, mensaje, detalles, request.getRequestURI(),
                Instant.now().toString());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
