package co.com.bancolombia.usecase.registrarusuario;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.TipoError;
import co.com.bancolombia.model.transaccion.gateways.TransaccionGateway;
import co.com.bancolombia.model.usuario.Usuario;
import co.com.bancolombia.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final TransaccionGateway transaccionGateway;

    public Usuario registrar(Usuario usuario) {
        Usuario valido = UsuarioValidador.validarYNormalizar(usuario);
        log.info(() -> "Datos del usuario válidos, verificando duplicados para "
                + enmascararCorreo(valido.getCorreoElectronico()));

        Usuario guardado = transaccionGateway.ejecutarEnTransaccion(() -> {
            validarCorreoNoRegistrado(valido.getCorreoElectronico());
            validarDocumentoNoRegistrado(valido.getDocumentoIdentidad());
            return usuarioRepository.guardar(valido);
        });

        log.info(() -> "Usuario registrado con id " + guardado.getIdUsuario());
        return guardado;
    }

    private void validarCorreoNoRegistrado(String correo) {
        if (usuarioRepository.existePorCorreo(correo)) {
            log.warning(() -> "Correo ya registrado: " + enmascararCorreo(correo));
            throw new BusinessException(TipoError.CORREO_YA_REGISTRADO);
        }
    }

    private void validarDocumentoNoRegistrado(String documento) {
        if (documento != null && usuarioRepository.existePorDocumento(documento)) {
            log.warning("Documento de identidad ya registrado");
            throw new BusinessException(TipoError.DOCUMENTO_YA_REGISTRADO);
        }
    }

    static String enmascararCorreo(String correo) {
        if (correo == null || !correo.contains("@")) {
            return "***";
        }
        int arroba = correo.indexOf('@');
        String visible = correo.substring(0, Math.min(2, arroba));
        return visible + "***" + correo.substring(arroba);
    }
}
