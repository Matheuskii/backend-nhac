package br.com.nhac.backend_nhac.domain.entrega;

import br.com.nhac.backend_nhac.domain.pedido.PedidoPreparandoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Liga a mudança de status do pedido ao despacho automático para motoboys.
 *
 * AFTER_COMMIT: o despacho cria ofertas que apontam para o pedido, então ele
 * precisa estar commitado antes. REQUIRES_NEW porque, nessa fase, não existe
 * mais transação ativa — sem isso, o save das ofertas rodaria em auto-commit
 * linha a linha.
 *
 * O try/catch é intencional: "nenhum entregador online por perto" e "loja sem
 * coordenadas GPS" são situações normais de operação, não erros. A loja tem
 * que conseguir aceitar o pedido de qualquer jeito, e o lojista sempre pode
 * reenviar as ofertas depois via POST /api/v1/entregas/despachar/{pedidoId}.
 */
@Component
public class DespachoEventListener {

    private static final Logger log = LoggerFactory.getLogger(DespachoEventListener.class);

    private final DespachoService despachoService;

    public DespachoEventListener(DespachoService despachoService) {
        this.despachoService = despachoService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoEntrarEmPreparo(PedidoPreparandoEvent evento) {
        try {
            var ofertas = despachoService.despacharPedido(evento.pedidoId());
            log.info("Despacho automático do pedido {}: {} oferta(s) enviada(s).",
                    evento.pedidoId(), ofertas.size());
        } catch (Exception e) {
            log.warn("Despacho automático do pedido {} não foi concluído: {}",
                    evento.pedidoId(), e.getMessage());
        }
    }
}
