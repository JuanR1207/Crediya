package co.com.bancolombia.api.usuario;

import co.com.bancolombia.api.usuario.dto.RegistrarUsuarioRequest;
import co.com.bancolombia.api.usuario.dto.UsuarioResponse;
import co.com.bancolombia.model.usuario.Usuario;

final class UsuarioDtoMapper {

    private UsuarioDtoMapper() {
    }

    static Usuario toDomain(RegistrarUsuarioRequest request) {
        return Usuario.builder()
                .nombres(request.nombres())
                .apellidos(request.apellidos())
                .fechaNacimiento(request.fechaNacimiento())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .correoElectronico(request.correoElectronico())
                .documentoIdentidad(request.documentoIdentidad())
                .salarioBase(request.salarioBase())
                .build();
    }

    static UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getFechaNacimiento(),
                usuario.getDireccion(),
                usuario.getTelefono(),
                usuario.getCorreoElectronico(),
                usuario.getDocumentoIdentidad(),
                usuario.getSalarioBase());
    }
}
