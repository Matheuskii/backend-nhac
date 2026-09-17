package br.com.nhac.backend_nhac.domain.pedido.dto;

import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO para atualização do status do pedido")
public record PedidoUpdateStatusDTO(
        @NotNull(message = "O novo status é obrigatório.")
        @Schema(description = "Novo status do pedido", example = "PREPARANDO")
        StatusPedido status
) {
}
