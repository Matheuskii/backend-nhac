-- ==========================================
-- V027__criar_unique_index_lojas_usuario.sql
-- ==========================================
-- Reconciliação de lojas órfãs (sem usuario_id) da seed V998
-- Estas lojas são dados de demonstração e não têm dono definido.
-- Para permitir o unique index, vamos remover essas lojas órfãs.

DELETE FROM tb_lojas WHERE usuario_id IS NULL;

-- Dropar o índice comum criado em V026
ALTER TABLE tb_lojas DROP INDEX idx_lojas_usuario_id;

-- Criar unique index para garantir que um usuário tenha no máximo uma loja
-- Em MariaDB, múltiplos NULLs são permitidos em índices únicos (comportamento desejado)
CREATE UNIQUE INDEX uk_lojas_usuario_id ON tb_lojas (usuario_id);
