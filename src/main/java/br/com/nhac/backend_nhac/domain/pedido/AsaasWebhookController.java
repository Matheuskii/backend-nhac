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
    private final String asaasWebhookToken;

    public AsaasWebhookController(PedidoService pedidoService,
                                  @Value("${asaas.webhook.token}") String asaasWebhookToken) {
        this.pedidoService = pedidoService;
        this.asaasWebhookToken = asaasWebhookToken;
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
            @RequestHeader(value = "asaas-access-token", required = false) String receivedToken,
            HttpServletRequest request) {

        if (receivedToken == null || !asaasWebhookToken.equals(receivedToken)) {
            return ResponseEntity.status(401).build();
        }

        try {
            JsonObject payload = JsonParser.parseString(payloadJson).getAsJsonObject();
            
            String notification = payload.has("notification") ? payload.get("notification").getAsString() : null;
            
            if (notification == null && payload.has("event")) {
                notification = payload.get("event").getAsString();
            }

            if (notification == null) {
                return ResponseEntity.badRequest().build();
            }

            JsonObject paymentData = payload.has("payment") ? payload.getAsJsonObject("payment") : payload;
            String externalReference = paymentData.has("externalReference") 
                    ? paymentData.get("externalReference").getAsString() 
                    : null;
            String asaasPaymentId = paymentData.has("id") 
                    ? paymentData.get("id").getAsString() 
                    : null;

            String pedidoId = extrairPedidoIdDaReferencia(externalReference);

            switch (notification) {
                case "PAYMENT_RECEIVED":
                    if (asaasPaymentId != null) {
                        pedidoService.marcarComoPagoPorAsaasPaymentId(asaasPaymentId);
                    } else if (pedidoId != null) {
                        throw new RuntimeException("Asaas Payment ID não encontrado no webhook");
                    }
                    break;

                case "PAYMENT_OVERDUE":
                    if (pedidoId != null) {
                        pedidoService.cancelarPorFalhaPagamentoAsaas(pedidoId);
                    }
                    break;

                case "PAYMENT_CANCELLED":
                    if (pedidoId != null) {
                        pedidoService.cancelarPorFalhaPagamentoAsaas(pedidoId);
                    }
                    break;

                default:
                    System.out.println("Evento Asaas não tratado: " + notification);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("Erro ao processar webhook do Asaas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    
    private String extrairPedidoIdDaReferencia(String externalReference) {
        if (externalReference == null || externalReference.isEmpty()) {
            return null;
        }

        if (externalReference.startsWith("pedidoId_")) {
            return externalReference.substring("pedidoId_".length());
        }

        return externalReference;
    }
}
