package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.loja.LojaRepository;
import br.com.nhac.backend_nhac.domain.lojista.dto.FuncionarioCreateDTO;
import br.com.nhac.backend_nhac.domain.lojista.dto.FuncionarioDTO;
import br.com.nhac.backend_nhac.domain.lojista.dto.FuncionarioUpdateDTO;
import br.com.nhac.backend_nhac.domain.lojista.service.LojaAccessService;
import br.com.nhac.backend_nhac.domain.pedido.PedidoRepository;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoDetalheLojistaDTO;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoDTO;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoLojistaDTO;
import br.com.nhac.backend_nhac.domain.produto.ProdutoRepository;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoLojistaDTO;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LojistaService {

    static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LojaRepository lojaRepository;
    private final LojaAccessService lojaAccessService;

    public LojistaService(ProdutoRepository produtoRepository, PedidoRepository pedidoRepository, 
                          UsuarioRepository usuarioRepository, LojaRepository lojaRepository,
                          LojaAccessService lojaAccessService) {
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.lojaRepository = lojaRepository;
        this.lojaAccessService = lojaAccessService;
    }

    @Transactional(readOnly = true)
    public Page<ProdutoLojistaDTO> listarProdutos(Usuario usuarioLogado, String categoriaMenu, String nome, Pageable pageable) {
        Loja loja = lojaAccessService.obterLojaAcessivel(usuarioLogado);
        if (loja == null) {
            // ADMIN não tem loja própria, retorna página vazia
            return Page.empty(pageable);
        }
        String categoriaFiltro = StringUtils.hasText(categoriaMenu) ? categoriaMenu : null;
        String nomeFiltro = StringUtils.hasText(nome) ? nome : null;
        return produtoRepository.findByLojaIdAndIsAtivoTrue(loja.getId(), limitarPagina(pageable))
                .map(ProdutoLojistaDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResumoLojistaDTO> listarPedidos(String usuarioId, StatusPedido status, Pageable pageable) {
        return pedidoRepository.findByLojista(usuarioId, status, limitarPagina(pageable))
                .map(pedido -> {
                    String clienteNome = usuarioRepository.findById(pedido.getUsuarioId())
                            .map(u -> u.getNome())
                            .orElse("Cliente removido");
                    return new PedidoResumoLojistaDTO(pedido, clienteNome);
                });
    }

    @Transactional(readOnly = true)
    public PedidoDetalheLojistaDTO buscarPedidoDetalhe(String pedidoId, Usuario usuarioLogado) {
        var pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido não encontrado."));

        // Valida acesso: ADMIN pode tudo, LOJISTA/FUNCIONARIO precisam pertencer à loja do pedido
        if (usuarioLogado.getPapel() != Papel.ADMIN) {
            Loja lojaAcessivel = lojaAccessService.obterLojaAcessivel(usuarioLogado);
            if (lojaAcessivel == null || !lojaAcessivel.getId().equals(pedido.getLoja().getId())) {
                throw new AcessoNegadoException("Acesso negado: você não tem permissão para visualizar este pedido.");
            }
        }

        String clienteNome = usuarioRepository.findById(pedido.getUsuarioId())
                .map(u -> u.getNome())
                .orElse("Cliente removido");
        String clienteTelefone = usuarioRepository.findById(pedido.getUsuarioId())
                .map(u -> u.getTelefone())
                .orElse("N/A");

        return new PedidoDetalheLojistaDTO(pedido, clienteNome, clienteTelefone);
    }

    @Transactional(readOnly = true)
    public Page<FuncionarioDTO> listarFuncionarios(Usuario usuarioLogado, Pageable pageable) {
        Loja loja = lojaAccessService.obterLojaAcessivel(usuarioLogado);
        if (loja == null) {
            throw new AcessoNegadoException("Usuário não possui loja associada.");
        }
        return usuarioRepository.findByLojaVinculadaId(loja.getId(), pageable)
                .map(u -> {
                    FuncionarioDTO dto = new FuncionarioDTO();
                    dto.setId(u.getId());
                    dto.setNome(u.getNome());
                    dto.setEmail(u.getEmail());
                    dto.setTelefone(u.getTelefone());
                    dto.setCargo(u.getCargo());
                    dto.setAtivo(u.isAtivo());
                    if (u.getLojaVinculada() != null) {
                        dto.setLojaId(u.getLojaVinculada().getId());
                        dto.setLojaNome(u.getLojaVinculada().getNome());
                    }
                    return dto;
                });
    }

    @Transactional
    public Usuario criarFuncionario(FuncionarioCreateDTO dto, Usuario criador) {
        Loja loja = lojaAccessService.obterLojaAcessivel(criador);
        if (loja == null) {
            throw new AcessoNegadoException("Usuário não possui loja associada.");
        }
        return lojaAccessService.criarFuncionario(loja.getId(), dto.getNome(), dto.getEmail(), 
                                                   dto.getSenha(), dto.getTelefone(), dto.getCargo(), criador);
    }

    @Transactional
    public Usuario atualizarFuncionario(String funcionarioId, FuncionarioUpdateDTO dto, Usuario editor) {
        return lojaAccessService.atualizarFuncionario(funcionarioId, dto.getNome(), 
                                                       dto.getTelefone(), dto.getCargo(), editor);
    }

    @Transactional
    public Usuario ativarOuDesativarFuncionario(String funcionarioId, boolean ativo, Usuario editor) {
        return lojaAccessService.ativarOuDesativarFuncionario(funcionarioId, ativo, editor);
    }

    Pageable limitarPagina(Pageable pageable) {
        int size = Math.min(Math.max(pageable.getPageSize(), 1), TAMANHO_MAXIMO_PAGINA);
        if (size == pageable.getPageSize()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }
}
