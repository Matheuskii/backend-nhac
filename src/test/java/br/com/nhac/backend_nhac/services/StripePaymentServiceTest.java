package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCriadoDTO;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class StripePaymentServiceTest {

    @InjectMocks
    private StripePaymentService stripePaymentService;

    @org.mockito.Mock
    private br.com.nhac.backend_nhac.repositories.PedidoRepository pedidoRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripePaymentService, "stripeApiKey", "sk_test_123");
    }

    @Test
    @DisplayName("Deve criar PaymentIntent com sucesso")
    void deveCriarPaymentIntentComSucesso() {
        Pedido pedido = new Pedido();
        pedido.setId(UUID.randomUUID().toString());
        pedido.setValorTotal(new BigDecimal("150.00"));

        PaymentIntent mockIntent = new PaymentIntent();
        mockIntent.setId("pi_12345");
        mockIntent.setClientSecret("pi_12345_secret");

        try (MockedStatic<PaymentIntent> paymentIntentMock = Mockito.mockStatic(PaymentIntent.class)) {
            paymentIntentMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            PedidoCriadoDTO dto = stripePaymentService.criarPaymentIntentCartao(pedido);

            assertNotNull(dto);
            assertEquals(pedido.getId(), dto.pedidoId());
            assertEquals("pi_12345_secret", dto.clientSecret());
            assertEquals("pi_12345", pedido.getStripePaymentIntentId());
        }
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando StripeException ocorrer")
    void deveLancarRuntimeExceptionAoFalharNoStripe() {
        Pedido pedido = new Pedido();
        pedido.setId(UUID.randomUUID().toString());
        pedido.setValorTotal(new BigDecimal("150.00"));

        try (MockedStatic<PaymentIntent> paymentIntentMock = Mockito.mockStatic(PaymentIntent.class)) {
            paymentIntentMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenThrow(new RuntimeException("Simulated Stripe Exception"));

            assertThrows(RuntimeException.class, () -> stripePaymentService.criarPaymentIntentCartao(pedido));
        }
    }
}
