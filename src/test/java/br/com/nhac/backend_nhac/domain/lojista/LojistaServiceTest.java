package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.PedidoRepository;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoDTO;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.produto.ProdutoRepository;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoLojistaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LojistaServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private LojistaService lojistaService;

    @Test
    @DisplayName("Deve listar produtos da loja incluindo inativos")
    void deveListarProdutosIncluindoInativos() {
        Produto ativo = produto("prod_1", "Hossomaki", true, 100);
        Produto inativo = produto("prod_2", "Temaki", false, 0);
        Pageable pageable = PageRequest.of(0, 20);
        when(produtoRepository.findByLojista("user_1", null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(ativo, inativo), pageable, 2));

        Page<ProdutoLojistaDTO> resultado = lojistaService.listarProdutos("user_1", "  ", null, pageable);

        assertEquals(2, resultado.getTotalElements());
        assertTrue(resultado.getContent().get(0).ativo());
        assertFalse(resultado.getContent().get(1).ativo());
        assertEquals(100, resultado.getContent().get(0).estoque());
        verify(produtoRepository).findByLojista("user_1", null, null, pageable);
    }

    @Test
    @DisplayName("Deve aplicar filtros de categoria e nome na listagem de produtos")
    void deveFiltrarProdutosPorCategoriaENome() {
        Pageable pageable = PageRequest.of(0, 20);
        when(produtoRepository.findByLojista("user_1", "Sushi", "salmao", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ProdutoLojistaDTO> resultado = lojistaService.listarProdutos("user_1", "Sushi", "salmao", pageable);

        assertTrue(resultado.getContent().isEmpty());
        verify(produtoRepository).findByLojista("user_1", "Sushi", "salmao", pageable);
    }

    @Test
    @DisplayName("Deve retornar lista vazia de pedidos quando a loja ainda não recebeu nenhum")
    void deveRetornarPedidosVaziosQuandoNaoHouverRecebidos() {
        Pageable pageable = PageRequest.of(0, 20);
        when(pedidoRepository.findByLojista("user_1", null, pageable))
                .thenReturn(Page.empty(pageable));

        Page<PedidoResumoDTO> resultado = lojistaService.listarPedidos("user_1", null, pageable);

        assertTrue(resultado.getContent().isEmpty());
    }

    @Test
    @DisplayName("Deve listar pedidos recebidos filtrando por status")
    void deveListarPedidosFiltrandoPorStatus() {
        Pedido pedido = new Pedido();
        pedido.setId("pedido_1");
        pedido.setValorTotal(new BigDecimal("40.00"));
        pedido.setStatus(StatusPedido.PENDENTE);

        Pageable pageable = PageRequest.of(0, 20);
        when(pedidoRepository.findByLojista("user_1", StatusPedido.PENDENTE, pageable))
                .thenReturn(new PageImpl<>(List.of(pedido), pageable, 1));

        Page<PedidoResumoDTO> resultado = lojistaService.listarPedidos("user_1", StatusPedido.PENDENTE, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("pedido_1", resultado.getContent().get(0).id());
        assertEquals(StatusPedido.PENDENTE, resultado.getContent().get(0).status());
    }

    private Produto produto(String id, String nome, boolean ativo, int estoque) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(nome);
        produto.setDescricao("desc");
        produto.setPreco(new BigDecimal("25.50"));
        produto.setCategoriaMenu("Sushi");
        produto.setImagemUrl("https://...");
        produto.setPeso("200g");
        produto.setPercentualDesconto(10);
        produto.setAtivo(ativo);
        produto.setEstoque(estoque);
        return produto;
    }
}
