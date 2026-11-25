package co.com.bancolombia.model.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class BusinessException extends RuntimeException {

    private final TipoError tipoError;
    private final List<String> detalles;

    public BusinessException(TipoError tipoError) {
        this(tipoError, List.of());
    }

    public BusinessException(TipoError tipoError, List<String> detalles) {
        super(tipoError.getMensaje());
        this.tipoError = tipoError;
        this.detalles = detalles == null ? List.of() : List.copyOf(detalles);
    }
}
