package com.ipca.lojasocial.presentation.ui.screens.kit

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
import com.ipca.lojasocial.domain.model.Kit
import com.ipca.lojasocial.presentation.ui.components.EmptyState
import com.ipca.lojasocial.presentation.ui.components.ErrorMessage
import com.ipca.lojasocial.presentation.ui.components.LoadingIndicator
import com.ipca.lojasocial.presentation.viewmodel.KitViewModel

/**
 * Tela principal de gestão de kits
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitsListScreen(
    viewModel: KitViewModel = hiltViewModel(),
    onNavigateToAddKit: () -> Unit,
    onNavigateToKitDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showStatsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kits") },
                actions = {
                    // Estatísticas
                    IconButton(onClick = { showStatsDialog = true }) {
                        // ✅ FIX: BadgedBox correto
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text("${uiState.statistics?.active ?: 0}")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Inventory, "Estatísticas")
                        }
                    }
                    // Toggle ativo/inativo
                    IconButton(
                        onClick = { viewModel.toggleShowOnlyActive() }
                    ) {
                        Icon(
                            if (uiState.showOnlyActive) {
                                Icons.Default.ToggleOn
                            } else {
                                Icons.Default.ToggleOff
                            },
                            contentDescription = "Filtrar ativos"
                        )
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
                onClick = onNavigateToAddKit,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Adicionar Kit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de pesquisa
            SearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.searchKits(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Estatísticas rápidas
            uiState.statistics?.let { stats ->
                StatsRow(
                    total = stats.active,
                    averageItems = stats.averageItemsPerKit,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de kits
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }

                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error ?: "Erro desconhecido",
                        onRetry = { viewModel.loadKits() }
                    )
                }

                uiState.filteredKits.isEmpty() -> {
                    EmptyState(
                        message = if (searchQuery.isNotBlank()) {
                            "Nenhum kit encontrado"
                        } else {
                            "Nenhum kit cadastrado"
                        },
                        icon = Icons.Default.Inventory
                    )
                }

                else -> {
                    KitsList(
                        kits = uiState.filteredKits,
                        onKitClick = { onNavigateToKitDetails(it.id) }
                    )
                }
            }
        }
    }

    // Diálogo de estatísticas
    if (showStatsDialog) {
        StatisticsDialog(
            statistics = uiState.statistics,
            onDismiss = { showStatsDialog = false }
        )
    }

    // Mensagens de sucesso/erro
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            // TODO: Mostrar Snackbar
            viewModel.clearMessages()
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Pesquisar kits...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Limpar")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun StatsRow(
    total: Int,
    averageItems: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip(
            icon = Icons.Default.Inventory,
            label = "Total",
            value = total.toString(),
            color = MaterialTheme.colorScheme.primary
        )

        StatChip(
            icon = Icons.Default.ShoppingCart,
            label = "Média de itens",
            value = String.format("%.1f", averageItems),
            color = MaterialTheme.colorScheme.secondary
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "$value $label",
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun KitsList(
    kits: List<Kit>,
    onKitClick: (Kit) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(kits) { kit ->
            KitCard(
                kit = kit,
                onClick = { onKitClick(kit) }
            )
        }
    }
}

@Composable
private fun KitCard(
    kit: Kit,
    onClick: () -> Unit
) {
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Ícone
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Info
                    Column {
                        Text(
                            text = kit.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (kit.description.isNotBlank()) {
                            Text(
                                text = kit.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Status badge
                StatusBadge(isActive = kit.isActive)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Número de produtos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${kit.items.size} produtos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Ver detalhes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(isActive: Boolean) {
    val (text, color) = if (isActive) {
        "Ativo" to MaterialTheme.colorScheme.primary
    } else {
        "Inativo" to MaterialTheme.colorScheme.error
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatisticsDialog(
    statistics: com.ipca.lojasocial.domain.usecase.kit.KitStatistics?,
    onDismiss: () -> Unit
) {
    if (statistics == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Estatísticas de Kits") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatRow("Total", statistics.total.toString())
                StatRow("Ativos", statistics.active.toString())
                StatRow("Inativos", statistics.inactive.toString())
                StatRow(
                    "Total de Itens",
                    statistics.totalItems.toString()
                )
                StatRow(
                    "Média de Itens por Kit",
                    String.format("%.1f", statistics.averageItemsPerKit)
                )
                StatRow(
                    "Máximo de Produtos num Kit",
                    statistics.maxProductsInKit.toString()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
