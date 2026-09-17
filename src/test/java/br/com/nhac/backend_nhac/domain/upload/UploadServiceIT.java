package br.com.nhac.backend_nhac.domain.upload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de integração REAL com o Firebase Storage (sobe os bytes de verdade para o bucket).
 *
 * Só roda quando a system property nhac.storage.mock-mode=false é passada explicitamente, ex:
 *   ./mvnw verify -Dit.test=UploadServiceIT -Dnhac.storage.mock-mode=false
 *
 * Sem essa property (CI, ./mvnw test, ./mvnw verify), o teste é PULADO — nunca roda em modo mock.
 * Usa o perfil "test" (H2 em memória), então não precisa de MariaDB.
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "nhac.storage.mock-mode", matches = "false")
class UploadServiceIT {

    @Autowired
    private UploadService service;

    private MockMultipartFile arquivo() {
        return new MockMultipartFile("arquivo", "teste.jpg", "image/jpeg", "teste".getBytes());
    }

    @Test
    void uploadProduto() {
        String url = service.enviarImagem(arquivo(), "produtos");
        System.out.println("URL produto: " + url);
        assertTrue(url.contains("/produtos%2F") || url.contains("/produtos/"));
    }

    @Test
    void uploadLoja() {
        String url = service.enviarImagem(arquivo(), "lojas");
        System.out.println("URL loja: " + url);
        assertTrue(url.contains("/lojas%2F") || url.contains("/lojas/"));
    }

    @Test
    void uploadUsuario() {
        String url = service.enviarImagem(arquivo(), "usuarios");
        System.out.println("URL usuario: " + url);
        assertTrue(url.contains("/usuarios%2F") || url.contains("/usuarios/"));
    }

    @Test
    void uploadFuncionario() {
        String url = service.enviarImagem(arquivo(), "funcionarios");
        System.out.println("URL funcionario: " + url);
        assertTrue(url.contains("/funcionarios%2F") || url.contains("/funcionarios/"));
    }
}