Atualizações/Melhorias:
- Refatoração do ViewModel seguindo princípios Clean Architecture com separação adequada entre camadas de apresentação, domínio e dados conforme orientação do professor
- Implementação de sistema de monitorização de candidaturas com otimização de filtros na camada de domínio
- Funcionalidade de cancelamento de entregas por beneficiários
- Atualização e melhorias no histórico de entregas
- Alteração do modelo de campanhas de valores monetários para sistema de kits alimentares
- Implementação de sistema de código de barras para gestão de produtos e entregas
- Funcionalidade para beneficiários realizarem pedidos diretamente na aplicação
- Correções de bugs e melhorias gerais na experiência do utilizador

# IPCA Loja Social - Aplicação Android

Aplicação móvel nativa Android para gestão da Loja Social dos Serviços de Ação Social (SAS) do IPCA.

## 📱 Visão Geral

Aplicação destinada ao suporte operacional da Loja Social do IPCA, que apoia estudantes em situação de vulnerabilidade socioeconómica através da gestão e distribuição de bens essenciais (alimentares, higiene e limpeza).

## 🏗️ Arquitetura

O projeto segue **Clean Architecture** com três camadas principais:

### 1. Presentation Layer (`presentation/`)
- **UI (Jetpack Compose)**: Interface 100% declarativa sem XML
- **ViewModels**: Gestão de estado com StateFlow
- **Navigation**: Navegação entre telas

### 2. Domain Layer (`domain/`)
- **Models**: Entidades de domínio
- **Use Cases**: Lógica de negócio
- **Repository Interfaces**: Contratos para acesso a dados

### 3. Data Layer (`data/`)
- **Repository Implementations**: Implementação dos contratos
- **Data Sources**: Firebase, DataStore
- **DTOs**: Modelos de transferência de dados

## 🛠️ Stack Tecnológico

### Core
- **Kotlin 1.9+**
- **Jetpack Compose** - UI 100% declarativa
- **Material Design 3** - Design system moderno
- **Clean Architecture** - Separação de responsabilidades

### Arquitetura & Injeção de Dependências
- **Hilt** - Dependency Injection
- **ViewModel + StateFlow** - Gestão de estado
- **Coroutines + Flow** - Programação assíncrona

### Backend & Persistência
- **Firebase Authentication** - Autenticação
- **Cloud Firestore** - Base de dados
- **Cloud Storage** - Armazenamento de imagens
- **Cloud Messaging** - Notificações push
- **DataStore** - Preferências locais

### Network & Imagens
- **Retrofit + OkHttp** - Cliente HTTP
- **Coil** - Carregamento de imagens

### Outras
- **Navigation Compose** - Navegação
- **Accompanist** - Utilitários Compose
- **ZXing** - Scanner código de barras

## 📂 Estrutura de Pastas

```
app/src/main/java/com/ipca/lojasocial/
├── presentation/
│   ├── ui/
│   │   ├── theme/              # Tema e cores IPCA
│   │   ├── components/         # Componentes reutilizáveis
│   │   ├── screens/            # Telas da aplicação
│   │   │   ├── auth/          # Autenticação
│   │   │   ├── beneficiary/   # Telas do beneficiário
│   │   │   ├── collaborator/  # Telas do colaborador
│   │   │   └── public/        # Telas públicas
│   │   └── navigation/         # Navegação
│   └── viewmodel/              # ViewModels
├── domain/
│   ├── model/                  # Entidades de domínio
│   ├── usecase/                # Casos de uso
│   │   ├── auth/
│   │   ├── beneficiary/
│   │   ├── inventory/
│   │   ├── kit/
│   │   ├── delivery/
│   │   ├── campaign/
│   │   └── report/
│   └── repository/             # Interfaces dos repositórios
├── data/
│   ├── repository/             # Implementações dos repositórios
│   ├── datasource/
│   │   ├── firebase/          # Data sources Firebase
│   │   └── preferences/       # DataStore
│   └── model/                  # DTOs
└── di/                         # Módulos Hilt
```

## 👥 Perfis de Utilizador

### 1. Beneficiário (Autenticado)
- Login com email institucional IPCA
- Consulta de perfil e histórico
- Gestão de entregas (confirmação, reagendamento)
- Notificações

### 2. Colaborador SAS (Autenticado)
- Login com email institucional IPCA
- Gestão completa de:
  - Beneficiários
  - Inventário e stock
  - Kits
  - Entregas
  - Campanhas
  - Relatórios e dashboard

### 3. Utilizador Público (Não Autenticado)
- Visualização de campanhas
- Informações institucionais
- Contactos e horários

## 🗄️ Modelo de Dados (Firestore)

