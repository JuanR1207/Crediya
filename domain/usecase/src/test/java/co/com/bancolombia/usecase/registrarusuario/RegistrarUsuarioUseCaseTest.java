package co.com.bancolombia.usecase.registrarusuario;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.TipoError;
import co.com.bancolombia.model.transaccion.gateways.TransaccionGateway;
import co.com.bancolombia.model.usuario.Usuario;
import co.com.bancolombia.model.usuario.gateways.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TransaccionGateway transaccionGateway;

    private RegistrarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarUsuarioUseCase(usuarioRepository, transaccionGateway);
        lenient().when(transaccionGateway.ejecutarEnTransaccion(any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
    }

    private Usuario usuarioValido() {
        return Usuario.builder()
                .nombres("Juan José")
                .apellidos("Rendón")
                .fechaNacimiento(LocalDate.of(2000, 5, 10))
                .direccion("Calle 10 # 20-30")
                .telefono("3001234567")
                .correoElectronico("  Juan@Correo.com ")
                .documentoIdentidad("1020304050")
                .salarioBase(new BigDecimal("2500000"))
                .build();
    }

    private void simularGuardado() {
        when(usuarioRepository.guardar(any(Usuario.class)))
                .thenAnswer(inv -> ((Usuario) inv.getArgument(0)).toBuilder().idUsuario(1L).build());
    }

    @Test
    void debeRegistrarUsuarioValidoYNormalizarCorreo() {
        when(usuarioRepository.existePorCorreo("juan@correo.com")).thenReturn(false);
        when(usuarioRepository.existePorDocumento("1020304050")).thenReturn(false);
        simularGuardado();

        Usuario guardado = useCase.registrar(usuarioValido());

        assertEquals(1L, guardado.getIdUsuario());
        assertEquals("juan@correo.com", guardado.getCorreoElectronico());
        verify(transaccionGateway).ejecutarEnTransaccion(any());
    }

    @Test
    void debeRechazarCamposObligatoriosVacios() {
        Usuario usuario = usuarioValido().toBuilder()
                .nombres(" ")
                .apellidos(null)
                .correoElectronico("")
                .salarioBase(null)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.registrar(usuario));

        assertEquals(TipoError.DATOS_INVALIDOS, ex.getTipoError());
        assertEquals(4, ex.getDetalles().size());
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void debeRechazarCorreoConFormatoInvalido() {
        Usuario usuario = usuarioValido().toBuilder().correoElectronico("juan.correo.com").build();

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.registrar(usuario));

        assertTrue(ex.getDetalles().contains("El campo correo_electronico no tiene un formato de email válido"));
    }

    @Test
    void debeRechazarSalarioNegativo() {
        Usuario usuario = usuarioValido().toBuilder().salarioBase(new BigDecimal("-1")).build();

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.registrar(usuario));

        assertTrue(ex.getDetalles().contains("El campo salario_base debe estar entre 0 y 15000000"));
    }

    @Test
    void debeRechazarSalarioMayorAlMaximo() {
        Usuario usuario = usuarioValido().toBuilder().salarioBase(new BigDecimal("15000000.01")).build();

        assertThrows(BusinessException.class, () -> useCase.registrar(usuario));
    }

    @Test
    void debeAceptarSalarioEnLosLimites() {
        when(usuarioRepository.existePorCorreo(any())).thenReturn(false);
        when(usuarioRepository.existePorDocumento(any())).thenReturn(false);
        simularGuardado();

        assertEquals(new BigDecimal("15000000"),
                useCase.registrar(usuarioValido().toBuilder().salarioBase(new BigDecimal("15000000")).build())
                        .getSalarioBase());
        assertEquals(BigDecimal.ZERO,
                useCase.registrar(usuarioValido().toBuilder().salarioBase(BigDecimal.ZERO).build())
                        .getSalarioBase());
    }

    @Test
    void debeRechazarCorreoYaRegistrado() {
        when(usuarioRepository.existePorCorreo("juan@correo.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.registrar(usuarioValido()));

        assertEquals(TipoError.CORREO_YA_REGISTRADO, ex.getTipoError());
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void debeRechazarDocumentoYaRegistrado() {
        when(usuarioRepository.existePorCorreo(any())).thenReturn(false);
        when(usuarioRepository.existePorDocumento("1020304050")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.registrar(usuarioValido()));

        assertEquals(TipoError.DOCUMENTO_YA_REGISTRADO, ex.getTipoError());
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void noDebeConsultarDocumentoSiNoSeEnvia() {
        when(usuarioRepository.existePorCorreo(any())).thenReturn(false);
        simularGuardado();
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        useCase.registrar(usuarioValido().toBuilder().documentoIdentidad("  ").build());

        verify(usuarioRepository, never()).existePorDocumento(any());
        verify(usuarioRepository).guardar(captor.capture());
        assertEquals("Juan José", captor.getValue().getNombres());
    }

    @Test
    void debeRechazarFechaNacimientoFuturaYTelefonoInvalido() {
        Usuario usuario = usuarioValido().toBuilder()
                .fechaNacimiento(LocalDate.now().plusDays(1))
                .telefono("abc")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.registrar(usuario));

        assertEquals(2, ex.getDetalles().size());
    }

    @Test
    void debeEnmascararCorreo() {
        assertEquals("ju***@correo.com", RegistrarUsuarioUseCase.enmascararCorreo("juan@correo.com"));
        assertEquals("***", RegistrarUsuarioUseCase.enmascararCorreo(null));
    }
}
