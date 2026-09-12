
CREATE TABLE tb_pedidos_status_historico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id VARCHAR(50) NOT NULL,
    status_anterior VARCHAR(30) NOT NULL,
    status_novo VARCHAR(30) NOT NULL,
    alterado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historico_pedido FOREIGN KEY (pedido_id) REFERENCES tb_pedidos(id) ON DELETE CASCADE
);

CREATE INDEX idx_historico_pedido_id ON tb_pedidos_status_historico (pedido_id);

CREATE TRIGGER trg_pedido_status_historico
    AFTER UPDATE ON tb_pedidos
    FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO tb_pedidos_status_historico (pedido_id, status_anterior, status_novo)
        VALUES (NEW.id, OLD.status, NEW.status);
    END IF;
END;
