package br.com.nhac.backend_nhac.domain.pedido.dto;

import br.com.nhac.backend_nhac.domain.pedido.ItemPedido;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Resumo de pedido para a listagem do PAINEL DO LOJISTA (GET /lojista/pedidos).
 * PedidoResumoDTO (usado no histórico do cliente) tem lojaId/lojaNome, que aqui
 * é sempre a própria loja de quem está logado — inútil pro lojista e, pior,
 * fazia a UI mostrar o nome da loja no lugar do nome do cliente. Trocamos por
 * clienteNome (o dado que o lojista realmente precisa pra identificar o
 * pedido na lista) e quantidadeItens (que a UI também precisava e não tinha).
 */
@Schema(description = "Dados simplificados de um pedido para a listagem do painel do lojista")
public record PedidoResumoLojistaDTO(
        @Schema(description = "ID do pedido") String id,
        @Schema(description = "Nome do cliente que fez o pedido") String clienteNome,
        @Schema(description = "Quantidade total de itens no pedido") int quantidadeItens,
        @Schema(description = "Valor total do pedido") BigDecimal valorTotal,
        @Schema(description = "Status atual") StatusPedido status,
        @Schema(description = "Data de criação") Instant criadoEm
) {
    public PedidoResumoLojistaDTO(Pedido pedido, String clienteNome) {
        this(
                pedido.getId(),
                clienteNome,
                pedido.getItens() == null ? 0 : pedido.getItens().stream().mapToInt(ItemPedido::getQuantidade).sum(),
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getCriadoEm()
        );
    }
}
