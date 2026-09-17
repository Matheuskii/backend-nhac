CREATE TABLE tb_usuario_segue_loja (
    usuario_id VARCHAR(50) NOT NULL,
    loja_id VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, loja_id),
    CONSTRAINT fk_usu_segue FOREIGN KEY (usuario_id) REFERENCES tb_usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_loja_seguida FOREIGN KEY (loja_id) REFERENCES tb_lojas(id) ON DELETE CASCADE
);
