# IPCA Loja Social - Visão Geral do Projeto

## 📱 Sobre o Projeto

Aplicação móvel Android nativa desenvolvida para digitalizar e otimizar a gestão da Loja Social dos Serviços de Ação Social (SAS) do Instituto Politécnico do Cávado e do Ave (IPCA).

### Objetivo Principal

Facilitar a gestão e distribuição de bens essenciais (alimentares, higiene e limpeza) a estudantes em situação de vulnerabilidade socioeconómica, melhorando a eficiência operacional e a experiência dos beneficiários.

## 🎯 Funcionalidades Principais

### Para Beneficiários
- ✅ Login seguro com email institucional
- 📋 Consulta de perfil e histórico de entregas
- 📅 Visualização de entregas agendadas
- ✔️ Confirmação de recebimento
- 🔄 Reagendamento de entregas
- 🔔 Notificações de entregas e atualizações

### Para Colaboradores SAS
- 👥 Gestão completa de beneficiários
- 📦 Controlo de inventário e stock
- 📊 Movimentações de entrada/saída
- 🎁 Criação e gestão de kits
- 🚚 Agendamento e gestão de entregas
- 📱 Scanner de código de barras
- 📈 Dashboard com indicadores operacionais
- 📊 Relatórios e estatísticas
- 🎗️ Gestão de campanhas

### Para Público Geral
- 📢 Visualização de campanhas ativas
- ℹ️ Informações sobre a Loja Social
- 📞 Contactos e horários
- 🏢 Locais de entrega

## 🏗️ Arquitetura Técnica

### Clean Architecture

```
┌─────────────────────────────────────────────┐
│         Presentation Layer                   │
│  (UI, ViewModels, Navigation)                │
│  - 100% Jetpack Compose                      │
│  - Material Design 3                         │
│  - StateFlow para gestão de estado           │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│          Domain Layer                        │
│  (Entidades, Use Cases, Interfaces)          │
│  - Lógica de negócio pura                    │
│  - Independente de frameworks                │
│  - Regras de validação                       │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│          Data Layer                          │
│  (Repositories, Data Sources)                │
│  - Firebase (Auth, Firestore, Storage, FCM) │
│  - DataStore (preferências locais)           │
│  - Cache e sincronização                     │
└─────────────────────────────────────────────┘
```

### Stack Tecnológico

**Frontend:**
- Kotlin 1.9+
- Jetpack Compose (UI 100% declarativa)
- Material Design 3
- Hilt (Dependency Injection)
- Navigation Compose
- Coroutines & Flow

**Backend:**
- Firebase Authentication
- Cloud Firestore
- Cloud Storage
- Cloud Messaging (FCM)
- Cloud Functions (opcional)

**Outras Bibliotecas:**
- Coil (carregamento de imagens)
- Retrofit + OkHttp (API calls)
- DataStore (preferências)
- ZXing (scanner de código de barras)

## 📊 Modelo de Dados

### Entidades Principais

1. **User** - Utilizadores do sistema
2. **Beneficiary** - Dados específicos de estudantes beneficiários
3. **Product** - Produtos em inventário
4. **Kit** - Conjuntos de produtos para entrega
5. **Delivery** - Entregas agendadas/realizadas
6. **StockMovement** - Movimentações de stock
7. **Campaign** - Campanhas de doação

### Relações
- User → StockMovement (1:N)
- User → Delivery (1:N)
- Beneficiary → Delivery (1:N)
- Kit → Delivery (1:N)
- Kit → KitItem (1:N)
- Product → KitItem (1:N)
- Product → StockMovement (1:N)

## 🎨 Design System IPCA

