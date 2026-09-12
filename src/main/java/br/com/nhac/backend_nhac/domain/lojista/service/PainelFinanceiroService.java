package br.com.nhac.backend_nhac.domain.lojista.service;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.lojista.dto.FinanceiroResumoDTO;
import br.com.nhac.backend_nhac.domain.lojista.dto.PainelResumoDTO;
import br.com.nhac.backend_nhac.domain.pedido.PedidoRepository;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PainelFinanceiroService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LojaAccessService lojaAccessService;

    public PainelFinanceiroService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository, LojaAccessService lojaAccessService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.lojaAccessService = lojaAccessService;
    }

    @Cacheable(value = "painel", key = "#usuarioLogado.id", unless = "#result == null")
    public PainelResumoDTO obterPainelResumo(br.com.nhac.backend_nhac.domain.usuario.Usuario usuarioLogado) {
        Loja loja = lojaAccessService.obterLojaAcessivel(usuarioLogado);
        if (loja == null) {
            return criarPainelVazio();
        }

        String lojaId = loja.getId();
        boolean lojaAberta = loja.isAberto();

        BigDecimal faturamentoHoje = pedidoRepository.calcularFaturamentoHoje(lojaId);
        if (faturamentoHoje == null) faturamentoHoje = BigDecimal.ZERO;

        long pedidosPendentes = pedidoRepository.contarPedidosPorStatus(lojaId, StatusPedido.PENDENTE);
        long pedidosPagos = pedidoRepository.contarPedidosPorStatus(lojaId, StatusPedido.PAGO);
        long pedidosPreparando = pedidoRepository.contarPedidosPorStatus(lojaId, StatusPedido.PREPARANDO);
        long pedidosSaiuEntrega = pedidoRepository.contarPedidosPorStatus(lojaId, StatusPedido.SAIU_ENTREGA);
        long pedidosEntregues = pedidoRepository.contarPedidosPorStatus(lojaId, StatusPedido.ENTREGUE);
        long pedidosCancelados = pedidoRepository.contarPedidosPorStatus(lojaId, StatusPedido.CANCELADO);

        Instant seteDiasAtras = Instant.now().minus(7, ChronoUnit.DAYS);
        BigDecimal faturamentoUltimos7Dias = pedidoRepository.calcularFaturamentoUltimos7Dias(lojaId, seteDiasAtras);
        if (faturamentoUltimos7Dias == null) faturamentoUltimos7Dias = BigDecimal.ZERO;

        var pedidosRecentesPage = pedidoRepository.encontrarPedidosRecentes(lojaId, PageRequest.of(0, 5));
        List<PainelResumoDTO.PedidoRecenteDTO> pedidosRecentes = pedidosRecentesPage.getContent().stream()
                .map(p -> {
                    String clienteNome = usuarioRepository.findById(p.getUsuarioId())
                            .map(u -> u.getNome())
                            .orElse("Cliente removido");
                    return new PainelResumoDTO.PedidoRecenteDTO(
                            p.getId(),
                            clienteNome,
                            p.getValorTotal(),
                            p.getStatus().name(),
                            p.getCriadoEm()
                    );
                })
                .collect(Collectors.toList());

        return new PainelResumoDTO(
                lojaAberta,
                faturamentoHoje,
                pedidosPendentes,
                pedidosPagos,
                pedidosPreparando,
                pedidosSaiuEntrega,
                pedidosEntregues,
                pedidosCancelados,
                faturamentoUltimos7Dias,
                pedidosRecentes
        );
    }

    @Cacheable(value = "financeiro", key = "#usuarioLogado.id + ':' + #periodoDias", unless = "#result == null")
    public FinanceiroResumoDTO obterFinanceiroResumo(br.com.nhac.backend_nhac.domain.usuario.Usuario usuarioLogado, int periodoDias) {
        Loja loja = lojaAccessService.obterLojaAcessivel(usuarioLogado);
        if (loja == null) {
            return criarFinanceiroVazio();
        }

        String lojaId = loja.getId();
        Instant dataInicio = Instant.now().minus(periodoDias, ChronoUnit.DAYS);

        BigDecimal faturamentoTotal = pedidoRepository.calcularFaturamentoTotal(lojaId);
        if (faturamentoTotal == null) faturamentoTotal = BigDecimal.ZERO;

        long totalPedidos = pedidoRepository.contarPedidosEntregues(lojaId);
        BigDecimal ticketMedio = totalPedidos > 0 ? faturamentoTotal.divide(BigDecimal.valueOf(totalPedidos), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

        FinanceiroResumoDTO.ResumoFinanceiro resumo = new FinanceiroResumoDTO.ResumoFinanceiro(faturamentoTotal, totalPedidos, ticketMedio);

        List<Object[]> faturamentoPorDiaRaw = pedidoRepository.calcularFaturamentoPorDia(lojaId, dataInicio);
        List<FinanceiroResumoDTO.DadoFaturamentoDiario> faturamentoPorDia = faturamentoPorDiaRaw.stream()
                .map(row -> {
                    LocalDate data = ((java.sql.Date) row[0]).toLocalDate();
                    BigDecimal valor = (BigDecimal) row[1];
                    return new FinanceiroResumoDTO.DadoFaturamentoDiario(data, valor != null ? valor : BigDecimal.ZERO);
                })
                .collect(Collectors.toList());

        List<Object[]> vendasPorCategoriaRaw = pedidoRepository.calcularVendasPorCategoria(lojaId);
        Map<String, BigDecimal> vendasPorCategoria = vendasPorCategoriaRaw.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> { BigDecimal v = (BigDecimal) row[1]; return v != null ? v : BigDecimal.ZERO; }
                ));

        List<Object[]> vendasPorFormaPagamentoRaw = pedidoRepository.calcularVendasPorFormaPagamento(lojaId);
        Map<String, BigDecimal> vendasPorFormaPagamento = vendasPorFormaPagamentoRaw.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> { BigDecimal v = (BigDecimal) row[1]; return v != null ? v : BigDecimal.ZERO; }
                ));

        FinanceiroResumoDTO.ProdutoMaisVendido produtoMaisVendido = null;
        List<Object[]> produtoMaisVendidoRaw = pedidoRepository.encontrarProdutoMaisVendido(lojaId, PageRequest.of(0, 1));
        if (!produtoMaisVendidoRaw.isEmpty()) {
            Object[] row = produtoMaisVendidoRaw.get(0);
            produtoMaisVendido = new FinanceiroResumoDTO.ProdutoMaisVendido(
                    (String) row[0],
                    (String) row[1],
                    ((Number) row[2]).longValue(),
                    (BigDecimal) row[3]
            );
        }

        List<Object[]> faturamentoPorHoraRaw = pedidoRepository.calcularFaturamentoPorHora(lojaId);
        Map<Integer, BigDecimal> faturamentoPorHora = faturamentoPorHoraRaw.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(),
                        row -> { BigDecimal v = (BigDecimal) row[1]; return v != null ? v : BigDecimal.ZERO; }
                ));

        return new FinanceiroResumoDTO(
                resumo,
                faturamentoPorDia,
                vendasPorCategoria,
                vendasPorFormaPagamento,
                produtoMaisVendido,
                faturamentoPorHora
        );
    }

    private PainelResumoDTO criarPainelVazio() {
        return new PainelResumoDTO(false, BigDecimal.ZERO, 0, 0, 0, 0, 0, 0, BigDecimal.ZERO, Collections.emptyList());
    }

    private FinanceiroResumoDTO criarFinanceiroVazio() {
        return new FinanceiroResumoDTO(
                new FinanceiroResumoDTO.ResumoFinanceiro(BigDecimal.ZERO, 0, BigDecimal.ZERO),
                Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                null,
                Collections.emptyMap()
        );
    }

    // Records internos para simplificar (apenas DadoFaturamentoDiario e ProdutoMaisVendido foram removidos, pois agora usamos os do DTO)
}
