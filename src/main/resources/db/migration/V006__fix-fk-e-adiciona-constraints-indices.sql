


ALTER TABLE tb_pedidos
    MODIFY COLUMN usuario_id VARCHAR(50) NOT NULL;

ALTER TABLE tb_pedidos
    ADD CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES tb_usuarios(id);

ALTER TABLE tb_usuarios
    ADD CONSTRAINT uq_usuario_email UNIQUE (email);

CREATE INDEX idx_produtos_nome ON tb_produtos (nome);
CREATE INDEX idx_produtos_categoria_menu ON tb_produtos (categoria_menu);
CREATE INDEX idx_produtos_preco ON tb_produtos (preco);
CREATE INDEX idx_produtos_is_ativo ON tb_produtos (is_ativo);

CREATE INDEX idx_lojas_categoria ON tb_lojas (categoria);
CREATE INDEX idx_lojas_is_aberto ON tb_lojas (is_aberto);

CREATE INDEX idx_pedidos_status ON tb_pedidos (status);

ALTER TABLE tb_produtos
    ADD CONSTRAINT chk_produto_preco_positivo CHECK (preco >= 0),
    ADD CONSTRAINT chk_produto_desconto_valido CHECK (percentual_desconto IS NULL OR percentual_desconto BETWEEN 0 AND 100);

ALTER TABLE tb_pedidos
    ADD CONSTRAINT chk_pedido_valor_total_positivo CHECK (valor_total >= 0),
    ADD CONSTRAINT chk_pedido_taxa_frete_positiva CHECK (taxa_frete >= 0);

ALTER TABLE tb_itens_pedido
    ADD CONSTRAINT chk_item_quantidade_positiva CHECK (quantidade > 0),
    ADD CONSTRAINT chk_item_preco_historico_positivo CHECK (preco_historico >= 0);

ALTER TABLE tb_lojas
    ADD CONSTRAINT chk_loja_avaliacao_media_valida CHECK (avaliacao_media BETWEEN 0 AND 5);
