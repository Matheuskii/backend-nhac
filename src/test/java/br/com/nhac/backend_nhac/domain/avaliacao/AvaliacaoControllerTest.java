package br.com.nhac.backend_nhac.domain.avaliacao;

import br.com.nhac.backend_nhac.domain.avaliacao.dto.AvaliacaoCreateDTO;
import br.com.nhac.backend_nhac.domain.avaliacao.dto.AvaliacaoResumoDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.services.AvaliacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvaliacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AvaliacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AvaliacaoService avaliacaoService;

    @MockitoBean
    private br.com.nhac.backend_nhac.infra.security.TokenService tokenService;

    @MockitoBean
    private br.com.nhac.backend_nhac.repositories.UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId("user_123");
        usuarioMock.setEmail("teste@nhac.com");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(usuarioMock, null, Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Deve retornar 201 ao criar avaliação válida")
    void deveCriarAvaliacaoComSucesso() throws Exception {
        AvaliacaoCreateDTO dto = new AvaliacaoCreateDTO("pedido_1", 5, "Excelente!");
        AvaliacaoResumoDTO resumo = new AvaliacaoResumoDTO("aval_1", "João", 5, "Excelente!", LocalDateTime.now());

        when(avaliacaoService.criarAvaliacao(eq("user_123"), any())).thenReturn(resumo);

        mockMvc.perform(post("/api/v1/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect((ResultMatcher) jsonPath("$.id").value("aval_1"))
                .andExpect((ResultMatcher) jsonPath("$.nota").value(5));
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar criar avaliação com nota fora do limite")
    void deveRetornarErro400AoCriarAvaliacaoNotaInvalida() throws Exception {
        AvaliacaoCreateDTO dto = new AvaliacaoCreateDTO("pedido_1", 6, "Excelente!"); // Nota maior que 5

        mockMvc.perform(post("/api/v1/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect((ResultMatcher) jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar criar avaliação sem pedidoId")
    void deveRetornarErro400AoCriarAvaliacaoSemPedidoId() throws Exception {
        AvaliacaoCreateDTO dto = new AvaliacaoCreateDTO("", 5, "Excelente!");

        mockMvc.perform(post("/api/v1/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect((ResultMatcher) jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve retornar 200 ao listar avaliações de uma loja")
    void deveListarAvaliacoesPorLoja() throws Exception {
        AvaliacaoResumoDTO resumo = new AvaliacaoResumoDTO("aval_1", "João", 5, "Excelente!", LocalDateTime.now());
        Page<AvaliacaoResumoDTO> pagina = new PageImpl<>(List.of(resumo), PageRequest.of(0, 10), 1);

        when(avaliacaoService.listarAvaliacoesPorLoja(eq("loja_1"), any())).thenReturn(pagina);

        mockMvc.perform(get("/api/v1/lojas/{lojaId}/avaliacoes", "loja_1"))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.content[0].id").value("aval_1"))
                .andExpect((ResultMatcher) jsonPath("$.content[0].nota").value(5));
    }

    @Test
    @DisplayName("Deve retornar 404 ao listar avaliações de loja inexistente")
    void deveRetornar404AoListarAvaliacoesLojaInexistente() throws Exception {
        when(avaliacaoService.listarAvaliacoesPorLoja(eq("loja_fantasma"), any()))
                .thenThrow(new br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException("A loja com o id: loja_fantasma não foi encontrada."));

        mockMvc.perform(get("/api/v1/lojas/{lojaId}/avaliacoes", "loja_fantasma"))
                .andExpect(status().isNotFound())
                .andExpect((ResultMatcher) jsonPath("$.message").value("A loja com o id: loja_fantasma não foi encontrada."));
    }
}
