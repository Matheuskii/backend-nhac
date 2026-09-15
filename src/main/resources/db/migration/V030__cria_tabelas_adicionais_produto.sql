-- ==========================================
-- V030__cria_tabelas_adicionais_produto.sql
-- ==========================================
-- Parte B5 (Spec Alinhamento API): Adiciona suporte a adicionais de produto
-- Cria tabelas tb_grupo_adicional e tb_item_adicional para modelar grupos de adicionais
-- e seus itens associados a produtos

CREATE TABLE tb_grupo_adicional (
    id VARCHAR(50) PRIMARY KEY,
    produto_id VARCHAR(50) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    obrigatorio BOOLEAN NOT NULL DEFAULT FALSE,
    minimo INT,
    maximo INT,
    CONSTRAINT fk_grupo_adicional_produto FOREIGN KEY (produto_id) REFERENCES tb_produtos(id) ON DELETE CASCADE
);

CREATE TABLE tb_item_adicional (
    id VARCHAR(50) PRIMARY KEY,
    grupo_adicional_id VARCHAR(50) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_adicional_grupo FOREIGN KEY (grupo_adicional_id) REFERENCES tb_grupo_adicional(id) ON DELETE CASCADE
);

CREATE INDEX idx_grupo_adicional_produto ON tb_grupo_adicional(produto_id);
CREATE INDEX idx_item_adicional_grupo ON tb_item_adicional(grupo_adicional_id);
