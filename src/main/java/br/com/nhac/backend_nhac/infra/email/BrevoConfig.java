package br.com.nhac.backend_nhac.infra.email;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(BrevoProperties.class)
public class BrevoConfig {

    @Bean
    public RestClient brevoRestClient(BrevoProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));

        return RestClient.builder()
                .baseUrl(properties.getApiBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // Usando interceptor para ler a chave em tempo de execução a cada requisição
                .requestInterceptor((request, body, execution) -> {
                    String apiKey = properties.getApiKey();
                    if (apiKey == null || apiKey.isBlank()) {
                        throw new IllegalStateException(
                            "Erro Crítico: A API Key do Brevo está NULA ou VAZIA nas propriedades do sistema!"
                        );
                    }
                    request.getHeaders().add("api-key", apiKey);
                    return execution.execute(request, body);
                })
                .build();
    }
}
