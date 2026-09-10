package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.domain.pedido.PedidoRepository;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoDTO;
import br.com.nhac.backend_nhac.domain.produto.ProdutoRepository;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoLojistaDTO;
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

    public LojistaService(ProdutoRepository produtoRepository, PedidoRepository pedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProdutoLojistaDTO> listarProdutos(String usuarioId, String categoriaMenu, String nome, Pageable pageable) {
        String categoriaFiltro = StringUtils.hasText(categoriaMenu) ? categoriaMenu : null;
        String nomeFiltro = StringUtils.hasText(nome) ? nome : null;
        return produtoRepository.findByLojista(usuarioId, categoriaFiltro, nomeFiltro, limitarPagina(pageable))
                .map(ProdutoLojistaDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResumoDTO> listarPedidos(String usuarioId, StatusPedido status, Pageable pageable) {
        return pedidoRepository.findByLojista(usuarioId, status, limitarPagina(pageable))
                .map(PedidoResumoDTO::new);
    }

    Pageable limitarPagina(Pageable pageable) {
        int size = Math.min(Math.max(pageable.getPageSize(), 1), TAMANHO_MAXIMO_PAGINA);
        if (size == pageable.getPageSize()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }
}
