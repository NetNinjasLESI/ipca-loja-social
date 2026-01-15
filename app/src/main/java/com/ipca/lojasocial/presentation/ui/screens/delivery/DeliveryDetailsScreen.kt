package com.ipca.lojasocial.presentation.ui.screens.delivery

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
import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.presentation.viewmodel.DeliveryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailsScreen(
    deliveryId: String,
    userId: String,
    viewModel: DeliveryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deliveryId) {
        viewModel.loadDeliveryById(deliveryId)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Entrega") },
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
            uiState.isLoading && uiState.selectedDelivery == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && uiState.selectedDelivery == null -> {
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
                        Text(uiState.error!!)
                        TextButton(onClick = { viewModel.loadDeliveryById(deliveryId) }) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }

            uiState.selectedDelivery != null -> {
                DeliveryDetailsContent(
                    delivery = uiState.selectedDelivery!!,
                    onConfirm = { showConfirmDialog = true },
                    onCancel = { showCancelDialog = true },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    if (showConfirmDialog) {
        ConfirmDeliveryDialog(
            onConfirm = {
                viewModel.confirmDelivery(deliveryId, userId)
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    if (showCancelDialog) {
        CancelDeliveryDialog(
            onConfirm = { reason ->
                viewModel.cancelDelivery(deliveryId, userId, reason)
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false }
        )
    }
}

@Composable
private fun DeliveryDetailsContent(
    delivery: Delivery,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DeliveryHeader(delivery)
        }

        item {
            StatusCard(delivery.status)
        }

        item {
            InfoCard(title = "Beneficiário") {
                Text(
                    delivery.beneficiaryName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            InfoCard(title = "Kit") {
                Text(
                    delivery.kitName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            InfoCard(title = "Data Agendada") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        formatDateTime(delivery.scheduledDate),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (delivery.notes.isNotBlank()) {
            item {
                InfoCard(title = "Notas") {
                    Text(delivery.notes)
                }
            }
        }

        if (delivery.status == DeliveryStatus.CONFIRMED) {
            item {
                Card(
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
                                "Detalhes de Confirmação",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            "Data: ${formatDateTime(delivery.confirmedDate!!)}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Confirmado por: ${delivery.confirmedBy}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        HorizontalDivider()
                        Text(
                            "✓ Stock atualizado automaticamente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (delivery.status == DeliveryStatus.CANCELLED) {
            item {
                Card(
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
                                "Detalhes de Cancelamento",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            "Data: ${formatDateTime(delivery.cancelledDate!!)}",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Cancelado por: ${delivery.cancelledBy}",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Motivo: ${delivery.cancellationReason}",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (delivery.status == DeliveryStatus.SCHEDULED) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar Entrega")
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Cancel, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancelar Entrega")
                    }
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Informações de Sistema",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Criado em: ${formatDateTime(delivery.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Última atualização: ${formatDateTime(delivery.updatedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "ID: ${delivery.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DeliveryHeader(delivery: Delivery) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.LocalShipping,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Column {
                Text(
                    "Entrega",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    delivery.beneficiaryName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StatusCard(status: DeliveryStatus) {
    data class StatusInfo(
        val text: String,
        val color: androidx.compose.ui.graphics.Color,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val description: String
    )

    val statusInfo = when (status) {
        DeliveryStatus.PENDING_APPROVAL -> StatusInfo(
            "Aguardando Aprovação",
            MaterialTheme.colorScheme.secondary,
            Icons.Default.HourglassEmpty,
            "Solicitação enviada para análise"
        )
        DeliveryStatus.APPROVED -> StatusInfo(
            "Aprovada",
            MaterialTheme.colorScheme.primary,
            Icons.Default.ThumbUp,
            "Aguardando agendamento"
        )
        DeliveryStatus.REJECTED -> StatusInfo(
            "Rejeitada",
            MaterialTheme.colorScheme.error,
            Icons.Default.ThumbDown,
            "Solicitação foi rejeitada"
        )
        DeliveryStatus.SCHEDULED -> StatusInfo(
            "Agendada",
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Schedule,
            "A aguardar confirmação"
        )
        DeliveryStatus.CONFIRMED -> StatusInfo(
            "Confirmada",
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle,
            "Entrega realizada com sucesso"
        )
        DeliveryStatus.CANCELLED -> StatusInfo(
            "Cancelada",
            MaterialTheme.colorScheme.error,
            Icons.Default.Cancel,
            "Entrega foi cancelada"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusInfo.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                statusInfo.icon,
                null,
                modifier = Modifier.size(32.dp),
                tint = statusInfo.color
            )
            Column {
                Text(
                    "Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = statusInfo.color
                )
                Text(
                    statusInfo.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = statusInfo.color
                )
                Text(
                    statusInfo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusInfo.color
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun ConfirmDeliveryDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, null) },
        title = { Text("Confirmar Entrega") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tem certeza que deseja confirmar esta entrega?")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "✓ O stock dos produtos será atualizado automaticamente",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmar")
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
private fun CancelDeliveryDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Cancel, null) },
        title = { Text("Cancelar Entrega") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tem certeza que deseja cancelar esta entrega?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo do cancelamento *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank()
            ) {
                Text("Cancelar Entrega")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Voltar")
            }
        }
    )
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}