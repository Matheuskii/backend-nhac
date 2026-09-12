package br.com.nhac.backend_nhac.domain.pedido.dto;

import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO com dados simplificados de um pedido para a listagem do histórico do lojista.
 * Substitui lojaId/lojaNome por clienteNome e quantidadeItens para melhor UX no painel.
 */
@Schema(description = "Dados simplificados de um pedido para a listagem do histórico do lojista")
public record PedidoResumoLojistaDTO(
        @Schema(description = "ID do pedido") String id,
        // TODO: adicionar numeroPedido sequencial amigável por loja em tarefa futura
        @Schema(description = "Nome do cliente que fez o pedido") String clienteNome,
        @Schema(description = "Quantidade total de itens no pedido") Integer quantidadeItens,
        @Schema(description = "Valor total do pedido") BigDecimal valorTotal,
        @Schema(description = "Status atual") StatusPedido status,
        @Schema(description = "Data de criação") Instant criadoEm
) {
    public PedidoResumoLojistaDTO(Pedido pedido, String clienteNome) {
        this(
                pedido.getId(),
                clienteNome,
                pedido.getItens() != null ? pedido.getItens().stream()
                        .mapToInt(item -> item.getQuantidade())
                        .sum() : 0,
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getCriadoEm()
        );
    }
}
