-- ==========================================
-- V028__adiciona_campos_endereco_e_operacionais_lojas.sql
-- ==========================================
-- Parte B2: Adiciona bairro e complemento ao endereço da loja
-- Parte B3: Adiciona campos de dados operacionais (entrega própria, retirada no local, raio de entrega)

-- B2: Campos de endereço (bairro e complemento)
-- Bairro é obrigatório com valor padrão vazio para retrocompatibilidade
ALTER TABLE tb_lojas ADD COLUMN end_bairro VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE tb_lojas ADD COLUMN end_complemento VARCHAR(100) NULL;

-- B3: Campos de dados operacionais
-- entrega_propria: default TRUE para não quebrar comportamento existente
ALTER TABLE tb_lojas ADD COLUMN entrega_propria BOOLEAN NOT NULL DEFAULT TRUE;

-- retirada_no_local: default FALSE para não quebrar comportamento existente  
ALTER TABLE tb_lojas ADD COLUMN retirada_no_local BOOLEAN NOT NULL DEFAULT FALSE;

-- raio_entrega_km: nullable (null = ilimitado/não se aplica)
ALTER TABLE tb_lojas ADD COLUMN raio_entrega_km DECIMAL(6,2) NULL;
