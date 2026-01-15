package com.ipca.lojasocial.presentation.ui.screens.collaborator.applications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ipca.lojasocial.domain.model.ApplicationStatus
import com.ipca.lojasocial.presentation.viewmodel.ApplicationViewModel
import com.ipca.lojasocial.presentation.viewmodel.LoginViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailsScreen(
    applicationId: String,
    onNavigateBack: () -> Unit,
    viewModel: ApplicationViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val loginUiState by loginViewModel.uiState.collectAsState()

    val currentUser = FirebaseAuth.getInstance().currentUser
    // Nome do COLABORADOR que está a rever (logado)
    val reviewerName = loginUiState.currentUser?.name
        ?: currentUser?.email?.substringBefore("@")
        ?: ""

    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(applicationId) {
        viewModel.loadApplicationById(applicationId)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }

    // Approve Dialog
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Aprovar Candidatura") },
            text = { Text("Confirmas a aprovação desta candidatura? O utilizador será automaticamente promovido a beneficiário.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approveApplication(
                            applicationId = applicationId,
                            reviewedBy = currentUser?.uid ?: "",
                            reviewedByName = reviewerName
                        )
                        showApproveDialog = false
                    }
                ) {
                    Text("Aprovar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Reject Dialog
    if (showRejectDialog) {
        var rejectionReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Rejeitar Candidatura") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Indica o motivo da rejeição:")
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Motivo *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReason.isNotBlank()) {
                            viewModel.rejectApplication(
                                applicationId = applicationId,
                                reviewedBy = currentUser?.uid ?: "",
                                reviewedByName = reviewerName,
                                reason = rejectionReason
                            )
                            showRejectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = rejectionReason.isNotBlank()
                ) {
                    Text("Rejeitar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Candidatura") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (uiState.selectedApplication?.status == ApplicationStatus.PENDING) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            enabled = !uiState.isLoading
                        ) {
                            Icon(Icons.Default.Cancel, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rejeitar")
                        }

                        Button(
                            onClick = { showApproveDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aprovar")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.selectedApplication == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.selectedApplication != null -> {
                ApplicationContent(
                    application = uiState.selectedApplication!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Candidatura não encontrada")
                }
            }
        }
    }
}

@Composable
private fun ApplicationContent(
    application: com.ipca.lojasocial.domain.model.BeneficiaryApplication,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card com nome do CANDIDATO (vem do application.userName)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Column {
                        Text(
                            application.userName, // ✅ NOME DO CANDIDATO
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            application.userEmail,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item { StatusBadge(application.status) }

        item {
            InfoCard(
                title = "Informações do Estudante",
                icon = Icons.Default.Badge
            ) {
                InfoRow("Número Estudante", application.studentNumber)
                InfoRow("Telefone", application.phone)
                InfoRow("NIF", application.nif)
            }
        }

        item {
            InfoCard(
                title = "Informações Académicas",
                icon = Icons.Default.School
            ) {
                InfoRow("Grau", application.academicDegree.displayName)
                InfoRow("Curso", application.course)
                InfoRow("Ano Académico", "${application.academicYear}º ano")
            }
        }

        item {
            InfoCard(
                title = "Morada",
                icon = Icons.Default.Home
            ) {
                InfoRow("Morada", application.address)
                if (application.zipCode.isNotBlank()) {
                    InfoRow("Código Postal", application.zipCode)
                }
                InfoRow("Cidade", application.city)
            }
        }

        item {
            InfoCard(
                title = "Informações Familiares",
                icon = Icons.Default.People
            ) {
                InfoRow("Agregado Familiar", "${application.familySize} pessoa(s)")
                if (application.monthlyIncome > 0) {
                    InfoRow(
                        "Rendimento Mensal",
                        String.format("%.2f€", application.monthlyIncome)
                    )
                }
            }
        }

        if (application.hasSpecialNeeds) {
            item {
                InfoCard(
                    title = "Necessidades Especiais",
                    icon = Icons.Default.Accessible
                ) {
                    Text(
                        application.specialNeedsDescription,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (application.observations.isNotBlank()) {
            item {
                InfoCard(
                    title = "Observações",
                    icon = Icons.Default.Notes
                ) {
                    Text(
                        application.observations,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Informações da Candidatura",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Submetida em: ${formatDateTime(application.appliedAt)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "ID: ${application.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (application.status != ApplicationStatus.PENDING) {
            item { ReviewCard(application) }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun StatusBadge(status: ApplicationStatus) {
    val (text, color, icon) = when (status) {
        ApplicationStatus.PENDING -> Triple(
            "Pendente de Revisão",
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Schedule
        )
        ApplicationStatus.APPROVED -> Triple(
            "Aprovada",
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle
        )
        ApplicationStatus.REJECTED -> Triple(
            "Rejeitada",
            MaterialTheme.colorScheme.error,
            Icons.Default.Cancel
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color)
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
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
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ReviewCard(application: com.ipca.lojasocial.domain.model.BeneficiaryApplication) {
    val color = if (application.status == ApplicationStatus.APPROVED)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
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
                Icon(Icons.Default.AssignmentTurnedIn, null, tint = color)
                Text(
                    "Informações de Revisão",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider()

            if (application.reviewedByName != null) {
                InfoRow("Revisto por", application.reviewedByName) // ✅ NOME DO COLABORADOR
            }

            if (application.reviewedAt != null) {
                InfoRow("Data", formatDateTime(application.reviewedAt))
            }

            if (application.status == ApplicationStatus.REJECTED &&
                application.rejectionReason != null
            ) {
                HorizontalDivider()
                Text(
                    "Motivo da Rejeição:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    application.rejectionReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
        }
    }
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}