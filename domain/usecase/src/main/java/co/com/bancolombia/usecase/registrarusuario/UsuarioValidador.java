package co.com.bancolombia.usecase.registrarusuario;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.TipoError;
import co.com.bancolombia.model.usuario.Usuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Reglas de validación del registro de usuarios (HU1).
 * Acumula todos los errores para devolverlos juntos al cliente.
 */
public final class UsuarioValidador {

    public static final BigDecimal SALARIO_MINIMO = BigDecimal.ZERO;
    public static final BigDecimal SALARIO_MAXIMO = new BigDecimal("15000000");

    private static final int MAX_NOMBRES = 100;
    private static final int MAX_DIRECCION = 200;
    private static final int MAX_CORREO = 150;

    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\+?\\d{7,15}$");
    private static final Pattern PATRON_DOCUMENTO = Pattern.compile("^\\d{5,15}$");

    private UsuarioValidador() {
    }

    /**
     * Valida el usuario y devuelve una copia normalizada (textos sin espacios extra y correo en minúsculas).
     *
     * @throws BusinessException con TipoError.DATOS_INVALIDOS y el detalle de cada campo inválido
     */
    public static Usuario validarYNormalizar(Usuario usuario) {
        Usuario normalizado = normalizar(usuario);
        List<String> errores = new ArrayList<>();

        validarTextoObligatorio(normalizado.getNombres(), "nombres", MAX_NOMBRES, errores);
        validarTextoObligatorio(normalizado.getApellidos(), "apellidos", MAX_NOMBRES, errores);
        validarCorreo(normalizado.getCorreoElectronico(), errores);
        validarSalario(normalizado.getSalarioBase(), errores);
        validarFechaNacimiento(normalizado.getFechaNacimiento(), errores);
        validarOpcional(normalizado.getTelefono(), PATRON_TELEFONO,
                "El campo telefono debe tener entre 7 y 15 dígitos (puede iniciar con +)", errores);
        validarOpcional(normalizado.getDocumentoIdentidad(), PATRON_DOCUMENTO,
                "El campo documento_identidad debe tener entre 5 y 15 dígitos", errores);

        if (normalizado.getDireccion() != null && normalizado.getDireccion().length() > MAX_DIRECCION) {
            errores.add("El campo direccion no puede superar " + MAX_DIRECCION + " caracteres");
        }

        if (!errores.isEmpty()) {
            throw new BusinessException(TipoError.DATOS_INVALIDOS, errores);
        }
        return normalizado;
    }

    private static Usuario normalizar(Usuario usuario) {
        String correo = limpiar(usuario.getCorreoElectronico());
        return usuario.toBuilder()
                .nombres(limpiar(usuario.getNombres()))
                .apellidos(limpiar(usuario.getApellidos()))
                .direccion(limpiar(usuario.getDireccion()))
                .telefono(limpiar(usuario.getTelefono()))
                .documentoIdentidad(limpiar(usuario.getDocumentoIdentidad()))
                .correoElectronico(correo == null ? null : correo.toLowerCase(Locale.ROOT))
                .build();
    }

    private static String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private static void validarTextoObligatorio(String valor, String campo, int maximo, List<String> errores) {
        if (valor == null) {
            errores.add("El campo " + campo + " es obligatorio y no puede estar vacío");
        } else if (valor.length() > maximo) {
            errores.add("El campo " + campo + " no puede superar " + maximo + " caracteres");
        }
    }

    private static void validarCorreo(String correo, List<String> errores) {
        if (correo == null) {
            errores.add("El campo correo_electronico es obligatorio y no puede estar vacío");
        } else if (correo.length() > MAX_CORREO || !PATRON_CORREO.matcher(correo).matches()) {
            errores.add("El campo correo_electronico no tiene un formato de email válido");
        }
    }

    private static void validarSalario(BigDecimal salario, List<String> errores) {
        if (salario == null) {
            errores.add("El campo salario_base es obligatorio");
        } else if (salario.compareTo(SALARIO_MINIMO) < 0 || salario.compareTo(SALARIO_MAXIMO) > 0) {
            errores.add("El campo salario_base debe estar entre 0 y 15000000");
        }
    }

    private static void validarFechaNacimiento(LocalDate fecha, List<String> errores) {
        if (fecha != null && !fecha.isBefore(LocalDate.now())) {
            errores.add("El campo fecha_nacimiento debe ser una fecha pasada");
        }
    }

    private static void validarOpcional(String valor, Pattern patron, String mensaje, List<String> errores) {
        if (valor != null && !patron.matcher(valor).matches()) {
            errores.add(mensaje);
        }
    }
}
