package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.PedidoService;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoLojistaDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoLojistaDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LojistaController.class)
@AutoConfigureMockMvc(addFilters = false)
class LojistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LojistaService lojistaService;

        @MockitoBean
        private PedidoService pedidoService;

    @MockitoBean
    private br.com.nhac.backend_nhac.infra.security.TokenService tokenService;

    @MockitoBean
    private br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository usuarioRepository;

    private Usuario usuarioLogado;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario();
        usuarioLogado.setId("user_123");
        usuarioLogado.setEmail("lojista@nhac.com");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(usuarioLogado, null, usuarioLogado.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve listar produtos do lojista autenticado sem exigir lojaId")
    void deveListarProdutosDoLojista() throws Exception {
        ProdutoLojistaDTO produto = new ProdutoLojistaDTO(
                "prod_0007", "Hossomaki de Salmão", "Rolinho de arroz e alga com salmão.",
                new BigDecimal("25.50"), "Sushi", "https://...", "200g", 10, true, 100, null);
        Page<ProdutoLojistaDTO> pagina = new PageImpl<>(List.of(produto), PageRequest.of(0, 20), 1);

        when(lojistaService.listarProdutos(eq(usuarioLogado), eq("Sushi"), eq("salmao"), any()))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/v1/lojista/produtos")
                        .param("categoriaMenu", "Sushi")
                        .param("nome", "salmao")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("prod_0007"))
                .andExpect(jsonPath("$.content[0].ativo").value(true))
                .andExpect(jsonPath("$.content[0].estoque").value(100))
                .andExpect(jsonPath("$.content[0].lojaId").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Deve listar pedidos recebidos filtrando por status")
    void deveListarPedidosRecebidos() throws Exception {
        PedidoResumoLojistaDTO pedido = new PedidoResumoLojistaDTO("pedido_1", "Cliente 1", 2, new BigDecimal("40.00"),
                StatusPedido.PENDENTE, Instant.parse("2026-09-09T12:00:00Z"));
        Page<PedidoResumoLojistaDTO> pagina = new PageImpl<>(List.of(pedido), PageRequest.of(0, 20), 1);

        when(lojistaService.listarPedidos(eq(usuarioLogado), eq(StatusPedido.PENDENTE), any()))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/v1/lojista/pedidos")
                        .param("status", "PENDENTE")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("pedido_1"))
                .andExpect(jsonPath("$.content[0].status").value("PENDENTE"));

        verify(lojistaService).listarPedidos(eq(usuarioLogado), eq(StatusPedido.PENDENTE), any());
    }

    @Test
    @DisplayName("Deve listar todos os pedidos recebidos quando status não for informado")
    void deveListarTodosOsPedidosQuandoStatusAusente() throws Exception {
        when(lojistaService.listarPedidos(eq(usuarioLogado), isNull(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/lojista/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
