-- Suporte a funcionários: um funcionário é um tb_usuarios comum (papel = FUNCIONARIO)
-- vinculado a uma loja que ele NÃO é dono. O dono continua identificado por
-- tb_lojas.usuario_id (índice único da V027).
-- tb_usuarios nunca teve data de criação (diferente de tb_produtos/tb_pedidos).
-- A tela de funcionários precisa de "Data de Cadastro" na listagem, então
-- aproveitamos esta migration pra adicionar de forma genérica (não só p/ funcionário).
ALTER TABLE tb_usuarios
    ADD COLUMN loja_vinculada_id VARCHAR(50) NULL,
    ADD COLUMN cargo VARCHAR(30) NULL,
    ADD COLUMN criado_em TIMESTAMP NULL;

UPDATE tb_usuarios SET criado_em = NOW() WHERE criado_em IS NULL;

ALTER TABLE tb_usuarios
    ADD CONSTRAINT fk_usuarios_loja_vinculada
    FOREIGN KEY (loja_vinculada_id) REFERENCES tb_lojas(id);

CREATE INDEX idx_usuarios_loja_vinculada_id ON tb_usuarios (loja_vinculada_id);
