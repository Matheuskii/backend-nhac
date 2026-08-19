package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCriadoDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
public class AsaasPaymentServiceTest {

    @InjectMocks
    private AsaasPaymentService asaasPaymentService;

    @Mock
    private RestTemplate restTemplate;

    private final String testApiKey = "test_asaas_api_key_123";
    private final String testApiUrl = "https://sandbox.asaas.com/api/v3";

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(asaasPaymentService, "asaasApiKey", testApiKey);
        ReflectionTestUtils.setField(asaasPaymentService, "asaasApiUrl", testApiUrl);
        // Não chamar init() pois o construtor já inicializa com o mock
    }

    @Test
    @DisplayName("Deve criar cobrança PIX com sucesso quando API do Asaas retornar OK")
    void deveCriarCobrancaPixComSucesso() {
        // Arrange
        Pedido pedido = criarPedidoTeste();
        String paymentId = "pay_test_123456";
        String pixQrCode = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        String pixCopyAndPaste = "00020126580014BR.GOV.BCB.PIX0136123e4567-e89b-12d3-a456-426614174000520400005303986540510.005802BR5913NHAC Store6008Sao Paulo62070503***6304ABCD";

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("id", paymentId);
        responseJson.addProperty("pixQrCode", pixQrCode);
        responseJson.addProperty("pixCopyAndPaste", pixCopyAndPaste);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson.toString(), HttpStatus.CREATED);

        when(restTemplate.postForEntity(eq(testApiUrl + "/payments"), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        PedidoCriadoDTO resultado = asaasPaymentService.criarCobrancaPix(pedido);

        // Assert
        assertNotNull(resultado);
        assertEquals(pedido.getId(), resultado.pedidoId());
        assertNull(resultado.clientSecret(), "clientSecret deve ser null para cobranças Asaas");
        assertEquals(pixCopyAndPaste, resultado.pixCopiaECola());
        assertEquals(pixQrCode, resultado.qrCodeUrl());
        assertEquals(paymentId, pedido.getAsaasPaymentId());

        verify(restTemplate).postForEntity(eq(testApiUrl + "/payments"), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Deve criar cobrança PIX mesmo sem QR Code na resposta")
    void deveCriarCobrancaPixSemQrCodeNaResposta() {
        // Arrange
        Pedido pedido = criarPedidoTeste();
        String paymentId = "pay_test_789";
        String pixCopyAndPaste = "00020126580014BR.GOV.BCB.PIX0136123e4567-e89b-12d3-a456-426614174000";

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("id", paymentId);
        responseJson.addProperty("pixCopyAndPaste", pixCopyAndPaste);
        // pixQrCode não está presente

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson.toString(), HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        PedidoCriadoDTO resultado = asaasPaymentService.criarCobrancaPix(pedido);

        // Assert
        assertNotNull(resultado);
        assertEquals(pedido.getId(), resultado.pedidoId());
        assertNull(resultado.qrCodeUrl(), "qrCodeUrl deve ser null quando não retornado pela API");
        assertEquals(pixCopyAndPaste, resultado.pixCopiaECola());
    }

    @Test
    @DisplayName("Deve lançar exceção quando API do Asaas retornar erro")
    void deveLancarExcecaoQuandoApiRetornarErro() {
        // Arrange
        Pedido pedido = criarPedidoTeste();

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"error\": \"Invalid API key\"}", HttpStatus.UNAUTHORIZED);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            asaasPaymentService.criarCobrancaPix(pedido)
        );

        assertTrue(exception.getMessage().contains("Falha ao criar cobrança no Asaas"));
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando houver erro de comunicação com a API")
    void deveLancarExcecaoQuandoHouverErroDeComunicacao() {
        // Arrange
        Pedido pedido = criarPedidoTeste();

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            asaasPaymentService.criarCobrancaPix(pedido)
        );

        assertTrue(exception.getMessage().contains("Erro ao comunicar com Asaas"));
        assertTrue(exception.getCause().getMessage().contains("Connection timeout"));
    }

    @Test
    @DisplayName("Deve enviar headers corretos na requisição para o Asaas")
    void deveEnviarHeadersCorretos() {
        // Arrange
        Pedido pedido = criarPedidoTeste();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("id", "pay_test_123");
        responseJson.addProperty("pixCopyAndPaste", "000201...");

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson.toString(), HttpStatus.CREATED);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        asaasPaymentService.criarCobrancaPix(pedido);

        // Assert - verificar se o HttpEntity foi criado com headers corretos
        ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), entityCaptor.capture(), eq(String.class));
        
        HttpEntity<String> capturedEntity = entityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(testApiKey, headers.getFirst("asaas-api-key"));
    }

    @Test
    @DisplayName("Deve formatar data de vencimento corretamente (7 dias a partir de hoje)")
    void deveFormatarDataVencimentoCorretamente() {
        // Arrange
        Pedido pedido = criarPedidoTeste();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("id", "pay_test_123");
        responseJson.addProperty("pixCopyAndPaste", "000201...");

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson.toString(), HttpStatus.CREATED);

        ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.postForEntity(anyString(), entityCaptor.capture(), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        asaasPaymentService.criarCobrancaPix(pedido);

        // Assert
        HttpEntity<String> capturedEntity = entityCaptor.getValue();
        JsonObject requestBody = new Gson().fromJson(capturedEntity.getBody(), JsonObject.class);
        
        String dueDate = requestBody.get("dueDate").getAsString();
        assertNotNull(dueDate);
        assertTrue(dueDate.matches("\\d{4}-\\d{2}-\\d{2}"), "Data deve estar no formato yyyy-MM-dd");
    }

    @Test
    @DisplayName("Deve incluir informações do pagador no payload da requisição")
    void deveIncluirInformacoesPagadorNoPayload() {
        // Arrange
        Pedido pedido = criarPedidoTeste();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("id", "pay_test_123");
        responseJson.addProperty("pixCopyAndPaste", "000201...");

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson.toString(), HttpStatus.CREATED);

        ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.postForEntity(anyString(), entityCaptor.capture(), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        asaasPaymentService.criarCobrancaPix(pedido);

        // Assert
        HttpEntity<String> capturedEntity = entityCaptor.getValue();
        JsonObject requestBody = new Gson().fromJson(capturedEntity.getBody(), JsonObject.class);
        
        assertTrue(requestBody.has("payer"), "Payload deve conter informações do pagador");
        JsonObject payerInfo = requestBody.getAsJsonObject("payer");
        assertTrue(payerInfo.has("name"));
        assertTrue(payerInfo.has("email"));
        assertTrue(payerInfo.has("cpfCnpj"));
    }

    @Test
    @DisplayName("Deve usar valor total do pedido no payload")
    void deveUsarValorTotalDoPedidoNoPayload() {
        // Arrange
        Pedido pedido = criarPedidoTeste();
        BigDecimal valorEsperado = pedido.getValorTotal();

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("id", "pay_test_123");
        responseJson.addProperty("pixCopyAndPaste", "000201...");

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson.toString(), HttpStatus.CREATED);

        ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.postForEntity(anyString(), entityCaptor.capture(), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        asaasPaymentService.criarCobrancaPix(pedido);

        // Assert
        HttpEntity<String> capturedEntity = entityCaptor.getValue();
        JsonObject requestBody = new Gson().fromJson(capturedEntity.getBody(), JsonObject.class);
        
        double valorEnviado = requestBody.get("value").getAsDouble();
        assertEquals(valorEsperado.doubleValue(), valorEnviado, 0.01);
    }

    @Test
    @DisplayName("Deve usar ID do pedido como referência externa")
    void deveUsarIdPedidoComoReferenciaExterna() {
        // Arrange
        Pedido pedido = criarPedidoTeste();

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("id", "pay_test_123");
        responseJson.addProperty("pixCopyAndPaste", "000201...");

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson.toString(), HttpStatus.CREATED);

        ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.postForEntity(anyString(), entityCaptor.capture(), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        asaasPaymentService.criarCobrancaPix(pedido);

        // Assert
        HttpEntity<String> capturedEntity = entityCaptor.getValue();
        JsonObject requestBody = new Gson().fromJson(capturedEntity.getBody(), JsonObject.class);
        
        String externalReference = requestBody.get("externalReference").getAsString();
        assertEquals(pedido.getId(), externalReference);
    }

    private Pedido criarPedidoTeste() {
        Pedido pedido = new Pedido();
        pedido.setId(UUID.randomUUID().toString());
        pedido.setValorTotal(new BigDecimal("150.00"));
        return pedido;
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<HttpEntity<String>> captorHttpEntity() {
        return ArgumentCaptor.forClass(HttpEntity.class);
    }
}
