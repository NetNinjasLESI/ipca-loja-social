package com.ipca.lojasocial.presentation.ui.screens.beneficiary

// ESTE É O DASHBOARD MODIFICADO PARA USAR A NOVA FUNCIONALIDADE
// Substitui o teu BeneficiaryDashboardScreen por este código

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ipca.lojasocial.domain.model.Beneficiary
import com.ipca.lojasocial.presentation.viewmodel.BeneficiaryDashboardViewModel
import com.ipca.lojasocial.presentation.viewmodel.BeneficiaryDeliveryViewModel
import com.ipca.lojasocial.presentation.viewmodel.LoginViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryDashboardScreen(
    dashboardViewModel: BeneficiaryDashboardViewModel = hiltViewModel(),
    deliveryViewModel: BeneficiaryDeliveryViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel(),
    onNavigateToDeliveries: () -> Unit,
    onNavigateToRequestDelivery: (String) -> Unit,  // ✅ MUDANÇA: Agora recebe beneficiaryId
    onLogout: () -> Unit
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val deliveryState by deliveryViewModel.uiState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            dashboardViewModel.loadBeneficiaryData(currentUserId)
        }
    }

    // Carregar entregas quando beneficiário carregar
    LaunchedEffect(dashboardState.beneficiary) {
        dashboardState.beneficiary?.let { beneficiary ->
            deliveryViewModel.loadMyDeliveries(beneficiary.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha Conta") },
                actions = {
                    IconButton(onClick = { dashboardViewModel.refresh(currentUserId) }) {
                        Icon(Icons.Default.Refresh, "Atualizar")
                    }
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
        when {
            dashboardState.isLoading && dashboardState.beneficiary == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            dashboardState.error != null && dashboardState.beneficiary == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(dashboardState.error!!)
                        Button(onClick = { dashboardViewModel.refresh(currentUserId) }) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }

            dashboardState.beneficiary != null -> {
                DashboardContent(
                    beneficiary = dashboardState.beneficiary!!,
                    pendingRequests = deliveryState.pendingRequests.size,
                    approvedRequests = deliveryState.approvedRequests.size,
                    scheduledDeliveries = deliveryState.scheduledDeliveries.size,
                    totalDeliveries = dashboardState.totalDeliveries,
                    confirmedDeliveries = dashboardState.confirmedDeliveries,
                    upcomingDelivery = dashboardState.upcomingDelivery,
                    onNavigateToDeliveries = onNavigateToDeliveries,
                    onNavigateToRequestDelivery = {
                        // ✅ MUDANÇA: Passa o ID do beneficiário
                        onNavigateToRequestDelivery(dashboardState.beneficiary!!.id)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    beneficiary: Beneficiary,
    pendingRequests: Int,
    approvedRequests: Int,
    scheduledDeliveries: Int,
    totalDeliveries: Int,
    confirmedDeliveries: Int,
    upcomingDelivery: com.ipca.lojasocial.domain.model.Delivery?,
    onNavigateToDeliveries: () -> Unit,
    onNavigateToRequestDelivery: () -> Unit,  // ✅ Aqui continua sem parâmetro (lambda interna já passa)
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            WelcomeHeader(beneficiary.name)
        }

        // Quick Actions
        item {
            QuickActionsCard(
                onRequestDelivery = onNavigateToRequestDelivery,
                onViewDeliveries = onNavigateToDeliveries
            )
        }

        // Requests Status
        if (pendingRequests > 0 || approvedRequests > 0) {
            item {
                RequestsStatusCard(
                    pending = pendingRequests,
                    approved = approvedRequests
                )
            }
        }

        // Status Card
        item {
            StatusCard(
                isActive = beneficiary.isActive,
                scheduledDeliveries = scheduledDeliveries,
                totalDeliveries = totalDeliveries,
                confirmedDeliveries = confirmedDeliveries
            )
        }

        // Upcoming Delivery Card
        if (upcomingDelivery != null) {
            item {
                UpcomingDeliveryCard(upcomingDelivery)
            }
        }

        // Personal Info Card
        item {
            PersonalInfoCard(beneficiary)
        }

        // Academic Info Card
        item {
            AcademicInfoCard(beneficiary)
        }

        // Family Info Card
        item {
            FamilyInfoCard(beneficiary)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ✅ MUDANÇA NO BOTÃO: Texto atualizado para ser mais claro
@Composable
private fun QuickActionsCard(
    onRequestDelivery: () -> Unit,
    onViewDeliveries: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Ações Rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRequestDelivery,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    // ✅ OPCIONAL: Texto mais claro
                    Text("Solicitar Kit", maxLines = 1)
                }

                OutlinedButton(
                    onClick = onViewDeliveries,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.List, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Minhas Entregas", maxLines = 1)
                }
            }
        }
    }
}

// Resto dos composables continua igual...
// (WelcomeHeader, StatusCard, etc. não mudam)

@Composable
private fun RequestsStatusCard(
    pending: Int,
    approved: Int
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Pending,
                    null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "Estado das Solicitações",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (pending > 0) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            pending.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "Pendentes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (approved > 0) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            approved.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Aprovadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeader(name: String) {
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
                    "Olá,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    isActive: Boolean,
    scheduledDeliveries: Int,
    totalDeliveries: Int,
    confirmedDeliveries: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        null,
                        tint = if (isActive)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                    Text(
                        if (isActive) "Conta Ativa" else "Conta Inativa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Agendadas",
                    value = scheduledDeliveries.toString(),
                    icon = Icons.Default.Schedule
                )
                StatItem(
                    label = "Total",
                    value = totalDeliveries.toString(),
                    icon = Icons.Default.LocalShipping
                )
                StatItem(
                    label = "Recebidas",
                    value = confirmedDeliveries.toString(),
                    icon = Icons.Default.CheckCircle
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpcomingDeliveryCard(delivery: com.ipca.lojasocial.domain.model.Delivery) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Event,
                    null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    "Próxima Entrega",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Text(
                delivery.kitName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    formatDate(delivery.scheduledDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun PersonalInfoCard(beneficiary: Beneficiary) {
    InfoCard(
        title = "Informações Pessoais",
        icon = Icons.Default.Person
    ) {
        InfoRow("Email", beneficiary.email)
        InfoRow("Telefone", beneficiary.phone)
        if (beneficiary.nif?.isNotBlank() == true ) {
            InfoRow("NIF", beneficiary.nif)
        }
    }
}

@Composable
private fun AcademicInfoCard(beneficiary: Beneficiary) {
    InfoCard(
        title = "Informações Académicas",
        icon = Icons.Default.School
    ) {
        InfoRow("Número de Estudante", beneficiary.studentNumber)
        InfoRow("Curso", beneficiary.course)
        InfoRow("Ano Académico", "${beneficiary.academicYear}º ano")
    }
}

@Composable
private fun FamilyInfoCard(beneficiary: Beneficiary) {
    InfoCard(
        title = "Informações Familiares",
        icon = Icons.Default.Home
    ) {
        InfoRow("Agregado Familiar", "${beneficiary.familySize} pessoa(s)")
        if (beneficiary.monthlyIncome > 0) {
            InfoRow(
                "Rendimento Mensal",
                String.format("%.2f€", beneficiary.monthlyIncome)
            )
        }
        if (beneficiary.address.isNotBlank()) {
            InfoRow("Morada", beneficiary.address)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
private fun InfoRow(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (value != null) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}