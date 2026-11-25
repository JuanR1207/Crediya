package co.com.bancolombia.model.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoError {
    CUERPO_REQUERIDO("USR-001", "El cuerpo de la petición es obligatorio"),
    DATOS_INVALIDOS("USR-002", "La información enviada no es válida"),
    CORREO_YA_REGISTRADO("USR-003", "El correo electrónico ya está registrado por otro usuario"),
    DOCUMENTO_YA_REGISTRADO("USR-004", "El documento de identidad ya está registrado por otro usuario"),
    USUARIO_DUPLICADO("USR-005", "Ya existe un usuario con los datos enviados");

    private final String codigo;
    private final String mensaje;
}
