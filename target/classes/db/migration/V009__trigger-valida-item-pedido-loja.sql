
CREATE TRIGGER trg_valida_item_pedido_mesma_loja
    BEFORE INSERT ON tb_itens_pedido
    FOR EACH ROW
BEGIN
    DECLARE loja_do_pedido VARCHAR(50);
    DECLARE loja_do_produto VARCHAR(50);

    SELECT loja_id INTO loja_do_pedido FROM tb_pedidos WHERE id = NEW.pedido_id;
    SELECT loja_id INTO loja_do_produto FROM tb_produtos WHERE id = NEW.produto_id;

    IF loja_do_pedido IS NOT NULL AND loja_do_produto IS NOT NULL AND loja_do_pedido <> loja_do_produto THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Produto não pertence à mesma loja do pedido.';
    END IF;
END;