### Cores Institucionais
- **Primary**: Verde IPCA (#00853E)
- **Secondary**: Verde Claro (#4CAF50)
- **Accent**: Verde Escuro (#006B32)

### Princípios de Design
- ✨ Minimalista e funcional
- 📱 Mobile-first
- ♿ Acessível (WCAG 2.1)
- 🎯 Foco na usabilidade
- 💚 Identidade visual IPCA

### Componentes Personalizados
- IPCAButton
- IPCATextField
- IPCAPasswordField
- LoadingIndicator
- ErrorMessage
- EmptyState
- StatCard
- AlertCard

## 🚀 Fluxos Principais

### 1. Login e Autenticação
```
Tela Login
    ↓
Validar credenciais (Firebase Auth)
    ↓
Obter dados do utilizador (Firestore)
    ↓
Redirecionar baseado no role:
    - Beneficiário → Home Beneficiário
    - Colaborador → Dashboard
```

### 2. Agendamento de Entrega
```
Dashboard Colaborador
    ↓
Selecionar Beneficiário
    ↓
Escolher Kit
    ↓
Verificar stock disponível
    ↓
Agendar data/hora
    ↓
Criar entrega (Firestore)
    ↓
Atualizar stock (Transaction)
    ↓
Enviar notificação (FCM)
```

### 3. Gestão de Stock
```
Adicionar Produto
    ↓
Scan código de barras (opcional)
    ↓
Preencher informações
    ↓
Salvar em Firestore
    ↓
Registar movimentação de entrada
    ↓
Verificar alertas (stock baixo / validade)
```

## 📁 Estrutura do Projeto

```
ipca-loja-social/
├── app/
│   ├── src/main/
│   │   ├── java/com/ipca/lojasocial/
│   │   │   ├── presentation/      # UI, ViewModels, Navigation
│   │   │   ├── domain/            # Entidades, Use Cases, Repositories
│   │   │   ├── data/              # Implementações, Data Sources
│   │   │   └── di/                # Módulos Hilt
│   │   ├── res/                   # Recursos (strings, drawables)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml         # Catálogo de versões
├── build.gradle.kts
├── settings.gradle.kts
├── README.md                      # Documentação principal
├── FIREBASE_SETUP.md              # Guia de configuração Firebase
├── FIRESTORE_STRUCTURE.md         # Estrutura de dados detalhada
└── PROJECT_OVERVIEW.md            # Este arquivo
```

## 📋 Requisitos Funcionais

### ✅ Implementados (Fase 1)
- RF01: Autenticação e perfis
  - Login com email institucional
  - Gestão de sessão
  - Verificação de permissões

### 🔨 Em Desenvolvimento (Fase 2)
- RF02: Gestão de beneficiários
- RF03: Gestão de inventário
- RF05: Gestão de entregas

### 📝 Planejados (Fase 3)
- RF04: Gestão de kits
- RF06: Gestão de campanhas
- RF08: Relatórios e dashboard

## 🔒 Segurança

### Autenticação
- Firebase Authentication com Email/Password
- Validação de domínios institucionais (@ipca.pt, @alunos.ipca.pt)
- Gestão segura de tokens

### Autorização
- Role-Based Access Control (RBAC)
- Regras de segurança Firestore
- Validação de permissões em cada operação

### Proteção de Dados
- RGPD compliant
- Criptografia de dados em trânsito (HTTPS)
- Dados pessoais minimizados
- Soft delete com campo isActive

## 📊 Métricas e Analytics

### KPIs Principais
- Número de beneficiários ativos
- Entregas realizadas por mês
- Taxa de utilização de stock
- Tempo médio de processamento
- Taxa de confirmação de entregas

### Firebase Analytics
- Eventos customizados
- Funis de conversão
- Comportamento do utilizador
- Crashes e erros

## 🧪 Testing (Futuro)

### Testes Unitários
- Use Cases
- ViewModels
- Repositórios
- Validações

### Testes de Integração
- Fluxos completos
- Interação com Firebase
- Navegação

### Testes UI
- Compose Testing
- Cenários end-to-end

## 🚀 Deploy e Distribuição

### Desenvolvimento
- Firebase App Distribution para testes internos
- Versões de desenvolvimento com dados de teste

### Produção
- Google Play Store (internal testing → beta → production)
- Versionamento semântico
- Changelog automático

## 📈 Roadmap

### Q1 2025
- ✅ Estrutura base do projeto
- ✅ Autenticação e login
- 🔄 Dashboard de colaborador
- 🔄 Gestão de beneficiários básica
- 🔄 Gestão de inventário

### Q2 2025
- Sistema completo de entregas
- Notificações push
- Scanner de código de barras
- Gestão de kits
- Relatórios básicos

### Q3 2025
- Campanhas públicas
- Analytics avançado
- Exportação de relatórios
- Modo offline
- Otimizações de performance

### Q4 2025
- Testes beta com utilizadores reais
- Refinamentos baseados em feedback
- Documentação completa
- Preparação para produção

## 👥 Equipa

**Desenvolvedor:** [Nome]
**Orientador:** [Nome]
**Cliente:** Serviços de Ação Social - IPCA

## 📞 Suporte

**Email:** sas@ipca.pt  
**Website:** https://www.ipca.pt/

## 📄 Licença

© 2025 IPCA - Instituto Politécnico do Cávado e do Ave  
Projeto académico desenvolvido para os Serviços de Ação Social

---

**Última Atualização:** 02 de Janeiro de 2025  
**Versão:** 1.0.0 (Estrutura Base)
