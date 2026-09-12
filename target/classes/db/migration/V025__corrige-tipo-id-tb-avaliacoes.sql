-- =====================================================
-- V023: Corrige tipo do ID de tb_avaliacoes
-- Bug: Entidade Avaliacao.java usa @GeneratedValue(strategy = GenerationType.UUID)
-- que gera String UUID, mas a V010 criou 'id BIGINT AUTO_INCREMENT'.
-- Como não há dados reais na tabela (a feature nunca funcionou em produção
-- devido ao bug da coluna 'criado_em'/'data_criacao'), é seguro recriar.
-- =====================================================

-- 1. Remove triggers dependentes
DROP TRIGGER IF EXISTS trg_avaliacao_insert_atualiza_loja;
DROP TRIGGER IF EXISTS trg_avaliacao_update_atualiza_loja;
DROP TRIGGER IF EXISTS trg_avaliacao_delete_atualiza_loja;

-- 2. Dropa a tabela antiga
DROP TABLE IF EXISTS tb_avaliacoes;

-- 3. Recria com id VARCHAR(36) compatível com UUID
CREATE TABLE tb_avaliacoes (
    id VARCHAR(36) PRIMARY KEY,
    pedido_id VARCHAR(50) NOT NULL,
    usuario_id VARCHAR(50) NOT NULL,
    loja_id VARCHAR(50) NOT NULL,
    nota TINYINT NOT NULL,
    comentario TEXT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_avaliacao_pedido UNIQUE (pedido_id),
    CONSTRAINT chk_avaliacao_nota_valida CHECK (nota BETWEEN 1 AND 5),
    CONSTRAINT fk_avaliacao_pedido FOREIGN KEY (pedido_id) REFERENCES tb_pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacao_usuario FOREIGN KEY (usuario_id) REFERENCES tb_usuarios(id),
    CONSTRAINT fk_avaliacao_loja FOREIGN KEY (loja_id) REFERENCES tb_lojas(id)
);

CREATE INDEX idx_avaliacoes_loja_id ON tb_avaliacoes (loja_id);

-- 4. Recria triggers de atualização automática de rating na loja

CREATE TRIGGER trg_avaliacao_insert_atualiza_loja
    AFTER INSERT ON tb_avaliacoes
    FOR EACH ROW
BEGIN
    UPDATE tb_lojas
    SET avaliacao_media = (SELECT ROUND(AVG(nota), 1) FROM tb_avaliacoes WHERE loja_id = NEW.loja_id),
        total_avaliacoes = (SELECT COUNT(*) FROM tb_avaliacoes WHERE loja_id = NEW.loja_id)
    WHERE id = NEW.loja_id;
END;

CREATE TRIGGER trg_avaliacao_update_atualiza_loja
    AFTER UPDATE ON tb_avaliacoes
    FOR EACH ROW
BEGIN
    IF OLD.nota <> NEW.nota THEN
        UPDATE tb_lojas
        SET avaliacao_media = (SELECT ROUND(AVG(nota), 1) FROM tb_avaliacoes WHERE loja_id = NEW.loja_id)
        WHERE id = NEW.loja_id;
    END IF;
END;

CREATE TRIGGER trg_avaliacao_delete_atualiza_loja
    AFTER DELETE ON tb_avaliacoes
    FOR EACH ROW
BEGIN
    UPDATE tb_lojas
    SET avaliacao_media = COALESCE((SELECT ROUND(AVG(nota), 1) FROM tb_avaliacoes WHERE loja_id = OLD.loja_id), 0.0),
        total_avaliacoes = (SELECT COUNT(*) FROM tb_avaliacoes WHERE loja_id = OLD.loja_id)
    WHERE id = OLD.loja_id;
END;
