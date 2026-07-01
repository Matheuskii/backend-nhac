package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.services.PedidoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

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
}