
ALTER TABLE tb_lojas
    ADD COLUMN usuario_id VARCHAR(50) NULL;

ALTER TABLE tb_lojas
    ADD CONSTRAINT fk_loja_usuario FOREIGN KEY (usuario_id) REFERENCES tb_usuarios(id);

CREATE INDEX idx_lojas_usuario_id ON tb_lojas (usuario_id);
