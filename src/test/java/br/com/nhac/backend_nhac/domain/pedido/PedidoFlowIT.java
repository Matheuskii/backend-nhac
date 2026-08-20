package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.AbstractIntegrationTest;
import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.infra.security.TokenService;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import br.com.nhac.backend_nhac.services.StripePaymentService;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCriadoDTO;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PedidoFlowIT extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LojaRepository lojaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private TokenService tokenService;

    private Loja loja;
    private Produto produto;
    private String token;

    @MockitoBean
    private StripePaymentService stripePaymentService;

    @BeforeEach
    public void prepareData() {
        produtoRepository.deleteAll();
        lojaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID().toString());
        usuario.setNome("Comprador Teste");
        usuario.setEmail("comprador@teste.com");
        usuario.setSenha("senha123");
        usuario.setTelefone("11999999999");
        usuarioRepository.save(usuario);

        token = tokenService.gerarToken(usuario);

        loja = new Loja();
        loja.setId("loja-123");
        loja.setNome("Pizzaria Nhac");
        loja.setAberto(true);
        lojaRepository.save(loja);

        // 4. Criar Produto
        produto = new Produto();
        produto.setId(UUID.randomUUID().toString());
        produto.setNome("Pizza de Calabresa");
        produto.setPreco(new BigDecimal("45.00"));
        produto.setAtivo(true);
        produto.setCategoriaMenu("Pizzas");
        produto.setLoja(loja);
        produtoRepository.save(produto);

        // Mock StripePaymentService - agora usa método para cartão/Google Pay
        Mockito.when(stripePaymentService.criarPaymentIntentCartao(Mockito.any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedidoSalvo = invocation.getArgument(0);
            return new PedidoCriadoDTO(pedidoSalvo.getId(), "mock-secret", null, null);
        });
    }

    @Test
    public void deveCriarEFinalizarPedidoComSucesso() throws Exception {
        PedidoCreateDTO.EnderecoEntregaDTO endereco = new PedidoCreateDTO.EnderecoEntregaDTO(
                "Rua Teste", "123", "Bairro", "Cidade", "SP", "12345-678", "Apto 1"
        );

        PedidoCreateDTO.ItemPedidoDTO item = new PedidoCreateDTO.ItemPedidoDTO(
                produto.getId(), produto.getNome(), null, 2
        );

        PedidoCreateDTO pedidoDto = new PedidoCreateDTO(
                loja.getId(),
                "CARTAO",
                "Sem cebola",
                null,
                null,
                endereco,
                List.of(item)
        );

        mockMvc.perform(post("/api/v1/pedidos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedidoDto)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pedidoId").exists());
    }
}
