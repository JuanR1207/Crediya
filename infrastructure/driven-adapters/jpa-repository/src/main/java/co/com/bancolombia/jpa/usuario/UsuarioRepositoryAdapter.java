package co.com.bancolombia.jpa.usuario;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.TipoError;
import co.com.bancolombia.model.usuario.Usuario;
import co.com.bancolombia.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioDataRepository repository;

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        log.debug("Persistiendo usuario en base de datos");
        try {
            // saveAndFlush fuerza el INSERT aquí, así una violación de UNIQUE se captura en este punto
            UsuarioData guardado = repository.saveAndFlush(UsuarioDataMapper.toData(usuario));
            log.debug("Usuario persistido con id {}", guardado.getIdUsuario());
            return UsuarioDataMapper.toDomain(guardado);
        } catch (DataIntegrityViolationException e) {
            log.warn("Violación de integridad al guardar usuario: {}", e.getMostSpecificCause().getMessage());
            throw new BusinessException(TipoError.USUARIO_DUPLICADO);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCorreo(String correoElectronico) {
        return repository.existsByCorreoElectronico(correoElectronico);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorDocumento(String documentoIdentidad) {
        return repository.existsByDocumentoIdentidad(documentoIdentidad);
    }
}
