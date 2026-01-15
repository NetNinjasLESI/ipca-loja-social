package com.ipca.lojasocial.presentation.ui.screens.user

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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApplicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            viewModel.loadMyApplication(currentUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha Candidatura") },
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
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.myApplication == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.myApplication != null -> {
                ApplicationContent(
                    application = uiState.myApplication!!,
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
        // Status Card
        item {
            StatusCard(application)
        }

        // Personal Info
        item {
            InfoSection(
                title = "Informações Pessoais",
                icon = Icons.Default.Person
            ) {
                InfoRow("Número Estudante", application.studentNumber)
                InfoRow("Telefone", application.phone)
                InfoRow("NIF", application.nif)
            }
        }

        // Academic Info
        item {
            InfoSection(
                title = "Informações Académicas",
                icon = Icons.Default.School
            ) {
                InfoRow("Grau", application.academicDegree.displayName)
                InfoRow("Curso", application.course)
                InfoRow("Ano Académico", "${application.academicYear}º ano")
            }
        }

        // Address Info
        item {
            InfoSection(
                title = "Morada",
                icon = Icons.Default.Home
            ) {
                InfoRow("Morada", application.address)
                InfoRow("Código Postal", application.zipCode)
                InfoRow("Cidade", application.city)
            }
        }

        // Family Info
        item {
            InfoSection(
                title = "Informações Familiares",
                icon = Icons.Default.People
            ) {
                InfoRow("Agregado Familiar", "${application.familySize} pessoa(s)")
                if (application.monthlyIncome > 0) {
                    InfoRow("Rendimento Mensal", String.format("%.2f€", application.monthlyIncome))
                }
            }
        }

        // Special Needs
        if (application.hasSpecialNeeds) {
            item {
                InfoSection(
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

        // Observations
        if (application.observations.isNotBlank()) {
            item {
                InfoSection(
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

        // Review Info (if reviewed)
        if (application.status != ApplicationStatus.PENDING && application.reviewedAt != null) {
            item {
                ReviewSection(application)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusCard(application: com.ipca.lojasocial.domain.model.BeneficiaryApplication) {
    val (statusText, statusColor, statusIcon, description) = when (application.status) {
        ApplicationStatus.PENDING -> Tuple4(
            "Pendente",
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Schedule,
            "Candidatura aguarda aprovação dos Serviços de Ação Social"
        )
        ApplicationStatus.APPROVED -> Tuple4(
            "Aprovada",
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle,
            "Parabéns! A tua candidatura foi aprovada. És agora beneficiário."
        )
        ApplicationStatus.REJECTED -> Tuple4(
            "Rejeitada",
            MaterialTheme.colorScheme.error,
            Icons.Default.Cancel,
            "A tua candidatura foi rejeitada"
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    statusIcon,
                    null,
                    modifier = Modifier.size(32.dp),
                    tint = statusColor
                )
                Column {
                    Text(
                        "Status: $statusText",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            Text(
                "Submetida em: ${formatDateTime(application.appliedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoSection(
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
                Icon(
                    icon,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
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
private fun ReviewSection(application: com.ipca.lojasocial.domain.model.BeneficiaryApplication) {
    val color = if (application.status == ApplicationStatus.APPROVED)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Icon(
                    Icons.Default.AssignmentInd,
                    null,
                    tint = color
                )
                Text(
                    "Informações de Revisão",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider()

            if (application.reviewedByName != null) {
                InfoRow("Revisto por", application.reviewedByName)
            }

            if (application.reviewedAt != null) {
                InfoRow("Data revisão", formatDateTime(application.reviewedAt))
            }

            if (application.status == ApplicationStatus.REJECTED &&
                application.rejectionReason != null &&
                application.rejectionReason.isNotBlank()) {
                HorizontalDivider()
                Text(
                    "Motivo:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    application.rejectionReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)