package br.com.nhac.backend_nhac.domain.entrega.dto;

import br.com.nhac.backend_nhac.domain.entrega.OfertaEntrega;
import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

public record OfertaEntregaDTO(
        String id,
        String pedidoId,
        String lojaNome,
        String lojaEndereco,
        Double lojaLatitude,
        Double lojaLongitude,
        String clienteBairro,
        String clienteCidade,
        BigDecimal taxaFrete,
        Instant criadoEm,
        Instant expiraEm,
        long tempoRestanteSegundos
) {
    public OfertaEntregaDTO(OfertaEntrega oferta) {
        this(
                oferta.getId(),
                oferta.getPedido().getId(),
                extrairLojaNome(oferta.getPedido()),
                extrairLojaEndereco(oferta.getPedido()),
                extrairLojaLat(oferta.getPedido()),
                extrairLojaLng(oferta.getPedido()),
                oferta.getPedido().getEnderecoEntrega() != null ? oferta.getPedido().getEnderecoEntrega().getBairro() : null,
                oferta.getPedido().getEnderecoEntrega() != null ? oferta.getPedido().getEnderecoEntrega().getCidade() : null,
                oferta.getPedido().getTaxaFrete(),
                oferta.getCriadoEm(),
                oferta.getExpiraEm(),
                calcularTempoRestante(oferta.getExpiraEm())
        );
    }

    private static String extrairLojaNome(Pedido pedido) {
        return pedido.getLoja() != null ? pedido.getLoja().getNome() : "";
    }

    private static String extrairLojaEndereco(Pedido pedido) {
        if (pedido.getLoja() == null || pedido.getLoja().getEndereco() == null) return "";
        var end = pedido.getLoja().getEndereco();
        return end.getRua() + ", " + end.getNumero() + " - " + end.getBairro() + ", " + end.getCidade();
    }

    private static Double extrairLojaLat(Pedido pedido) {
        return (pedido.getLoja() != null && pedido.getLoja().getGeoLocalizacao() != null)
                ? pedido.getLoja().getGeoLocalizacao().getGeoLat() : null;
    }

    private static Double extrairLojaLng(Pedido pedido) {
        return (pedido.getLoja() != null && pedido.getLoja().getGeoLocalizacao() != null)
                ? pedido.getLoja().getGeoLocalizacao().getGeoLng() : null;
    }

    private static long calcularTempoRestante(Instant expiraEm) {
        long segundos = Duration.between(Instant.now(), expiraEm).toSeconds();
        return Math.max(0, segundos);
    }
}
