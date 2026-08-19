# Especificação: Integração Asaas para Pagamento via PIX

## 1. Contexto e Objetivo

Com a migração do Stripe para suportar apenas **cartão de crédito e Google Pay**, este documento especifica a implementação de um novo serviço de pagamento usando **Asaas** para processar pagamentos via **PIX**.

### Problema
- Stripe não suporta PIX na conta atual
- Necessidade de oferecer PIX como opção de pagamento para clientes brasileiros
- Asaas é uma plataforma que suporta PIX nativamente no Brasil

### Solução
Criar um `AsaasPaymentService` que será chamado quando o usuário selecionar "PIX" como forma de pagamento, mantendo o Stripe para cartão/Google Pay.

---

## 2. Requisitos Funcionais

### RF01 - Criar Cobrança PIX no Asaas
**Dado** que um usuário finalizou um pedido com forma de pagamento "PIX"  
**Quando** o `PedidoService.finalizarPedido()` for executado  
**Então** deve chamar `AsaasPaymentService.criarCobrancaPix()`  
**E** deve retornar o código Copia e Cola e URL do QR Code  

### RF02 - Webhook do Asaas
**Dado** que um pagamento PIX foi confirmado no Asaas  
**Quando** o Asaas enviar um webhook de confirmação  
**Então** o sistema deve atualizar o status do pedido para "PAGO"  

### RF03 - Webhook de Falha/Cancelamento
**Dado** que um pagamento PIX falhou ou expirou no Asaas  
**Quando** o Asaas enviar um webhook de falha  
**Então** o sistema deve cancelar o pedido  

---

## 3. Arquitetura da Solução

### 3.1 Componentes

```
┌─────────────────────────────────────────────────────┐
│                 PedidoController                     │
│  POST /api/v1/pedidos                                │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│                  PedidoService                       │
│  finalizarPedido()                                   │
│  ├─ Se formaPagamento = "PIX" → AsaasPaymentService  │
│  └─ Se formaPagamento = "CARTAO"/"GOOGLE_PAY" →     │
│     StripePaymentService                             │
└──────────────────┬──────────────────────────────────┘
                   │
        ┌──────────┴──────────┐
        ▼                     ▼
┌──────────────────┐  ┌──────────────────┐
│ AsaasPayment     │  │ StripePayment    │
│ Service          │  │ Service          │
│ (NOVO)           │  │ (EXISTENTE)      │
│ • criarCobranca  │  │ • criarPayment   │
│   Pix()          │  │   IntentCartao() │
└──────────────────┘  └──────────────────┘
```

### 3.2 Fluxo de Pagamento PIX

1. **Cliente finaliza pedido** → `PedidoController.criarPedido()`
2. **PedidoService verifica formaPagamento** → Se "PIX", chama Asaas
3. **AsaasPaymentService.criarCobrancaPix()**:
   - Conecta à API do Asaas
   - Cria uma cobrança PIX
   - Retorna `pixCopiaECola` e `qrCodeUrl`
4. **Retorna ao frontend** → Cliente escaneia QR Code ou usa Copia e Cola
5. **Cliente paga no banco** → Asaas detecta pagamento
6. **Asaas envia webhook** → `AsaasWebhookController` recebe notificação
7. **Atualiza pedido** → Status muda para "PAGO"

---

## 4. Especificação Técnica

### 4.1 Novo Serviço: AsaasPaymentService

**Localização**: `src/main/java/br/com/nhac/backend_nhac/services/AsaasPaymentService.java`

**Responsabilidades**:
- Configurar conexão com API Asaas
- Criar cobranças PIX
- Retornar dados do PIX (Copia e Cola, QR Code)

**Métodos**:
```java
public class AsaasPaymentService {
    
    @Value("${asaas.api.key}")
    private String asaasApiKey;
    
    @Value("${asaas.api.url}")
    private String asaasApiUrl;
    
    /**
     * Cria uma cobrança PIX no Asaas
     * @param pedido Pedido a ser cobrado
     * @return PedidoCriadoDTO com pixCopiaECola e qrCodeUrl preenchidos
     */
    public PedidoCriadoDTO criarCobrancaPix(Pedido pedido);
}
```

