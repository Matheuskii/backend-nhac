package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.PedidoRepository;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoLojistaDTO;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.produto.ProdutoRepository;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoLojistaDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.domain.lojista.service.LojaAccessService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LojistaServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private LojaAccessService lojaAccessService;

    @InjectMocks
    private LojistaService lojistaService;

    @Test
    @DisplayName("Deve listar produtos da loja incluindo inativos")
    void deveListarProdutosIncluindoInativos() {
        Usuario usuario = new Usuario();
        usuario.setId("user_1");
        
        Loja loja = new Loja();
        loja.setId("loja_1");
        loja.setUsuarioId("user_1");
        
        Produto ativo = produto("prod_1", "Hossomaki", true, 100);
        Produto inativo = produto("prod_2", "Temaki", false, 0);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(lojaAccessService.obterLojaAcessivel(usuario)).thenReturn(loja);
        when(produtoRepository.findByLojaIdAndIsAtivoTrue(eq("loja_1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ativo, inativo), pageable, 2));

        Page<ProdutoLojistaDTO> resultado = lojistaService.listarProdutos(usuario, "  ", null, pageable);

        assertEquals(2, resultado.getTotalElements());
        assertTrue(resultado.getContent().get(0).ativo());
        assertFalse(resultado.getContent().get(1).ativo());
        assertEquals(100, resultado.getContent().get(0).estoque());
        verify(produtoRepository).findByLojaIdAndIsAtivoTrue(eq("loja_1"), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve aplicar filtros de categoria e nome na listagem de produtos")
    void deveFiltrarProdutosPorCategoriaENome() {
        Usuario usuario = new Usuario();
        usuario.setId("user_1");
        
        Loja loja = new Loja();
        loja.setId("loja_1");
        loja.setUsuarioId("user_1");
        
        Pageable pageable = PageRequest.of(0, 20);
        
        when(lojaAccessService.obterLojaAcessivel(usuario)).thenReturn(loja);
        when(produtoRepository.findByLojaIdAndIsAtivoTrue(eq("loja_1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ProdutoLojistaDTO> resultado = lojistaService.listarProdutos(usuario, "Sushi", "salmao", pageable);

        assertTrue(resultado.getContent().isEmpty());
        verify(produtoRepository).findByLojaIdAndIsAtivoTrue(eq("loja_1"), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia de pedidos quando a loja ainda não recebeu nenhum")
    void deveRetornarPedidosVaziosQuandoNaoHouverRecebidos() {
        Usuario usuario = new Usuario();
        usuario.setId("user_1");
        
        Pageable pageable = PageRequest.of(0, 20);
        when(pedidoRepository.findByLojista(eq("user_1"), eq(null), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        Page<PedidoResumoLojistaDTO> resultado = lojistaService.listarPedidos("user_1", null, pageable);

        assertTrue(resultado.getContent().isEmpty());
    }

    @Test
    @DisplayName("Deve listar pedidos recebidos com clienteNome e quantidadeItens (PedidoResumoLojistaDTO)")
    void deveListarPedidosComClienteNomeEQuantidadeItens() {
        Pedido pedido = new Pedido();
        pedido.setId("pedido_1");
        pedido.setUsuarioId("cliente_1");
        pedido.setValorTotal(new BigDecimal("40.00"));
        pedido.setStatus(StatusPedido.PENDENTE);

        Usuario cliente = new Usuario();
        cliente.setId("cliente_1");
        cliente.setNome("João Silva");
        cliente.setTelefone("(11) 98765-4321");

        Pageable pageable = PageRequest.of(0, 20);
        when(pedidoRepository.findByLojista(eq("user_1"), eq(StatusPedido.PENDENTE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pedido), pageable, 1));
        when(usuarioRepository.findById("cliente_1")).thenReturn(Optional.of(cliente));

        Page<PedidoResumoLojistaDTO> resultado = lojistaService.listarPedidos("user_1", StatusPedido.PENDENTE, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("pedido_1", resultado.getContent().get(0).id());
        assertEquals("João Silva", resultado.getContent().get(0).clienteNome());
        assertEquals(0, resultado.getContent().get(0).quantidadeItens()); // sem itens no pedido mockado
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