### Collections Principais
- `users` - Utilizadores (colaboradores e beneficiários)
- `beneficiaries` - Informação adicional de beneficiários
- `products` - Produtos em inventário
- `stock_movements` - Movimentações de stock
- `kits` - Kits de produtos
- `deliveries` - Entregas agendadas e realizadas
- `campaigns` - Campanhas de doação

### Relações
- User → StockMovement (1:N)
- User → Delivery (1:N)
- Beneficiary → Delivery (1:N)
- Kit → Delivery (1:N)
- Kit → KitItem (1:N)
- Product → KitItem (1:N)
- Product → StockMovement (1:N)

## 🎨 Design System IPCA

### Cores Principais
- **IPCA Green**: `#00853E` - Verde institucional
- **IPCA Green Dark**: `#006B32`
- **IPCA Green Light**: `#4CAF50`

### Componentes Personalizados
- `IPCAButton` - Botão primário
- `IPCAOutlinedButton` - Botão secundário
- `IPCATextField` - Campo de texto
- `IPCAPasswordField` - Campo de password
- `LoadingIndicator` - Indicador de carregamento
- `ErrorMessage` - Mensagem de erro
- `EmptyState` - Estado vazio

## 🚀 Como Começar

### 1. Configurar Firebase

1. Criar projeto no [Firebase Console](https://console.firebase.google.com/)
2. Adicionar app Android com package name: `com.ipca.lojasocial`
3. Baixar `google-services.json` e colocar em `app/`
4. Ativar serviços:
   - Authentication (Email/Password)
   - Cloud Firestore
   - Cloud Storage
   - Cloud Messaging

### 2. Estrutura Firestore

Criar as seguintes collections no Firestore:

```
users/
  {userId}/
    - email: string
    - name: string
    - role: string (BENEFICIARY | COLLABORATOR | ADMINISTRATOR)
    - isActive: boolean
    - createdAt: timestamp
    - updatedAt: timestamp

beneficiaries/
  {beneficiaryId}/
    - userId: string (ref to users)
    - studentNumber: string
    - course: string
    - phoneNumber: string
    - ...

products/
  {productId}/
    - name: string
    - category: string
    - currentStock: number
    - minimumStock: number
    - ...

deliveries/
  {deliveryId}/
    - beneficiaryId: string
    - kitId: string
    - scheduledDate: timestamp
    - status: string
    - ...

campaigns/
  {campaignId}/
    - title: string
    - description: string
    - isActive: boolean
    - isPublic: boolean
    - ...
```

### 3. Compilar e Executar

```bash
# Clone o repositório
git clone [url-do-repositorio]

# Abrir no Android Studio
# File > Open > Selecionar pasta do projeto

# Sincronizar Gradle
# Aguardar download de dependências

# Executar
# Run > Run 'app'
```

## 📋 Requisitos Funcionais Implementados

- ✅ RF01 - Autenticação e Perfis
  - Login com email institucional
  - Gestão de sessão
  - Verificação de permissões

(Os demais RF serão implementados progressivamente)

## 🔄 Próximos Passos

### Prioridade Alta
1. Implementar telas de Beneficiário
2. Implementar Dashboard de Colaborador
3. Gestão de Inventário
4. Sistema de Entregas

### Prioridade Média
5. Gestão de Kits
6. Sistema de Notificações
7. Gestão de Campanhas
8. Relatórios e Analytics

### Prioridade Baixa
9. Scanner de código de barras
10. Exportação de relatórios (PDF/Excel)
11. Modo offline
12. Testes automatizados

## 📝 Convenções de Código

### Nomenclatura
- **Classes**: PascalCase (ex: `ProductRepository`)
- **Funções**: camelCase (ex: `signIn()`)
- **Variáveis**: camelCase (ex: `currentUser`)
- **Constantes**: UPPER_SNAKE_CASE (ex: `MAX_RETRY_COUNT`)

### Compose
- Funções @Composable em PascalCase
- Parâmetros modifier sempre primeiro
- Eventos com prefixo `on` (ex: `onClick`)

### Comentários
- KDoc para classes e funções públicas
- Comentários inline para lógica complexa
- TODO para funcionalidades pendentes

## 🧪 Testing (Futuro)

```
app/src/test/          # Testes unitários
app/src/androidTest/   # Testes de integração
```

## 📄 Licença

Projeto académico - IPCA © 2025

## 👨‍💻 Desenvolvimento

Desenvolvido como projeto para os Serviços de Ação Social do IPCA.

## 📞 Contacto

Para questões sobre o projeto, contactar através do email institucional.

---

**Nota**: Este é um projeto base com a estrutura inicial. As funcionalidades serão implementadas progressivamente seguindo os requisitos funcionais especificados.
