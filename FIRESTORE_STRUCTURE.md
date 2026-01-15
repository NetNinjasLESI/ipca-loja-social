# Estrutura de Dados Firestore - IPCA Loja Social

Este documento detalha a estrutura completa das collections no Firestore.

## Collections Principais

### 1. users

Armazena informações de todos os utilizadores (colaboradores e beneficiários).

**Path**: `/users/{userId}`

**Campos**:
```json
{
  "id": "abc123def456",
  "email": "joao.silva@alunos.ipca.pt",
  "name": "João Silva",
  "role": "BENEFICIARY", // BENEFICIARY | COLLABORATOR | ADMINISTRATOR
  "isActive": true,
  "createdAt": "2025-01-02T10:00:00Z",
  "updatedAt": "2025-01-02T10:00:00Z",
  "profileImageUrl": "https://storage.firebase.com/users/abc123/profile.jpg",
  "fcmToken": "firebase-cloud-messaging-token"
}
```

**Índices**:
- `email` (Ascending)
- `role` (Ascending)

---

### 2. beneficiaries

Informações adicionais específicas dos beneficiários.

**Path**: `/beneficiaries/{beneficiaryId}`

**Campos**:
```json
{
  "id": "ben123abc",
  "userId": "abc123def456", // Referência para users
  "studentNumber": "20231234",
  "course": "Engenharia Informática",
  "academicYear": 2,
  "phoneNumber": "+351912345678",
  "address": "Rua Exemplo, 123, Barcelos",
  "familySize": 4,
  "monthlyIncome": 800.50,
  "observations": "Estudante deslocado, necessita apoio alimentar",
  "isActive": true,
  "registeredAt": "2025-01-02T10:00:00Z",
  "registeredBy": "collab456", // userId do colaborador
  "lastUpdatedBy": "collab456",
  "updatedAt": "2025-01-02T10:00:00Z"
}
```

**Índices**:
- `userId` (Ascending)
- `studentNumber` (Ascending)
- `isActive` (Ascending)

---

### 3. products

Catálogo de produtos disponíveis no inventário.

**Path**: `/products/{productId}`

**Campos**:
```json
{
  "id": "prod789xyz",
  "name": "Arroz Carolino",
  "description": "Arroz carolino branco, 1kg",
  "category": "FOOD", // FOOD | HYGIENE | CLEANING | OTHER
  "barcode": "5601234567890",
  "unit": "KILOGRAM", // UNIT | KILOGRAM | LITER | PACKAGE
  "currentStock": 45.5,
  "minimumStock": 20.0,
  "expiryDate": "2025-12-31T00:00:00Z",
  "imageUrl": "https://storage.firebase.com/products/prod789/image.jpg",
  "isActive": true,
  "createdAt": "2025-01-02T10:00:00Z",
  "createdBy": "collab456",
  "updatedAt": "2025-01-02T10:00:00Z"
}
```

**Índices**:
- `category` (Ascending) + `name` (Ascending)
- `barcode` (Ascending)
- `currentStock` (Ascending) + `minimumStock` (Ascending)
- `expiryDate` (Ascending)

---

### 4. stock_movements

Registo de todas as movimentações de stock (entradas, saídas, ajustes).

**Path**: `/stock_movements/{movementId}`

**Campos**:
```json
{
  "id": "move123abc",
  "productId": "prod789xyz",
  "productName": "Arroz Carolino",
  "type": "ENTRY", // ENTRY | EXIT | ADJUSTMENT | TRANSFER
  "quantity": 10.0,
  "unit": "KILOGRAM",
  "reason": "Doação - Continente",
  "referenceDocument": "FAT-2025-001",
  "performedBy": "collab456",
  "performedAt": "2025-01-02T14:30:00Z",
  "notes": "Doação recebida em bom estado"
}
```

**Índices**:
- `productId` (Ascending) + `performedAt` (Descending)
- `performedBy` (Ascending) + `performedAt` (Descending)
- `type` (Ascending)

---

### 5. kits

Conjuntos predefinidos de produtos para entrega.

**Path**: `/kits/{kitId}`

**Campos**:
```json
{
  "id": "kit456def",
  "name": "Kit Alimentar Mensal",
  "description": "Kit básico para família de 4 pessoas durante 1 mês",
  "items": [
    {
      "productId": "prod789xyz",
      "productName": "Arroz Carolino",
      "quantity": 5.0,
      "unit": "KILOGRAM"
    },
    {
      "productId": "prod456abc",
      "productName": "Massa Esparguete",
      "quantity": 3.0,
      "unit": "KILOGRAM"
    },
    {
      "productId": "prod123xyz",
      "productName": "Óleo Alimentar",
      "quantity": 2.0,
      "unit": "LITER"
    }
  ],
  "isActive": true,
  "isPredefined": true,
  "createdAt": "2025-01-02T10:00:00Z",
  "createdBy": "collab456",
  "updatedAt": "2025-01-02T10:00:00Z"
}
```

