package br.com.nhac.backend_nhac.domain.produto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoAvaliacaoResumoDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoResumoDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoUpdateDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.infra.security.TokenService;

@WebMvcTest(ProdutoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProdutoService produtoService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

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
    @DisplayName("Deve retornar Erro 400 quando criar produto com nome vazio")
    void deveDevolverErro400QuandoNomeVazio() throws Exception {
        ProdutoCreateDTO dtoInvalido = new ProdutoCreateDTO(
                "", "Desc", new BigDecimal("10.00"), "Cat", null, "12", null, null, null
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve retornar Erro 400 quando criar produto com preço negativo")
    void deveDevolverErro400QuandoPrecoNegativo() throws Exception {
        ProdutoCreateDTO dtoInvalido = new ProdutoCreateDTO(
                "Hambúrguer", "Desc", new BigDecimal("-5.00"), "Cat", "url", "23", 0, null, null
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve retornar 201 ao cadastrar um produto com dados válidos")
    void deveCadastrarProdutoComSucesso() throws Exception {
        ProdutoCreateDTO dtoValido = new ProdutoCreateDTO(
                "Hossomaki", "Descrição", new BigDecimal("25.50"),
                "Sushi", "url", "200g", 10, null, 10
        );

        Produto produtoSalvo = new Produto();
        produtoSalvo.setId("produto_1");
        produtoSalvo.setNome("Hossomaki");
        produtoSalvo.setDescricao("Descrição");
        produtoSalvo.setPreco(new BigDecimal("25.50"));
        produtoSalvo.setCategoriaMenu("Sushi");
        
        br.com.nhac.backend_nhac.domain.loja.Loja lojaMock = new br.com.nhac.backend_nhac.domain.loja.Loja();
        lojaMock.setId("loja_123");
        produtoSalvo.setLoja(lojaMock);

        when(produtoService.cadastrarProduto(any(), any())).thenReturn(produtoSalvo);

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve retornar 200 ao listar produtos sem filtros")
    void deveListarProdutosComSucesso() throws Exception {
        ProdutoResumoDTO produto = new ProdutoResumoDTO(
                "produto_1", "loja_123", "Loja Teste", "Hossomaki", "Hossomakinho",
                new BigDecimal("25.50"), "Sushi", "url", "23g", 0, true, null
        );
        Page<ProdutoResumoDTO> pagina = new PageImpl<>(List.of(produto), PageRequest.of(0, 10), 1);

        when(produtoService.listarProdutos(any(), any(), any(), any(), any())).thenReturn(pagina);

        mockMvc.perform(get("/api/v1/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("produto_1"));
    }

    @Test
    @DisplayName("Deve retornar 200 com os dados do produto ao buscar por ID")
    void deveBuscarProdutoPorIdComSucesso() throws Exception {
        ProdutoResumoDTO produto = new ProdutoResumoDTO(
                "produto_1", "loja_123", "Loja Teste", "Hossomaki", "Hossomakinho",
                new BigDecimal("25.50"), "Sushi", "url", "23g", 0, true, null
        );

        when(produtoService.buscarProdutoPorId("produto_1")).thenReturn(produto);

        mockMvc.perform(get("/api/v1/produtos/{produtoId}", "produto_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("produto_1"))
                .andExpect(jsonPath("$.nome").value("Hossomaki"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar um produto que não existe ou está inativo")
    void deveRetornar404AoBuscarProdutoInexistente() throws Exception {
        when(produtoService.buscarProdutoPorId("produto_fantasma"))
                .thenThrow(new IdNaoEncontradoException(
                        "O produto com o id: produto_fantasma não foi encontrado."));

        mockMvc.perform(get("/api/v1/produtos/{produtoId}", "produto_fantasma"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("O produto com o id: produto_fantasma não foi encontrado."));
    }

    @Test
    @DisplayName("Deve retornar 200 ao atualizar um produto com dados válidos")
    void deveAtualizarProdutoComSucesso() throws Exception {
        ProdutoUpdateDTO dtoValido = new ProdutoUpdateDTO(
                "Hossomaki Editado", "Nova descrição", new BigDecimal("30.00"),
                "Sushi", "nova-url", "250g", 0, true, null, 50
        );

        ProdutoResumoDTO produtoAtualizado = new ProdutoResumoDTO(
                "produto_1", "loja_123", "Loja Teste", "Hossomaki Editado", "Nova descrição",
                new BigDecimal("30.00"), "Sushi", "nova-url", "250g", 0, true, null
        );

        when(produtoService.atualizarProduto(any(), any(), any())).thenReturn(produtoAtualizado);

        mockMvc.perform(put("/api/v1/produtos/{produtoId}", "produto_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Hossomaki Editado"));
    }

    @Test
    @DisplayName("Deve retornar Erro 400 ao tentar atualizar produto com nome vazio")
    void deveRetornarErro400AoAtualizarComNomeVazio() throws Exception {
        ProdutoUpdateDTO dtoInvalido = new ProdutoUpdateDTO(
                "", "Nova descrição", new BigDecimal("30.00"),
                "Sushi", "nova-url", "250g", 0, true, null, 50
        );

        mockMvc.perform(put("/api/v1/produtos/{produtoId}", "produto_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve retornar Erro 404 ao tentar atualizar um produto que não existe")
    void deveRetornarErro404AoAtualizarProdutoInexistente() throws Exception {
        ProdutoUpdateDTO dtoValido = new ProdutoUpdateDTO(
                "Hossomaki Editado", "Nova descrição", new BigDecimal("30.00"),
                "Sushi", "nova-url", "250g", 0, true, null, 50
        );

        when(produtoService.atualizarProduto(any(), any(), any()))
                .thenThrow(new IdNaoEncontradoException("O produto com o id: produto_fantasma não foi encontrado."));

        mockMvc.perform(put("/api/v1/produtos/{produtoId}", "produto_fantasma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 204 ao desativar um produto existente")
    void deveRetornar204AoDesativarProduto() throws Exception {
        doNothing().when(produtoService).desativarProduto(any(), any());

        mockMvc.perform(delete("/api/v1/produtos/{produtoId}", "produto_1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar Erro 404 ao tentar desativar produto que não existe")
    void deveRetornar404AoDesativarProdutoInexistente() throws Exception {
        doThrow(new IdNaoEncontradoException("O produto com o id: produto_fantasma não foi encontrado."))
                .when(produtoService).desativarProduto(any(), any());

        mockMvc.perform(delete("/api/v1/produtos/{produtoId}", "produto_fantasma"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 200 com resumo das avaliações do produto")
    void deveRetornarResumoAvaliacoesProduto() throws Exception {
        ProdutoAvaliacaoResumoDTO resumo = new ProdutoAvaliacaoResumoDTO(15L, 4.8);

        when(produtoService.buscarResumoAvaliacoes("produto_1")).thenReturn(resumo);

        mockMvc.perform(get("/api/v1/produtos/{produtoId}/avaliacoes/resumo", "produto_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAvaliacoes").value(15))
                .andExpect(jsonPath("$.mediaNotas").value(4.8));
    }

    @Test
    @DisplayName("Deve retornar 200 ao reativar um produto com sucesso")
    void deveRetornar200AoAtivarProdutoComSucesso() throws Exception {
        ProdutoResumoDTO resumo = new ProdutoResumoDTO(
                "produto_1", "loja_1", "Sushi Ken", "Hossomaki", "Descrição",
                new BigDecimal("25.50"), "Sushi", "url", "200g", 0, true, List.of()
        );

        when(produtoService.ativarProduto(eq("produto_1"), any())).thenReturn(resumo);

        mockMvc.perform(patch("/api/v1/produtos/{produtoId}/ativar", "produto_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("produto_1"))
                .andExpect(jsonPath("$.nome").value("Hossomaki"));
    }

    @Test
    @DisplayName("Deve retornar 403 ao tentar reativar produto de outro usuário")
    void deveRetornar403AoAtivarProdutoDeOutroUsuario() throws Exception {
        when(produtoService.ativarProduto(eq("produto_1"), any()))
                .thenThrow(new AcessoNegadoException("Acesso negado"));

        mockMvc.perform(patch("/api/v1/produtos/{produtoId}/ativar", "produto_1"))
                .andExpect(status().isForbidden());
    }
}