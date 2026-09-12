package br.com.nhac.backend_nhac.domain.lojista.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record FinanceiroResumoDTO(
    ResumoFinanceiro resumo,
    List<DadoFaturamentoDiario> faturamentoPorDia,
    Map<String, BigDecimal> vendasPorCategoria,
    Map<String, BigDecimal> vendasPorFormaPagamento,
    ProdutoMaisVendido produtoMaisVendido,
    Map<Integer, BigDecimal> faturamentoPorHora
) {
    public record ResumoFinanceiro(
        BigDecimal faturamentoTotal,
        long totalPedidos,
        BigDecimal ticketMedio
    ) {}

    public record DadoFaturamentoDiario(
        java.time.LocalDate data,
        BigDecimal valor
    ) {}

    public record ProdutoMaisVendido(
        String produtoId,
        String nome,
        long quantidadeVendida,
        BigDecimal valorTotal
    ) {}
}
