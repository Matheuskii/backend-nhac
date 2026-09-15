-- ==========================================
-- V037__cria_tabelas_entregador_e_entrega.sql
-- ==========================================
-- Criação da estrutura para entregadores (motoboys), gestão de status operacional,
-- localização em tempo real, ofertas de despacho e vínculo com pedidos.

CREATE TABLE tb_entregadores (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    usuario_id VARCHAR(50) NOT NULL UNIQUE,
    cnh VARCHAR(30) NOT NULL,
    placa_veiculo VARCHAR(20) NOT NULL,
    tipo_veiculo VARCHAR(20) NOT NULL DEFAULT 'MOTO',
    status_operacional VARCHAR(30) NOT NULL DEFAULT 'OFFLINE',
    latitude_atual DOUBLE NULL,
    longitude_atual DOUBLE NULL,
    ultima_atualizacao_localizacao TIMESTAMP NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_entregadores_usuario FOREIGN KEY (usuario_id) REFERENCES tb_usuarios(id)
);

CREATE TABLE tb_ofertas_entrega (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    pedido_id VARCHAR(50) NOT NULL,
    entregador_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_em TIMESTAMP NOT NULL,
    CONSTRAINT fk_ofertas_pedido FOREIGN KEY (pedido_id) REFERENCES tb_pedidos(id),
    CONSTRAINT fk_ofertas_entregador FOREIGN KEY (entregador_id) REFERENCES tb_entregadores(id)
);

CREATE INDEX idx_entregadores_status_op ON tb_entregadores (status_operacional, ativo);
CREATE INDEX idx_ofertas_entregador_status ON tb_ofertas_entrega (entregador_id, status);
CREATE INDEX idx_ofertas_pedido_status ON tb_ofertas_entrega (pedido_id, status);

ALTER TABLE tb_pedidos
    ADD COLUMN entregador_id VARCHAR(50) NULL,
    ADD COLUMN entrega_latitude DOUBLE NULL,
    ADD COLUMN entrega_longitude DOUBLE NULL,
    ADD CONSTRAINT fk_pedidos_entregador FOREIGN KEY (entregador_id) REFERENCES tb_entregadores(id);
