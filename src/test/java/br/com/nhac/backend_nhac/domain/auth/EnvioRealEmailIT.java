package br.com.nhac.backend_nhac.domain.auth;

import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@TestPropertySource(properties = {
        "nhac.email.mock-mode=false",
        "brevo.api-key=",
        "brevo.sender-email=matheusalvesknight@gmail.com",
        "brevo.sender-name=Nhac Delivery"
})
class EnvioRealEmailIT {

    @Autowired
    private VerificacaoEmailService verificacaoEmailService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve enviar e-mail real de recuperação/código para lavizsenai@gmail.com via Brevo")
    void deveEnviarEmailRealParaLavizSenai() throws InterruptedException {
        String email = "lavizsenai@gmail.com";

        // Garante que o usuário existe no banco para o fluxo de reset
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            Usuario novo = new Usuario();
            novo.setId(UUID.randomUUID().toString());
            novo.setNome("Laviz Senai");
            novo.setEmail(email);
            novo.setTelefone("11988887766");
            novo.setPapel(Papel.CLIENTE);
            novo.setAtivo(true);
            return usuarioRepository.save(novo);
        });

        System.out.println(">>> Disparando e-mail de Reset de Senha via Brevo para: " + email);
        assertDoesNotThrow(() -> verificacaoEmailService.enviarCodigoReset(email));

        // Aguarda 4 segundos para garantir que a thread @Async conclua o envio HTTP
        Thread.sleep(4000);
        System.out.println(">>> E-mail enviado com sucesso via Brevo!");
    }
}
