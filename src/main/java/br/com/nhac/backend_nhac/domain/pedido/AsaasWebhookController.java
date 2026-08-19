package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.services.PedidoService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
public class AsaasWebhookController {

    private final PedidoService pedidoService;
    private final String asaasApiKey;

    public AsaasWebhookController(PedidoService pedidoService, 
                                  @Value("${asaas.api.key}") String asaasApiKey) {
        this.pedidoService = pedidoService;
        this.asaasApiKey = asaasApiKey;
    }

    /**
     * Endpoint para receber webhooks do Asaas
     * 
     * Eventos suportados:
     * - PAYMENT_RECEIVED: Pagamento confirmado → Marca pedido como PAGO
     * - PAYMENT_OVERDUE: Pagamento vencido → Cancela pedido
     * - PAYMENT_CANCELLED: Pagamento cancelado → Cancela pedido
     */
    @PostMapping("/asaas")
    public ResponseEntity<Void> receberWebhookAsaas(
            @RequestBody String payloadJson,
            @RequestHeader("asaas-api-key") String receivedApiKey,
            HttpServletRequest request) {

        // Validar chave da API (segurança básica)
        if (!asaasApiKey.equals(receivedApiKey)) {
            return ResponseEntity.status(401).build();
        }

        try {
            JsonObject payload = JsonParser.parseString(payloadJson).getAsJsonObject();
            
            String notification = payload.has("notification") ? payload.get("notification").getAsString() : null;
            
            if (notification == null && payload.has("event")) {
                // Formato alternativo: {"event": "PAYMENT_RECEIVED", ...}
                notification = payload.get("event").getAsString();
            }

            if (notification == null) {
                return ResponseEntity.badRequest().build();
            }

            // Extrair informações do pagamento
            JsonObject paymentData = payload.has("payment") ? payload.getAsJsonObject("payment") : payload;
            String externalReference = paymentData.has("externalReference") 
                    ? paymentData.get("externalReference").getAsString() 
                    : null;
            String asaasPaymentId = paymentData.has("id") 
                    ? paymentData.get("id").getAsString() 
                    : null;

            // Extrair ID do pedido da referência externa
            String pedidoId = extrairPedidoIdDaReferencia(externalReference);

            switch (notification) {
                case "PAYMENT_RECEIVED":
                    // Pagamento confirmado
                    if (asaasPaymentId != null) {
                        pedidoService.marcarComoPagoPorAsaasPaymentId(asaasPaymentId);
                    } else if (pedidoId != null) {
                        // Fallback: buscar por pedidoId se não tiver asaasPaymentId
                        throw new RuntimeException("Asaas Payment ID não encontrado no webhook");
                    }
                    break;

                case "PAYMENT_OVERDUE":
                    // Pagamento vencido - cancelar pedido
                    if (pedidoId != null) {
                        pedidoService.cancelarPorFalhaPagamentoAsaas(pedidoId);
                    }
                    break;

                case "PAYMENT_CANCELLED":
                    // Pagamento cancelado pelo usuário
                    if (pedidoId != null) {
                        pedidoService.cancelarPorFalhaPagamentoAsaas(pedidoId);
                    }
                    break;

                default:
                    // Evento não tratado - apenas logar
                    System.out.println("Evento Asaas não tratado: " + notification);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("Erro ao processar webhook do Asaas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Extrai o ID do pedido da referência externa do Asaas
     * Formato esperado: "pedidoId_a1b2c3d4" ou apenas "a1b2c3d4"
     */
    private String extrairPedidoIdDaReferencia(String externalReference) {
        if (externalReference == null || externalReference.isEmpty()) {
            return null;
        }

        // Se estiver no formato "pedidoId_a1b2c3d4"
        if (externalReference.startsWith("pedidoId_")) {
            return externalReference.substring("pedidoId_".length());
        }

        // Caso contrário, assume que é o próprio ID
        return externalReference;
    }
}
