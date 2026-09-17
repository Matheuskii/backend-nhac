package br.com.nhac.backend_nhac.domain.financeiro.dto;

import br.com.nhac.backend_nhac.domain.painel.dto.FaturamentoDiaDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs do endpoint composto GET /lojista/financeiro (item 4.1 da spec).
 * Agrupados num arquivo só porque são pequenos e só existem em função da
 * resposta principal (FinanceiroDTO) — não fazem sentido usados isolados.
 */
public class FinanceiroDTOs {

    public record ResumoFinanceiroDTO(
            BigDecimal faturamentoPeriodo,
            long numeroPedidos,
            BigDecimal ticketMedio,
            BigDecimal taxaCancelamentoPercentual
    ) {}

    public record PedidosPorDiaSemanaDTO(String diaSemana, long quantidade) {}

    public record VendasPorCategoriaDTO(String categoria, BigDecimal valor, BigDecimal percentual) {}

    public record VendasPorPagamentoDTO(String formaPagamento, BigDecimal valor, BigDecimal percentual) {}

    public record ProdutoMaisVendidoDTO(String produtoId, String nome, long quantidadeVendida, BigDecimal faturamento) {}

    public record PedidosPorHoraDTO(int hora, long quantidade) {}

    public record FinanceiroDTO(
            String periodo,
            ResumoFinanceiroDTO resumo,
            List<FaturamentoDiaDTO> faturamentoDiario,
            List<PedidosPorDiaSemanaDTO> pedidosPorDiaSemana,
            List<VendasPorCategoriaDTO> vendasPorCategoria,
            List<VendasPorPagamentoDTO> vendasPorFormaPagamento,
            List<ProdutoMaisVendidoDTO> produtosMaisVendidos,
            List<PedidosPorHoraDTO> pedidosPorHora
    ) {}
}
