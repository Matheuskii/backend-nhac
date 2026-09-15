package br.com.nhac.backend_nhac.domain.entregador.dto;

import br.com.nhac.backend_nhac.domain.entregador.Entregador;
import br.com.nhac.backend_nhac.domain.entregador.StatusOperacional;
import br.com.nhac.backend_nhac.domain.entregador.TipoVeiculo;

import java.time.Instant;

public record EntregadorResponseDTO(
        String id,
        String usuarioId,
        String nome,
        String email,
        String telefone,
        String cnh,
        String placaVeiculo,
        TipoVeiculo tipoVeiculo,
        StatusOperacional statusOperacional,
        Double latitudeAtual,
        Double longitudeAtual,
        Instant ultimaAtualizacaoLocalizacao,
        boolean ativo
) {
    public EntregadorResponseDTO(Entregador entregador) {
        this(
                entregador.getId(),
                entregador.getUsuario().getId(),
                entregador.getUsuario().getNome(),
                entregador.getUsuario().getEmail(),
                entregador.getUsuario().getTelefone(),
                entregador.getCnh(),
                entregador.getPlacaVeiculo(),
                entregador.getTipoVeiculo(),
                entregador.getStatusOperacional(),
                entregador.getLatitudeAtual(),
                entregador.getLongitudeAtual(),
                entregador.getUltimaAtualizacaoLocalizacao(),
                entregador.isAtivo()
        );
    }
}
