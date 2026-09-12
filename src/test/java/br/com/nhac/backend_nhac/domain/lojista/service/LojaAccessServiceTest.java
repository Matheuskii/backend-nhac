package br.com.nhac.backend_nhac.domain.lojista.service;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.loja.LojaRepository;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LojaAccessService Tests")
class LojaAccessServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LojaRepository lojaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LojaAccessService lojaAccessService;

    private Usuario donoUsuario;
    private Usuario funcionarioUsuario;
    private Usuario adminUsuario;
    private Loja loja;

    @BeforeEach
    void setUp() {
        // Criando loja
        loja = new Loja();
        loja.setId("loja-123");
        loja.setNome("Loja Teste");
        loja.setUsuarioId("dono-123");

        // Criando usuário DONO
        donoUsuario = new Usuario();
        donoUsuario.setId("dono-123");
        donoUsuario.setNome("Dono da Loja");
        donoUsuario.setEmail("dono@teste.com");
        donoUsuario.setPapel(Papel.LOJISTA);

        // Criando usuário FUNCIONARIO
        funcionarioUsuario = new Usuario();
        funcionarioUsuario.setId("func-456");
        funcionarioUsuario.setNome("Funcionário Teste");
        funcionarioUsuario.setEmail("func@teste.com");
        funcionarioUsuario.setPapel(Papel.FUNCIONARIO);
        funcionarioUsuario.setLojaVinculada(loja);
        funcionarioUsuario.setCargo("Vendedor");
        funcionarioUsuario.setAtivo(true);

        // Criando usuário ADMIN
        adminUsuario = new Usuario();
        adminUsuario.setId("admin-789");
        adminUsuario.setNome("Admin");
        adminUsuario.setEmail("admin@teste.com");
        adminUsuario.setPapel(Papel.ADMIN);
    }

    @Test
    @DisplayName("Deve obter loja quando usuário é LOJISTA")
    void testObterLojaAcessivel_Lojista() {
        when(lojaRepository.findByUsuarioId(donoUsuario.getId())).thenReturn(Optional.of(loja));

        Loja resultado = lojaAccessService.obterLojaAcessivel(donoUsuario);

        assertNotNull(resultado);
        assertEquals("loja-123", resultado.getId());
        verify(lojaRepository).findByUsuarioId(donoUsuario.getId());
    }

    @Test
    @DisplayName("Deve obter loja quando usuário é FUNCIONARIO")
    void testObterLojaAcessivel_Funcionario() {
        Loja resultado = lojaAccessService.obterLojaAcessivel(funcionarioUsuario);

        assertNotNull(resultado);
        assertEquals("loja-123", resultado.getId());
        verify(lojaRepository, never()).findByUsuarioId(anyString());
    }

    @Test
    @DisplayName("Deve retornar null quando usuário é ADMIN")
    void testObterLojaAcessivel_Admin() {
        Loja resultado = lojaAccessService.obterLojaAcessivel(adminUsuario);

        assertNull(resultado);
        verify(lojaRepository, never()).findByUsuarioId(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando LOJISTA não tem loja associada")
    void testObterLojaAcessivel_LojistaSemLoja() {
        when(lojaRepository.findByUsuarioId(donoUsuario.getId())).thenReturn(Optional.empty());

        assertThrows(AcessoNegadoException.class, () -> 
            lojaAccessService.obterLojaAcessivel(donoUsuario)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando FUNCIONARIO não tem loja vinculada")
    void testObterLojaAcessivel_FuncionarioSemLoja() {
        Usuario funcSemLoja = new Usuario();
        funcSemLoja.setId("func-sem-loja");
        funcSemLoja.setPapel(Papel.FUNCIONARIO);
        funcSemLoja.setLojaVinculada(null);

        assertThrows(AcessoNegadoException.class, () -> 
            lojaAccessService.obterLojaAcessivel(funcSemLoja)
        );
    }

    @Test
    @DisplayName("Deve validar permissão quando usuário é DONO da loja")
    void testValidarPermissaoGerenciarFuncionarios_Dono() {
        when(lojaRepository.findById(loja.getId())).thenReturn(Optional.of(loja));

        assertDoesNotThrow(() -> 
            lojaAccessService.validarPermissaoGerenciarFuncionarios(donoUsuario, loja.getId())
        );
    }

    @Test
    @DisplayName("Deve validar permissão quando usuário é ADMIN")
    void testValidarPermissaoGerenciarFuncionarios_Admin() {
        assertDoesNotThrow(() -> 
            lojaAccessService.validarPermissaoGerenciarFuncionarios(adminUsuario, loja.getId())
        );
        verify(lojaRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é dono da loja")
    void testValidarPermissaoGerenciarFuncionarios_NaoDono() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId("outro-usuario");
        outroUsuario.setPapel(Papel.LOJISTA);

        when(lojaRepository.findById(loja.getId())).thenReturn(Optional.of(loja));

        assertThrows(AcessoNegadoException.class, () -> 
            lojaAccessService.validarPermissaoGerenciarFuncionarios(outroUsuario, loja.getId())
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando loja não existe")
    void testValidarPermissaoGerenciarFuncionarios_LojaNaoExiste() {
        when(lojaRepository.findById("loja-inexistente")).thenReturn(Optional.empty());

        assertThrows(IdNaoEncontradoException.class, () -> 
            lojaAccessService.validarPermissaoGerenciarFuncionarios(donoUsuario, "loja-inexistente")
        );
    }

    @Test
    @DisplayName("Deve criar funcionário com sucesso")
    void testCriarFuncionario_Sucesso() {
        String nome = "Novo Funcionário";
        String email = "novo@teste.com";
        String senha = "senha123";
        String telefone = "11999999999";
        String cargo = "Gerente";

        when(lojaRepository.findById(loja.getId())).thenReturn(Optional.of(loja));
        when(usuarioRepository.countByLojaVinculadaIdAndAtivoTrue(loja.getId())).thenReturn(0L);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(senha)).thenReturn("senhaHash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = lojaAccessService.criarFuncionario(loja.getId(), nome, email, senha, telefone, cargo, donoUsuario);

        assertNotNull(resultado);
        assertEquals(nome, resultado.getNome());
        assertEquals(email, resultado.getEmail());
        assertEquals(Papel.FUNCIONARIO, resultado.getPapel());
        assertEquals(loja, resultado.getLojaVinculada());
        assertEquals(cargo, resultado.getCargo());
        assertTrue(resultado.isAtivo());
        verify(passwordEncoder).encode(senha);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao ultrapassar limite de 15 funcionários")
    void testCriarFuncionario_LimiteUltrapassado() {
        when(lojaRepository.findById(loja.getId())).thenReturn(Optional.of(loja));
        when(usuarioRepository.countByLojaVinculadaIdAndAtivoTrue(loja.getId())).thenReturn(15L);

        assertThrows(RegraDeNegocioException.class, () -> 
            lojaAccessService.criarFuncionario(loja.getId(), "Nome", "email@teste.com", "senha", "tel", "cargo", donoUsuario)
        );
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar funcionário com email duplicado")
    void testCriarFuncionario_EmailDuplicado() {
        String emailExistente = "existente@teste.com";

        when(lojaRepository.findById(loja.getId())).thenReturn(Optional.of(loja));
        when(usuarioRepository.countByLojaVinculadaIdAndAtivoTrue(loja.getId())).thenReturn(0L);
        when(usuarioRepository.findByEmail(emailExistente)).thenReturn(Optional.of(new Usuario()));

        assertThrows(RegraDeNegocioException.class, () -> 
            lojaAccessService.criarFuncionario(loja.getId(), "Nome", emailExistente, "senha", "tel", "cargo", donoUsuario)
        );
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve atualizar funcionário com sucesso")
    void testAtualizarFuncionario_Sucesso() {
        when(usuarioRepository.findById(funcionarioUsuario.getId())).thenReturn(Optional.of(funcionarioUsuario));
        when(lojaRepository.findById(loja.getId())).thenReturn(Optional.of(loja));
        when(usuarioRepository.save(funcionarioUsuario)).thenReturn(funcionarioUsuario);

        String novoNome = "Nome Atualizado";
        String novoTelefone = "11888888888";
        String novoCargo = "Supervisor";

        Usuario resultado = lojaAccessService.atualizarFuncionario(
            funcionarioUsuario.getId(), novoNome, novoTelefone, novoCargo, donoUsuario
        );

        assertNotNull(resultado);
        assertEquals(novoNome, resultado.getNome());
        assertEquals(novoTelefone, resultado.getTelefone());
        assertEquals(novoCargo, resultado.getCargo());
        verify(usuarioRepository).save(funcionarioUsuario);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar usuário que não é funcionário")
    void testAtualizarFuncionario_NaoEFuncionario() {
        Usuario naoFuncionario = new Usuario();
        naoFuncionario.setId("nao-func");
        naoFuncionario.setPapel(Papel.LOJISTA);

        when(usuarioRepository.findById(naoFuncionario.getId())).thenReturn(Optional.of(naoFuncionario));

        assertThrows(RegraDeNegocioException.class, () -> 
            lojaAccessService.atualizarFuncionario(naoFuncionario.getId(), "Nome", "Tel", "Cargo", donoUsuario)
        );
    }

    @Test
    @DisplayName("Deve ativar/desativar funcionário com sucesso")
    void testAtivarOuDesativarFuncionario_Sucesso() {
        when(usuarioRepository.findById(funcionarioUsuario.getId())).thenReturn(Optional.of(funcionarioUsuario));
        when(lojaRepository.findById(loja.getId())).thenReturn(Optional.of(loja));
        when(usuarioRepository.save(funcionarioUsuario)).thenReturn(funcionarioUsuario);

        Usuario resultado = lojaAccessService.ativarOuDesativarFuncionario(funcionarioUsuario.getId(), false, donoUsuario);

        assertFalse(resultado.isAtivo());
        verify(usuarioRepository).save(funcionarioUsuario);
    }

    @Test
    @DisplayName("Deve listar funcionários paginados por loja")
    void testListarFuncionariosPorLoja() {
        List<Usuario> funcionarios = Arrays.asList(funcionarioUsuario);
        Page<Usuario> paginaEsperada = new PageImpl<>(funcionarios, PageRequest.of(0, 10), funcionarios.size());

        when(usuarioRepository.findByLojaVinculadaId(loja.getId(), PageRequest.of(0, 10))).thenReturn(paginaEsperada);

        Page<Usuario> resultado = lojaAccessService.listarFuncionariosPorLoja(loja.getId(), PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("func-456", resultado.getContent().get(0).getId());
        verify(usuarioRepository).findByLojaVinculadaId(loja.getId(), PageRequest.of(0, 10));
    }
}
