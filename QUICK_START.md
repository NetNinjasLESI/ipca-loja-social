# Guia Rápido de Início - IPCA Loja Social

## 🚀 Começar em 5 Passos

### 1. Extrair o Projeto
```bash
tar -xzf ipca-loja-social.tar.gz
cd ipca-loja-social
```

### 2. Abrir no Android Studio
1. Abrir Android Studio
2. File → Open
3. Selecionar a pasta `ipca-loja-social`
4. Aguardar sincronização do Gradle

### 3. Configurar Firebase

#### A. Criar Projeto Firebase
1. Aceder a [Firebase Console](https://console.firebase.google.com/)
2. Criar novo projeto: "IPCA Loja Social"
3. Adicionar app Android com package: `com.ipca.lojasocial`

#### B. Baixar Configuração
1. Baixar `google-services.json`
2. Colocar em `app/google-services.json`

#### C. Ativar Serviços
No Firebase Console:
- ✅ Authentication → Email/Password
- ✅ Firestore Database (modo production)
- ✅ Cloud Storage
- ✅ Cloud Messaging

#### D. Criar Utilizador de Teste
No Authentication:
- Email: `admin@ipca.pt`
- Password: (definir)

### 4. Criar Estrutura Firestore

No Firestore, criar collection `users` com um documento:
```json
{
  "email": "admin@ipca.pt",
  "name": "Administrador",
  "role": "ADMINISTRATOR",
  "isActive": true,
  "createdAt": [timestamp atual],
  "updatedAt": [timestamp atual]
}
```

**IMPORTANTE:** Usar o UID do utilizador criado no passo 3D como ID do documento!

### 5. Executar a Aplicação
1. Conectar dispositivo Android ou iniciar emulador
2. Run → Run 'app'
3. Login com credenciais criadas

## 📋 Verificação Rápida

### Após executar, verificar:
- [ ] App abre sem erros
- [ ] Tela de login aparece
- [ ] Login funciona com credenciais de teste
- [ ] Dashboard ou home aparecem após login

## 🐛 Resolução de Problemas

### Erro: "google-services.json not found"
**Solução:** Colocar arquivo `google-services.json` em `app/`

### Erro: "FirebaseApp initialization unsuccessful"
**Solução:** Verificar se `google-services.json` está correto

### Erro ao fazer login
**Solução:** 
1. Verificar se utilizador existe no Authentication
2. Verificar se documento existe no Firestore
3. Verificar se UID do Auth = ID do documento

### Erro de sincronização Gradle
**Solução:** File → Invalidate Caches → Invalidate and Restart

## 📚 Próximos Passos

1. Ler `README.md` para documentação completa
2. Ler `FIREBASE_SETUP.md` para configuração detalhada
3. Ler `FIRESTORE_STRUCTURE.md` para estrutura de dados
4. Implementar features adicionais conforme requisitos

## 📁 Arquivos Importantes

```
ipca-loja-social/
├── README.md                 ← Documentação principal
├── FIREBASE_SETUP.md         ← Guia Firebase detalhado
├── FIRESTORE_STRUCTURE.md    ← Estrutura de dados
├── PROJECT_OVERVIEW.md       ← Visão geral do projeto
├── QUICK_START.md           ← Este arquivo
└── app/
    ├── build.gradle.kts     ← Configuração do módulo
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/ipca/lojasocial/
            ├── presentation/  ← UI e ViewModels
            ├── domain/        ← Lógica de negócio
            ├── data/          ← Repositórios
            └── di/            ← Injeção de dependências
```

## ⚡ Comandos Úteis

### Limpar e reconstruir
```bash
./gradlew clean build
```

### Ver relatório de assinatura (para SHA-1)
```bash
./gradlew signingReport
```

### Atualizar dependências
```bash
./gradlew --refresh-dependencies
```

## 🆘 Suporte

Se encontrar problemas:
1. Verificar logs no Logcat do Android Studio
2. Consultar documentação nos arquivos .md
3. Verificar configuração do Firebase Console

## ✅ Checklist de Configuração

- [ ] Android Studio instalado (versão 2023.1+)
- [ ] JDK 17 configurado
- [ ] Projeto extraído e aberto
- [ ] Firebase projeto criado
- [ ] google-services.json colocado em app/
- [ ] Authentication ativado
- [ ] Firestore criado
- [ ] Utilizador de teste criado no Auth
- [ ] Documento de utilizador criado no Firestore
- [ ] App compila sem erros
- [ ] Login funciona

## 🎯 Estrutura Mínima para Funcionar

Para a app funcionar básicamente, precisa ter no Firestore:

**Collection: users**
```
/users/{userId}
  - email: string
  - name: string
  - role: string ("BENEFICIARY" | "COLLABORATOR" | "ADMINISTRATOR")
  - isActive: boolean
  - createdAt: timestamp
  - updatedAt: timestamp
```

**Importante:** O `{userId}` deve ser o mesmo UID do Firebase Authentication!

---

**Tempo estimado de configuração:** 15-20 minutos

Boa sorte com o desenvolvimento! 🚀
