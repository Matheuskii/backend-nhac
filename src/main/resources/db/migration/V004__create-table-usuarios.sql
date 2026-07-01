CREATE TABLE tb_usuarios (
                             id VARCHAR(50) NOT NULL,
                             nome VARCHAR(100) NOT NULL,
                             email VARCHAR(100) NOT NULL,
                             telefone VARCHAR(20) NOT NULL,
                             imagem_url TEXT,
                             PRIMARY KEY (id)
);

CREATE TABLE tb_enderecos_usuario (
                                      id VARCHAR(50) NOT NULL,
                                      usuario_id VARCHAR(50) NOT NULL,
                                      rua VARCHAR(150) NOT NULL,
                                      numero VARCHAR(20) NOT NULL,
                                      bairro VARCHAR(100) NOT NULL,
                                      cidade VARCHAR(100) NOT NULL,
                                      estado VARCHAR(2) NOT NULL,
                                      cep VARCHAR(9) NOT NULL,
                                      complemento VARCHAR(150),
                                      is_padrao BOOLEAN DEFAULT FALSE NOT NULL,
                                      PRIMARY KEY (id),
                                      CONSTRAINT fk_endereco_usuario FOREIGN KEY (usuario_id) REFERENCES tb_usuarios(id) ON DELETE CASCADE
);