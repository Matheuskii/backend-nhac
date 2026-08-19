package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCriadoDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class AsaasPaymentService {

    @Value("${asaas.api.key}")
    private String asaasApiKey;

    @Value("${asaas.api.url:https://sandbox.asaas.com/api/v3}")
    private String asaasApiUrl;

    private RestTemplate restTemplate;
    private Gson gson;

    public AsaasPaymentService() {
        // Construtor padrão para injeção de dependências
    }

    public AsaasPaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.gson = new Gson();
    }

    @PostConstruct
    public void init() {
        if (this.restTemplate == null) {
            this.restTemplate = new RestTemplate();
        }
        this.gson = new Gson();
    }

    /**
     * Cria uma cobrança PIX no Asaas
     * @param pedido Pedido a ser cobrado
     * @return PedidoCriadoDTO com pixCopiaECola e qrCodeUrl preenchidos
     */
    public PedidoCriadoDTO criarCobrancaPix(Pedido pedido) {
        try {
            // Preparar headers da requisição
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("asaas-api-key", asaasApiKey);

            // Formatar data de vencimento (7 dias a partir de hoje)
            String dueDate = LocalDate.now().plusDays(7)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Criar payload da requisição
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("externalReference", pedido.getId());
            requestBody.addProperty("description", "Pedido #" + pedido.getId());
            requestBody.addProperty("billingType", "PIX");
            requestBody.addProperty("value", pedido.getValorTotal().doubleValue());
            requestBody.addProperty("dueDate", dueDate);

            // Em um cenário real, é necessário criar um cliente no Asaas primeiro
            // ou usar um cliente existente. Esta implementação usa dados simplificados.
            // Pode ser necessário adaptar conforme a necessidade.
            
            // Opção 1: Se já tiver um customer ID do Asaas associado ao usuário
            // requestBody.addProperty("customer", customerIdDoAsaas);
            
            // Opção 2: Criar cobrança sem customer (algumas configurações permitem)
            // Adicionar informações do pagador diretamente
            JsonObject payerInfo = new JsonObject();
            payerInfo.addProperty("name", "Cliente do Pedido " + pedido.getId().substring(0, Math.min(8, pedido.getId().length())));
            payerInfo.addProperty("email", "cliente@exemplo.com");
            payerInfo.addProperty("cpfCnpj", "00000000000");
            requestBody.add("payer", payerInfo);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            // Fazer requisição para API do Asaas
            String url = asaasApiUrl + "/payments";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                JsonObject responseBody = gson.fromJson(response.getBody(), JsonObject.class);
                
                // Extrair dados do PIX da resposta
                String paymentId = responseBody.get("id").getAsString();
                String pixQrCode = responseBody.has("pixQrCode") ? responseBody.get("pixQrCode").getAsString() : null;
                String pixCopyAndPaste = responseBody.has("pixCopyAndPaste") ? responseBody.get("pixCopyAndPaste").getAsString() : null;

                // Armazenar ID do pagamento no pedido (opcional, para rastreio)
                pedido.setAsaasPaymentId(paymentId);

                // Retornar DTO com dados do PIX
                // clientSecret será null pois não usamos Stripe para PIX
                return new PedidoCriadoDTO(pedido.getId(), null, pixCopyAndPaste, pixQrCode);
            } else {
                throw new RuntimeException("Falha ao criar cobrança no Asaas: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com Asaas para criar cobrança PIX: " + e.getMessage(), e);
        }
    }
}
