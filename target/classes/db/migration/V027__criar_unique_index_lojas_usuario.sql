DELETE FROM tb_lojas WHERE usuario_id IS NULL;

ALTER TABLE tb_lojas DROP FOREIGN KEY fk_loja_usuario;

ALTER TABLE tb_lojas DROP INDEX idx_lojas_usuario_id;

CREATE UNIQUE INDEX uk_lojas_usuario_id ON tb_lojas (usuario_id);

ALTER TABLE tb_lojas ADD CONSTRAINT fk_loja_usuario
    FOREIGN KEY (usuario_id) REFERENCES tb_usuarios (id);