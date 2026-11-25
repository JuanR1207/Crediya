package co.com.bancolombia.jpa.usuario;

import co.com.bancolombia.model.usuario.Usuario;

final class UsuarioDataMapper {

    private UsuarioDataMapper() {
    }

    static UsuarioData toData(Usuario usuario) {
        return UsuarioData.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .direccion(usuario.getDireccion())
                .telefono(usuario.getTelefono())
                .correoElectronico(usuario.getCorreoElectronico())
                .documentoIdentidad(usuario.getDocumentoIdentidad())
                .salarioBase(usuario.getSalarioBase())
                .build();
    }

    static Usuario toDomain(UsuarioData data) {
        return Usuario.builder()
                .idUsuario(data.getIdUsuario())
                .nombres(data.getNombres())
                .apellidos(data.getApellidos())
                .fechaNacimiento(data.getFechaNacimiento())
                .direccion(data.getDireccion())
                .telefono(data.getTelefono())
                .correoElectronico(data.getCorreoElectronico())
                .documentoIdentidad(data.getDocumentoIdentidad())
                .salarioBase(data.getSalarioBase())
                .build();
    }
}
