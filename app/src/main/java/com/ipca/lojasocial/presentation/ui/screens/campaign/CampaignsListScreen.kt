package com.ipca.lojasocial.presentation.ui.screens.campaign

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.ipca.lojasocial.domain.model.Campaign
import com.ipca.lojasocial.domain.model.CampaignStatus
import com.ipca.lojasocial.presentation.viewmodel.CampaignViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsListScreen(
    viewModel: CampaignViewModel = hiltViewModel(),
    onNavigateToCreateCampaign: () -> Unit,
    onNavigateToCampaignDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showStatsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campanhas") },
                actions = {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text("${uiState.statistics?.active ?: 0}")
                            }
                        }
                    ) {
                        IconButton(onClick = { showStatsDialog = true }) {
                            Icon(Icons.Default.Campaign, "Estatísticas")
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
                onClick = onNavigateToCreateCampaign,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Nova Campanha")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de pesquisa
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchCampaigns(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Pesquisar campanhas...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.searchCampaigns("")
                        }) {
                            Icon(Icons.Default.Clear, "Limpar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // ✅ Filtros horizontalmente scrollable
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedStatus == null,
                        onClick = { viewModel.filterByStatus(null) },
                        label = { Text("Todas") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedStatus == CampaignStatus.ACTIVE,
                        onClick = { viewModel.filterByStatus(CampaignStatus.ACTIVE) },
                        label = { Text("Ativas") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedStatus == CampaignStatus.DRAFT,
                        onClick = { viewModel.filterByStatus(CampaignStatus.DRAFT) },
                        label = { Text("Rascunhos") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedStatus == CampaignStatus.COMPLETED,
                        onClick = { viewModel.filterByStatus(CampaignStatus.COMPLETED) },
                        label = { Text("Concluídas") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedStatus == CampaignStatus.CANCELLED,
                        onClick = { viewModel.filterByStatus(CampaignStatus.CANCELLED) },
                        label = { Text("Canceladas") }
                    )
                }
            }

            // Estatísticas rápidas
            uiState.statistics?.let { stats ->
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        StatChip(
                            icon = Icons.Default.Campaign,
                            label = "Ativas",
                            value = stats.active.toString(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        StatChip(
                            icon = Icons.Default.ShoppingCart,
                            label = "Total",
                            value = stats.total.toString(),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    if (stats.draft > 0) {
                        item {
                            StatChip(
                                icon = Icons.Default.Edit,
                                label = "Rascunhos",
                                value = stats.draft.toString(),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    if (stats.completed > 0) {
                        item {
                            StatChip(
                                icon = Icons.Default.CheckCircle,
                                label = "Concluídas",
                                value = stats.completed.toString(),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de campanhas
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
                            TextButton(onClick = { viewModel.loadCampaigns() }) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }

                uiState.filteredCampaigns.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Campaign,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Nenhuma campanha encontrada")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredCampaigns) { campaign ->
                            CampaignCard(
                                campaign = campaign,
                                onClick = { onNavigateToCampaignDetails(campaign.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de estatísticas
    if (showStatsDialog && uiState.statistics != null) {
        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = { Text("Estatísticas de Campanhas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatRow("Total", uiState.statistics!!.total.toString())
                    StatRow("Ativas", uiState.statistics!!.active.toString())
                    StatRow("Rascunhos", uiState.statistics!!.draft.toString())
                    StatRow("Concluídas", uiState.statistics!!.completed.toString())
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

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun CampaignCard(campaign: Campaign, onClick: () -> Unit) {
    val daysRemaining = calculateDaysRemaining(campaign.endDate)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        campaign.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        campaign.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                StatusBadge(campaign.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.ShoppingCart,
                    label = "Recolha de Produtos",
                    color = MaterialTheme.colorScheme.primary
                )

                if (daysRemaining > 0 && campaign.status == CampaignStatus.ACTIVE) {
                    InfoChip(
                        icon = Icons.Default.AccessTime,
                        label = "$daysRemaining dias",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Início: ${formatDate(campaign.startDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Fim: ${formatDate(campaign.endDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusBadge(status: CampaignStatus) {
    val (text, color) = when (status) {
        CampaignStatus.DRAFT -> "Rascunho" to MaterialTheme.colorScheme.outline
        CampaignStatus.ACTIVE -> "Ativa" to MaterialTheme.colorScheme.primary
        CampaignStatus.COMPLETED -> "Concluída" to MaterialTheme.colorScheme.tertiary
        CampaignStatus.CANCELLED -> "Cancelada" to MaterialTheme.colorScheme.error
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
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "PT"))
    return sdf.format(date)
}

private fun calculateDaysRemaining(endDate: Date): Long {
    val diff = endDate.time - System.currentTimeMillis()
    return diff / (1000 * 60 * 60 * 24)
}