**Dependência Maven** (se necessário SDK oficial):
```xml
<!-- Opção 1: Usar cliente HTTP direto (Recomendado para simplicidade) -->
<!-- Spring já inclui RestTemplate/WebClient -->

<!-- Opção 2: SDK oficial do Asaas (se disponível) -->
<!-- Verificar em https://github.com/asaasdev/asaas-sdk-java -->
```

### 4.2 Atualização: PedidoService

**Arquivo**: `src/main/java/br/com/nhac/backend_nhac/services/PedidoService.java`

**Mudanças**:
```java
// Adicionar dependência
private final AsaasPaymentService asaasPaymentService;

// Construtor atualizado
public PedidoService(..., AsaasPaymentService asaasPaymentService) {
    this.asaasPaymentService = asaasPaymentService;
    // ...
}

// Método finalizarPedido atualizado
@Transactional
public PedidoCriadoDTO finalizarPedido(PedidoCreateDTO dto, String usuarioIdLogado) {
    // ... (código existente de criação do pedido)
    
    if ("PIX".equalsIgnoreCase(pedido.getFormaPagamento())) {
        return asaasPaymentService.criarCobrancaPix(pedidoSalvo);
    } else if ("CARTAO".equalsIgnoreCase(pedido.getFormaPagamento()) || 
               "GOOGLE_PAY".equalsIgnoreCase(pedido.getFormaPagamento()) ||
               "STRIPE".equalsIgnoreCase(pedido.getFormaPagamento())) {
        return stripePaymentService.criarPaymentIntentCartao(pedidoSalvo);
    }
    
    return new PedidoCriadoDTO(pedidoSalvo.getId(), null, null, null);
}
```

### 4.3 Novo Controller: AsaasWebhookController

**Localização**: `src/main/java/br/com/nhac/backend_nhac/domain/pedido/AsaasWebhookController.java`

**Endpoint**: `POST /api/v1/webhooks/asaas`

**Responsabilidades**:
- Receber notificações do Asaas
- Validar assinatura do webhook (segurança)
- Processar eventos: `PAYMENT_RECEIVED`, `PAYMENT_OVERDUE`, `PAYMENT_CANCELLED`

**Eventos do Asaas**:
| Evento | Ação no Sistema |
|--------|-----------------|
| `PAYMENT_RECEIVED` | Marcar pedido como PAGO |
| `PAYMENT_OVERDUE` | Cancelar pedido (após X dias) |
| `PAYMENT_CANCELLED` | Cancelar pedido |

**Exemplo de payload do Asaas**:
```json
{
  "notification": "PAYMENT_RECEIVED",
  "payment": {
    "id": "pay_123456",
    "externalReference": "pedidoId_a1b2c3d4",
    "status": "RECEIVED",
    "value": 50.00,
    "pixTransaction": {
      "qrCode": "...",
      "transactionReceipt": "..."
    }
  }
}
```

### 4.4 DTO: PedidoCriadoDTO (Sem alterações)

**Arquivo**: `src/main/java/br/com/nhac/backend_nhac/domain/pedido/dto/PedidoCriadoDTO.java`

O DTO já está preparado:
- Para **PIX**: `pixCopiaECola` e `qrCodeUrl` serão preenchidos
- Para **Cartão/Google Pay**: `clientSecret` será preenchido, PIX será `null`

### 4.5 Configurações: application.properties

**Adicionar**:
```properties
# Asaas API Configuration
asaas.api.key=${ASAAS_API_KEY:your_default_key}
asaas.api.url=https://sandbox.asaas.com/api/v3
# Em produção: https://www.asaas.com/api/v3
```

### 4.6 Entidade: Pedido (Possível atualização)

**Arquivo**: `src/main/java/br/com/nhac/backend_nhac/domain/pedido/Pedido.java`

**Campo adicional** (opcional):
```java
@Column(name = "asaas_payment_id")
private String asaasPaymentId; // ID da cobrança no Asaas
```

---

## 5. Plano de Implementação

### Fase 1: Configuração e Dependências ✅
- [x] Adicionar configurações do Asaas no `application.properties`
- [ ] Criar variáveis de ambiente no `.env.example`

### Fase 2: Core Services
- [ ] Criar `AsaasPaymentService.java`
  - Implementar método `criarCobrancaPix(Pedido pedido)`
  - Usar `RestTemplate` ou `WebClient` para chamar API do Asaas
  - Mapear resposta para `PedidoCriadoDTO`

