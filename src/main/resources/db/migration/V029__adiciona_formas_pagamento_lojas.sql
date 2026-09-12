-- ==========================================
-- V029__adiciona_formas_pagamento_lojas.sql
-- ==========================================
-- Parte B4: Adiciona formas de pagamento à loja
-- Cada campo representa uma forma de pagamento que a loja aceita
-- Defaults definidos para não quebrar comportamento existente (todas as formas habilitadas exceto VA/VR)

ALTER TABLE tb_lojas ADD COLUMN aceita_dinheiro BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE tb_lojas ADD COLUMN aceita_credito BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE tb_lojas ADD COLUMN aceita_debito BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE tb_lojas ADD COLUMN aceita_pix BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE tb_lojas ADD COLUMN aceita_vale_refeicao BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tb_lojas ADD COLUMN aceita_vale_alimentacao BOOLEAN NOT NULL DEFAULT FALSE;
