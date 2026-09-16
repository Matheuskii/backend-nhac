package br.com.nhac.backend_nhac.domain.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Preferências de notificação do usuário (painel do lojista/app), mesmos 4 toggles já existentes na tela de configurações da conta")
public record PreferenciasNotificacaoDTO(
        @Schema(description = "Notificar quando chegar um novo pedido", example = "true")
        @NotNull(message = "notificarNovoPedido é obrigatório.")
        Boolean notificarNovoPedido,

        @Schema(description = "Notificar sobre novas mensagens de clientes", example = "true")
        @NotNull(message = "notificarMensagens é obrigatório.")
        Boolean notificarMensagens,

        @Schema(description = "Notificar sobre novas avaliações", example = "false")
        @NotNull(message = "notificarAvaliacoes é obrigatório.")
        Boolean notificarAvaliacoes,

        @Schema(description = "Notificar sobre novidades e promoções da plataforma", example = "false")
        @NotNull(message = "notificarNovidades é obrigatório.")
        Boolean notificarNovidades
) {
}
