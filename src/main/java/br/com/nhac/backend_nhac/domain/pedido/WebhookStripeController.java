package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.services.PedidoService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@Tag(name = "Webhook Stripe", description = "Endpoints para recebimento de eventos da Stripe")
public class WebhookStripeController {

    private final PedidoService pedidoService;
    private final String webhookSecret;

    public WebhookStripeController(
            PedidoService pedidoService,
            @Value("${stripe.webhook.secret}") String webhookSecret) {
        this.pedidoService = pedidoService;
        this.webhookSecret = webhookSecret;
    }

    @Operation(summary = "Receber eventos da Stripe", description = "Webhook para processar eventos assíncronos da Stripe, como falhas de pagamento.")
    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        if (sigHeader == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltando header Stripe-Signature");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assinatura do webhook inválida");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payload inválido");
        }

        if ("payment_intent.payment_failed".equals(event.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            if (dataObjectDeserializer.getObject().isPresent()) {
                StripeObject stripeObject = dataObjectDeserializer.getObject().get();
                if (stripeObject instanceof PaymentIntent paymentIntent) {
                    Map<String, String> metadata = paymentIntent.getMetadata();
                    if (metadata != null && metadata.containsKey("pedidoId")) {
                        String pedidoId = metadata.get("pedidoId");
                        try {
                            pedidoService.marcarComoCanceladoPorFalhaDePagamento(pedidoId);
                        } catch (Exception e) {
                            // Registra erro, mas retorna 200 para a Stripe não reenviar
                            System.err.println("Erro ao cancelar pedido via webhook: " + e.getMessage());
                        }
                    }
                }
            }
        }

        return ResponseEntity.ok("Success");
    }
}
