package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.domain.pedido.ItemPedido;
import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PainelFinanceiroEndpointsIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Usuario donoLoja;
    private Loja loja;
    private String authToken;

    @BeforeEach
    public void setUp() {
        // Criar usuário dono da loja
        donoLoja = new Usuario();
        donoLoja.setId(UUID.randomUUID().toString());
        donoLoja.setNome("Dono da Loja");
        donoLoja.setEmail("dono@teste.com");
        donoLoja.setSenha("$2a$10$hash");
        donoLoja.setPapel(Papel.LOJISTA);
        donoLoja.setAtivo(true);
        donoLoja.setTelefone("+5511999999999");
        entityManager.persist(donoLoja);

        // Criar loja
        loja = new Loja();
        loja.setId(UUID.randomUUID().toString());
        loja.setNome("Loja Teste");
        loja.setUsuarioId(donoLoja.getId());
        loja.setAberto(true);
        loja.setDescricao("Descrição da loja");
        loja.setCategoria("Lanches");
        loja.setDadosOperacionais(new br.com.nhac.backend_nhac.domain.loja.DadosOperacionais());
        loja.setEndereco(new br.com.nhac.backend_nhac.domain.loja.EnderecoLoja("Rua Teste", "123", "São Paulo", "SP", "01000-000", "Centro", null));
        loja.setGeoLocalizacao(new br.com.nhac.backend_nhac.domain.loja.GeoLocalizacao(-23.5505, -46.6333, "9q8z7y6x"));
        loja.setHorariosFuncionamento(new br.com.nhac.backend_nhac.domain.loja.HorariosFuncionamento());
        entityManager.persist(loja);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @WithMockUser(username = "donote ste", roles = {"LOJISTA"})
    public void deveRetornarPainelComDadosConsistentes() throws Exception {
        // Criar pedidos em diferentes status e datas
        Instant agora = Instant.now();
        Instant ontem = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant doisDiasAtras = Instant.now().minus(2, ChronoUnit.DAYS);

        // Pedido ENTREGUE hoje (entra no faturamentoHoje)
        criarPedido(StatusPedido.ENTREGUE, agora, new BigDecimal("100.00"));
        
        // Pedido ENTREGUE ontem (entra nos últimos 7 dias)
        criarPedido(StatusPedido.ENTREGUE, ontem, new BigDecimal("50.00"));
        
        // Pedido PENDENTE hoje (não entra no faturamento, mas conta na contagem)
        criarPedido(StatusPedido.PENDENTE, agora, new BigDecimal("30.00"));
        
        // Pedido CANCELADO (não entra em nada)
        criarPedido(StatusPedido.CANCELADO, agora, new BigDecimal("20.00"));

        var response = mockMvc.perform(get("/api/v1/lojista/painel")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        // Validações
        assertTrue(json.has("lojaAberta"));
        assertTrue(json.get("lojaAberta").asBoolean());

        // Faturamento hoje: apenas o pedido ENTREGUE de 100.00
        assertEquals(100.00, json.get("faturamentoHoje").asDouble(), 0.01);

        // Contagens por status
        assertEquals(1, json.get("pedidosPendentes").asLong());
        assertEquals(0, json.get("pedidosPagos").asLong());
        assertEquals(0, json.get("pedidosPreparando").asLong());
        assertEquals(0, json.get("pedidosSaiuEntrega").asLong());
        assertEquals(1, json.get("pedidosEntregues").asLong());
        assertEquals(1, json.get("pedidosCancelados").asLong());

        // Faturamento últimos 7 dias: 100 + 50 = 150
        assertEquals(150.00, json.get("faturamentoUltimos7Dias").asDouble(), 0.01);

        // Pedidos recentes deve ter pelo menos 1
        assertTrue(json.has("pedidosRecentes"));
        assertTrue(json.get("pedidosRecentes").isArray());
        assertTrue(json.get("pedidosRecentes").size() >= 1);
    }

    @Test
    @WithMockUser(username = "donote ste", roles = {"LOJISTA"})
    public void deveRetornarFinanceiroComDadosConsistentes() throws Exception {
        // Criar pedidos ENTREGUE em diferentes datas
        Instant agora = Instant.now();
        Instant ontem = Instant.now().minus(1, ChronoUnit.DAYS);

        // Pedido ENTREGUE hoje
        criarPedido(StatusPedido.ENTREGUE, agora, new BigDecimal("200.00"));
        
        // Pedido ENTREGUE ontem
        criarPedido(StatusPedido.ENTREGUE, ontem, new BigDecimal("100.00"));

        // Pedido PENDENTE (não deve entrar no financeiro)
        criarPedido(StatusPedido.PENDENTE, agora, new BigDecimal("50.00"));

        var response = mockMvc.perform(get("/api/v1/lojista/financeiro")
                .param("periodo", "7")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        // Validações do resumo
        assertTrue(json.has("resumo"));
        JsonNode resumo = json.get("resumo");
        
        // Faturamento total: 200 + 100 = 300 (apenas ENTREGUE)
        assertEquals(300.00, resumo.get("faturamentoTotal").asDouble(), 0.01);
        
        // Total de pedidos entregues: 2
        assertEquals(2, resumo.get("totalPedidos").asInt());
        
        // Ticket médio: 300 / 2 = 150
        assertEquals(150.00, resumo.get("ticketMedio").asDouble(), 0.01);

        // Faturamento por dia deve ter dados
        assertTrue(json.has("faturamentoPorDia"));
        assertTrue(json.get("faturamentoPorDia").isArray());

        // Vendas por categoria
        assertTrue(json.has("vendasPorCategoria"));

        // Vendas por forma de pagamento
        assertTrue(json.has("vendasPorFormaPagamento"));

        // Faturamento por hora
        assertTrue(json.has("faturamentoPorHora"));
    }

    @Test
    @WithMockUser(username = "donote ste", roles = {"LOJISTA"})
    public void deveRetornarDadosVaziosQuandoNaoHouverPedidos() throws Exception {
        var responsePainel = mockMvc.perform(get("/api/v1/lojista/painel")
                .with(csrf()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode jsonPainel = objectMapper.readTree(responsePainel);
        assertEquals(0.0, jsonPainel.get("faturamentoHoje").asDouble(), 0.01);
        assertEquals(0, jsonPainel.get("pedidosPendentes").asLong());
        assertEquals(0.0, jsonPainel.get("faturamentoUltimos7Dias").asDouble(), 0.01);

        var responseFinanceiro = mockMvc.perform(get("/api/v1/lojista/financeiro")
                .param("periodo", "30")
                .with(csrf()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode jsonFinanceiro = objectMapper.readTree(responseFinanceiro);
        JsonNode resumo = jsonFinanceiro.get("resumo");
        assertEquals(0.0, resumo.get("faturamentoTotal").asDouble(), 0.01);
        assertEquals(0, resumo.get("totalPedidos").asInt());
    }

    @Test
    @WithMockUser(username = "donote ste", roles = {"LOJISTA"})
    public void deveRejeitarPeriodoInvalidoNoFinanceiro() throws Exception {
        mockMvc.perform(get("/api/v1/lojista/financeiro")
                .param("periodo", "0")
                .with(csrf()))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/lojista/financeiro")
                .param("periodo", "-5")
                .with(csrf()))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/lojista/financeiro")
                .param("periodo", "400")
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    private void criarPedido(StatusPedido status, Instant dataCriacao, BigDecimal valorTotal) {
        Pedido pedido = new Pedido();
        pedido.setId(UUID.randomUUID().toString());
        pedido.setLoja(loja);
        pedido.setUsuarioId(donoLoja.getId());
        pedido.setStatus(status);
        pedido.setValorTotal(valorTotal);
        pedido.setCriadoEm(dataCriacao);
        pedido.setFormaPagamento("CREDITO");
        pedido.setTaxaFrete(BigDecimal.ZERO);
        entityManager.persist(pedido);

        // Criar um item de pedido para testar vendas por categoria
        if (status == StatusPedido.ENTREGUE) {
            Produto produto = new Produto();
            produto.setId(UUID.randomUUID().toString());
            produto.setLoja(loja);
            produto.setNome("Produto Teste");
            produto.setDescricao("Descrição");
            produto.setPreco(BigDecimal.TEN);
            produto.setCategoriaMenu("Lanches");
            produto.setAtivo(true);
            entityManager.persist(produto);

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setId(UUID.randomUUID().toString());
            itemPedido.setPedido(pedido);
            itemPedido.setProduto(produto);
            itemPedido.setNome("Produto Teste");
            itemPedido.setQuantidade(1);
            itemPedido.setPrecoHistorico(valorTotal);
            entityManager.persist(itemPedido);
        }

        entityManager.flush();
    }
}
