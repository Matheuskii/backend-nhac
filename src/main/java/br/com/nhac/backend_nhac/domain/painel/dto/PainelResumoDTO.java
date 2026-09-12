package br.com.nhac.backend_nhac.domain.painel.dto;

import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoLojistaDTO;

import java.math.BigDecimal;
import java.util.List;

public record PainelResumoDTO(
        boolean lojaAberta,
        BigDecimal faturamentoHoje,
        long pedidosEmPreparo,
        long pedidosACaminho,
        long pedidosConcluidosHoje,
        List<FaturamentoDiaDTO> faturamentoUltimos7Dias,
        List<PedidoResumoLojistaDTO> pedidosRecentes
) {
}
