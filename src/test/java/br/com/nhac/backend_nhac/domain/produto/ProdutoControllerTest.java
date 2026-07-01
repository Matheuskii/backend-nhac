package br.com.nhac.backend_nhac.domain.produto;


import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoCreateDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.services.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutoControllerTest {


    @Autowired
    private MockMvc mockMvc;

    private tools.jackson.databind.ObjectMapper objectMapper;

    public ProdutoControllerTest(tools.jackson.databind.ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @MockitoBean
    private ProdutoService produtoService;

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
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve retornar Erro 422 quando criar produto com preço negativo")
    void deveDevolverErro400QuandoPrecoNegativo() throws Exception {
        ProdutoCreateDTO dtoInvalido = new ProdutoCreateDTO(
                "loja_123", "Hambúrguer", "Desc", new BigDecimal("-5.00"), "Cat", null, 0, null
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect((ResultMatcher) jsonPath("$.message").exists());
    }
}