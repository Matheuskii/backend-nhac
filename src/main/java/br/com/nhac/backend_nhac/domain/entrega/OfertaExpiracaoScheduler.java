package br.com.nhac.backend_nhac.domain.entrega;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Varre as ofertas PENDENTES vencidas e as marca como EXPIRADA.
 *
 * O repositório já tinha findByStatusAndExpiraEmBefore desde a V037, mas
 * ninguém chamava: as ofertas nunca respondidas ficavam PENDENTE pra sempre.
 * O listarOfertasPendentes até filtrava com isExpirada() em memória, então o
 * motoboy não via lixo — mas a tabela crescia com linhas eternamente pendentes
 * e qualquer consulta por status ficava errada.
 *
 * Roda a cada 30s: o prazo da oferta é 45s, então uma oferta vencida vira
 * EXPIRADA em no máximo ~75s.
 */
@Component
public class OfertaExpiracaoScheduler {

    private static final Logger log = LoggerFactory.getLogger(OfertaExpiracaoScheduler.class);

    private final DespachoService despachoService;

    public OfertaExpiracaoScheduler(DespachoService despachoService) {
        this.despachoService = despachoService;
    }

    @Scheduled(fixedDelay = 30_000L, initialDelay = 30_000L)
    public void expirarOfertas() {
        try {
            int total = despachoService.expirarOfertasVencidas();
            if (total > 0) {
                log.debug("{} oferta(s) de entrega marcada(s) como EXPIRADA.", total);
            }
        } catch (Exception e) {
            // Nunca deixa a exceção subir: o agendador do Spring desliga a task
            // depois de um erro não tratado dependendo da configuração.
            log.error("Falha ao expirar ofertas de entrega: {}", e.getMessage());
        }
    }
}
