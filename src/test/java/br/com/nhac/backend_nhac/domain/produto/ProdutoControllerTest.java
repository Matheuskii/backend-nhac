package br.com.nhac.backend_nhac.domain.produto;


import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoResumoDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.services.ProdutoService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutoControllerTest {


    @Autowired
    private MockMvc mockMvc;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @MockitoBean
    private ProdutoService produtoService;

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
    @DisplayName("Deve retornar Erro 422 quando criar produto com nome vazio")
    void deveDevolverErro400QuandoNomeVazio() throws Exception {
        ProdutoCreateDTO dtoInvalido = new ProdutoCreateDTO(
                "loja_123", "", "Desc", new BigDecimal("10.00"), "Cat", null, "12", null
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect((ResultMatcher) jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve retornar Erro 422 quando criar produto com preço negativo")
    void deveDevolverErro400QuandoPrecoNegativo() throws Exception {
        ProdutoCreateDTO dtoInvalido = new ProdutoCreateDTO(
                "loja_123", "Hambúrguer", "Desc", new BigDecimal("-5.00"), "Cat", "url", "23", 0
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect((ResultMatcher) jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve retornar 201 ao cadastrar um produto com dados válidos")
    void deveCadastrarProdutoComSucesso() throws Exception {
        ProdutoCreateDTO dtoValido = new ProdutoCreateDTO(
                "loja_123", "Hossomaki", "Descrição", new BigDecimal("25.50"),
                "Sushi", "url", "200g", 10
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve retornar 200 ao listar produtos sem filtros")
    void deveListarProdutosComSucesso() throws Exception {
        ProdutoResumoDTO produto = new ProdutoResumoDTO(
                "produto_1", "loja_123", "Hossomaki", "Hossomakinho", new BigDecimal("25.50"), "Sushi", "url", "23g",0
        );
        Page<ProdutoResumoDTO> pagina = new PageImpl<>(List.of(produto), PageRequest.of(0, 10), 1);

        when(produtoService.listarProdutos(any(), any(), any(), any(), any())).thenReturn(pagina);

        mockMvc.perform(get("/api/v1/produtos"))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.content[0].id").value("produto_1"));
    }
}