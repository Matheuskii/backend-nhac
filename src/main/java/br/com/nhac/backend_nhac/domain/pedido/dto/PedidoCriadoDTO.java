package br.com.nhac.backend_nhac.domain.pedido.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PedidoCriadoDTO(
        @Schema(description = "ID do pedido recém-criado", example = "a1b2c3d4-e5f6-...")
        String pedidoId
) {}
