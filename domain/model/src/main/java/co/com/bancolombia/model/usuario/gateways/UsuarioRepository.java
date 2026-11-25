package co.com.bancolombia.model.usuario.gateways;

import co.com.bancolombia.model.usuario.Usuario;

public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    boolean existePorCorreo(String correoElectronico);

    boolean existePorDocumento(String documentoIdentidad);
}
