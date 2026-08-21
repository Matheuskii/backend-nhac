package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.services.PedidoService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@Tag(name = "Stripe Webhooks", description = "Endpoints para recebimento de eventos assíncronos de pagamento do Stripe")
public class StripeWebhookController {

    private final PedidoService pedidoService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    public StripeWebhookController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Operation(summary = "Receber webhooks do Stripe", description = "Rota aberta chamada pelo Stripe para atualizar o status do pedido")
    @PostMapping
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        if (sigHeader == null) {
            System.err.println("❌ Webhook Stripe: Faltando header Stripe-Signature");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltando header Stripe-Signature");
        }

        Event event = null;

        try {
            // Verifica a assinatura e desserializa o evento usando a SDK
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            // Assinatura inválida (pode ser alguém tentando invadir a API)
            System.err.println("❌ Webhook Stripe: Assinatura inválida - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            System.err.println("❌ Webhook Stripe: Payload inválido - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        // Trata os tipos de eventos suportados
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (paymentIntent != null) {
                    System.out.println("✅ Webhook Stripe: Pagamento bem-sucedido para PaymentIntent: " + paymentIntent.getId());
                    try {
                        pedidoService.marcarComoPagoPorPaymentIntentId(paymentIntent.getId());
                    } catch (br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException e) {
                        System.out.println("⚠️ Webhook Stripe ignorado: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("❌ Erro no webhook Stripe: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Força o retry do Stripe
                    }
                }
                break;
            case "payment_intent.payment_failed":
                PaymentIntent failedIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (failedIntent != null) {
                    Map<String, String> metadata = failedIntent.getMetadata();
                    if (metadata != null && metadata.containsKey("pedidoId")) {
                        String pedidoId = metadata.get("pedidoId");
                        System.out.println("⚠️ Webhook Stripe: Pagamento falhou para pedido: " + pedidoId);
                        try {
                            pedidoService.marcarComoCanceladoPorFalhaDePagamento(pedidoId);
                        } catch (br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException e) {
                            System.out.println("⚠️ Webhook Stripe ignorado: " + e.getMessage());
                        } catch (Exception e) {
                            System.err.println("❌ Erro ao cancelar pedido via webhook: " + e.getMessage());
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                        }
                    }
                }
                break;
            default:
                System.out.println("⚠️ Webhook Stripe: Evento não tratado: " + event.getType());
        }

        return ResponseEntity.ok("Success");
    }
}
