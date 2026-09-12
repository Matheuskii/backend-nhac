CREATE TABLE tb_produtos (
                             id VARCHAR(50) NOT NULL,
                             loja_id VARCHAR(50) NOT NULL,
                             nome VARCHAR(100) NOT NULL,
                             descricao TEXT,
                             preco DECIMAL(10,2) NOT NULL,
                             categoria_menu VARCHAR(50) NOT NULL,
                             imagem_url TEXT,
                             is_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                             criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             peso VARCHAR(20),
                             percentual_desconto INT,

                             PRIMARY KEY (id),
                             CONSTRAINT fk_produto_loja FOREIGN KEY (loja_id) REFERENCES tb_lojas(id)
);