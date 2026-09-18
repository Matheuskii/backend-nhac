package br.com.nhac.backend_nhac.domain.entregador.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Agregação de ganhos do motoboy no período. Calculada em cima de tb_pedidos
 * (status ENTREGUE + entregue_em dentro da janela), sem tabela nova — mesmo
 * padrão do GET /lojista/financeiro.
 *
 * ATENÇÃO (ver pergunta em aberto #3 do relatório): "ganho" aqui é a
 * taxa_frete cheia do pedido. O sistema ainda não tem regra de repasse
 * (percentual da plataforma, bônus, taxa dinâmica), então o valor exibido no
 * app é o frete bruto, não o líquido do entregador.
 */
public record GanhosEntregadorDTO(
        String periodo,
        BigDecimal totalGanhos,
        long totalEntregas,
        BigDecimal ticketMedio,
        List<GanhoDiaDTO> porDia
) {
    public record GanhoDiaDTO(LocalDate data, BigDecimal valor, long entregas) {}
}
