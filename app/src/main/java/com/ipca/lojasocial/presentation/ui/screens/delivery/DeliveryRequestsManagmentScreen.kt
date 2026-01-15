package com.ipca.lojasocial.presentation.ui.screens.delivery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.presentation.viewmodel.CollaboratorDeliveryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryRequestsManagementScreen(
    viewModel: CollaboratorDeliveryViewModel = hiltViewModel(),
    userId: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var requestToApprove by remember { mutableStateOf<Delivery?>(null) }
    var requestToReject by remember { mutableStateOf<Delivery?>(null) }
    var requestToSchedule by remember { mutableStateOf<Delivery?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadPendingRequests()
        viewModel.loadApprovedRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitações de Entrega") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (uiState.pendingRequests.isNotEmpty()) {
                                Badge {
                                    Text("${uiState.pendingRequests.size}")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { viewModel.loadPendingRequests() }) {
                            Icon(Icons.Default.Refresh, "Atualizar")
                        }
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
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pendentes (${uiState.pendingRequests.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Aprovadas (${uiState.approvedRequests.size})") }
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

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(uiState.error!!)
                            Button(onClick = {
                                if (selectedTab == 0) {
                                    viewModel.loadPendingRequests()
                                } else {
                                    viewModel.loadApprovedRequests()
                                }
                            }) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }

                else -> {
                    when (selectedTab) {
                        0 -> PendingRequestsTab(
                            requests = uiState.pendingRequests,
                            onApprove = { requestToApprove = it },
                            onReject = { requestToReject = it }
                        )
                        1 -> ApprovedRequestsTab(
                            requests = uiState.approvedRequests,
                            onSchedule = { requestToSchedule = it }
                        )
                    }
                }
            }
        }
    }

    // Approve Dialog
    if (requestToApprove != null) {
        ApproveRequestDialog(
            request = requestToApprove!!,
            onConfirm = {
                viewModel.approveRequest(requestToApprove!!.id, userId)
                requestToApprove = null
            },
            onDismiss = { requestToApprove = null }
        )
    }

    // Reject Dialog
    if (requestToReject != null) {
        RejectRequestDialog(
            request = requestToReject!!,
            onConfirm = { reason ->
                viewModel.rejectRequest(requestToReject!!.id, userId, reason)
                requestToReject = null
            },
            onDismiss = { requestToReject = null }
        )
    }

    // Schedule Dialog
    if (requestToSchedule != null) {
        ScheduleDeliveryDialog(
            request = requestToSchedule!!,
            onConfirm = { date, notes ->
                viewModel.scheduleDelivery(requestToSchedule!!.id, date, notes, userId)
                requestToSchedule = null
            },
            onDismiss = { requestToSchedule = null }
        )
    }

    // Success Message
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            // TODO: Show snackbar
            viewModel.clearMessages()
        }
    }
}

@Composable
private fun PendingRequestsTab(
    requests: List<Delivery>,
    onApprove: (Delivery) -> Unit,
    onReject: (Delivery) -> Unit
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Nenhuma solicitação pendente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { request ->
                PendingRequestCard(
                    request = request,
                    onApprove = { onApprove(request) },
                    onReject = { onReject(request) }
                )
            }
        }
    }
}

@Composable
private fun ApprovedRequestsTab(
    requests: List<Delivery>,
    onSchedule: (Delivery) -> Unit
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Event,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Nenhuma entrega aguardando agendamento",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { request ->
                ApprovedRequestCard(
                    request = request,
                    onSchedule = { onSchedule(request) }
                )
            }
        }
    }
}

// ========== Cards ==========

@Composable
private fun PendingRequestCard(
    request: Delivery,
    onApprove: () -> Unit,
    onReject: () -> Unit
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
            // Header
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
                        Icons.Default.Person,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            request.beneficiaryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        request.requestedDate?.let {
                            Text(
                                "Solicitado em ${formatDate(it)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Kit Info
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Inventory,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        request.kitName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Notes
            if (request.requestNotes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Observações:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            request.requestNotes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            HorizontalDivider()

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rejeitar")
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aprovar")
                }
            }
        }
    }
}

@Composable
private fun ApprovedRequestCard(
    request: Delivery,
    onSchedule: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Aprovada",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                request.beneficiaryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                request.kitName,
                style = MaterialTheme.typography.bodyLarge
            )

            request.approvedDate?.let {
                Text(
                    "Aprovado em ${formatDate(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Button(
                onClick = onSchedule,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarMonth, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agendar Entrega")
            }
        }
    }
}

// ========== Dialogs ==========

@Composable
private fun ApproveRequestDialog(
    request: Delivery,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, null) },
        title = { Text("Aprovar Solicitação") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Confirma aprovação da solicitação de:")
                Text(
                    "Beneficiário: ${request.beneficiaryName}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Kit: ${request.kitName}",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "✓ Após aprovação, você poderá agendar a entrega",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Aprovar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun RejectRequestDialog(
    request: Delivery,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Cancel, null) },
        title = { Text("Rejeitar Solicitação") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Rejeitar solicitação de ${request.beneficiaryName}?")

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo da rejeição *") },
                    placeholder = {
                        Text("Ex: Stock insuficiente, dados incompletos...")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Rejeitar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ScheduleDeliveryDialog(
    request: Delivery,
    onConfirm: (Date, String) -> Unit,
    onDismiss: () -> Unit
) {
    var scheduledDate by remember { mutableStateOf<Date?>(null) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CalendarMonth, null) },
        title = { Text("Agendar Entrega") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Agendar entrega para ${request.beneficiaryName}")

                // Date Picker Button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        scheduledDate?.let { formatDateTime(it) }
                            ?: "Selecionar data"
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { scheduledDate?.let { onConfirm(it, notes) } },
                enabled = scheduledDate != null
            ) {
                Text("Agendar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // Date Picker (simplified - você pode usar o DatePickerDialog completo)
    if (showDatePicker) {
        // TODO: Implementar DatePickerDialog completo
        // Por agora, usar data atual + 1 dia
        LaunchedEffect(Unit) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, 1)
            scheduledDate = cal.time
            showDatePicker = false
        }
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "PT"))
    return sdf.format(date)
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}