package br.com.nhac.backend_nhac.domain.pedido;

/**
 * Publicado quando a loja move o pedido para PREPARANDO, ou seja, quando ela
 * aceita o pedido e a comida entra na fila de produção.
 *
 * Consumido em AFTER_COMMIT pelo DespachoEventListener (domain.entrega), que
 * dispara as ofertas para os motoboys próximos. Evento em vez de chamada
 * direta pra não acoplar o domínio de pedido ao de entrega e pra não deixar
 * uma falha no despacho marcar a transação do pedido como rollback-only.
 */
public record PedidoPreparandoEvent(String pedidoId) {
}
