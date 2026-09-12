ALTER TABLE tb_usuarios ADD COLUMN telefone_verificado BOOLEAN DEFAULT FALSE;

CREATE TABLE tb_codigos_verificacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    telefone VARCHAR(20) NOT NULL,
    codigo VARCHAR(6) NOT NULL,
    data_expiracao TIMESTAMP NOT NULL,
    tentativas INT DEFAULT 0,
    utilizado BOOLEAN DEFAULT FALSE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_telefone_expiracao (telefone, data_expiracao)
);
