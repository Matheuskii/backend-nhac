package br.com.nhac.backend_nhac.domain.auth;

import br.com.nhac.backend_nhac.domain.auth.dto.EnviarCodigoSmsDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.ValidarCodigoSmsDTO;
import br.com.nhac.backend_nhac.services.VerificacaoTelefoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VerificacaoTelefoneControllerTest {

    @Mock
    private VerificacaoTelefoneService verificacaoTelefoneService;

    @InjectMocks
    private VerificacaoTelefoneController verificacaoTelefoneController;

    private MockMvc mockMvc;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(verificacaoTelefoneController).build();
    }

    @Test
    @DisplayName("Deve retornar status 200 OK ao enviar cÃƒÂ³digo de verificaÃƒÂ§ÃƒÂ£o")
    void deveEnviarCodigoComSucesso() throws Exception {
        EnviarCodigoSmsDTO dto = new EnviarCodigoSmsDTO("+5511999999999");

        doNothing().when(verificacaoTelefoneService).enviarCodigo(any(EnviarCodigoSmsDTO.class));

        mockMvc.perform(post("/api/v1/verificacao-telefone/enviar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(verificacaoTelefoneService).enviarCodigo(any(EnviarCodigoSmsDTO.class));
    }

    @Test
    @DisplayName("Deve retornar status 400 Bad Request se telefone estiver vazio no envio de cÃƒÂ³digo")
    void deveRetornarBadRequestSeTelefoneVazioNoEnvio() throws Exception {
        EnviarCodigoSmsDTO dto = new EnviarCodigoSmsDTO(""); // InvÃƒÂ¡lido

        mockMvc.perform(post("/api/v1/verificacao-telefone/enviar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar status 200 OK ao validar cÃƒÂ³digo de verificaÃƒÂ§ÃƒÂ£o")
    void deveValidarCodigoComSucesso() throws Exception {
        ValidarCodigoSmsDTO dto = new ValidarCodigoSmsDTO("+5511999999999", "123456", null);

        doNothing().when(verificacaoTelefoneService).validarCodigo(any(ValidarCodigoSmsDTO.class));

        mockMvc.perform(post("/api/v1/verificacao-telefone/validar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(verificacaoTelefoneService).validarCodigo(any(ValidarCodigoSmsDTO.class));
    }

    @Test
    @DisplayName("Deve retornar status 400 Bad Request se formato de DTO for invÃƒÂ¡lido ao validar cÃƒÂ³digo")
    void deveRetornarBadRequestAoValidarCodigoInvalido() throws Exception {
        ValidarCodigoSmsDTO dto = new ValidarCodigoSmsDTO("11999999999", "123", null); // Sem + e cÃƒÂ³digo curto (invÃƒÂ¡lido)

        mockMvc.perform(post("/api/v1/verificacao-telefone/validar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
