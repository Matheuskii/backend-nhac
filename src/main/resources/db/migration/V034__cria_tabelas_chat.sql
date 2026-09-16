-- Chat entre cliente e loja (item 7 da spec — arquitetura decidida: WebSocket
-- em vez de polling). Uma conversa por par (loja, cliente); mensagens dentro
-- dela, com o lado que enviou (CLIENTE ou LOJA) e status de leitura por lado.
CREATE TABLE tb_conversas (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    loja_id VARCHAR(50) NOT NULL,
    cliente_id VARCHAR(50) NOT NULL,
    criada_em TIMESTAMP NOT NULL,
    ultima_mensagem_em TIMESTAMP NOT NULL,
    ultima_mensagem_preview VARCHAR(255) NULL,
    nao_lidas_loja INT NOT NULL DEFAULT 0,
    nao_lidas_cliente INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_conversas_loja FOREIGN KEY (loja_id) REFERENCES tb_lojas(id),
    CONSTRAINT uq_conversas_loja_cliente UNIQUE (loja_id, cliente_id)
);

CREATE TABLE tb_mensagens (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    conversa_id VARCHAR(50) NOT NULL,
    remetente_tipo VARCHAR(10) NOT NULL, -- CLIENTE ou LOJA
    remetente_usuario_id VARCHAR(50) NOT NULL, -- id de quem realmente digitou (cliente, dono ou funcionário)
    conteudo TEXT NOT NULL,
    enviada_em TIMESTAMP NOT NULL,
    CONSTRAINT fk_mensagens_conversa FOREIGN KEY (conversa_id) REFERENCES tb_conversas(id)
);

CREATE INDEX idx_conversas_loja_id ON tb_conversas (loja_id);
CREATE INDEX idx_mensagens_conversa_id_enviada_em ON tb_mensagens (conversa_id, enviada_em);
