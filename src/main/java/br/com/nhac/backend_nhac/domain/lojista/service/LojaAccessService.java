package br.com.nhac.backend_nhac.domain.lojista.service;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.loja.LojaRepository;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LojaAccessService {

    private final UsuarioRepository usuarioRepository;
    private final LojaRepository lojaRepository;
    private final PasswordEncoder passwordEncoder;

    public LojaAccessService(UsuarioRepository usuarioRepository, LojaRepository lojaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.lojaRepository = lojaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obtém a loja acessível pelo usuário.
     * - ADMIN: pode acessar qualquer loja (não usa este método diretamente)
     * - LOJISTA: retorna a loja onde é dono (usuario_id)
     * - FUNCIONARIO: retorna a loja vinculada (loja_vinculada_id)
     */
    @Transactional(readOnly = true)
    public Loja obterLojaAcessivel(Usuario usuario) {
        if (usuario.getPapel() == Papel.ADMIN) {
            return null; // Admin não tem loja própria, bypass em outros pontos
        }

        if (usuario.getPapel() == Papel.LOJISTA) {
            return lojaRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new AcessoNegadoException("Usuário lojista não possui loja associada."));
        }

        if (usuario.getPapel() == Papel.FUNCIONARIO) {
            if (usuario.getLojaVinculada() == null) {
                throw new AcessoNegadoException("Funcionário não possui loja vinculada.");
            }
            return usuario.getLojaVinculada();
        }

        throw new AcessoNegadoException("Usuário não tem permissão para acessar loja.");
    }

    /**
     * Valida se o usuário pode gerenciar funcionários da loja.
     * Apenas DONO ou ADMIN podem criar/gerenciar funcionários.
     */
    @Transactional(readOnly = true)
    public void validarPermissaoGerenciarFuncionarios(Usuario usuario, String lojaId) {
        if (usuario.getPapel() == Papel.ADMIN) {
            return;
        }

        Loja loja = lojaRepository.findById(lojaId)
                .orElseThrow(() -> new IdNaoEncontradoException("Loja não encontrada."));

        if (!loja.getUsuarioId().equals(usuario.getId())) {
            throw new AcessoNegadoException("Apenas o dono da loja pode gerenciar funcionários.");
        }
    }

    /**
     * Cria um funcionário vinculado à loja do usuário logado.
     */
    @Transactional
    public Usuario criarFuncionario(String lojaId, String nome, String email, String senha, 
                                     String telefone, String cargo, Usuario criador) {
        // Valida que o criador é dono da loja ou ADMIN
        validarPermissaoGerenciarFuncionarios(criador, lojaId);

        // Verifica limite de 15 funcionários por loja
        long funcionariosAtivos = usuarioRepository.countByLojaVinculadaIdAndAtivoTrue(lojaId);
        if (funcionariosAtivos >= 15) {
            throw new RegraDeNegocioException("Limite máximo de 15 funcionários por loja atingido.");
        }

        // Verifica se email já existe
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new RegraDeNegocioException("Email já cadastrado no sistema.");
        }

        Loja loja = lojaRepository.findById(lojaId)
                .orElseThrow(() -> new IdNaoEncontradoException("Loja não encontrada."));

        Usuario funcionario = new Usuario();
        funcionario.setId(java.util.UUID.randomUUID().toString());
        funcionario.setNome(nome);
        funcionario.setEmail(email);
        funcionario.setSenha(passwordEncoder.encode(senha));
        funcionario.setTelefone(telefone);
        funcionario.setPapel(Papel.FUNCIONARIO);
        funcionario.setLojaVinculada(loja);
        funcionario.setCargo(cargo);
        funcionario.setAtivo(true);

        return usuarioRepository.save(funcionario);
    }

    /**
     * Atualiza dados de um funcionário (exceto email e senha).
     */
    @Transactional
    public Usuario atualizarFuncionario(String funcionarioId, String nome, String telefone, 
                                         String cargo, Usuario editor) {
        Usuario funcionario = usuarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new IdNaoEncontradoException("Funcionário não encontrado."));

        if (funcionario.getPapel() != Papel.FUNCIONARIO) {
            throw new RegraDeNegocioException("Usuário não é um funcionário.");
        }

        // Valida que o editor tem acesso à loja do funcionário
        validarPermissaoGerenciarFuncionarios(editor, funcionario.getLojaVinculada().getId());

        if (nome != null) {
            funcionario.setNome(nome);
        }
        if (telefone != null) {
            funcionario.setTelefone(telefone);
        }
        if (cargo != null) {
            funcionario.setCargo(cargo);
        }

        return usuarioRepository.save(funcionario);
    }

    /**
     * Ativa ou desativa um funcionário (soft delete quando ativo=false).
     */
    @Transactional
    public Usuario ativarOuDesativarFuncionario(String funcionarioId, boolean ativo, Usuario editor) {
        Usuario funcionario = usuarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new IdNaoEncontradoException("Funcionário não encontrado."));

        if (funcionario.getPapel() != Papel.FUNCIONARIO) {
            throw new RegraDeNegocioException("Usuário não é um funcionário.");
        }

        // Valida que o editor tem acesso à loja do funcionário
        validarPermissaoGerenciarFuncionarios(editor, funcionario.getLojaVinculada().getId());

        funcionario.setAtivo(ativo);
        return usuarioRepository.save(funcionario);
    }

    /**
     * Lista funcionários de uma loja.
     */
    @Transactional(readOnly = true)
    public Page<Usuario> listarFuncionariosPorLoja(String lojaId, Pageable pageable) {
        return usuarioRepository.findByLojaVinculadaId(lojaId, pageable);
    }
}
