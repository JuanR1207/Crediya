package co.com.bancolombia.jpa.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioDataRepository extends JpaRepository<UsuarioData, Long> {

    boolean existsByCorreoElectronico(String correoElectronico);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);
}
