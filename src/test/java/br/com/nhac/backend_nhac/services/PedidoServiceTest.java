package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.Produto;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }}