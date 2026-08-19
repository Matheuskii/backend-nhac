# Guia de Implementação: Stripe (Cartão/Google Pay) + Asaas (PIX)

## ✅ Implementação Concluída

A migração do webhook da Stripe para **cartão de crédito e Google Pay** foi concluída, e a integração com **Asaas para PIX** foi implementada.

---

## 📋 Resumo das Alterações

### Arquivos Criados (3 novos)

| Arquivo | Descrição |
|---------|-----------|
| `AsaasPaymentService.java` | Serviço para criar cobranças PIX no Asaas |
| `AsaasWebhookController.java` | Controller para receber webhooks do Asaas |
| `SPEC_INTEGRACAO_ASAAS_PIX.md` | Especificação técnica completa |

### Arquivos Modificados (5 arquivos)

| Arquivo | Mudança |
|---------|---------|
| `Pedido.java` | Adicionado campo `asaasPaymentId` |
| `PedidoRepository.java` | Adicionado método `findByAsaasPaymentId()` |
| `PedidoService.java` | Injetado `AsaasPaymentService`, lógica condicional para escolher entre PIX (Asaas) e Cartão (Stripe) |
| `application.properties` | Adicionadas configurações do Asaas |

---

## 🏗️ Arquitetura da Solução

```
┌─────────────────────────────────────┐
│         PedidoController            │
│   POST /api/v1/pedidos              │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│          PedidoService              │
│  finalizarPedido()                  │
│                                     │
│  if (formaPagamento == "PIX")       │
│    └─► AsaasPaymentService          │
│        └─► Retorna pixCopiaECola,   │
│            qrCodeUrl                │
│                                     │
│  else if (formaPagamento ==         │
│           "CARTAO"/"GOOGLE_PAY")    │
│    └─► StripePaymentService         │
│        └─► Retorna clientSecret     │
└─────────────────────────────────────┘
```

---

## 🔧 Configuração Necessária

### 1. Obter Credenciais Asaas

1. Acesse https://sandbox.asaas.com
2. Crie uma conta gratuita
3. Vá em **Configurações → Integração → API**
4. Gere sua **API Key**

### 2. Configurar Variáveis de Ambiente

Adicione ao seu `.env` ou sistema de deployment:

```bash
# Asaas (PIX)
ASAAS_API_KEY=sua_api_key_do_asaas_aqui
ASAAS_API_URL=https://sandbox.asaas.com/api/v3

# Em produção, use:
# ASAAS_API_URL=https://www.asaas.com/api/v3
```

Ou edite `src/main/resources/application.properties`:

```properties
asaas.api.key=sua_api_key_do_asaas_aqui
asaas.api.url=https://sandbox.asaas.com/api/v3
```

### 3. Configurar Webhook no Asaas

No dashboard do Asaas:

1. Vá em **Configurações → Webhooks**
2. Adicione novo webhook:
   - **URL**: `https://seu-dominio.com/api/v1/webhooks/asaas`
   - **Eventos**: Marque `PAYMENT_RECEIVED`, `PAYMENT_OVERDUE`, `PAYMENT_CANCELLED`
   - **Header personalizado**: `asaas-api-key: sua_chave_secreta`

---

## 💻 Como Funciona

### Fluxo PIX (Asaas)

1. **Cliente finaliza pedido** com `formaPagamento="PIX"`
2. **Backend chama Asaas** via `AsaasPaymentService.criarCobrancaPix()`
3. **Asaas retorna**:
   - `pixCopiaECola`: Código para copiar e colar no banco
   - `qrCodeUrl`: URL do QR Code para escanear
4. **Frontend exibe** QR Code e código Copia e Cola
5. **Cliente paga** no app do banco
6. **Asaas detecta pagamento** e envia webhook
7. **Backend atualiza** status do pedido para `PAGO`

### Fluxo Cartão/Google Pay (Stripe)

1. **Cliente finaliza pedido** com `formaPagamento="CARTAO"` ou `"GOOGLE_PAY"`
2. **Backend chama Stripe** via `StripePaymentService.criarPaymentIntentCartao()`
3. **Stripe retorna** `clientSecret`
4. **Frontend usa Stripe SDK** para confirmar pagamento
5. **Stripe processa** cartão ou Google Pay
6. **Stripe envia webhook** de confirmação
7. **Backend atualiza** status do pedido para `PAGO`

