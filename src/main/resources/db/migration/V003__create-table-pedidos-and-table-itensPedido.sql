CREATE TABLE tb_pedidos (
                            id VARCHAR(50) NOT NULL PRIMARY KEY,
                            usuario_id VARCHAR(100) NOT NULL,
                            loja_id VARCHAR(50) NOT NULL,

                            valor_total DECIMAL(10,2) NOT NULL,
                            taxa_frete DECIMAL(10,2) NOT NULL,
                            forma_pagamento VARCHAR(50) NOT NULL DEFAULT 'não informado',
                            observacao TEXT,
                            status VARCHAR(30) NOT NULL,

                            entrega_rua VARCHAR(150),
                            entrega_numero VARCHAR(20),
                            entrega_bairro VARCHAR(100),
                            entrega_cidade VARCHAR(100),
                            entrega_estado VARCHAR(2),
                            entrega_cep VARCHAR(20),
                            entrega_complemento VARCHAR(100),

                            criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_pedido_loja FOREIGN KEY (loja_id) REFERENCES tb_lojas(id)
);

CREATE TABLE tb_itens_pedido (
                                 id VARCHAR(50) NOT NULL PRIMARY KEY,
                                 pedido_id VARCHAR(50) NOT NULL,
                                 produto_id VARCHAR(50) NOT NULL,

                                 nome_historico VARCHAR(100) NOT NULL,
                                 imagem_url_historica TEXT,
                                 preco_historico DECIMAL(10,2) NOT NULL,
                                 quantidade INT NOT NULL,

                                 CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id) REFERENCES tb_pedidos(id) ON DELETE CASCADE,

                                 CONSTRAINT fk_item_produto FOREIGN KEY (produto_id) REFERENCES tb_produtos(id)
);