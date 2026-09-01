package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.auth.CodigoVerificacaoEmail;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.repositories.CodigoVerificacaoEmailRepository;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class VerificacaoEmailServiceTest {

    @Autowired
    private VerificacaoEmailService verificacaoEmailService;

    @Autowired
    private CodigoVerificacaoEmailRepository codigoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    public void testDeadlockEmRequisicoesConcorrentes() throws InterruptedException {
        String email = "deadlock_test@nhac.com.br";
        
        Usuario u = new Usuario();
        u.setId(java.util.UUID.randomUUID().toString());
        u.setNome("Deadlock Tester");
        u.setEmail(email);
        u.setTelefone("99999999999");
        usuarioRepository.save(u);

        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    verificacaoEmailService.salvarNovoCodigo(email);
                } catch (Exception e) {
                    fail("Falhou com exceção de concorrência: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); 
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        List<CodigoVerificacaoEmail> codigos = codigoRepository.findAll();
        long count = codigos.stream().filter(c -> c.getEmail().equals(email)).count();
        assertTrue(count >= 1, "Pelo menos um código deveria ter sido gerado com sucesso sem deadlock");
    }
}
