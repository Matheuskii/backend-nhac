package br.com.nhac.backend_nhac.infra.security;

import br.com.nhac.backend_nhac.infra.security.AutoridadesFactory;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class WebMvcSecurityTestSupport {

    @Bean
    @Primary
    public AutoridadesFactory autoridadesFactory() {
        return Mockito.mock(AutoridadesFactory.class);
    }
}