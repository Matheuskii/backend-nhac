package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.services.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

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
    @DisplayName("Deve devolver Erro 400 quando o carrinho de itens estiver vazio")
    void deveDevolverErro400QuandoCarrinhoVazio() throws Exception {

        String jsonEstragado = """
                {
                  "usuarioId": "firebase_uid_123",
                  "lojaId": "loja-001",
                  "valorTotal": 31.00,
                  "taxaFrete": 5.50,
                  "formaPagamento": "PIX",
                  "enderecoEntrega": {
                    "rua": "Rua A", "numero": "123", "bairro": "Centro",
                    "cidade": "SP", "estado": "SP", "cep": "01000-000"
                  },
                  "itens": []
                }
                """;

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEstragado))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve devolver Erro 400 quando o CEP estiver fora do padrão")
    void deveDevolverErro400QuandoCepInvalido() throws Exception {

        String jsonEstragado = """
                {
                  "usuarioId": "firebase_uid_123",
                  "lojaId": "loja-001",
                  "valorTotal": 31.00,
                  "taxaFrete": 5.50,
                  "formaPagamento": "PIX",
                  "enderecoEntrega": {
                    "rua": "Rua A", "numero": "123", "bairro": "Centro",
                    "cidade": "SP", "estado": "SP", 
                    "cep": "123" 
                  },
                  "itens": [
                    { "produtoId": "p1", "nome": "Sushi", "precoHistorico": 20.00, "quantidade": 1 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEstragado))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve devolver Erro 400 quando a quantidade de um item for zero ou negativa")
    void deveDevolverErro400QuandoQuantidadeNegativa() throws Exception {

        String jsonEstragado = """
                {
                  "usuarioId": "firebase_uid_123",
                  "lojaId": "loja-001",
                  "valorTotal": 31.00,
                  "taxaFrete": 5.50,
                  "formaPagamento": "PIX",
                  "enderecoEntrega": {
                    "rua": "Rua A", "numero": "123", "bairro": "Centro",
                    "cidade": "SP", "estado": "SP", "cep": "01000-000"
                  },
                  "itens": [
                    { "produtoId": "p1", "nome": "Sushi", "precoHistorico": 20.00, 
                      "quantidade": 0 
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEstragado))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve devolver 201 e o ID do pedido quando os dados forem válidos")
    void deveCriarPedidoComSucesso() throws Exception {
        String jsonValido = """
                {
                  "lojaId": "loja-001",
                  "formaPagamento": "PIX",
                  "enderecoEntrega": {
                    "rua": "Rua A", "numero": "123", "bairro": "Centro",
                    "cidade": "SP", "estado": "SP", "cep": "01000-000"
                  },
                  "itens": [
                    { "produtoId": "p1", "nome": "Sushi", "quantidade": 1 }
                  ]
                }
                """;

        when(pedidoService.finalizarPedido(any(), anyString())).thenReturn("pedido_gerado_001");

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string("pedido_gerado_001"));
    }
}