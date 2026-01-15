package com.ipca.lojasocial.presentation.ui.screens.collaborator.applications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.ipca.lojasocial.domain.model.ApplicationStatus
import com.ipca.lojasocial.presentation.viewmodel.ApplicationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApplicationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    viewModel: ApplicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var filterStatus by remember { mutableStateOf<ApplicationStatus?>(ApplicationStatus.PENDING) }

    // ✅ CORRIGIDO: Carregar candidaturas baseado no filtro selecionado
    LaunchedEffect(filterStatus) {
        android.util.Log.d("PendingAppsScreen", "Filtro alterado para: $filterStatus")
        when (filterStatus) {
            ApplicationStatus.PENDING -> viewModel.loadPendingApplications()
            ApplicationStatus.APPROVED -> viewModel.loadApplicationsByStatus(ApplicationStatus.APPROVED)
            ApplicationStatus.REJECTED -> viewModel.loadApplicationsByStatus(ApplicationStatus.REJECTED)
            null -> viewModel.loadAllApplications()
        }
    }

    // ✅ LOG para ver o que está sendo exibido
    LaunchedEffect(uiState.applications) {
        android.util.Log.d("PendingAppsScreen", "UI mostrando ${uiState.applications.size} candidaturas:")
        uiState.applications.forEach { app ->
            android.util.Log.d("PendingAppsScreen", "  - ${app.userName}: ${app.status}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Candidaturas")
                        // ✅ CORRIGIDO: Só mostra contador se filtro for PENDING
                        if (filterStatus == ApplicationStatus.PENDING && uiState.pendingCount > 0) {
                            Text(
                                "${uiState.pendingCount} pendente(s)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (filterStatus != null) {
                            Text(
                                "${uiState.applications.size} candidatura(s)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // ✅ Refresh baseado no filtro atual
                        when (filterStatus) {
                            ApplicationStatus.PENDING -> viewModel.loadPendingApplications()
                            ApplicationStatus.APPROVED -> viewModel.loadApplicationsByStatus(ApplicationStatus.APPROVED)
                            ApplicationStatus.REJECTED -> viewModel.loadApplicationsByStatus(ApplicationStatus.REJECTED)
                            null -> viewModel.loadAllApplications()
                        }
                    }) {
                        Icon(Icons.Default.Refresh, "Atualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterStatus == ApplicationStatus.PENDING,
                    onClick = { filterStatus = ApplicationStatus.PENDING },
                    label = {
                        Text(
                            "Pendentes",
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    leadingIcon = if (filterStatus == ApplicationStatus.PENDING) {
                        { Icon(Icons.Default.Schedule, null, Modifier.size(14.dp)) }
                    } else null
                )

                FilterChip(
                    selected = filterStatus == ApplicationStatus.APPROVED,
                    onClick = { filterStatus = ApplicationStatus.APPROVED },
                    label = {
                        Text(
                            "Aprovadas",
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    leadingIcon = if (filterStatus == ApplicationStatus.APPROVED) {
                        { Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp)) }
                    } else null
                )

                FilterChip(
                    selected = filterStatus == ApplicationStatus.REJECTED,
                    onClick = { filterStatus = ApplicationStatus.REJECTED },
                    label = {
                        Text(
                            "Rejeitadas",
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    leadingIcon = if (filterStatus == ApplicationStatus.REJECTED) {
                        { Icon(Icons.Default.Cancel, null, Modifier.size(14.dp)) }
                    } else null
                )

                FilterChip(
                    selected = filterStatus == null,
                    onClick = { filterStatus = null },
                    label = {
                        Text(
                            "Todas",
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.applications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.AssignmentLate,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Nenhuma candidatura encontrada",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.applications) { application ->
                            ApplicationCard(
                                application = application,
                                onClick = { onNavigateToDetails(application.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationCard(
    application: com.ipca.lojasocial.domain.model.BeneficiaryApplication,
    onClick: () -> Unit
) {
    val (statusColor, statusIcon) = when (application.status) {
        ApplicationStatus.PENDING -> Pair(
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Schedule
        )
        ApplicationStatus.APPROVED -> Pair(
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle
        )
        ApplicationStatus.REJECTED -> Pair(
            MaterialTheme.colorScheme.error,
            Icons.Default.Cancel
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        statusIcon,
                        null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    application.userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.School,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        application.course,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Badge,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Nº ${application.studentNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatDate(application.appliedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Arrow
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "PT"))
    return sdf.format(date)
}