**Índices**:
- `isPredefined` (Ascending)
- `isActive` (Ascending)

---

### 6. deliveries

Entregas agendadas e realizadas aos beneficiários.

**Path**: `/deliveries/{deliveryId}`

**Campos**:
```json
{
  "id": "del789ghi",
  "beneficiaryId": "ben123abc",
  "beneficiaryName": "João Silva",
  "kitId": "kit456def",
  "kitName": "Kit Alimentar Mensal",
  "scheduledDate": "2025-01-15T10:00:00Z",
  "deliveredDate": null,
  "status": "SCHEDULED", // SCHEDULED | CONFIRMED | IN_PROGRESS | COMPLETED | CANCELLED | RESCHEDULED
  "location": "Loja Social - Campus IPCA",
  "notes": "",
  "deliveredBy": null, // userId quando completado
  "confirmedBy": null, // userId (beneficiário ou colaborador)
  "createdAt": "2025-01-02T10:00:00Z",
  "createdBy": "collab456",
  "updatedAt": "2025-01-02T10:00:00Z"
}
```

**Índices**:
- `beneficiaryId` (Ascending) + `scheduledDate` (Descending)
- `status` (Ascending) + `scheduledDate` (Ascending)
- `scheduledDate` (Ascending)

**Subcollection**: `history` (opcional, para rastrear mudanças de status)

---

### 7. campaigns

Campanhas de doação e sensibilização.

**Path**: `/campaigns/{campaignId}`

**Campos**:
```json
{
  "id": "camp123xyz",
  "title": "Campanha de Natal 2024",
  "description": "Recolha de alimentos não perecíveis para apoiar estudantes durante o período festivo",
  "imageUrl": "https://storage.firebase.com/campaigns/camp123/banner.jpg",
  "startDate": "2024-12-01T00:00:00Z",
  "endDate": "2024-12-31T23:59:59Z",
  "isActive": true,
  "neededCategories": ["FOOD", "HYGIENE"],
  "goal": "Recolher 500kg de alimentos",
  "progress": 65, // Percentagem
  "isPublic": true,
  "createdAt": "2024-11-15T10:00:00Z",
  "createdBy": "collab456",
  "updatedAt": "2024-12-20T15:30:00Z"
}
```

**Índices**:
- `isActive` (Ascending) + `startDate` (Descending)
- `isPublic` (Ascending)

---

## Subcollections

### notifications (dentro de users)

**Path**: `/users/{userId}/notifications/{notificationId}`

```json
{
  "id": "notif123",
  "title": "Entrega Agendada",
  "body": "A sua entrega foi agendada para 15/01/2025 às 10h00",
  "type": "DELIVERY", // DELIVERY | ALERT | INFO | CAMPAIGN
  "data": {
    "deliveryId": "del789ghi"
  },
  "isRead": false,
  "createdAt": "2025-01-02T10:00:00Z"
}
```

---

## Exemplos de Queries

### 1. Obter beneficiários ativos
```javascript
db.collection('beneficiaries')
  .where('isActive', '==', true)
  .orderBy('name', 'asc')
  .get()
```

### 2. Produtos com stock baixo
```javascript
db.collection('products')
  .where('currentStock', '<=', db.collection('products').doc().data().minimumStock)
  .get()
```

### 3. Entregas de um beneficiário
```javascript
db.collection('deliveries')
  .where('beneficiaryId', '==', 'ben123abc')
  .orderBy('scheduledDate', 'desc')
  .get()
```

### 4. Entregas de hoje
```javascript
const today = new Date();
today.setHours(0, 0, 0, 0);
const tomorrow = new Date(today);
tomorrow.setDate(tomorrow.getDate() + 1);

db.collection('deliveries')
  .where('scheduledDate', '>=', today)
  .where('scheduledDate', '<', tomorrow)
  .where('status', 'in', ['SCHEDULED', 'CONFIRMED'])
  .get()
```

### 5. Movimentações de um produto
```javascript
db.collection('stock_movements')
  .where('productId', '==', 'prod789xyz')
  .orderBy('performedAt', 'desc')
  .limit(20)
  .get()
```

### 6. Campanhas públicas ativas
```javascript
const now = new Date();

db.collection('campaigns')
  .where('isPublic', '==', true)
  .where('isActive', '==', true)
  .where('startDate', '<=', now)
  .where('endDate', '>=', now)
  .orderBy('startDate', 'desc')
  .get()
```

