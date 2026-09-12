CREATE TABLE tb_lojas (
                          id VARCHAR(50) PRIMARY KEY,
                          nome VARCHAR(100) NOT NULL,
                          descricao TEXT,
                          categoria VARCHAR(50),
                          imagem_url TEXT,
                          is_aberto BOOLEAN DEFAULT TRUE,

                          avaliacao_media DECIMAL(3,1) DEFAULT 0.0,
                          taxa_entrega_base DECIMAL(10,2) DEFAULT 0.00,
                          tempo_entrega_min INT,
                          tempo_entrega_max INT,
                          total_avaliacoes INT DEFAULT 0,

                          end_rua VARCHAR(150),
                          end_numero VARCHAR(20),
                          end_cidade VARCHAR(100),
                          end_estado VARCHAR(2),
                          end_cep VARCHAR(20),

                          geo_lat DECIMAL(10,8),
                          geo_lng DECIMAL(11,8),
                          geo_geohash VARCHAR(20),

                          horario_domingo VARCHAR(20),
                          horario_segunda VARCHAR(20),
                          horario_terca VARCHAR(20),
                          horario_quarta VARCHAR(20),
                          horario_quinta VARCHAR(20),
                          horario_sexta VARCHAR(20),
                          horario_sabado VARCHAR(20)
);