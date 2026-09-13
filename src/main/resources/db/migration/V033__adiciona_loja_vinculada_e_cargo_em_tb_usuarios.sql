-- Suporte a funcionários: um funcionário é um tb_usuarios comum (papel = FUNCIONARIO)
-- vinculado a uma loja que ele NÃO é dono. O dono continua identificado por
-- tb_lojas.usuario_id (índice único da V027).
-- A tela de funcionários precisa de "Data de Cadastro" na listagem; a coluna
-- criado_em já existe em tb_usuarios desde uma migration anterior.
ALTER TABLE tb_usuarios
    ADD COLUMN loja_vinculada_id VARCHAR(50) NULL,
    ADD COLUMN cargo VARCHAR(30) NULL;

UPDATE tb_usuarios SET criado_em = NOW() WHERE criado_em IS NULL;

ALTER TABLE tb_usuarios
    ADD CONSTRAINT fk_usuarios_loja_vinculada
    FOREIGN KEY (loja_vinculada_id) REFERENCES tb_lojas(id);

CREATE INDEX idx_usuarios_loja_vinculada_id ON tb_usuarios (loja_vinculada_id);
