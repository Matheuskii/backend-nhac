package br.com.nhac.backend_nhac.domain.pedido.dto;

import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO com dados detalhados de um pedido para visualização do lojista.
 * Inclui informações do cliente (nome e telefone) que não estão no DTO padrão do cliente.
 */
@Schema(description = "Dados detalhados de um pedido para visualização do lojista")
public record PedidoDetalheLojistaDTO(
        @Schema(description = "ID do pedido") String id,
        // TODO: adicionar numeroPedido sequencial amigável por loja em tarefa futura
        @Schema(description = "Nome do cliente que fez o pedido") String clienteNome,
        @Schema(description = "Telefone do cliente") String clienteTelefone,
        @Schema(description = "Valor total do pedido") BigDecimal valorTotal,
        @Schema(description = "Taxa de frete cobrada") BigDecimal taxaFrete,
        @Schema(description = "Forma de pagamento") String formaPagamento,
        @Schema(description = "Troco para") BigDecimal trocoPara,
        @Schema(description = "Observações") String observacao,
        @Schema(description = "Status atual do pedido") StatusPedido status,
        @Schema(description = "Data e hora da criação") Instant criadoEm,
        @Schema(description = "Endereço onde será entregue") EnderecoEntregaResponseDTO enderecoEntrega,
        @Schema(description = "Itens do pedido") List<ItemPedidoResponseDTO> itens
) {
    public PedidoDetalheLojistaDTO(Pedido pedido, String clienteNome, String clienteTelefone) {
        this(
                pedido.getId(),
                clienteNome,
                clienteTelefone,
                pedido.getValorTotal(),
                pedido.getTaxaFrete(),
                pedido.getFormaPagamento(),
                pedido.getTrocoPara(),
                pedido.getObservacao(),
                pedido.getStatus(),
                pedido.getCriadoEm(),
                pedido.getEnderecoEntrega() != null ? new EnderecoEntregaResponseDTO(
                        pedido.getEnderecoEntrega().getRua(),
                        pedido.getEnderecoEntrega().getNumero(),
                        pedido.getEnderecoEntrega().getBairro(),
                        pedido.getEnderecoEntrega().getCidade(),
                        pedido.getEnderecoEntrega().getEstado(),
                        pedido.getEnderecoEntrega().getCep(),
                        pedido.getEnderecoEntrega().getComplemento()
                ) : null,
                pedido.getItens() != null ? pedido.getItens().stream().map(item -> new ItemPedidoResponseDTO(
                        item.getId(),
                        item.getProduto().getId(),
                        item.getNome(),
                        item.getImagemUrl(),
                        item.getPrecoHistorico(),
                        item.getQuantidade()
                )).toList() : List.of()
        );
    }

    @Schema(description = "Endereço de entrega do pedido")
    public record EnderecoEntregaResponseDTO(
            String rua,
            String numero,
            String bairro,
            String cidade,
            String estado,
            String cep,
            String complemento
    ) {}

    @Schema(description = "Item individual do pedido")
    public record ItemPedidoResponseDTO(
            String id,
            String produtoId,
            String nome,
            String imagemUrl,
            BigDecimal preco,
            Integer quantidade
    ) {}
}
