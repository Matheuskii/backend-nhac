package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCriadoDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripePaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public PedidoCriadoDTO criarPaymentIntentCartao(Pedido pedido) {
        try {
            // Stripe espera o valor em centavos (ex: R$ 50.00 -> 5000)
            long valorEmCentavos = pedido.getValorTotal().multiply(new BigDecimal("100")).longValue();

            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(valorEmCentavos)
                            .setCurrency("brl")
                            // Habilita métodos de pagamento automáticos (cartão e Google Pay)
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                            .setEnabled(true)
                                            .build()
                            )
                            .putMetadata("pedidoId", pedido.getId())
                            .putMetadata("usuarioId", pedido.getUsuarioId())
                            .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Vincula o ID gerado pelo Stripe ao Pedido
            pedido.setStripePaymentIntentId(paymentIntent.getId());

            // Para cartão/Google Pay, não há QR Code PIX
            // Os campos pixCopiaECola e qrCodeUrl serão null
            String clientSecret = paymentIntent.getClientSecret();

            return new PedidoCriadoDTO(pedido.getId(), clientSecret, null, null);

        } catch (StripeException e) {
            throw new RuntimeException("Falha ao comunicar com Stripe para criar PaymentIntent: " + e.getMessage(), e);
        }
    }
}
