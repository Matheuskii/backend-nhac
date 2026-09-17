-- Corrige o efeito colateral da V031.
--
-- A V031 adicionou email_verificado com DEFAULT FALSE e não fez backfill.
-- Como o AuthController.login passou a exigir isEmailVerificado(), TODA conta
-- criada antes da V031 ficou trancada fora do app com a mensagem
-- "Verifique seu e-mail antes de fazer login." — sem nenhuma rota de saída,
-- porque /auth/enviar-codigo-cadastro recusa e-mail que já existe.
--
-- Critério: quem tem e-mail no cadastro entrou por um fluxo em que o e-mail
-- já era confiável (registro próprio ou Google). Contas de SMS têm email NULL
-- e não usam /auth/login, então continuam FALSE sem prejuízo.

UPDATE tb_usuarios
SET email_verificado = TRUE
WHERE email IS NOT NULL
  AND email <> ''
  AND email_verificado = FALSE;