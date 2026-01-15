package com.ipca.lojasocial.presentation.ui.screens.beneficiary

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
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.presentation.viewmodel.DeliveryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryDeliveryDetailsScreen(
    deliveryId: String,
    viewModel: DeliveryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(deliveryId) {
        viewModel.loadDeliveryById(deliveryId)
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
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun DeliveryDetailsContent(
    delivery: com.ipca.lojasocial.domain.model.Delivery,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            DeliveryHeader(delivery)
        }

        // Status Card
        item {
            StatusCard(delivery)
        }

        // Kit Info
        item {
            InfoCard(title = "Kit") {
                Text(
                    delivery.kitName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Schedule Info
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

        // Notes
        if (delivery.notes.isNotBlank()) {
            item {
                InfoCard(title = "Notas") {
                    Text(
                        delivery.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Confirmation Details
        if (delivery.status == DeliveryStatus.CONFIRMED) {
            item {
                ConfirmationCard(delivery)
            }
        }

        // Cancellation Details
        if (delivery.status == DeliveryStatus.CANCELLED) {
            item {
                CancellationCard(delivery)
            }
        }

        // System Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        "Informações de Sistema",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Criado em: ${formatDateTime(delivery.createdAt)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Última atualização: ${formatDateTime(delivery.updatedAt)}",
                        style = MaterialTheme.typography.bodySmall
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
private fun DeliveryHeader(delivery: com.ipca.lojasocial.domain.model.Delivery) {
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
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.LocalShipping,
                        null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Column {
                Text(
                    "Entrega para",
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
private fun StatusCard(delivery: com.ipca.lojasocial.domain.model.Delivery) {
    val (text, color, icon, description) = when (delivery.status) {
        DeliveryStatus.PENDING_APPROVAL -> {
            Tuple4(
                "Aguardando Aprovação",
                MaterialTheme.colorScheme.secondary,
                Icons.Default.HourglassEmpty,
                "Solicitação enviada para análise"
            )
        }
        DeliveryStatus.APPROVED -> {
            Tuple4(
                "Aprovada",
                MaterialTheme.colorScheme.primary,
                Icons.Default.ThumbUp,
                "Aguardando agendamento"
            )
        }
        DeliveryStatus.REJECTED -> {
            Tuple4(
                "Rejeitada",
                MaterialTheme.colorScheme.error,
                Icons.Default.ThumbDown,
                "Solicitação foi rejeitada"
            )
        }
        DeliveryStatus.SCHEDULED -> {
            Tuple4(
                "Agendada",
                MaterialTheme.colorScheme.tertiary,
                Icons.Default.Schedule,
                "Entrega aguarda confirmação"
            )
        }
        DeliveryStatus.CONFIRMED -> {
            Tuple4(
                "Recebida",
                MaterialTheme.colorScheme.primary,
                Icons.Default.CheckCircle,
                "Entrega já foi confirmada"
            )
        }
        DeliveryStatus.CANCELLED -> {
            Tuple4(
                "Cancelada",
                MaterialTheme.colorScheme.error,
                Icons.Default.Cancel,
                "Entrega foi cancelada"
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
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
                icon,
                null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Column {
                Text(
                    "Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
                Text(
                    text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun ConfirmationCard(delivery: com.ipca.lojasocial.domain.model.Delivery) {
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
                    "Entrega Confirmada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (delivery.confirmedDate != null) {
                Text(
                    "Confirmado em: ${formatDateTime(delivery.confirmedDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Stock foi atualizado automaticamente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CancellationCard(delivery: com.ipca.lojasocial.domain.model.Delivery) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    "Entrega Cancelada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            if (delivery.cancelledDate != null) {
                Text(
                    "Cancelado em: ${formatDateTime(delivery.cancelledDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            if (delivery.cancellationReason != null && 
                delivery.cancellationReason.isNotBlank()) {
                Divider()
                Text(
                    "Motivo:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    delivery.cancellationReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
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
            Divider()
            content()
        }
    }
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}

// Helper data class
private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
