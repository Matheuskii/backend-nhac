ALTER TABLE tb_pedidos ADD COLUMN idempotency_key VARCHAR(100) UNIQUE;
