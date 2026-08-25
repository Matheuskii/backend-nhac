package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.auth.CodigoVerificacaoEmail;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import br.com.nhac.backend_nhac.repositories.CodigoVerificacaoEmailRepository;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificacaoEmailService {

    private final CodigoVerificacaoEmailRepository codigoRepository;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;

    private static final int TEMPO_EXPIRACAO_MINUTOS = 15;
    private static final int MAX_TENTATIVAS = 3;

    @Transactional
    public void enviarCodigoReset(String email) {
        email = email.trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        String nomeUsuario = (usuario != null && usuario.getNome() != null) ? usuario.getNome() : "Usuário";

        LocalDateTime agora = LocalDateTime.now();
        codigoRepository.inativarCodigosAtivosPorEmail(email);

        String codigo = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        CodigoVerificacaoEmail novoCodigo = CodigoVerificacaoEmail.builder()
                .email(email)
                .codigo(codigo)
                .dataExpiracao(agora.plusMinutes(TEMPO_EXPIRACAO_MINUTOS))
                .tentativas(0)
                .utilizado(false)
                .build();

        codigoRepository.save(novoCodigo);

        String assunto = "Recuperação de Senha - Nhac Delivery";
        
        String htmlConteudo = """
                <!DOCTYPE html>
                <html lang="pt-BR" xmlns="http://www.w3.org/1999/xhtml">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <title>Redefinir senha · Nhac</title>
                <!--[if mso]>
                <noscript>
                <xml>
                <o:OfficeDocumentSettings>
                <o:PixelsPerInch>96</o:PixelsPerInch>
                </o:OfficeDocumentSettings>
                </xml>
                </noscript>
                <![endif]-->
                <style>
                body, table, td { font-family: 'Roboto', Arial, Helvetica, sans-serif; }
                body { margin:0; padding:0; background-color:#FFE7E5; -webkit-text-size-adjust:100%%; -ms-text-size-adjust:100%%; }
                table { border-collapse:collapse; }
                img { border:0; line-height:100%%; outline:none; text-decoration:none; }
                a { text-decoration:none; }
                @media screen and (max-width: 600px) {
                    .email-container { width:100%% !important; }
                    .fluid-padding { padding-left:20px !important; padding-right:20px !important; }
                }
                </style>
                </head>
                <body style="margin:0; padding:0; background-color:#FFE7E5;">
                <div style="display:none; max-height:0; overflow:hidden; opacity:0; mso-hide:all;">
                Recebemos uma solicitação para redefinir a senha da sua conta Nhac. O código de verificação expira em 15 minutos.
                </div>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color:#FFE7E5;">
                <tr>
                <td align="center" style="padding:40px 16px;">
                <table role="presentation" class="email-container" width="600" cellpadding="0" cellspacing="0" border="0" style="width:600px; max-width:600px;">
                <tr>
                <td align="center" style="padding-bottom:28px;">
                <table role="presentation" cellpadding="0" cellspacing="0" border="0">
                <tr>
                <td style="font-size:26px; font-weight:700; color:#FF6961; font-family:Arial, Helvetica, sans-serif;">
                Nhac
                </td>
                </tr>
                </table>
                </td>
                </tr>
                <tr>
                <td style="background-color:#FFFFFF; border-radius:20px; padding:40px;" class="fluid-padding">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                <td align="center" style="padding-bottom:24px;">
                <table role="presentation" cellpadding="0" cellspacing="0" border="0">
                <tr>
                <td width="64" height="64" align="center" valign="middle" style="background-color:#FFEBD9; border-radius:50%%; width:64px; height:64px; font-size:26px; line-height:64px;">
                          🔒
                        </td>
                </tr>
                </table>
                </td>
                </tr>
                <tr>
                <td align="center" style="padding-bottom:8px;">
                <span style="font-size:22px; font-weight:700; color:#5D201C; font-family:Arial, Helvetica, sans-serif;">
                Redefinir sua senha
                    </span>
                </td>
                </tr>
                <tr>
                <td align="center" style="padding-bottom:28px;">
                <span style="font-size:14px; line-height:22px; color:#8A8A8A; font-family:Arial, Helvetica, sans-serif;">
                Olá, %s. Recebemos uma solicitação para redefinir a<br>
        senha da sua conta Nhac. Utilize o código abaixo no aplicativo para continuar.
                    </span>
                </td>
                </tr>
                <tr>
                <td align="center" style="padding-bottom:24px;">
                <table role="presentation" cellpadding="0" cellspacing="0" border="0">
                <tr>
                <td align="center" style="background-color:#f9f9f9; border: 1px dashed #cccccc; border-radius:8px; padding:20px 40px;">
                <span style="display:inline-block; font-size:32px; font-weight:700; letter-spacing: 4px; color:#FF6961; font-family:monospace, Arial, Helvetica, sans-serif;">
                %s
                </span>
                </td>
                </tr>
                </table>
                </td>
                </tr>
                <tr>
                <td align="center" style="padding-bottom:24px;">
                <span style="font-size:12.5px; color:#C9BCBC; font-family:Arial, Helvetica, sans-serif;">
                Este código expira em %d minutos
                </span>
                </td>
                </tr>
                </table>
                </td>
                </tr>
                <tr>
                <td style="padding:24px 8px 0;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color:#FFF6F5; border-radius:14px;">
                <tr>
                <td style="padding:16px 20px;">
                <span style="font-size:12.5px; line-height:19px; color:#5D201C; font-family:Arial, Helvetica, sans-serif;">
                      🔒 <strong>Não foi você?</strong> Se você não solicitou a redefinição de senha, pode ignorar este e-mail com segurança — sua senha atual continua válida.
                </span>
                </td>
                </tr>
                </table>
                </td>
                </tr>
                <tr>
                <td align="center" style="padding:32px 20px 0;">
                <span style="font-size:11.5px; color:#C9BCBC; font-family:Arial, Helvetica, sans-serif;">
                Enviado por Nhac · Este é um e-mail automático, não responda.
                </span>
                <br><br>
                <span style="font-size:11.5px; color:#C9BCBC; font-family:Arial, Helvetica, sans-serif;">
                © %d Nhac. Todos os direitos reservados.
              </span>
                </td>
                </tr>
                </table>
                </td>
                </tr>
                </table>
                </body>
                </html>
                """.formatted(nomeUsuario, codigo, TEMPO_EXPIRACAO_MINUTOS, java.time.Year.now().getValue());

        emailService.enviarEmailHtml(email, assunto, htmlConteudo);
    }

    @Transactional(noRollbackFor = RegraDeNegocioException.class)
    public void verificarCodigoValido(String email, String codigoDigitado) {
        email = email.trim().toLowerCase();
        LocalDateTime agora = LocalDateTime.now();

        CodigoVerificacaoEmail registro = codigoRepository
                .findTopByEmailAndUtilizadoFalseAndDataExpiracaoAfterOrderByCriadoEmDesc(email, agora)
                .orElseThrow(() -> new RegraDeNegocioException("Código expirado ou não encontrado. Solicite um novo código."));

        if (registro.getTentativas() >= MAX_TENTATIVAS) {
            registro.setUtilizado(true);
            codigoRepository.save(registro);
            throw new RegraDeNegocioException("Limite de tentativas excedido para este código. Solicite um novo.");
        }

        if (!registro.getCodigo().equals(codigoDigitado.trim())) {
            registro.setTentativas(registro.getTentativas() + 1);
            codigoRepository.save(registro);
            throw new RegraDeNegocioException("Código de verificação inválido.");
        }

        registro.setUtilizado(true);
        codigoRepository.save(registro);
    }
}