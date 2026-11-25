package co.com.bancolombia.api.error;

import co.com.bancolombia.model.exception.TipoError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void debeMapearCadaTipoDeErrorAUnStatusHttp() {
        assertEquals(HttpStatus.BAD_REQUEST, GlobalExceptionHandler.statusPara(TipoError.DATOS_INVALIDOS));
        assertEquals(HttpStatus.BAD_REQUEST, GlobalExceptionHandler.statusPara(TipoError.CUERPO_REQUERIDO));
        assertEquals(HttpStatus.CONFLICT, GlobalExceptionHandler.statusPara(TipoError.CORREO_YA_REGISTRADO));
        assertEquals(HttpStatus.CONFLICT, GlobalExceptionHandler.statusPara(TipoError.DOCUMENTO_YA_REGISTRADO));
        assertEquals(HttpStatus.CONFLICT, GlobalExceptionHandler.statusPara(TipoError.USUARIO_DUPLICADO));
    }
}
