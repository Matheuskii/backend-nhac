package br.com.nhac.backend_nhac.domain.lojista.dto;

import java.math.BigDecimal;
import java.util.List;

public record PainelResumoDTO(
    boolean lojaAberta,
    BigDecimal faturamentoHoje,
    long pedidosPendentes,
    long pedidosPagos,
    long pedidosPreparando,
    long pedidosSaiuEntrega,
    long pedidosEntregues,
    long pedidosCancelados,
    BigDecimal faturamentoUltimos7Dias,
    List<PedidoRecenteDTO> pedidosRecentes
) {
    public record PedidoRecenteDTO(
        String id,
        String clienteNome,
        BigDecimal valorTotal,
        String status,
        java.time.Instant criadoEm
    ) {}
}
