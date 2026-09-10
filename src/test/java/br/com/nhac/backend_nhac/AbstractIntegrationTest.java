package br.com.nhac.backend_nhac;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected br.com.nhac.backend_nhac.domain.auth.CodigoVerificacaoEmailRepository codigoVerificacaoEmailRepository;

    protected void criarCodigoVerificadoPara(String email) {
        codigoVerificacaoEmailRepository.save(
                br.com.nhac.backend_nhac.domain.auth.CodigoVerificacaoEmail.builder()
                        .email(email.trim().toLowerCase())
                        .codigo("123456")
                        .dataExpiracao(java.time.LocalDateTime.now().plusHours(1))
                        .tentativas(0)
                        .utilizado(true)
                        .tipo(br.com.nhac.backend_nhac.domain.auth.CodigoVerificacaoEmail.TipoCodigo.CADASTRO)
                        .build()
        );
    }

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }
}
