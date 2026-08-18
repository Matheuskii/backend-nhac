package br.com.nhac.backend_nhac.domain.pedido.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PedidoCriadoDTO(
        @Schema(description = "ID do pedido recém-criado", example = "a1b2c3d4-e5f6-...")
        String pedidoId,

        @Schema(description = "Client secret do Stripe (caso necessite confirmar no front)", example = "pi_123_secret_456")
        String clientSecret,

        @Schema(description = "Código Copia e Cola do PIX para pagamento", example = "00020101021126580014br.gov.bcb.pix...")
        String pixCopiaECola,

        @Schema(description = "URL da imagem ou payload do QR Code do PIX", example = "https://qr.stripe.com/test_123")
        String qrCodeUrl
) {}
