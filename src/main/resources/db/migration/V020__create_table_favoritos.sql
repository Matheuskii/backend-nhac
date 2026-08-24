CREATE TABLE tb_favoritos (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    usuario_id VARCHAR(50) NOT NULL,
    loja_id VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favoritos_usuario FOREIGN KEY (usuario_id) REFERENCES tb_usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_favoritos_loja FOREIGN KEY (loja_id) REFERENCES tb_lojas(id) ON DELETE CASCADE,
    CONSTRAINT uk_usuario_loja UNIQUE (usuario_id, loja_id)
);
