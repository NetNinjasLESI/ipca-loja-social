# Configuração Firebase - IPCA Loja Social

Este documento contém instruções detalhadas para configurar o Firebase para a aplicação.

## 1. Criar Projeto Firebase

1. Aceder a [Firebase Console](https://console.firebase.google.com/)
2. Clicar em "Adicionar projeto"
3. Nome do projeto: **IPCA Loja Social**
4. Desativar Google Analytics (opcional para desenvolvimento)
5. Criar projeto

## 2. Adicionar App Android

1. No painel do projeto, clicar no ícone Android
2. Configurar:
   - **Package name**: `com.ipca.lojasocial`
   - **App nickname**: IPCA Loja Social
   - **Debug signing certificate SHA-1**: (obter do Android Studio)

Para obter SHA-1:
```bash
cd android (pasta do projeto)
./gradlew signingReport
```

3. Baixar `google-services.json`
4. Colocar em `app/google-services.json`
5. Seguir instruções de configuração do SDK

## 3. Authentication

### Ativar Email/Password

1. Authentication > Sign-in method
2. Ativar "Email/Password"
3. Configurar domínios autorizados:
   - `ipca.pt`
   - `alunos.ipca.pt`

### Criar Utilizadores Iniciais

Criar manualmente alguns utilizadores para teste:

**Administrador:**
- Email: `admin@ipca.pt`
- Password: (definir)
- Role: ADMINISTRATOR

**Colaborador:**
- Email: `colaborador@ipca.pt`
- Password: (definir)
- Role: COLLABORATOR

**Beneficiário:**
- Email: `beneficiario@alunos.ipca.pt`
- Password: (definir)
- Role: BENEFICIARY

## 4. Cloud Firestore

### Iniciar Firestore

1. Firestore Database > Create database
2. Modo: **Production mode**
3. Localização: `europe-west1` (Belgium)

### Regras de Segurança Iniciais

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Função auxiliar para verificar autenticação
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Função para verificar papel do utilizador
    function getUserRole() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role;
    }
    
    // Função para verificar se é colaborador ou admin
    function isCollaboratorOrAdmin() {
      return isAuthenticated() && 
             (getUserRole() == 'COLLABORATOR' || getUserRole() == 'ADMINISTRATOR');
    }
    
    // Função para verificar se é admin
    function isAdmin() {
      return isAuthenticated() && getUserRole() == 'ADMINISTRATOR';
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow write: if isAdmin() || request.auth.uid == userId;
    }
    
    // Beneficiaries collection
    match /beneficiaries/{beneficiaryId} {
      allow read: if isAuthenticated();
      allow write: if isCollaboratorOrAdmin();
    }
    
    // Products collection
    match /products/{productId} {
      allow read: if isAuthenticated();
      allow write: if isCollaboratorOrAdmin();
    }
    
    // Stock movements collection
    match /stock_movements/{movementId} {
      allow read: if isAuthenticated();
      allow create: if isCollaboratorOrAdmin();
      allow update, delete: if isAdmin();
    }
    
    // Kits collection
    match /kits/{kitId} {
      allow read: if isAuthenticated();
      allow write: if isCollaboratorOrAdmin();
    }
    
    // Deliveries collection
    match /deliveries/{deliveryId} {
      allow read: if isAuthenticated();
      allow create: if isCollaboratorOrAdmin();
      allow update: if isAuthenticated();
      allow delete: if isAdmin();
    }
    
    // Campaigns collection
    match /campaigns/{campaignId} {
      allow read: if true; // Público pode ver campanhas
      allow write: if isCollaboratorOrAdmin();
    }
  }
}
```

### Criar Índices

Criar os seguintes índices compostos:

**deliveries:**
- `beneficiaryId` (Ascending) + `scheduledDate` (Descending)
- `status` (Ascending) + `scheduledDate` (Ascending)

**products:**
- `category` (Ascending) + `name` (Ascending)
- `currentStock` (Ascending) + `minimumStock` (Ascending)

**stock_movements:**
- `productId` (Ascending) + `performedAt` (Descending)

## 5. Cloud Storage

### Configurar Storage

1. Storage > Get Started
2. Modo: **Production mode**
3. Localização: `europe-west1`

### Regras de Segurança Storage

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isCollaboratorOrAdmin() {
      return isAuthenticated() && 
        (firestore.get(/databases/(default)/documents/users/$(request.auth.uid)).data.role == 'COLLABORATOR' ||
         firestore.get(/databases/(default)/documents/users/$(request.auth.uid)).data.role == 'ADMINISTRATOR');
    }
    
    // Product images
    match /products/{productId}/{allPaths=**} {
      allow read: if isAuthenticated();
      allow write: if isCollaboratorOrAdmin();
    }
    
    // Campaign images
    match /campaigns/{campaignId}/{allPaths=**} {
      allow read: if true; // Público pode ver
      allow write: if isCollaboratorOrAdmin();
    }
    
    // User profile images
    match /users/{userId}/{allPaths=**} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && request.auth.uid == userId;
    }
  }
}
```