---

## Operações Complexas

### 1. Criar Entrega e Atualizar Stock

Quando uma entrega é criada, o stock deve ser decrementado:

```javascript
// Transaction
const batch = db.batch();

// 1. Criar entrega
const deliveryRef = db.collection('deliveries').doc();
batch.set(deliveryRef, deliveryData);

// 2. Para cada item no kit, decrementar stock
kit.items.forEach(item => {
  const productRef = db.collection('products').doc(item.productId);
  batch.update(productRef, {
    currentStock: admin.firestore.FieldValue.increment(-item.quantity)
  });
  
  // 3. Registar movimentação
  const movementRef = db.collection('stock_movements').doc();
  batch.set(movementRef, {
    productId: item.productId,
    type: 'EXIT',
    quantity: item.quantity,
    reason: 'Entrega agendada',
    performedBy: userId,
    performedAt: new Date()
  });
});

await batch.commit();
```

### 2. Verificar Stock Disponível para Kit

```javascript
async function checkKitStock(kitId) {
  const kitDoc = await db.collection('kits').doc(kitId).get();
  const kit = kitDoc.data();
  
  const checks = await Promise.all(
    kit.items.map(async (item) => {
      const productDoc = await db.collection('products').doc(item.productId).get();
      const product = productDoc.data();
      return {
        productId: item.productId,
        productName: item.productName,
        required: item.quantity,
        available: product.currentStock,
        sufficient: product.currentStock >= item.quantity
      };
    })
  );
  
  return {
    allSufficient: checks.every(c => c.sufficient),
    details: checks
  };
}
```

---

## Triggers e Cloud Functions

### 1. Alertar Stock Baixo

```javascript
exports.checkLowStock = functions.firestore
  .document('products/{productId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    
    // Se stock passou a estar baixo
    if (before.currentStock > before.minimumStock && 
        after.currentStock <= after.minimumStock) {
      
      // Notificar colaboradores
      const collaborators = await admin.firestore()
        .collection('users')
        .where('role', 'in', ['COLLABORATOR', 'ADMINISTRATOR'])
        .get();
      
      const notifications = collaborators.docs.map(doc => ({
        to: doc.data().fcmToken,
        notification: {
          title: 'Alerta: Stock Baixo',
          body: `${after.name} está abaixo do stock mínimo`
        }
      }));
      
      return admin.messaging().sendAll(notifications);
    }
  });
```

### 2. Lembrete de Entrega

```javascript
exports.deliveryReminder = functions.pubsub
  .schedule('every day 09:00')
  .onRun(async (context) => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);
    
    const dayAfter = new Date(tomorrow);
    dayAfter.setDate(dayAfter.getDate() + 1);
    
    // Entregas para amanhã
    const deliveries = await admin.firestore()
      .collection('deliveries')
      .where('scheduledDate', '>=', tomorrow)
      .where('scheduledDate', '<', dayAfter)
      .where('status', 'in', ['SCHEDULED', 'CONFIRMED'])
      .get();
    
    const notifications = await Promise.all(
      deliveries.docs.map(async (doc) => {
        const delivery = doc.data();
        const beneficiary = await admin.firestore()
          .collection('beneficiaries')
          .doc(delivery.beneficiaryId)
          .get();
        
        return {
          to: beneficiary.data().fcmToken,
          notification: {
            title: 'Lembrete: Entrega Amanhã',
            body: `Não se esqueça da sua entrega amanhã às ${formatTime(delivery.scheduledDate)}`
          }
        };
      })
    );
    
    return admin.messaging().sendAll(notifications);
  });
```

---

## Boas Práticas

1. **Sempre usar transações** para operações que envolvem múltiplos documentos
2. **Criar índices** para queries frequentes
3. **Desnormalizar dados** quando necessário (ex: beneficiaryName em deliveries)
4. **Usar paginação** para listas grandes
5. **Implementar cache** no cliente quando apropriado
6. **Validar dados** antes de salvar
7. **Usar timestamps** do servidor: `FieldValue.serverTimestamp()`
8. **Implementar soft delete** com campo `isActive` ao invés de deletar

---

## Segurança

Ver arquivo `FIREBASE_SETUP.md` para regras de segurança detalhadas.

Pontos principais:
- Autenticação obrigatória para a maioria das operações
- Validação de roles para operações sensíveis
- Campanhas públicas acessíveis sem autenticação
- Beneficiários só podem ver seus próprios dados
- Colaboradores têm acesso amplo para gestão

---

## Backups

Configurar backups automáticos no Firebase Console:
- Frequência: Diária
- Retenção: 30 dias
- Incluir todas as collections
