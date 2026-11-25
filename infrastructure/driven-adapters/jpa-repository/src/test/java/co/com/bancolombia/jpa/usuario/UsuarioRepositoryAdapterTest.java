package co.com.bancolombia.jpa.usuario;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.TipoError;
import co.com.bancolombia.model.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioRepositoryAdapterTest {

    @Mock
    private UsuarioDataRepository repository;

    @InjectMocks
    private UsuarioRepositoryAdapter adapter;

    private final Usuario usuario = Usuario.builder()
            .nombres("Ana")
            .apellidos("Gómez")
            .fechaNacimiento(LocalDate.of(1995, 1, 20))
            .direccion("Cra 1")
            .telefono("3000000000")
            .correoElectronico("ana@correo.com")
            .documentoIdentidad("12345678")
            .salarioBase(new BigDecimal("1800000"))
            .build();

    @Test
    void debeGuardarYMapearUsuario() {
        when(repository.saveAndFlush(any(UsuarioData.class))).thenAnswer(inv -> {
            UsuarioData data = inv.getArgument(0);
            data.setIdUsuario(7L);
            return data;
        });

        Usuario guardado = adapter.guardar(usuario);

        assertEquals(7L, guardado.getIdUsuario());
        assertEquals("ana@correo.com", guardado.getCorreoElectronico());
        assertEquals(new BigDecimal("1800000"), guardado.getSalarioBase());
        assertEquals(LocalDate.of(1995, 1, 20), guardado.getFechaNacimiento());
    }

    @Test
    void debeConvertirViolacionDeIntegridadEnErrorDeNegocio() {
        when(repository.saveAndFlush(any(UsuarioData.class)))
                .thenThrow(new DataIntegrityViolationException("unique"));

        BusinessException ex = assertThrows(BusinessException.class, () -> adapter.guardar(usuario));

        assertEquals(TipoError.USUARIO_DUPLICADO, ex.getTipoError());
    }

    @Test
    void debeConsultarExistenciaPorCorreoYDocumento() {
        when(repository.existsByCorreoElectronico("ana@correo.com")).thenReturn(true);
        when(repository.existsByDocumentoIdentidad("12345678")).thenReturn(true);

        assertTrue(adapter.existePorCorreo("ana@correo.com"));
        assertTrue(adapter.existePorDocumento("12345678"));
    }
}
