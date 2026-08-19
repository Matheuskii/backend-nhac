# Especificação Técnica: Migração de Pagamento PIX para Cartão e Google Pay na Stripe

## 1. Visão Geral

### Contexto
Atualmente o sistema utiliza a Stripe para processar pagamentos via PIX. No entanto, a conta Stripe em uso não possui suporte a PIX habilitado. Esta especificação define a migração para pagamentos com **Cartão de Crédito** e **Google Pay**.

### Objetivo
Alterar o fluxo de pagamento para utilizar métodos suportados pela conta Stripe (cartão de crédito e Google Pay), mantendo a integração existente com webhooks para confirmação assíncrona de pagamentos.

---

## 2. Análise do Estado Atual

### Componentes Envolvidos

#### 2.1 `StripePaymentService.java`
- **Método atual**: `criarPaymentIntentPix(Pedido pedido)`
- Configura PaymentIntent com:
  - Moeda: BRL
  - Tipo de método: `pix`
  - Extrai dados do QR Code PIX (`nextAction.pixDisplayQrCode`)
- **Problema**: Método específico para PIX que não funcionará em contas sem suporte a PIX

#### 2.2 `PedidoService.java`
- **Método**: `finalizarPedido(PedidoCreateDTO dto, String usuarioIdLogado)`
- Verifica se forma de pagamento é "PIX" e chama `stripePaymentService.criarPaymentIntentPix()`
- **Problema**: Lógica acoplada ao método PIX

#### 2.3 `PedidoCriadoDTO.java`
- Campos específicos para PIX:
  - `pixCopiaECola`
  - `qrCodeUrl`
- **Problema**: Campos não serão utilizados no novo fluxo

#### 2.4 `StripeWebhookController.java`
- Trata eventos `payment_intent.succeeded` e `payment_intent.payment_failed`
- **Status**: ✅ Compatível com cartão e Google Pay (não requer alterações)

#### 2.5 Testes
- `PedidoFlowIT.java`: Mock do método `criarPaymentIntentPix`
- `StripeWebhookControllerTest.java`: Testes de webhook genéricos

---

## 3. Requisitos da Nova Implementação

### 3.1 Requisitos Funcionais

1. **RF01** - O sistema deve criar PaymentIntents configurados para aceitar cartões de crédito
2. **RF02** - O sistema deve configurar automatic_payment_methods para habilitar Google Pay
3. **RF03** - O clientSecret retornado deve permitir confirmação do pagamento no frontend via Stripe Elements ou Stripe SDK
4. **RF04** - O webhook deve continuar processando eventos de sucesso/falha de pagamento
5. **RF05** - Metadados do pedido (pedidoId, usuarioId) devem ser preservados no PaymentIntent

### 3.2 Requisitos Não-Funcionais

1. **RNF01** - Manter compatibilidade com a versão 33.3.0 da stripe-java
2. **RNF02** - Manter estrutura de DTOs existente (campos PIX podem permanecer como opcionais/null)
3. **RNF03** - Preservar tratamento de exceções e logs existentes
4. **RNF04** - Manter cobertura de testes existente

---

## 4. Especificação das Mudanças

### 4.1 Alteração em `StripePaymentService.java`

#### 4.1.1 Renomear/Substituir Método
```java
// DE:
public PedidoCriadoDTO criarPaymentIntentPix(Pedido pedido)

// PARA:
public PedidoCriadoDTO criarPaymentIntentCartao(Pedido pedido)
```

#### 4.1.2 Nova Implementação do PaymentIntent
```java
PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
    .setAmount(valorEmCentavos)
    .setCurrency("brl")
    .addPaymentMethodType("card")  // Habilita cartão
    .setAutomaticPaymentMethods(   // Habilita Google Pay e outros métodos automáticos
        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
            .setEnabled(true)
            .build()
    )
    .putMetadata("pedidoId", pedido.getId())
    .putMetadata("usuarioId", pedido.getUsuarioId())
    .build();
```

#### 4.1.3 Retorno do DTO
- `clientSecret`: Mantido (necessário para confirmação no frontend)
- `pixCopiaECola`: null (não aplicável)
- `qrCodeUrl`: null (não aplicável)

### 4.2 Alteração em `PedidoService.java`

#### 4.2.1 Atualizar Condição de Pagamento
```java
// DE:
if ("PIX".equalsIgnoreCase(pedido.getFormaPagamento())) {
    return stripePaymentService.criarPaymentIntentPix(pedidoSalvo);
}

// PARA:
if ("CARTAO".equalsIgnoreCase(pedido.getFormaPagamento()) || 
    "GOOGLE_PAY".equalsIgnoreCase(pedido.getFormaPagamento()) ||
    "STRIPE".equalsIgnoreCase(pedido.getFormaPagamento())) {
    return stripePaymentService.criarPaymentIntentCartao(pedidoSalvo);
}
```

**Nota**: Considerar aceitar múltiplos identificadores para flexibilidade no frontend.

### 4.3 Atualização em `PedidoCriadoDTO.java`

#### 4.3.1 Atualizar Schema Documentation
```java
@Schema(description = "Código Copia e Cola do PIX para pagamento (null para pagamento com cartão)", example = "00020101021126580014br.gov.bcb.pix...")
String pixCopiaECola,

@Schema(description = "URL da imagem ou payload do QR Code do PIX (null para pagamento com cartão)", example = "https://qr.stripe.com/test_123")
String qrCodeUrl
```

