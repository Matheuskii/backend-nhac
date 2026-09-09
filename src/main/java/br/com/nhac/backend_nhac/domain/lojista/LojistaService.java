package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.domain.pedido.PedidoRepository;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoDTO;
import br.com.nhac.backend_nhac.domain.produto.ProdutoRepository;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoLojistaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LojistaService {

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
        return produtoRepository.findByLojista(usuarioId, categoriaFiltro, nomeFiltro, pageable)
                .map(ProdutoLojistaDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResumoDTO> listarPedidos(String usuarioId, StatusPedido status, Pageable pageable) {
        return pedidoRepository.findByLojista(usuarioId, status, pageable)
                .map(PedidoResumoDTO::new);
    }
}
