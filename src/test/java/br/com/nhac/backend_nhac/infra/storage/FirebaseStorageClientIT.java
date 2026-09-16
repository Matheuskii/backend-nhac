package br.com.nhac.backend_nhac.infra.storage;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste MANUAL: faz upload REAL no bucket nhac-backend.firebasestorage.app e exige credenciais
 * válidas. Por isso está no grupo "manual" (excluído do verify/CI pelo failsafe) — rodar assim:
 *   NHAC_STORAGE_MOCK_MODE=false ./mvnw test -Dtest=FirebaseStorageClientIT
 * (a env var desliga o mock-mode=true do perfil de teste; sem ela, o teste roda em MOCK e falha
 * nas asserções de URL real, como esperado.)
 */
@SpringBootTest
@Tag("manual")
@ActiveProfiles("test")
class FirebaseStorageClientIT {

    @Autowired
    private FirebaseStorageClient client;

    @Test
    void uploadReal() throws Exception {
        byte[] bytes = "teste nhac".getBytes(StandardCharsets.UTF_8);
        String url = client.upload(bytes, "produtos", "txt", "text/plain");
        System.out.println("URL retornada: " + url);
        assertNotNull(url);
        assertTrue(url.startsWith("https://firebasestorage.googleapis.com/v0/b/"));
        assertTrue(url.contains("nhac-backend.firebasestorage.app"));
    }
}
