package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.usuario.EnderecoUsuario;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.dto.EnderecoUsuarioDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioCreateDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioResponseDTO;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.EnderecoUsuarioRepository;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EnderecoUsuarioRepository enderecoRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UsuarioService usuarioService;

    private Usuario usuarioPadrao(String id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Matheus Alves");
        usuario.setEmail("matheus@nhac.com");
        usuario.setTelefone("11999998888");
        return usuario;
    }

    // ---------- buscarUsuario ----------

    @Test
    @DisplayName("Deve retornar os dados públicos do usuário quando encontrado")
    void deveBuscarUsuarioComSucesso() {
        Usuario usuario = usuarioPadrao("user_1");
        when(usuarioRepository.findById("user_1")).thenReturn(Optional.of(usuario));

        UsuarioResponseDTO resultado = usuarioService.buscarUsuario("user_1");

        assertEquals("user_1", resultado.id());
        assertEquals("Matheus Alves", resultado.nome());
    }

    @Test
    @DisplayName("Deve lançar IdNaoEncontradoException ao buscar usuário inexistente")
    void deveLancarExcecaoQuandoBuscarUsuarioInexistente() {
        when(usuarioRepository.findById("fantasma")).thenReturn(Optional.empty());

        assertThrows(IdNaoEncontradoException.class, () -> usuarioService.buscarUsuario("fantasma"));
    }

    // ---------- salvarUsuario ----------

    @Test
    @DisplayName("Deve encriptar a senha e salvar o usuário quando a senha for informada")
    void deveSalvarUsuarioComSenhaEncriptada() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("user_1", "Matheus Alves", "matheus@nhac.com",
                "11999998888", null, "senhaSegura123");

        when(passwordEncoder.encode("senhaSegura123")).thenReturn("hash_gerado");

        usuarioService.salvarUsuario(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("hash_gerado", captor.getValue().getSenha());
    }

    @Test
    @DisplayName("Não deve encriptar senha quando ela for nula ou em branco")
    void deveSalvarUsuarioSemEncriptarQuandoSenhaAusente() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("user_1", "Matheus Alves", "matheus@nhac.com",
                "11999998888", null, "  ");

        usuarioService.salvarUsuario(dto);

        verify(passwordEncoder, never()).encode(any());
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("  ", captor.getValue().getSenha());
    }

    // ---------- atualizarUsuarioParcial ----------

    @Test
    @DisplayName("Deve atualizar apenas os campos informados no mapa de dados")
    void deveAtualizarUsuarioParcialmente() {
        Usuario usuario = usuarioPadrao("user_1");
        when(usuarioRepository.findById("user_1")).thenReturn(Optional.of(usuario));

        Map<String, Object> dados = Map.of("nome", "Novo Nome", "telefone", "11888887777");

        usuarioService.atualizarUsuarioParcial("user_1", dados);

        assertEquals("Novo Nome", usuario.getNome());
        assertEquals("11888887777", usuario.getTelefone());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar IdNaoEncontradoException ao atualizar usuário inexistente")
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
        when(usuarioRepository.findById("fantasma")).thenReturn(Optional.empty());

        assertThrows(IdNaoEncontradoException.class,
                () -> usuarioService.atualizarUsuarioParcial("fantasma", Map.of("nome", "X")));
    }

    // ---------- listarEnderecos ----------

    @Test
    @DisplayName("Deve listar os endereços de um usuário")
    void deveListarEnderecosDoUsuario() {
        EnderecoUsuario endereco = new EnderecoUsuario("end_1", usuarioPadrao("user_1"),
                "Rua A", "123", "Centro", "SP", "SP", "01000-000", null, true);

        when(enderecoRepository.findByUsuarioId("user_1")).thenReturn(List.of(endereco));

        List<EnderecoUsuarioDTO> resultado = usuarioService.listarEnderecos("user_1");

        assertEquals(1, resultado.size());
        assertEquals("end_1", resultado.get(0).id());
    }

    // ---------- adicionarEndereco ----------

    @Test
    @DisplayName("Deve adicionar um novo endereço ao usuário existente")
    void deveAdicionarEnderecoComSucesso() {
        Usuario usuario = usuarioPadrao("user_1");
        when(usuarioRepository.findById("user_1")).thenReturn(Optional.of(usuario));

        EnderecoUsuarioDTO dto = new EnderecoUsuarioDTO(null, "Rua A", "123", "Centro",
                "SP", "SP", "01000-000", null, true);

        usuarioService.adicionarEndereco("user_1", dto);

        verify(enderecoRepository, times(1)).save(any(EnderecoUsuario.class));
    }

    @Test
    @DisplayName("Deve lançar IdNaoEncontradoException ao adicionar endereço para usuário inexistente")
    void deveLancarExcecaoAoAdicionarEnderecoParaUsuarioInexistente() {
        when(usuarioRepository.findById("fantasma")).thenReturn(Optional.empty());

        EnderecoUsuarioDTO dto = new EnderecoUsuarioDTO(null, "Rua A", "123", "Centro",
                "SP", "SP", "01000-000", null, true);

        assertThrows(IdNaoEncontradoException.class,
                () -> usuarioService.adicionarEndereco("fantasma", dto));

        verify(enderecoRepository, never()).save(any());
    }

    // ---------- atualizarEndereco ----------

    @Test
    @DisplayName("Deve atualizar um endereço pertencente ao usuário")
    void deveAtualizarEnderecoComSucesso() {
        Usuario usuario = usuarioPadrao("user_1");
        EnderecoUsuario endereco = new EnderecoUsuario("end_1", usuario,
                "Rua Antiga", "1", "Bairro", "SP", "SP", "01000-000", null, false);

        when(enderecoRepository.findById("end_1")).thenReturn(Optional.of(endereco));

        EnderecoUsuarioDTO dto = new EnderecoUsuarioDTO("end_1", "Rua Nova", "2", "Bairro Novo",
                "Campinas", "SP", "13000-000", "Apto 1", true);

        usuarioService.atualizarEndereco("user_1", "end_1", dto);

        assertEquals("Rua Nova", endereco.getRua());
        assertEquals("Campinas", endereco.getCidade());
        assertTrue(endereco.isPadrao());
        verify(enderecoRepository).save(endereco);
    }

    @Test
    @DisplayName("Deve lançar IdNaoEncontradoException ao atualizar endereço inexistente")
    void deveLancarExcecaoAoAtualizarEnderecoInexistente() {
        when(enderecoRepository.findById("fantasma")).thenReturn(Optional.empty());

        EnderecoUsuarioDTO dto = new EnderecoUsuarioDTO("fantasma", "Rua Nova", "2", "Bairro Novo",
                "Campinas", "SP", "13000-000", null, true);

        assertThrows(IdNaoEncontradoException.class,
                () -> usuarioService.atualizarEndereco("user_1", "fantasma", dto));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao atualizar endereço de outro usuário")
    void deveLancarExcecaoAoAtualizarEnderecoDeOutroUsuario() {
        Usuario donoReal = usuarioPadrao("dono_real");
        EnderecoUsuario endereco = new EnderecoUsuario("end_1", donoReal,
                "Rua Antiga", "1", "Bairro", "SP", "SP", "01000-000", null, false);

        when(enderecoRepository.findById("end_1")).thenReturn(Optional.of(endereco));

        EnderecoUsuarioDTO dto = new EnderecoUsuarioDTO("end_1", "Rua Nova", "2", "Bairro Novo",
                "Campinas", "SP", "13000-000", null, true);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.atualizarEndereco("invasor_id", "end_1", dto));

        verify(enderecoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desmarcar outros endereços como padrão ao adicionar um novo endereço padrão")
    void deveDesmarcarOutrosEnderecosAoAdicionarNovoPadrao() {
        Usuario usuario = usuarioPadrao("user_1");
        when(usuarioRepository.findById("user_1")).thenReturn(Optional.of(usuario));

        EnderecoUsuario enderecoAntigoPadrao = new EnderecoUsuario("end_antigo", usuario,
                "Rua Antiga", "1", "Bairro", "SP", "SP", "01000-000", null, true);
        when(enderecoRepository.findByUsuarioId("user_1")).thenReturn(List.of(enderecoAntigoPadrao));

        EnderecoUsuarioDTO dto = new EnderecoUsuarioDTO(null, "Rua Nova", "2", "Centro",
                "SP", "SP", "02000-000", null, true);

        usuarioService.adicionarEndereco("user_1", dto);

        assertFalse(enderecoAntigoPadrao.isPadrao());
        verify(enderecoRepository).save(enderecoAntigoPadrao);
        verify(enderecoRepository).save(argThat(EnderecoUsuario::isPadrao));
    }

    @Test
    @DisplayName("Deve desmarcar outros endereços como padrão ao definir um endereço existente como padrão")
    void deveDesmarcarOutrosEnderecosAoAtualizarParaPadrao() {
        Usuario usuario = usuarioPadrao("user_1");
        EnderecoUsuario enderecoAtual = new EnderecoUsuario("end_1", usuario,
                "Rua Antiga", "1", "Bairro", "SP", "SP", "01000-000", null, false);
        EnderecoUsuario outroEnderecoPadrao = new EnderecoUsuario("end_2", usuario,
                "Rua Outra", "2", "Bairro", "SP", "SP", "03000-000", null, true);

        when(enderecoRepository.findById("end_1")).thenReturn(Optional.of(enderecoAtual));
        when(enderecoRepository.findByUsuarioId("user_1"))
                .thenReturn(List.of(enderecoAtual, outroEnderecoPadrao));

        EnderecoUsuarioDTO dto = new EnderecoUsuarioDTO("end_1", "Rua Nova", "2", "Bairro Novo",
                "Campinas", "SP", "13000-000", null, true);

        usuarioService.atualizarEndereco("user_1", "end_1", dto);

        assertTrue(enderecoAtual.isPadrao());
        assertFalse(outroEnderecoPadrao.isPadrao());
        verify(enderecoRepository).save(outroEnderecoPadrao);
    }

    // ---------- removerEndereco ----------

    @Test
    @DisplayName("Deve remover um endereço pertencente ao usuário")
    void deveRemoverEnderecoComSucesso() {
        Usuario usuario = usuarioPadrao("user_1");
        EnderecoUsuario endereco = new EnderecoUsuario("end_1", usuario,
                "Rua A", "1", "Bairro", "SP", "SP", "01000-000", null, false);

        when(enderecoRepository.findById("end_1")).thenReturn(Optional.of(endereco));

        usuarioService.removerEndereco("user_1", "end_1");

        verify(enderecoRepository, times(1)).delete(endereco);
    }

    @Test
    @DisplayName("Deve lançar IdNaoEncontradoException ao remover endereço inexistente")
    void deveLancarExcecaoAoRemoverEnderecoInexistente() {
        when(enderecoRepository.findById("fantasma")).thenReturn(Optional.empty());

        assertThrows(IdNaoEncontradoException.class,
                () -> usuarioService.removerEndereco("user_1", "fantasma"));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao remover endereço de outro usuário")
    void deveLancarExcecaoAoRemoverEnderecoDeOutroUsuario() {
        Usuario donoReal = usuarioPadrao("dono_real");
        EnderecoUsuario endereco = new EnderecoUsuario("end_1", donoReal,
                "Rua A", "1", "Bairro", "SP", "SP", "01000-000", null, false);

        when(enderecoRepository.findById("end_1")).thenReturn(Optional.of(endereco));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.removerEndereco("invasor_id", "end_1"));

        verify(enderecoRepository, never()).delete(any(EnderecoUsuario.class));
    }
}
