package br.com.nhac.backend_nhac.domain.loja;

import br.com.nhac.backend_nhac.AbstractIntegrationTest;
import br.com.nhac.backend_nhac.domain.auth.dto.LoginRequestDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.RegistroRequestDTO;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LojaCadastroIT extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LojaRepository lojaRepository;

    @BeforeEach
    void limparDados() {
        lojaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void deveRecusarCriacaoDeLojaSemToken() throws Exception {
        mockMvc.perform(post("/api/v1/lojas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCriacaoLoja("usuario-forjado")))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403);
                });
    }

    @Test
    void deveCriarLojaAutenticadoPromoverPapelEIgnorarUsuarioIdDoCorpo() throws Exception {
        String email = "lojista.fase2@nhac.com.br";
        String senha = "senhaForte123";

        RegistroRequestDTO registroReq = new RegistroRequestDTO(
                UUID.randomUUID().toString(),
                "Lojista Teste",
                email,
                "11988887777",
                senha
        );

        mockMvc.perform(post("/api/v1/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.papel").value("CLIENTE"));

        String token = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO(email, senha))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("CLIENTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String jwt = objectMapper.readTree(token).get("token").asText();
        String usuarioId = objectMapper.readTree(token).get("usuarioId").asText();

        mockMvc.perform(post("/api/v1/lojas")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCriacaoLoja("usuario-forjado")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Mercado Central"));

        List<Loja> lojas = lojaRepository.findAll();
        assertEquals(1, lojas.size());
        assertEquals(usuarioId, lojas.get(0).getUsuarioId());

        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        assertEquals(Papel.LOJISTA, usuario.getPapel());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO(email, senha))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("LOJISTA"));
    }

    private String jsonCriacaoLoja(String usuarioIdForjado) {
        return """
                {
                  "nome": "Mercado Central",
                  "descricao": "Orgânicos",
                  "categoria": "Restaurantes",
                  "imagemUrl": "https://example.com/banner.jpg",
                  "isAberto": true,
                  "usuarioId": "%s",
                  "dadosOperacionais": {
                    "taxaEntregaBase": 5.99,
                    "tempoEntregaMin": 30,
                    "tempoEntregaMax": 45
                  },
                  "endereco": {
                    "rua": "Avenida Paulista",
                    "numero": "1578",
                    "cidade": "São Paulo",
                    "estado": "SP",
                    "cep": "01310-200"
                  },
                  "horarios": {
                    "domingo": "18:00-23:00",
                    "segunda": "Fechado",
                    "terca": "11:00-23:00",
                    "quarta": "11:00-23:00",
                    "quinta": "11:00-23:00",
                    "sexta": "11:00-23:59",
                    "sabado": "11:00-23:59"
                  }
                }
                """.formatted(usuarioIdForjado);
    }
}
