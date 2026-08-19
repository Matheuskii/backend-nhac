# 📊 Relatório de Cobertura de Testes - JaCoCo

## 🎯 Visão Geral do Projeto

| Métrica | Total | Coberto | Perdido | Porcentagem |
|---------|-------|---------|---------|-------------|
| **Instruções** | 5,054 | 3,735 | 1,319 | **73%** |
| **Branches** | 227 | 111 | 116 | **48%** |
| **Complexidade Ciclomática** | 362 | 229 | 133 | **63%** |
| **Linhas** | 1,070 | 796 | 274 | **74%** |
| **Métodos** | 246 | 192 | 54 | **78%** |
| **Classes** | 77 | 68 | 9 | **88%** |

---

## 🔴 Classes Críticas que Precisam de Testes

### 1. **AsaasPaymentService** (PRIORIDADE MÁXIMA)
- **Cobertura**: 7% (14 de 185 instruções)
- **Branches**: 0% (0 de 8)
- **Linhas perdidas**: 29 de 33
- **Status**: ❌ Classe nova criada para integração com Asaas PIX - **NECESSITA TESTES URGENTES**

**O que testar:**
- [ ] `criarPagamentoPix()` - método principal de criação de pagamento
- [ ] `processarWebhook()` - processamento de notificações do Asaas
- [ ] Validações de parâmetros
- [ ] Tratamento de erros da API do Asaas
- [ ] Mock da API REST do Asaas

---

### 2. **AsaasWebhookController** (PRIORIDADE MÁXIMA)
- **Cobertura**: 5% (9 de 153 instruções)
- **Branches**: 0% (0 de 34)
- **Linhas perdidas**: 35 de 39
- **Status**: ❌ Controller novo para webhooks do Asaas - **NECESSITA TESTES URGENTES**

**O que testar:**
- [ ] Endpoint `/api/asaas/webhook`
- [ ] Validação de assinatura do webhook
- [ ] Processamento de eventos: `PAYMENT_CREATED`, `PAYMENT_SUCCESS`, `PAYMENT_OVERDUE`
- [ ] Atualização de status do pedido
- [ ] Cenários de erro e payload inválido

---

### 3. **StripePaymentService** (PRIORIDADE ALTA)
- **Cobertura**: 10% (7 de 66 instruções)
- **Branches**: N/A
- **Linhas perdidas**: 17 de 20
- **Status**: ⚠️ Classe modificada para cartão/Google Pay - **TESTES ATUALIZADOS NECESSÁRIOS**

**O que testar:**
- [ ] `criarPaymentIntentCartao()` - novo método no lugar de `criarPaymentIntentPix()`
- [ ] Configuração `automatic_payment_methods.enabled=true`
- [ ] Retorno de `pixCopiaECola=null` e `qrCodeUrl=null`
- [ ] Integração com Stripe SDK

---

### 4. **PedidoService** (PRIORIDADE ALTA)
- **Cobertura**: 73% (340 de 463 instruções)
- **Branches**: 60% (28 de 46)
- **Linhas perdidas**: 20 de 96
- **Status**: ⚠️ Modificado para suportar múltiplos métodos de pagamento

**O que testar adicionalmente:**
- [ ] Fluxo com `formaPagamento="PIX"` → chama Asaas
- [ ] Fluxo com `formaPagamento="CARTAO"` → chama Stripe
- [ ] Fluxo com `formaPagamento="GOOGLE_PAY"` → chama Stripe
- [ ] Validação de forma de pagamento inválida
- [ ] Branches não cobertos na lógica de seleção de gateway

---

### 5. **PedidoResponseDTO$EnderecoEntregaResponseDTO** (PRIORIDADE MÉDIA)
- **Cobertura**: 0% (0 de 24 instruções)
- **Status**: ❌ Inner class sem testes

**O que testar:**
- [ ] Getters e setters
- [ ] Builder pattern (se aplicável)
- [ ] Validações de campos

---

### 6. **PedidoResponseDTO$ItemPedidoResponseDTO** (PRIORIDADE MÉDIA)
- **Cobertura**: 0% (0 de 21 instruções)
- **Status**: ❌ Inner class sem testes

**O que testar:**
- [ ] Getters e setters
- [ ] Validações de campos

---

### 7. **VerificacaoTelefoneService** (PRIORIDADE MÉDIA)
- **Cobertura**: 53% (149 de 279 instruções)
- **Branches**: 50% (4 de 8)
- **Linhas perdidas**: 31 de 66
- **Status**: ⚠️ Cobertura insuficiente

**O que testar adicionalmente:**
- [ ] Cenários de expiração de código
- [ ] Múltiplas tentativas falhas
- [ ] Integração com Twilio em cenários de erro

---

### 8. **LojaService** (PRIORIDADE MÉDIA)
- **Cobertura**: 50% (46 de 91 instruções)
- **Branches**: 50% (3 de 6)
- **Linhas perdidas**: 8 de 18
- **Status**: ⚠️ Cobertura insuficiente

**O que testar adicionalmente:**
- [ ] Validações de negócio não cobertas
- [ ] Cenários de erro

---

## 🟡 Outras Classes com Cobertura Abaixo de 70%

| Classe | Cobertura | Instruções | Branches | Linhas |
|--------|-----------|------------|----------|--------|
| UsuarioService | 79% | 298/376 | 52% | 16/83 |
| StripeWebhookController | 21% | 21/96 | 15% | 19/27 |
| PedidoResumoDTO | 83% | 40/48 | 50% | 0/9 |
| PedidoResponseDTO | 65% | 83/126 | 50% | 13/31 |

---

## ✅ Classes com Cobertura Adequada (>90%)

| Classe | Cobertura | Status |
|--------|-----------|--------|
| SmsAuthService | 100% | ✅ |
| ProdutoService | 97% | ✅ |
| GoogleAuthService | 99% | ✅ |
| AvaliacaoService | 92% | ✅ |
| TwilioSmsService | 92% | ✅ |
| PedidoController | 100% | ✅ |
| PedidoCriadoDTO | 100% | ✅ |
| PedidoCreateDTO | 100% | ✅ |

---

## 📋 Plano de Ação Recomendado

### Fase 1: Crítica (Implementação Recente - Asaas + Stripe)
1. **Criar `AsaasPaymentServiceTest.java`**
   - Mock de `RestTemplate` ou `WebClient` para API do Asaas
   - Testar criação de pagamento PIX
   - Testar processamento de webhooks
   - Cobrir todos os cenários de erro

2. **Criar `AsaasWebhookControllerTest.java`**
   - Mock de `AsaasPaymentService`
   - Testar endpoint de webhook
   - Validar assinatura (se implementado)
   - Testar diferentes tipos de evento

3. **Atualizar `StripePaymentServiceTest.java`**
   - Remover testes do método antigo `criarPaymentIntentPix()`
   - Criar testes para `criarPaymentIntentCartao()`
   - Validar configuração de Google Pay

4. **Atualizar `PedidoServiceTest.java`**
   - Adicionar testes para fluxo com Asaas (PIX)
   - Adicionar testes para fluxo com Stripe (Cartão/Google Pay)
   - Mock de ambos os serviços de pagamento

### Fase 2: DTOs sem Cobertura
5. **Criar testes para inner classes de `PedidoResponseDTO`**
   - `PedidoResponseDTO$EnderecoEntregaResponseDTO`
   - `PedidoResponseDTO$ItemPedidoResponseDTO`

### Fase 3: Melhorar Cobertura Existente
6. **Completar testes de `VerificacaoTelefoneService`**
7. **Completar testes de `LojaService`**
8. **Completar testes de `UsuarioService`**
9. **Adicionar testes de `StripeWebhookController`** (atualmente apenas 2 testes)

---

## 🎯 Meta de Cobertura

| Categoria | Atual | Meta | Gap |
|-----------|-------|------|-----|
| **Instruções** | 73% | 85% | +12% |
| **Branches** | 48% | 75% | +27% |
| **Classes** | 88% | 95% | +7% |

---

## 📁 Arquivos de Teste a Criar

```
src/test/java/br/com/nhac/backend_nhac/services/
├── AsaasPaymentServiceTest.java          ❌ NÃO EXISTE - PRIORIDADE MÁXIMA
├── StripePaymentServiceTest.java         ⚠️ ATUALIZAR
└── PedidoServiceTest.java                ⚠️ ATUALIZAR

src/test/java/br/com/nhac/backend_nhac/domain/pedido/
├── AsaasWebhookControllerTest.java       ❌ NÃO EXISTE - PRIORIDADE MÁXIMA
└── StripeWebhookControllerTest.java      ⚠️ ADICIONAR MAIS TESTES
```

---

## 🔧 Comandos Úteis

```bash
# Rodar testes e gerar relatório JaCoCo
mvn clean test jacoco:report

# Abrir relatório HTML
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux
start target/site/jacoco/index.html  # Windows

# Rodar apenas testes específicos
mvn test -Dtest=AsaasPaymentServiceTest
mvn test -Dtest=PedidoServiceTest

# Verificar cobertura mínima (configurar no pom.xml)
mvn clean verify
```

---

## 📝 Notas Importantes

1. **AsaasPaymentService** e **AsaasWebhookController** foram criados recentemente e ainda não possuem testes
2. A migração do Stripe de PIX para Cartão/Google Pay foi realizada, mas os testes precisam ser atualizados
3. O `.env.example` foi atualizado com as configurações do Asaas
4. Webhooks do Stripe já possuem cobertura parcial (2 testes existentes)
5. Controllers em geral têm boa cobertura, exceto os novos webhooks do Asaas

---

**Gerado em**: 2026-08-19  
**Ferramenta**: JaCoCo 0.8.12  
**Total de Testes**: 150 (0 falhas)
