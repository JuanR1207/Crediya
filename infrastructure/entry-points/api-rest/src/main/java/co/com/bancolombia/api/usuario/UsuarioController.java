package co.com.bancolombia.api.usuario;

import co.com.bancolombia.api.error.RespuestaError;
import co.com.bancolombia.api.usuario.dto.RegistrarUsuarioRequest;
import co.com.bancolombia.api.usuario.dto.UsuarioResponse;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.TipoError;
import co.com.bancolombia.model.usuario.Usuario;
import co.com.bancolombia.usecase.registrarusuario.RegistrarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Registro de usuarios solicitantes")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @Operation(summary = "Registrar un nuevo usuario",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario registrado",
                            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos",
                            content = @Content(schema = @Schema(implementation = RespuestaError.class))),
                    @ApiResponse(responseCode = "409", description = "Correo o documento ya registrado",
                            content = @Content(schema = @Schema(implementation = RespuestaError.class))),
                    @ApiResponse(responseCode = "500", description = "Error interno",
                            content = @Content(schema = @Schema(implementation = RespuestaError.class)))
            })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioResponse> registrarUsuario(
            @RequestBody(required = false) RegistrarUsuarioRequest request) {
        log.info("Petición recibida: POST /api/v1/usuarios");
        if (request == null) {
            throw new BusinessException(TipoError.CUERPO_REQUERIDO);
        }

        Usuario registrado = registrarUsuarioUseCase.registrar(UsuarioDtoMapper.toDomain(request));

        log.info("Respuesta enviada: usuario creado con id {}", registrado.getIdUsuario());
        return ResponseEntity
                .created(URI.create("/api/v1/usuarios/" + registrado.getIdUsuario()))
                .body(UsuarioDtoMapper.toResponse(registrado));
    }
}
