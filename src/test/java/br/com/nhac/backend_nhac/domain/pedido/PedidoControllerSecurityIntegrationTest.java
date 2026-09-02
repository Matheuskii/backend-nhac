package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.AbstractIntegrationTest;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PedidoControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Deve retornar 403 Forbidden quando usuário CLIENTE tentar atualizar status do pedido")
    @WithMockUser(username = "cliente@nhac.com", roles = {"CLIENTE"})
    void deveRetornar403ParaCliente() throws Exception {
        String jsonBody = "{\"status\": \"PREPARANDO\"}";

        mockMvc.perform(patch("/api/v1/pedidos/pedido_123/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar erro 404 (Not Found) quando usuário LOJISTA tentar atualizar um pedido inexistente (O 403 não deve ocorrer)")
    @WithMockUser(username = "lojista@nhac.com", roles = {"LOJISTA"})
    void devePermitirAcessoParaLojista() throws Exception {
        String jsonBody = "{\"status\": \"PREPARANDO\"}";

        mockMvc.perform(patch("/api/v1/pedidos/pedido_123/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                // Retorna 404 porque o mock user acessou o controller, mas o pedido não existe no banco H2 de testes.
                // O importante aqui é garantir que não retornou 403.
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Deve retornar erro 404 (Not Found) quando usuário ADMIN tentar atualizar um pedido inexistente")
    @WithMockUser(username = "admin@nhac.com", roles = {"ADMIN"})
    void devePermitirAcessoParaAdmin() throws Exception {
        String jsonBody = "{\"status\": \"PREPARANDO\"}";

        mockMvc.perform(patch("/api/v1/pedidos/pedido_123/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound());
    }
}
