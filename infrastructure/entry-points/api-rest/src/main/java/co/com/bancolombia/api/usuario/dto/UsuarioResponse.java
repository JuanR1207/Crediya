package co.com.bancolombia.api.usuario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UsuarioResponse(
        @JsonProperty("id_usuario") Long idUsuario,
        @JsonProperty("nombres") String nombres,
        @JsonProperty("apellidos") String apellidos,
        @JsonProperty("fecha_nacimiento") LocalDate fechaNacimiento,
        @JsonProperty("direccion") String direccion,
        @JsonProperty("telefono") String telefono,
        @JsonProperty("correo_electronico") String correoElectronico,
        @JsonProperty("documento_identidad") String documentoIdentidad,
        @JsonProperty("salario_base") BigDecimal salarioBase
) {
}
