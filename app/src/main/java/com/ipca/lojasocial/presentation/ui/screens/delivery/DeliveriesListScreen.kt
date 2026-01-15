package com.ipca.lojasocial.presentation.ui.screens.delivery

import android.R.attr.text
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
import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.presentation.viewmodel.DeliveryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveriesListScreen(
    viewModel: DeliveryViewModel = hiltViewModel(),
    onNavigateToCreateDelivery: () -> Unit,
    onNavigateToDeliveryDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showStatsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entregas") },
                actions = {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text("${uiState.statistics?.scheduledToday ?: 0}")
                            }
                        }
                    ) {
                        IconButton(onClick = { showStatsDialog = true }) {
                            Icon(Icons.Default.Today, "Hoje")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateDelivery,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Agendar Entrega")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchDeliveries(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Pesquisar entregas...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.searchDeliveries("")
                        }) {
                            Icon(Icons.Default.Clear, "Limpar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // ✅ FILTROS COM SCROLL HORIZONTAL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedStatus == null,
                    onClick = { viewModel.filterByStatus(null) },
                    label = { Text("Todas") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == DeliveryStatus.SCHEDULED,
                    onClick = { viewModel.filterByStatus(DeliveryStatus.SCHEDULED) },
                    label = { Text("Agendadas") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == DeliveryStatus.CONFIRMED,
                    onClick = { viewModel.filterByStatus(DeliveryStatus.CONFIRMED) },
                    label = { Text("Confirmadas") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == DeliveryStatus.CANCELLED,
                    onClick = { viewModel.filterByStatus(DeliveryStatus.CANCELLED) },
                    label = { Text("Canceladas") }
                )
            }

            // ✅ ESTATÍSTICAS COM SCROLL HORIZONTAL E TEXTO NUMA LINHA
            uiState.statistics?.let { stats ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatChip(
                        icon = Icons.Default.Schedule,
                        label = "Agendadas",
                        value = stats.scheduled.toString(),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    StatChip(
                        icon = Icons.Default.CheckCircle,
                        label = "Confirmadas",
                        value = stats.confirmed.toString(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatChip(
                        icon = Icons.Default.Cancel,
                        label = "Canceladas",
                        value = stats.cancelled.toString(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                            TextButton(onClick = { viewModel.loadDeliveries() }) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }

                uiState.filteredDeliveries.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.LocalShipping,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (searchQuery.isNotBlank()) "Nenhuma entrega encontrada"
                                else "Nenhuma entrega agendada",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredDeliveries) { delivery ->
                            DeliveryCard(
                                delivery = delivery,
                                onClick = { onNavigateToDeliveryDetails(delivery.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStatsDialog && uiState.statistics != null) {
        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = { Text("Estatísticas de Entregas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatRow("Total", uiState.statistics!!.total.toString())
                    StatRow("Agendadas", uiState.statistics!!.scheduled.toString())
                    StatRow("Confirmadas", uiState.statistics!!.confirmed.toString())
                    StatRow("Canceladas", uiState.statistics!!.cancelled.toString())
                    StatRow("Agendadas Hoje", uiState.statistics!!.scheduledToday.toString())
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatsDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

// ✅ CHIP COM LARGURA FIXA E TEXTO NUMA LINHA
@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.width(IntrinsicSize.Min) // ✅ Ajusta ao conteúdo
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    maxLines = 1 // ✅ Força uma linha
                )
            }
        }
    }
}

@Composable
private fun DeliveryCard(delivery: Delivery, onClick: () -> Unit) {
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
            StatusIcon(delivery.status)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    delivery.beneficiaryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    delivery.kitName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatDate(delivery.scheduledDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(delivery.status)
            }
            Icon(
                Icons.Default.ChevronRight,
                "Ver detalhes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusIcon(status: DeliveryStatus) {
    val (icon, color) = when (status) {
        DeliveryStatus.PENDING_APPROVAL -> Icons.Default.HourglassEmpty to MaterialTheme.colorScheme.secondary
        DeliveryStatus.APPROVED -> Icons.Default.ThumbUp to MaterialTheme.colorScheme.primary
        DeliveryStatus.REJECTED -> Icons.Default.ThumbDown to MaterialTheme.colorScheme.error
        DeliveryStatus.SCHEDULED -> Icons.Default.Schedule to MaterialTheme.colorScheme.tertiary
        DeliveryStatus.CONFIRMED -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        DeliveryStatus.CANCELLED -> Icons.Default.Cancel to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(icon, null, tint = color)
        }
    }
}

@Composable
private fun StatusBadge(status: DeliveryStatus) {
    val (text, color) = when (status) {
        DeliveryStatus.PENDING_APPROVAL -> "Pendente" to MaterialTheme.colorScheme.secondary
        DeliveryStatus.APPROVED -> "Aprovada" to MaterialTheme.colorScheme.primary
        DeliveryStatus.REJECTED -> "Rejeitada" to MaterialTheme.colorScheme.error
        DeliveryStatus.SCHEDULED -> "Agendada" to MaterialTheme.colorScheme.tertiary
        DeliveryStatus.CONFIRMED -> "Confirmada" to MaterialTheme.colorScheme.primary
        DeliveryStatus.CANCELLED -> "Cancelada" to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}