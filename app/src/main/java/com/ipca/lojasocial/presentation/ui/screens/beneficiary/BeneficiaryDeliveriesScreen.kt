package com.ipca.lojasocial.presentation.ui.screens.beneficiary

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
import com.google.firebase.auth.FirebaseAuth
import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.presentation.viewmodel.BeneficiaryDashboardViewModel
import com.ipca.lojasocial.presentation.viewmodel.BeneficiaryDeliveryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryDeliveriesScreen(
    dashboardViewModel: BeneficiaryDashboardViewModel = hiltViewModel(),
    deliveryViewModel: BeneficiaryDeliveryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToRequestDelivery: () -> Unit
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val deliveryState by deliveryViewModel.uiState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var selectedTab by remember { mutableStateOf(0) }
    var deliveryToCancel by remember { mutableStateOf<Delivery?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // Carregar entregas
    LaunchedEffect(currentUserId, dashboardState.beneficiary) {
        if (currentUserId.isNotBlank() && dashboardState.beneficiary == null) {
            dashboardViewModel.loadBeneficiaryData(currentUserId)
        }

        dashboardState.beneficiary?.let { beneficiary ->
            deliveryViewModel.loadMyDeliveries(beneficiary.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Entregas") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToRequestDelivery,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Solicitar Entrega")
            }
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
                    text = { Text("Pendentes") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Agendadas") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Concluídas") }
                )
            }

            when {
                deliveryState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                deliveryState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(deliveryState.error!!)
                            Button(onClick = {
                                dashboardState.beneficiary?.let {
                                    deliveryViewModel.loadMyDeliveries(it.id)
                                }
                            }) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }

                else -> {
                    when (selectedTab) {
                        0 -> PendingTab(
                            pending = deliveryState.pendingRequests,
                            approved = deliveryState.approvedRequests,
                            rejected = deliveryState.rejectedRequests,
                            onCancelClick = { delivery ->
                                deliveryToCancel = delivery
                                showCancelDialog = true
                            },
                            onDetailsClick = onNavigateToDetails
                        )
                        1 -> ScheduledTab(
                            scheduled = deliveryState.scheduledDeliveries,
                            onDetailsClick = onNavigateToDetails
                        )
                        2 -> CompletedTab(
                            completed = deliveryState.completedDeliveries,
                            onDetailsClick = onNavigateToDetails
                        )
                    }
                }
            }
        }
    }

    // Cancel Dialog
    if (showCancelDialog && deliveryToCancel != null) {
        CancelDeliveryDialog(
            delivery = deliveryToCancel!!,
            onConfirm = { reason ->
                dashboardState.beneficiary?.let { beneficiary ->
                    deliveryViewModel.cancelDelivery(
                        deliveryId = deliveryToCancel!!.id,
                        beneficiaryId = beneficiary.id,
                        reason = reason
                    )
                }
                showCancelDialog = false
                deliveryToCancel = null
            },
            onDismiss = {
                showCancelDialog = false
                deliveryToCancel = null
            }
        )
    }

    // Success Snackbar
    LaunchedEffect(deliveryState.successMessage) {
        deliveryState.successMessage?.let {
            // TODO: Show snackbar
            deliveryViewModel.clearMessages()
        }
    }
}

@Composable
private fun PendingTab(
    pending: List<Delivery>,
    approved: List<Delivery>,
    rejected: List<Delivery>,
    onCancelClick: (Delivery) -> Unit,
    onDetailsClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pending Approval
        if (pending.isNotEmpty()) {
            item {
                Text(
                    "Aguardando Aprovação",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(pending) { delivery ->
                PendingDeliveryCard(
                    delivery = delivery,
                    onCancelClick = { onCancelClick(delivery) },
                    onClick = { onDetailsClick(delivery.id) }
                )
            }
        }

        // Approved
        if (approved.isNotEmpty()) {
            item {
                Text(
                    "Aprovadas - Aguardando Agendamento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(approved) { delivery ->
                ApprovedDeliveryCard(
                    delivery = delivery,
                    onClick = { onDetailsClick(delivery.id) }
                )
            }
        }

        // Rejected
        if (rejected.isNotEmpty()) {
            item {
                Text(
                    "Rejeitadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(rejected) { delivery ->
                RejectedDeliveryCard(
                    delivery = delivery,
                    onClick = { onDetailsClick(delivery.id) }
                )
            }
        }

        if (pending.isEmpty() && approved.isEmpty() && rejected.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Pending,
                    message = "Nenhuma solicitação pendente"
                )
            }
        }
    }
}

@Composable
private fun ScheduledTab(
    scheduled: List<Delivery>,
    onDetailsClick: (String) -> Unit
) {
    if (scheduled.isEmpty()) {
        EmptyState(
            icon = Icons.Default.CalendarMonth,
            message = "Nenhuma entrega agendada"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(scheduled) { delivery ->
                ScheduledDeliveryCard(
                    delivery = delivery,
                    onClick = { onDetailsClick(delivery.id) }
                )
            }
        }
    }
}

@Composable
private fun CompletedTab(
    completed: List<Delivery>,
    onDetailsClick: (String) -> Unit
) {
    if (completed.isEmpty()) {
        EmptyState(
            icon = Icons.Default.CheckCircle,
            message = "Nenhuma entrega concluída"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(completed) { delivery ->
                CompletedDeliveryCard(
                    delivery = delivery,
                    onClick = { onDetailsClick(delivery.id) }
                )
            }
        }
    }
}

// ========== Cards ==========

@Composable
private fun PendingDeliveryCard(
    delivery: Delivery,
    onCancelClick: () -> Unit,
    onClick: () -> Unit
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
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        Icons.Default.HourglassEmpty,
                        null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Aguardando Aprovação",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                delivery.kitName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            delivery.requestedDate?.let {
                Text(
                    "Solicitado em ${formatDate(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (delivery.requestNotes.isNotBlank()) {
                Text(
                    delivery.requestNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancelar Solicitação")
            }
        }
    }
}

@Composable
private fun ApprovedDeliveryCard(
    delivery: Delivery,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                delivery.kitName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Aguardando agendamento pelo colaborador",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun RejectedDeliveryCard(
    delivery: Delivery,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
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
                    Icons.Default.Cancel,
                    null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Rejeitada",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                delivery.kitName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            delivery.rejectionReason?.let { reason ->
                Text(
                    "Motivo: $reason",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ScheduledDeliveryCard(
    delivery: Delivery,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                delivery.kitName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    formatDateTime(delivery.scheduledDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (delivery.notes.isNotBlank()) {
                Text(
                    delivery.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompletedDeliveryCard(
    delivery: Delivery,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    Icons.Default.CheckCircle,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Concluída",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                delivery.kitName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            delivery.confirmedDate?.let {
                Text(
                    "Recebido em ${formatDateTime(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ========== Dialogs ==========

@Composable
private fun CancelDeliveryDialog(
    delivery: Delivery,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Cancel, null) },
        title = { Text("Cancelar Solicitação") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tem certeza que deseja cancelar a solicitação do kit \"${delivery.kitName}\"?")

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(reason.ifBlank { "Cancelado pelo beneficiário" })
                }
            ) {
                Text("Cancelar Solicitação")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Voltar")
            }
        }
    )
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ========== Helpers ==========

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "PT"))
    return sdf.format(date)
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}