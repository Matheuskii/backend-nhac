-- ==========================================
-- V036__adiciona-version-em-tb-conversas.sql
-- ==========================================
-- Adiciona coluna de controle de versão (optimistic lock) na tabela de
-- conversas do chat. Sem ela, duas mensagens enviadas simultaneamente na
-- mesma conversa fazem UPDATE em nao_lidas_* e a última transação
-- sobrescreve a anterior — o contador fica errado.
--
-- Com @Version no Hibernate, a segunda transação recebe
-- OptimisticLockException e o Spring retenta automaticamente, garantindo
-- o incremento correto.
--
-- default 0 garante que conversas já existentes começam com uma versão
-- válida (o Hibernate só incrementa a partir daí).

ALTER TABLE tb_conversas
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;