package co.com.bancolombia.api.usuario;

import co.com.bancolombia.api.error.GlobalExceptionHandler;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.TipoError;
import co.com.bancolombia.model.usuario.Usuario;
import co.com.bancolombia.usecase.registrarusuario.RegistrarUsuarioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioControllerTest {

    private static final String URL = "/api/v1/usuarios";
    private static final String BODY_VALIDO = """
            {
              "nombres": "Juan José",
              "apellidos": "Rendón",
              "fecha_nacimiento": "2000-05-10",
              "direccion": "Calle 10 # 20-30",
              "telefono": "3001234567",
              "correo_electronico": "juan@correo.com",
              "documento_identidad": "1020304050",
              "salario_base": 2500000
            }
            """;

    private RegistrarUsuarioUseCase useCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        useCase = mock(RegistrarUsuarioUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(useCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void debeRetornar201CuandoSeRegistraElUsuario() throws Exception {
        when(useCase.registrar(any(Usuario.class))).thenReturn(Usuario.builder()
                .idUsuario(1L)
                .nombres("Juan José")
                .apellidos("Rendón")
                .fechaNacimiento(LocalDate.of(2000, 5, 10))
                .correoElectronico("juan@correo.com")
                .salarioBase(new BigDecimal("2500000"))
                .build());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/usuarios/1"))
                .andExpect(jsonPath("$.id_usuario").value(1))
                .andExpect(jsonPath("$.correo_electronico").value("juan@correo.com"))
                .andExpect(jsonPath("$.fecha_nacimiento").value("2000-05-10"));
    }

    @Test
    void debeRetornar400ConDetallesCuandoLosDatosSonInvalidos() throws Exception {
        when(useCase.registrar(any(Usuario.class))).thenThrow(new BusinessException(TipoError.DATOS_INVALIDOS,
                List.of("El campo nombres es obligatorio y no puede estar vacío")));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("USR-002"))
                .andExpect(jsonPath("$.detalles[0]").value("El campo nombres es obligatorio y no puede estar vacío"))
                .andExpect(jsonPath("$.ruta").value(URL));
    }

    @Test
    void debeRetornar409CuandoElCorreoYaExiste() throws Exception {
        when(useCase.registrar(any(Usuario.class))).thenThrow(new BusinessException(TipoError.CORREO_YA_REGISTRADO));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("USR-003"));
    }

    @Test
    void debeRetornar400CuandoSalarioNoEsNumerico() throws Exception {
        String body = BODY_VALIDO.replace("2500000", "\"mucho\"");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("GEN-400"));

        verify(useCase, never()).registrar(any());
    }

    @Test
    void debeRetornar400CuandoElJsonEstaMalFormado() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{ \"nombres\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("GEN-400"));
    }

    @Test
    void debeRetornar400CuandoNoHayCuerpo() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("USR-001"));
    }

    @Test
    void debeRetornar415CuandoElContenidoNoEsJson() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value("GEN-415"));
    }

    @Test
    void debeRetornar500SinExponerDetallesInternos() throws Exception {
        when(useCase.registrar(any(Usuario.class))).thenThrow(new IllegalStateException("SQL roto: tabla x"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("GEN-500"))
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado, intente nuevamente más tarde"));
    }
}
