package br.com.nhac.backend_nhac.domain.produto;


import br.com.nhac.backend_nhac.services.ProdutoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private ProdutoService produtoService;

    @Test
    @DisplayName("Deve devolver Erro 400 (Bad Request) quando o preço for negativo")
    void deveDevolverErro400QuandoPrecoNegativo() throws Exception {

        String jsonEstragado = """
                {
                    "lojaId": "loja_123",
                    "nome": "Hossomaki",
                    "descricao": "Sushi de salmão",
                    "preco": -5.00,
                    "categoriaMenu": "Sushi",
                    "imagemUrl": "http://imagem.com",
                    "isAtivo": true,
                    "peso": "200g",
                    "percentualDesconto": 0
                }
                """;

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEstragado))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve devolver Erro 400 (Bad Request) quando o nome estiver vazio")
    void deveDevolverErro400QuandoNomeVazio() throws Exception {

        String jsonEstragado = """
                {
                    "lojaId": "loja_123",
                    "nome": "",
                    "descricao": "Sushi de salmão",
                    "preco": 25.50,
                    "categoriaMenu": "Sushi",
                    "imagemUrl": "http://imagem.com",
                    "isAtivo": true,
                    "peso": "200g",
                    "percentualDesconto": 0
                }
                """;

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEstragado))
                .andExpect(status().isBadRequest());
    }
}