### Fase 3: Integração no PedidoService
- [ ] Injetar `AsaasPaymentService` no construtor
- [ ] Adicionar lógica condicional para escolher entre Asaas (PIX) e Stripe (Cartão)

### Fase 4: Webhook
- [ ] Criar `AsaasWebhookController.java`
  - Endpoint `POST /api/v1/webhooks/asaas`
  - Validar assinatura (header `asaas-api-key`)
  - Processar eventos `PAYMENT_RECEIVED`, `PAYMENT_OVERDUE`, `PAYMENT_CANCELLED`
- [ ] Adicionar métodos no `PedidoService`:
  - `marcarComoPagoPorAsaasId(String asaasPaymentId)`
  - `cancelarPorFalhaPagamentoAsaas(String pedidoId)`

### Fase 5: Testes
- [ ] Criar `AsaasPaymentServiceTest.java` (mock da API Asaas)
- [ ] Criar `AsaasWebhookControllerTest.java`
- [ ] Atualizar `PedidoServiceTest.java` para testar ambos os fluxos
- [ ] Criar `PedidoFlowIT.java` com teste de integração PIX

### Fase 6: Documentação
- [ ] Atualizar OpenAPI/Swagger no `PedidoController`
- [ ] Documentar webhook no README

---

## 6. Critérios de Aceite

### Para PIX (Asaas)
- [ ] Ao criar pedido com `formaPagamento="PIX"`, retorna `pixCopiaECola` e `qrCodeUrl` válidos
- [ ] Webhook `PAYMENT_RECEIVED` atualiza status para `PAGO`
- [ ] Webhook `PAYMENT_CANCELLED` cancela o pedido
- [ ] Campos `clientSecret` é `null` para pedidos PIX

### Para Cartão/Google Pay (Stripe)
- [ ] Ao criar pedido com `formaPagamento="CARTAO"` ou `"GOOGLE_PAY"`, retorna `clientSecret` válido
- [ ] Campos `pixCopiaECola` e `qrCodeUrl` são `null`
- [ ] Webhook do Stripe continua funcionando normalmente

### Geral
- [ ] Todos os testes existentes continuam passando
- [ ] Novos testes cobrem ambos os fluxos (PIX e Cartão)
- [ ] Documentação Swagger atualizada

---

## 7. Referências

### API Asaas
- **Documentação Oficial**: https://docs.asaas.com/reference
- **Sandbox**: https://sandbox.asaas.com/docs
- **Webhooks**: https://docs.asaas.com/docs/webhooks

### Endpoints Principais
```bash
# Criar cobrança PIX
POST https://sandbox.asaas.com/api/v3/payments
Content-Type: application/json
asaas-api-key: $YOUR_API_KEY

{
  "customer": "cus_123",
  "billingType": "PIX",
  "value": 50.00,
  "dueDate": "2025-01-15",
  "description": "Pedido #a1b2c3d4",
  "externalReference": "a1b2c3d4"
}

# Resposta
{
  "id": "pay_456",
  "pixQrCode": "000201010211...",
  "pixCopyAndPaste": "000201010211..."
}
```

### Segurança do Webhook
- Validar header `asaas-api-key`
- Usar HTTPS em produção
- Logar todos os eventos recebidos

---

## 8. Riscos e Mitigações

| Risco | Mitigação |
|-------|-----------|
| API Asaas indisponível | Implementar retry com backoff exponencial |
| Webhook não chega | Implementar polling de status como fallback |
| Chave API vazada | Usar variáveis de ambiente, nunca commitar no código |
| Diferença entre sandbox e produção | Testar exaustivamente em sandbox antes de ir para produção |

---

## 9. Próximos Passos Imediatos

1. **Obter credenciais Asaas**:
   - Criar conta em https://sandbox.asaas.com
   - Gerar API Key de teste
   
2. **Implementar AsaasPaymentService** (prioridade máxima)

3. **Testar fluxo completo**:
   - Criar pedido PIX
   - Simular pagamento no sandbox
   - Verificar webhook e atualização do pedido

4. **Validar com frontend**:
   - Frontend precisa exibir QR Code e Copia e Cola
   - Implementar polling ou WebSocket para detectar pagamento

---

**Status**: 📝 Especificação criada - Aguardando implementação
**Data**: 2025-01-XX
**Autor**: Assistant
