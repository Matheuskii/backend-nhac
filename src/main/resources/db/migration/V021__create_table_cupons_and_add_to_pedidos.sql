CREATE TABLE tb_cupons (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    tipo_desconto VARCHAR(20) NOT NULL,
    valor_desconto DECIMAL(10,2) NOT NULL,
    valor_minimo_pedido DECIMAL(10,2),
    data_validade TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    limite_usos INT,
    usos_atuais INT DEFAULT 0,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE tb_pedidos ADD COLUMN cupom_id VARCHAR(50);
ALTER TABLE tb_pedidos ADD CONSTRAINT fk_pedidos_cupom FOREIGN KEY (cupom_id) REFERENCES tb_cupons(id);