### 4.4 Atualização em `PedidoController.java`

#### 4.4.1 Atualizar Documentação OpenAPI
```java
@Operation(
    summary = "Finalizar uma nova compra", 
    description = "Recebe o carrinho de compras, valida os itens e gera um novo pedido. " +
                  "Para pagamentos via Stripe (cartão/Google Pay), retorna clientSecret para confirmação no frontend."
)
```

### 4.5 Atualização nos Testes

#### 4.5.1 `PedidoFlowIT.java`
```java
// DE:
Mockito.when(stripePaymentService.criarPaymentIntentPix(Mockito.any(Pedido.class)))
    .thenAnswer(invocation -> {
        Pedido pedidoSalvo = invocation.getArgument(0);
        return new PedidoCriadoDTO(pedidoSalvo.getId(), "mock-secret", "mock-pix", "mock-url");
    });

// PARA:
Mockito.when(stripePaymentService.criarPaymentIntentCartao(Mockito.any(Pedido.class)))
    .thenAnswer(invocation -> {
        Pedido pedidoSalvo = invocation.getArgument(0);
        return new PedidoCriadoDTO(pedidoSalvo.getId(), "mock-secret", null, null);
    });
```

#### 4.5.2 Atualizar Dados do Teste
```java
// DE:
new PedidoCreateDTO(loja.getId(), "PIX", ...)

// PARA:
new PedidoCreateDTO(loja.getId(), "CARTAO", ...)
// ou
new PedidoCreateDTO(loja.getId(), "STRIPE", ...)
```

#### 4.5.3 `PedidoServiceTest.java`
- Atualizar mocks e verificações para o novo método `criarPaymentIntentCartao`

---

## 5. Plano de Implementação

### Fase 1: Core Services
1. [ ] Modificar `StripePaymentService.criarPaymentIntentPix()` para `criarPaymentIntentCartao()`
2. [ ] Implementar configuração de PaymentIntent para cartão + Google Pay
3. [ ] Atualizar retorno do DTO (pixCopiaECola e qrCodeUrl como null)

### Fase 2: Service Layer
4. [ ] Modificar `PedidoService.finalizarPedido()` para chamar novo método
5. [ ] Atualizar condição de forma de pagamento para aceitar CARTAO/GOOGLE_PAY/STRIPE

### Fase 3: Controller & Documentation
6. [ ] Atualizar documentação OpenAPI no `PedidoController`
7. [ ] Revisar schemas no `PedidoCriadoDTO`

### Fase 4: Testes
8. [ ] Atualizar `PedidoFlowIT.java` com novo mock e dados de teste
9. [ ] Atualizar `PedidoServiceTest.java`
10. [ ] Executar todos os testes para validar regressão

### Fase 5: Validação
11. [ ] Testar criação de PaymentIntent via Stripe Dashboard
12. [ ] Validar recebimento de webhooks `payment_intent.succeeded`
13. [ ] Confirmar que Google Pay está disponível no checkout

---

## 6. Considerações sobre Frontend

### Integração Necessária no Flutter/Frontend

O frontend deverá:
1. Receber o `clientSecret` da API
2. Utilizar **Stripe Elements** ou **Stripe SDK** para coletar dados do cartão
3. Confirmar o PaymentIntent usando o `clientSecret`
4. Para Google Pay: configurar Stripe Google Pay button conforme documentação oficial

**Documentação de Referência**:
- [Stripe Payment Intent API](https://stripe.com/docs/api/payment_intents/create)
- [Stripe Google Pay](https://stripe.com/docs/google-pay)
- [Stripe Elements](https://stripe.com/docs/elements)

---

## 7. Riscos e Mitigações

| Risco | Impacto | Mitigação |
|-------|---------|-----------|
| Conta Stripe não ter Google Pay habilitado | Médio | Testar no Stripe Dashboard antes da implantação |
| Frontend não estar preparado para fluxos de cartão | Alto | Coordenar deploy backend/frontend |
| Webhooks não serem recebidos corretamente | Alto | Testar com Stripe CLI antes de produção |
| Perda de histórico de pedidos PIX | Baixo | Manter campos no DTO como deprecated |

---

## 8. Critérios de Aceite

- [ ] PaymentIntent é criado com sucesso para método `card`
- [ ] Google Pay aparece como opção de pagamento no checkout Stripe
- [ ] Webhook `payment_intent.succeeded` atualiza pedido para PAGO
- [ ] Webhook `payment_intent.payment_failed` cancela pedido pendente
- [ ] Todos os testes unitários e de integração passam
- [ ] Documentação OpenAPI reflete novas formas de pagamento
- [ ] Campos PIX no retorno são null (sem quebrar contrato)

---

## 9. Referências Técnicas

- Stripe Payment Intents: https://stripe.com/docs/payments/payment-intents
- Stripe Java Library: https://github.com/stripe/stripe-java
- Google Pay com Stripe: https://stripe.com/docs/google-pay
- Stripe Webhooks: https://stripe.com/docs/webhooks

---

**Autor**: Especificação gerada via Spec Driven Development  
**Data**: 2025-01-XX  
**Status**: Pendente de implementação
