package br.com.nhac.backend_nhac.domain.entrega.dto;

import java.util.List;

public record RotaEntregaResponseDTO(
        String pedidoId,
        String lojaNome,
        PontoCoordenadaDTO origem,
        PontoCoordenadaDTO destino,
        Double distanciaMetros,
        Double distanciaKm,
        Integer duracaoEstimadaMinutos,
        String polyline,
        List<PontoCoordenadaDTO> waypoints
) {
}
