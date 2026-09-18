-- ==========================================================================
-- V039__entrega_coleta_e_chat_entregador.sql
-- ==========================================================================
-- 1) Marcos de tempo da entrega (coleta na loja e baixa no cliente). Hoje o
--    ciclo do motoboy morre em SAIU_ENTREGA: não existe rota pro entregador
--    dar baixa, então o pedido nunca chega em ENTREGUE e o entregador fica
--    preso em EM_ENTREGA pra sempre (nunca mais recebe oferta, porque o
--    despacho só procura quem está ONLINE).
-- 2) Índice pra buscar a entrega ativa / histórico por entregador.
-- 3) Chat: tb_conversas passa a aceitar outro tipo de participante além do
--    cliente (o entregador). A coluna continua chamando cliente_id pra não
--    quebrar o app do cliente nem o painel do lojista, mas agora o par único
--    é (loja, participante, tipo_do_participante).

ALTER TABLE tb_pedidos
    ADD COLUMN coletado_em TIMESTAMP NULL,
    ADD COLUMN entregue_em TIMESTAMP NULL;

CREATE INDEX idx_pedidos_entregador_status ON tb_pedidos (entregador_id, status);

ALTER TABLE tb_conversas
    ADD COLUMN participante_tipo VARCHAR(20) NOT NULL DEFAULT 'CLIENTE';

-- A unique antiga (loja_id, cliente_id) impediria a loja de ter, ao mesmo
-- tempo, uma conversa com o cliente X e outra com o entregador X (caso raro,
-- mas possível: a mesma pessoa pode ser cliente e entregador).
ALTER TABLE tb_conversas DROP INDEX uq_conversas_loja_cliente;

ALTER TABLE tb_conversas
    ADD CONSTRAINT uq_conversas_loja_participante UNIQUE (loja_id, cliente_id, participante_tipo);

CREATE INDEX idx_conversas_loja_tipo ON tb_conversas (loja_id, participante_tipo);