### Estrutura de Pastas Storage

```
/products/{productId}/
  - image.jpg

/campaigns/{campaignId}/
  - banner.jpg

/users/{userId}/
  - profile.jpg
```

## 6. Cloud Messaging (FCM)

### Configurar FCM

1. Project Settings > Cloud Messaging
2. Ativar Cloud Messaging API (legacy)
3. Guardar **Server key** para backend

### Criar Tópicos de Notificação

- `all_users` - Todas as notificações
- `beneficiaries` - Notificações para beneficiários
- `collaborators` - Notificações para colaboradores
- `delivery_reminders` - Lembretes de entrega

## 7. Cloud Functions (Opcional)

### Funções Recomendadas

1. **onDeliveryScheduled** - Enviar notificação quando entrega é agendada
2. **onStockLow** - Alertar quando stock está baixo
3. **onProductExpiring** - Alertar produtos perto da validade
4. **onDeliveryReminder** - Lembrete 1 dia antes da entrega

Exemplo básico:

```javascript
// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Notificar quando entrega é criada
exports.onDeliveryScheduled = functions.firestore
  .document('deliveries/{deliveryId}')
  .onCreate(async (snap, context) => {
    const delivery = snap.data();
    
    // Obter dados do beneficiário
    const beneficiaryDoc = await admin.firestore()
      .collection('beneficiaries')
      .doc(delivery.beneficiaryId)
      .get();
    
    const beneficiary = beneficiaryDoc.data();
    
    // Enviar notificação
    const message = {
      notification: {
        title: 'Nova Entrega Agendada',
        body: `Entrega de ${delivery.kitName} agendada para ${delivery.scheduledDate}`
      },
      token: beneficiary.fcmToken
    };
    
    return admin.messaging().send(message);
  });
```

## 8. Verificação Final

### Checklist

- [ ] App Android configurada no Firebase
- [ ] google-services.json na pasta app/
- [ ] Authentication Email/Password ativado
- [ ] Utilizadores de teste criados
- [ ] Firestore iniciado com regras de segurança
- [ ] Índices compostos criados
- [ ] Storage configurado com regras
- [ ] FCM ativado
- [ ] (Opcional) Cloud Functions deployadas

### Testar Conexão

Após configurar, executar app e verificar:
1. Login funciona
2. Dados são salvos no Firestore
3. Imagens são enviadas para Storage
4. Notificações são recebidas

## 9. Ambiente de Produção

### Considerações

1. **Backups**: Configurar backups automáticos do Firestore
2. **Monitoramento**: Ativar Firebase Performance Monitoring
3. **Analytics**: Configurar Google Analytics for Firebase
4. **Crashlytics**: Implementar Firebase Crashlytics
5. **App Distribution**: Usar para testes beta

### Cusas e Limites

- **Spark (Grátis)**:
  - Firestore: 1GB storage, 50K reads/day
  - Storage: 5GB, 1GB download/day
  - FCM: Ilimitado

- **Blaze (Pay as you go)**:
  - Necessário se exceder limites gratuitos
  - Configurar alertas de orçamento

## Recursos Adicionais

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firestore Data Modeling](https://firebase.google.com/docs/firestore/data-model)
- [Security Rules Guide](https://firebase.google.com/docs/rules)
