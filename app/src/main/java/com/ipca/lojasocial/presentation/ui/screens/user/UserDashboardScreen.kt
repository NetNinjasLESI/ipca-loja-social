package com.ipca.lojasocial.presentation.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ipca.lojasocial.presentation.viewmodel.ApplicationViewModel
import com.ipca.lojasocial.presentation.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    onNavigateToApply: () -> Unit,
    onNavigateToMyApplication: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ApplicationViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val loginUiState by loginViewModel.uiState.collectAsState()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUserName = loginUiState.currentUser?.name
        ?: FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@")
        ?: "Utilizador"

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            viewModel.checkExistingApplication(currentUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Área do Utilizador") },
                actions = {
                    IconButton(onClick = {
                        loginViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Sair")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Column {
                            Text(
                                "Bem-vindo(a)!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                currentUserName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Sobre a Loja Social",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "A Loja Social IPCA oferece apoio aos estudantes com necessidades económicas. " +
                                    "Candidata-te para receber kits de produtos essenciais.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Application Status
            if (uiState.hasExistingApplication && uiState.myApplication != null) {
                item {
                    ApplicationStatusCard(
                        application = uiState.myApplication!!,
                        onViewDetails = onNavigateToMyApplication
                    )
                }
            } else {
                // Apply Button
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AssignmentInd,
                                    null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column {
                                    Text(
                                        "Candidatura a Beneficiário",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Preenche o formulário de candidatura",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Button(
                                onClick = onNavigateToApply,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Candidatar-me")
                            }
                        }
                    }
                }
            }

            // Help Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Help,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Precisas de ajuda?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Se tiveres dúvidas sobre o processo de candidatura, contacta os Serviços de Ação Social.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationStatusCard(
    application: com.ipca.lojasocial.domain.model.BeneficiaryApplication,
    onViewDetails: () -> Unit
) {
    val (statusText, statusColor, statusIcon) = when (application.status) {
        com.ipca.lojasocial.domain.model.ApplicationStatus.PENDING -> Triple(
            "Pendente",
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Schedule
        )
        com.ipca.lojasocial.domain.model.ApplicationStatus.APPROVED -> Triple(
            "Aprovada",
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle
        )
        com.ipca.lojasocial.domain.model.ApplicationStatus.REJECTED -> Triple(
            "Rejeitada",
            MaterialTheme.colorScheme.error,
            Icons.Default.Cancel
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    statusIcon,
                    null,
                    tint = statusColor,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        "Candidatura $statusText",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        "Submetida em ${formatDate(application.appliedAt)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedButton(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Detalhes")
            }
        }
    }
}

private fun formatDate(date: java.util.Date): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "PT"))
    return sdf.format(date)
}