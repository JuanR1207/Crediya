package co.com.bancolombia.api.usuario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos para registrar un nuevo usuario solicitante")
public record RegistrarUsuarioRequest(
        @Schema(description = "Nombres del usuario", example = "Juan José", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("nombres") String nombres,

        @Schema(description = "Apellidos del usuario", example = "Rendón Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("apellidos") String apellidos,

        @Schema(description = "Fecha de nacimiento (yyyy-MM-dd)", example = "2000-05-10")
        @JsonProperty("fecha_nacimiento") LocalDate fechaNacimiento,

        @Schema(description = "Dirección de residencia", example = "Calle 10 # 20-30, Medellín")
        @JsonProperty("direccion") String direccion,

        @Schema(description = "Teléfono de contacto", example = "3001234567")
        @JsonProperty("telefono") String telefono,

        @Schema(description = "Correo electrónico (único)", example = "juan@correo.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("correo_electronico") String correoElectronico,

        @Schema(description = "Documento de identidad (único, opcional en esta HU)", example = "1020304050")
        @JsonProperty("documento_identidad") String documentoIdentidad,

        @Schema(description = "Salario base entre 0 y 15000000", example = "2500000", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("salario_base") BigDecimal salarioBase
) {
}
