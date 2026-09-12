package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoDetalheLojistaDTO;
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
import org.springframework.cache.CacheManager;

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
    private br.com.nhac.backend_nhac.domain.lojista.service.LojaAccessService lojaAccessService;

    @MockitoBean
    private br.com.nhac.backend_nhac.infra.security.TokenService tokenService;

    @MockitoBean
    private br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository usuarioRepository;

    @MockitoBean
    private br.com.nhac.backend_nhac.domain.lojista.service.PainelFinanceiroService painelFinanceiroService;

    @MockitoBean
    private CacheManager cacheManager;

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

        when(lojistaService.listarProdutos(any(Usuario.class), eq("Sushi"), eq("salmao"), any()))
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
    @DisplayName("Deve listar pedidos recebidos com clienteNome e quantidadeItens (PedidoResumoLojistaDTO)")
    void deveListarPedidosRecebidosComClienteNome() throws Exception {
        PedidoResumoLojistaDTO pedido = new PedidoResumoLojistaDTO(
                "pedido_1", "João Silva", 3,
                new BigDecimal("40.00"), StatusPedido.PENDENTE, Instant.parse("2026-09-09T12:00:00Z"));
        Page<PedidoResumoLojistaDTO> pagina = new PageImpl<>(List.of(pedido), PageRequest.of(0, 20), 1);

        when(lojistaService.listarPedidos(eq("user_123"), eq(StatusPedido.PENDENTE), any()))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/v1/lojista/pedidos")
                        .param("status", "PENDENTE")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("pedido_1"))
                .andExpect(jsonPath("$.content[0].clienteNome").value("João Silva"))
                .andExpect(jsonPath("$.content[0].quantidadeItens").value(3))
                .andExpect(jsonPath("$.content[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$.content[0].lojaId").doesNotExist())
                .andExpect(jsonPath("$.content[0].lojaNome").doesNotExist());

        verify(lojistaService).listarPedidos(eq("user_123"), eq(StatusPedido.PENDENTE), any());
    }

    @Test
    @DisplayName("Deve listar todos os pedidos recebidos quando status não for informado")
    void deveListarTodosOsPedidosQuandoStatusAusente() throws Exception {
        when(lojistaService.listarPedidos(eq("user_123"), isNull(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/lojista/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("Deve retornar detalhe do pedido com dados do cliente para lojista autorizado")
    void deveRetornarDetalheDoPedidoComDadosDoCliente() throws Exception {
        PedidoDetalheLojistaDTO pedidoDetalhe = new PedidoDetalheLojistaDTO(
                "pedido_1", "João Silva", "(11) 98765-4321",
                new BigDecimal("40.00"), new BigDecimal("5.00"), "PIX", null, "Entregar na portaria",
                StatusPedido.PENDENTE, Instant.parse("2026-09-09T12:00:00Z"),
                new PedidoDetalheLojistaDTO.EnderecoEntregaResponseDTO("Rua A", "100", "Centro", "São Paulo", "SP", "01000-000", "Apto 10"),
                List.of(new PedidoDetalheLojistaDTO.ItemPedidoResponseDTO("item_1", "prod_1", "Hossomaki", "https://...", new BigDecimal("20.00"), 2)));

        when(lojistaService.buscarPedidoDetalhe(eq("pedido_1"), any(Usuario.class)))
                .thenReturn(pedidoDetalhe);

        mockMvc.perform(get("/api/v1/lojista/pedidos/pedido_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pedido_1"))
                .andExpect(jsonPath("$.clienteNome").value("João Silva"))
                .andExpect(jsonPath("$.clienteTelefone").value("(11) 98765-4321"))
                .andExpect(jsonPath("$.observacao").value("Entregar na portaria"))
                .andExpect(jsonPath("$.enderecoEntrega.rua").value("Rua A"));
    }
}
