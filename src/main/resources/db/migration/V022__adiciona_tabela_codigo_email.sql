CREATE TABLE tb_codigos_verificacao_email (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    codigo VARCHAR(6) NOT NULL,
    data_expiracao TIMESTAMP NOT NULL,
    tentativas INT DEFAULT 0,
    utilizado BOOLEAN DEFAULT FALSE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_codigos_email ON tb_codigos_verificacao_email(email, utilizado);
