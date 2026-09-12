ALTER TABLE tb_usuarios ADD COLUMN loja_vinculada_id VARCHAR(50);
ALTER TABLE tb_usuarios ADD COLUMN cargo VARCHAR(50);

ALTER TABLE tb_usuarios ADD CONSTRAINT fk_usuario_loja_vinculada 
    FOREIGN KEY (loja_vinculada_id) REFERENCES tb_lojas(id);

-- Índice para melhorar performance nas consultas por loja
CREATE INDEX idx_usuarios_loja_vinculada ON tb_usuarios(loja_vinculada_id);
