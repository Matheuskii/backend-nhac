package br.com.nhac.backend_nhac.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do upload de uma imagem")
public record UploadResponseDTO(
        @Schema(description = "URL pública da imagem enviada, pronta para ser usada em imagemUrl de loja/produto",
                example = "https://firebasestorage.googleapis.com/v0/b/nhac-delivery.appspot.com/o/produtos%2Fabc123.jpg?alt=media&token=...")
        String url
) {
}
