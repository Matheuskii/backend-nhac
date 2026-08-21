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
            System.err.println("❌ Webhook Asaas: Token inválido");
            return ResponseEntity.status(401).build();
        }

        try {
            JsonObject payload = JsonParser.parseString(payloadJson).getAsJsonObject();
            
            String notification = payload.has("notification") ? payload.get("notification").getAsString() : null;
            
            if (notification == null && payload.has("event")) {
                notification = payload.get("event").getAsString();
            }

            if (notification == null) {
                System.err.println("❌ Webhook Asaas: Campo 'notification' ou 'event' não encontrado");
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

            System.out.println("📨 Webhook Asaas recebido - Evento: " + notification + ", PaymentId: " + asaasPaymentId);

            switch (notification) {
                case "PAYMENT_RECEIVED":
                    if (asaasPaymentId != null && !asaasPaymentId.isEmpty()) {
                        System.out.println("✅ Webhook Asaas: Pagamento recebido para Asaas Payment ID: " + asaasPaymentId);
                        try {
                            pedidoService.marcarComoPagoPorAsaasPaymentId(asaasPaymentId);
                        } catch (br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException e) {
                            System.out.println("⚠️ Webhook Asaas ignorado (idempotência): " + e.getMessage());
                        }
                    } else {
                        System.err.println("❌ Webhook Asaas: Asaas Payment ID não encontrado no payload");
                        return ResponseEntity.badRequest().build();
                    }
                    break;

                case "PAYMENT_OVERDUE":
                    if (pedidoId != null && !pedidoId.isEmpty()) {
                        System.out.println("⚠️ Webhook Asaas: Pagamento vencido para pedido: " + pedidoId);
                        try {
                            pedidoService.cancelarPorFalhaPagamentoAsaas(pedidoId);
                        } catch (br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException e) {
                            System.out.println("⚠️ Webhook Asaas ignorado (idempotência): " + e.getMessage());
                        }
                    } else {
                        System.out.println("⚠️ Webhook Asaas: PedidoId não encontrado para PAYMENT_OVERDUE");
                    }
                    break;

                case "PAYMENT_CANCELLED":
                    if (pedidoId != null && !pedidoId.isEmpty()) {
                        System.out.println("⚠️ Webhook Asaas: Pagamento cancelado para pedido: " + pedidoId);
                        try {
                            pedidoService.cancelarPorFalhaPagamentoAsaas(pedidoId);
                        } catch (br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException e) {
                            System.out.println("⚠️ Webhook Asaas ignorado (idempotência): " + e.getMessage());
                        }
                    } else {
                        System.out.println("⚠️ Webhook Asaas: PedidoId não encontrado para PAYMENT_CANCELLED");
                    }
                    break;

                default:
                    System.out.println("⚠️ Webhook Asaas: Evento não tratado: " + notification);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar webhook do Asaas: " + e.getMessage());
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