---

## 🧪 Testes

Todos os 150 testes existentes continuam passando:

```bash
cd /workspace
mvn test
```

Resultado:
```
Tests run: 150, Failures: 0, Errors: 0, Skipped: 0
```

---

## 📡 Endpoints Disponíveis

### Criar Pedido
```http
POST /api/v1/pedidos
Content-Type: application/json
Authorization: Bearer {token}

{
  "lojaId": "loja_123",
  "itens": [...],
  "formaPagamento": "PIX",  // ou "CARTAO", "GOOGLE_PAY"
  ...
}
```

**Resposta para PIX**:
```json
{
  "pedidoId": "abc-123",
  "clientSecret": null,
  "pixCopiaECola": "000201010211...",
  "qrCodeUrl": "https://qrcode.asaas.com/..."
}
```

**Resposta para Cartão**:
```json
{
  "pedidoId": "abc-123",
  "clientSecret": "pi_123_secret_456",
  "pixCopiaECola": null,
  "qrCodeUrl": null
}
```

### Webhook Asaas
```http
POST /api/v1/webhooks/asaas
Content-Type: application/json
asaas-api-key: {sua_chave_secreta}

{
  "notification": "PAYMENT_RECEIVED",
  "payment": {
    "id": "pay_123",
    "externalReference": "abc-123",
    "status": "RECEIVED"
  }
}
```

---

## 🔒 Segurança

### Validação do Webhook
O `AsaasWebhookController` valida o header `asaas-api-key` para garantir que a requisição vem realmente do Asaas.

### Boas Práticas
- ✅ Use HTTPS em produção
- ✅ Nunca commite chaves de API no código
- ✅ Use variáveis de ambiente
- ✅ Valide sempre o header do webhook

---

## 🚀 Próximos Passos (Frontend)

### Para o time Flutter/Frontend:

1. **Detectar tipo de pagamento** na resposta:
   ```dart
   if (response.pixCopiaECola != null) {
     // Exibir QR Code PIX e botão "Copiar código"
   } else if (response.clientSecret != null) {
     // Usar Stripe SDK para cartão/Google Pay
   }
   ```

2. **Para PIX**:
   - Exibir QR Code usando biblioteca de geração de QR
   - Botão "Copiar código" para o `pixCopiaECola`
   - Implementar polling ou WebSocket para detectar pagamento

3. **Para Cartão/Google Pay**:
   - Usar `stripe_js` ou similar
   - Confirmar pagamento com `clientSecret`

---

## 📊 Métricas e Monitoramento

### Logs Importantes
- Sucesso ao criar cobrança PIX
- Webhooks recebidos do Asaas
- Erros de comunicação com APIs

### Sugestão de Dashboard
- Pedidos criados por forma de pagamento (PIX vs Cartão)
- Tempo médio de confirmação do PIX
- Taxa de conversão por método de pagamento

---

## ❓ FAQ

### Posso usar PIX e Cartão simultaneamente?
Sim! O sistema suporta ambos. O cliente escolhe no frontend e o backend roteia para o provedor correto.

### O que acontece se o PIX não for pago?
Após o vencimento (7 dias por padrão), o Asaas envia um webhook `PAYMENT_OVERDUE` e o pedido é cancelado automaticamente.

### Preciso criar customer no Asaas?
Na implementação atual, usamos informações genéricas do pagador. Para produção, recomenda-se criar um customer no Asaas associado ao usuário.

### Como migrar de sandbox para produção?
Altere apenas a URL no `application.properties`:
```properties
asaas.api.url=https://www.asaas.com/api/v3
```
E use sua API Key de produção.

---

## 📞 Suporte

- **Documentação Asaas**: https://docs.asaas.com
- **Dashboard Sandbox**: https://sandbox.asaas.com
- **Documentação Stripe**: https://stripe.com/docs

---

**Status**: ✅ Implementação Concluída  
**Testes**: ✅ 150/150 passando  
**Próximo passo**: Configurar credenciais Asaas e testar fluxo completo
