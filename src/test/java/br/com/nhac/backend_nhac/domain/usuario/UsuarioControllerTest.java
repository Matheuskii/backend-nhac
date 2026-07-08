package br.com.nhac.backend_nhac.domain.usuario;

import br.com.nhac.backend_nhac.domain.usuario.dto.EnderecoUsuarioDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioCreateDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioResponseDTO;
import br.com.nhac.backend_nhac.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private br.com.nhac.backend_nhac.infra.security.TokenService tokenService;

    @MockitoBean
    private br.com.nhac.backend_nhac.repositories.UsuarioRepository usuarioRepository;

    private static final String USUARIO_LOGADO_ID = "user_123";

    @BeforeEach
    void setUp() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(USUARIO_LOGADO_ID);
        usuarioMock.setEmail("teste@nhac.com");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(usuarioMock, null, Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Deve retornar 200 ao buscar os próprios dados")
    void deveBuscarProprioUsuarioComSucesso() throws Exception {
        UsuarioResponseDTO dto = new UsuarioResponseDTO(USUARIO_LOGADO_ID, "Matheus Alves",
                "matheus@nhac.com", "11999998888", null);
        when(usuarioService.buscarUsuario(USUARIO_LOGADO_ID)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/usuarios/{id}", USUARIO_LOGADO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USUARIO_LOGADO_ID));
    }

    @Test
    @DisplayName("Deve retornar 403 ao tentar buscar dados de outro usuário")
    void deveRetornar403AoBuscarDadosDeOutroUsuario() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/{id}", "outro_usuario_id"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        verify(usuarioService, never()).buscarUsuario(any());
    }

    @Test
    @DisplayName("Deve retornar 201 ao criar um novo usuário com dados válidos")
    void deveCriarUsuarioComSucesso() throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("user_novo", "Nome Novo", "novo@nhac.com",
                "11999998888", null, "senha123");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(usuarioService, times(1)).salvarUsuario(any(UsuarioCreateDTO.class));
    }

    @Test
    @DisplayName("Deve retornar 422 ao criar usuário com e-mail inválido")
    void deveRetornar422AoCriarUsuarioComEmailInvalido() throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("user_novo", "Nome Novo", "email-invalido",
                "11999998888", null, "senha123");

        //noinspection deprecation
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).salvarUsuario(any());
    }

    @Test
    @DisplayName("Deve retornar 204 ao atualizar dados parciais do próprio usuário")
    void deveAtualizarDadosDoProprioUsuario() throws Exception {
        Map<String, Object> dados = Map.of("nome", "Nome Atualizado");

        mockMvc.perform(patch("/api/v1/usuarios/{id}", USUARIO_LOGADO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dados)))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).atualizarUsuarioParcial(eq(USUARIO_LOGADO_ID), any());
    }

    @Test
    @DisplayName("Deve retornar 200 ao listar endereços do próprio usuário")
    void deveListarEnderecosDoProprioUsuario() throws Exception {
        EnderecoUsuarioDTO endereco = new EnderecoUsuarioDTO("end_1", "Rua A", "123", "Centro",
                "SP", "SP", "01000-000", null, true);
        when(usuarioService.listarEnderecos(USUARIO_LOGADO_ID)).thenReturn(List.of(endereco));

        mockMvc.perform(get("/api/v1/usuarios/{usuarioId}/enderecos", USUARIO_LOGADO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("end_1"));
    }

    @Test
    @DisplayName("Deve retornar 201 ao adicionar um novo endereço válido")
    void deveAdicionarEnderecoComSucesso() throws Exception {
        EnderecoUsuarioDTO dto = new EnderecoUsuarioDTO(null, "Rua A", "123", "Centro",
                "SP", "SP", "01000-000", null, true);

        mockMvc.perform(post("/api/v1/usuarios/{usuarioId}/enderecos", USUARIO_LOGADO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(usuarioService, times(1)).adicionarEndereco(eq(USUARIO_LOGADO_ID), any());
    }

    @Test
    @DisplayName("Deve retornar 400 ao adicionar endereço com CEP em formato inválido")
    void deveRetornar422AoAdicionarEnderecoComCepInvalido() throws Exception {
        EnderecoUsuarioDTO dto = new EnderecoUsuarioDTO(null, "Rua A", "123", "Centro",
                "SP", "SP", "cep-invalido", null, true);

        //noinspection deprecation
        mockMvc.perform(post("/api/v1/usuarios/{usuarioId}/enderecos", USUARIO_LOGADO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).adicionarEndereco(any(), any());
    }

    @Test
    @DisplayName("Deve retornar 204 ao atualizar um endereço do próprio usuário")
    void deveAtualizarEnderecoComSucesso() throws Exception {
        EnderecoUsuarioDTO dto = new EnderecoUsuarioDTO("end_1", "Rua Nova", "456", "Centro",
                "SP", "SP", "01000-000", null, true);

        mockMvc.perform(put("/api/v1/usuarios/{usuarioId}/enderecos/{enderecoId}", USUARIO_LOGADO_ID, "end_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).atualizarEndereco(eq(USUARIO_LOGADO_ID), eq("end_1"), any());
    }

    @Test
    @DisplayName("Deve retornar 204 ao remover um endereço do próprio usuário")
    void deveRemoverEnderecoComSucesso() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/{usuarioId}/enderecos/{enderecoId}", USUARIO_LOGADO_ID, "end_1"))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).removerEndereco(USUARIO_LOGADO_ID, "end_1");
    }

    @Test
    @DisplayName("Deve retornar 403 ao tentar remover endereço de outro usuário")
    void deveRetornar403AoRemoverEnderecoDeOutroUsuario() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/{usuarioId}/enderecos/{enderecoId}", "outro_usuario_id", "end_1"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).removerEndereco(any(), any());
    }
}
