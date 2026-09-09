package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class LojistaEndpointsIT extends AbstractIntegrationTest {

    @Test
    void deveRecusarListagemDeProdutosSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/lojista/produtos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403);
                });
    }

    @Test
    void deveRecusarListagemDePedidosSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/lojista/pedidos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403);
                });
    }
}
