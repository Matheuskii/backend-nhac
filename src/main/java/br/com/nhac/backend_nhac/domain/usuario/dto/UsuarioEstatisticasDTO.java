package br.com.nhac.backend_nhac.domain.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estatísticas do usuário no app")
public record UsuarioEstatisticasDTO(
        @Schema(description = "Total de pedidos realizados", example = "15")
        long totalPedidos,

        @Schema(description = "Total de lojas favoritadas", example = "3")
        long lojasFavoritadas,

        @Schema(description = "Total de cupons resgatados (pedidos com cupom)", example = "5")
        long cuponsResgatados
) {
}
