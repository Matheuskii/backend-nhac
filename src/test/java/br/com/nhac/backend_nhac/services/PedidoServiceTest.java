package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.PedidoRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private LojaRepository lojaRepository;

    @InjectMocks private PedidoService pedidoService;

    @Test
    @DisplayName("Deve calcular o preço total usando o valor do Banco de Dados, prevenindo fraudes do Frontend")
    void deveCalcularPrecoRealDoBancoIgnorandoOCliente() {
        // 1. DADOS SIMULADOS (MOCKS)
        String usuarioId = "user_teste_123";

        // 🔴 MOCK DO ENDEREÇO (Passando os 7 parâmetros exatos do teu record)
        PedidoCreateDTO.EnderecoEntregaDTO enderecoMock = new PedidoCreateDTO.EnderecoEntregaDTO(
                "Rua Teste", "123", "Bairro", "Cidade", "SP", "01000-000", null
        );

        Loja lojaMock = new Loja();
        lojaMock.setId("loja_1");

        Produto burgerMock = new Produto();
        burgerMock.setId("prod_1");
        burgerMock.setLoja(lojaMock);
        burgerMock.setPreco(new BigDecimal("45.00"));

        PedidoCreateDTO.ItemPedidoDTO itemFraudulento = new PedidoCreateDTO.ItemPedidoDTO(
                "prod_1", "Hambúrguer", "http://imagem.com/burger.jpg", 2
        );

        PedidoCreateDTO dto = new PedidoCreateDTO(
                "loja_1", "PIX", "Sem cebola", enderecoMock, List.of(itemFraudulento)
        );

        when(lojaRepository.findByIdAndIsAbertoTrue("loja_1")).thenReturn(Optional.of(lojaMock));
        when(produtoRepository.findById("prod_1")).thenReturn(Optional.of(burgerMock));

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId("pedido_gerado_001");
            return p;
        });

        pedidoService.finalizarPedido(dto, usuarioId);

        verify(pedidoRepository).save(argThat(pedido -> {
            boolean totalCorreto = pedido.getValorTotal().compareTo(new BigDecimal("95.00")) == 0;
            boolean donoCorreto = pedido.getUsuarioId().equals(usuarioId);

            boolean enderecoNaoNulo = pedido.getEnderecoEntrega() != null;

            return totalCorreto && donoCorreto && enderecoNaoNulo;
        }));
    }

    @Test
    @DisplayName("Deve lançar IdNaoEncontradoException quando a loja não existir ou estiver fechada")
    void deveLancarExcecaoQuandoLojaNaoExisteOuEstaFechada() {
        PedidoCreateDTO.EnderecoEntregaDTO enderecoMock = new PedidoCreateDTO.EnderecoEntregaDTO(
                "Rua Teste", "123", "Bairro", "Cidade", "SP", "01000-000", null
        );
        PedidoCreateDTO.ItemPedidoDTO item = new PedidoCreateDTO.ItemPedidoDTO(
                "prod_1", "Hambúrguer", "http://imagem.com/burger.jpg", 1
        );
        PedidoCreateDTO dto = new PedidoCreateDTO("loja_fechada", "PIX", null, enderecoMock, List.of(item));

        when(lojaRepository.findByIdAndIsAbertoTrue("loja_fechada")).thenReturn(Optional.empty());

        Exception excecao = assertThrows(IdNaoEncontradoException.class,
                () -> pedidoService.finalizarPedido(dto, "user_teste"));

        assertEquals("A loja informada não existe ou está fechada.", excecao.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar IdNaoEncontradoException quando um produto do carrinho não existir")
    void deveLancarExcecaoQuandoProdutoNaoExiste() {
        PedidoCreateDTO.EnderecoEntregaDTO enderecoMock = new PedidoCreateDTO.EnderecoEntregaDTO(
                "Rua Teste", "123", "Bairro", "Cidade", "SP", "01000-000", null
        );

        Loja lojaMock = new Loja();
        lojaMock.setId("loja_1");

        PedidoCreateDTO.ItemPedidoDTO itemFantasma = new PedidoCreateDTO.ItemPedidoDTO(
                "prod_fantasma", "Produto Inexistente", null, 1
        );
        PedidoCreateDTO dto = new PedidoCreateDTO("loja_1", "PIX", null, enderecoMock, List.of(itemFantasma));

        when(lojaRepository.findByIdAndIsAbertoTrue("loja_1")).thenReturn(Optional.of(lojaMock));
        when(produtoRepository.findById("prod_fantasma")).thenReturn(Optional.empty());

        Exception excecao = assertThrows(IdNaoEncontradoException.class,
                () -> pedidoService.finalizarPedido(dto, "user_teste"));

        assertEquals("O produto com ID 'prod_fantasma' não existe no catálogo.", excecao.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o produto pertencer a outra loja")
    void deveLancarExcecaoQuandoProdutoPertenceAOutraLoja() {
        PedidoCreateDTO.EnderecoEntregaDTO enderecoMock = new PedidoCreateDTO.EnderecoEntregaDTO(
                "Rua Teste", "123", "Bairro", "Cidade", "SP", "01000-000", null
        );

        Loja lojaSelecionada = new Loja();
        lojaSelecionada.setId("loja_1");

        Loja outraLoja = new Loja();
        outraLoja.setId("loja_2");

        Produto produtoDeOutraLoja = new Produto();
        produtoDeOutraLoja.setId("prod_1");
        produtoDeOutraLoja.setNome("Hambúrguer");
        produtoDeOutraLoja.setLoja(outraLoja);
        produtoDeOutraLoja.setPreco(new BigDecimal("30.00"));

        PedidoCreateDTO.ItemPedidoDTO item = new PedidoCreateDTO.ItemPedidoDTO(
                "prod_1", "Hambúrguer", null, 1
        );
        PedidoCreateDTO dto = new PedidoCreateDTO("loja_1", "PIX", null, enderecoMock, List.of(item));

        when(lojaRepository.findByIdAndIsAbertoTrue("loja_1")).thenReturn(Optional.of(lojaSelecionada));
        when(produtoRepository.findById("prod_1")).thenReturn(Optional.of(produtoDeOutraLoja));

        Exception excecao = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.finalizarPedido(dto, "user_teste"));

        assertEquals("O produto 'Hambúrguer' não pertence à loja selecionada.", excecao.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve somar corretamente o valor de múltiplos itens e aplicar a taxa fixa de frete")
    void deveSomarValoresDeMultiplosItensComTaxaDeFreteFixa() {
        PedidoCreateDTO.EnderecoEntregaDTO enderecoMock = new PedidoCreateDTO.EnderecoEntregaDTO(
                "Rua Teste", "123", "Bairro", "Cidade", "SP", "01000-000", null
        );

        Loja lojaMock = new Loja();
        lojaMock.setId("loja_1");

        Produto produto1 = new Produto();
        produto1.setId("prod_1");
        produto1.setLoja(lojaMock);
        produto1.setPreco(new BigDecimal("10.00"));

        Produto produto2 = new Produto();
        produto2.setId("prod_2");
        produto2.setLoja(lojaMock);
        produto2.setPreco(new BigDecimal("20.00"));

        PedidoCreateDTO.ItemPedidoDTO item1 = new PedidoCreateDTO.ItemPedidoDTO("prod_1", "Item 1", null, 2);
        PedidoCreateDTO.ItemPedidoDTO item2 = new PedidoCreateDTO.ItemPedidoDTO("prod_2", "Item 2", null, 1);

        PedidoCreateDTO dto = new PedidoCreateDTO("loja_1", "PIX", null, enderecoMock, List.of(item1, item2));

        when(lojaRepository.findByIdAndIsAbertoTrue("loja_1")).thenReturn(Optional.of(lojaMock));
        when(produtoRepository.findById("prod_1")).thenReturn(Optional.of(produto1));
        when(produtoRepository.findById("prod_2")).thenReturn(Optional.of(produto2));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId("pedido_gerado_002");
            return p;
        });

        pedidoService.finalizarPedido(dto, "user_teste");

        // (10.00 * 2) + (20.00 * 1) = 40.00 + taxa fixa de 5.00 = 45.00
        verify(pedidoRepository).save(argThat(pedido ->
                pedido.getValorTotal().compareTo(new BigDecimal("45.00")) == 0
                        && pedido.getTaxaFrete().compareTo(new BigDecimal("5.00")) == 0
                        && pedido.getItens().size() == 2
        ));
    }
}