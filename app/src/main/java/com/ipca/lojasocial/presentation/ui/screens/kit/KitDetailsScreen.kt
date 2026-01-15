package com.ipca.lojasocial.presentation.ui.screens.kit

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
import com.ipca.lojasocial.data.repository.KitItemAvailability
import com.ipca.lojasocial.domain.model.Kit
import com.ipca.lojasocial.domain.model.KitItem
import com.ipca.lojasocial.presentation.ui.components.ErrorMessage
import com.ipca.lojasocial.presentation.ui.components.LoadingIndicator
import com.ipca.lojasocial.presentation.viewmodel.KitViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de detalhes do kit com verificação de disponibilidade
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitDetailsScreen(
    kitId: String,
    viewModel: KitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeactivateDialog by remember { mutableStateOf(false) }

    // Carregar kit
    LaunchedEffect(kitId) {
        viewModel.loadKitById(kitId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Kit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(kitId) }) {
                        Icon(Icons.Default.Edit, "Editar")
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
        when {
            uiState.isLoading && uiState.selectedKit == null -> {
                LoadingIndicator()
            }

            uiState.error != null && uiState.selectedKit == null -> {
                ErrorMessage(
                    message = uiState.error!!,
                    onRetry = {
                        viewModel.loadKitById(kitId)
                    }
                )
            }

            uiState.selectedKit != null -> {
                KitDetailsContent(
                    kit = uiState.selectedKit!!,
                    availability = uiState.kitAvailability,
                    onToggleStatus = { showDeactivateDialog = true },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    // Diálogo de confirmação de desativação
    if (showDeactivateDialog) {
        val kit = uiState.selectedKit
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = {
                Text(
                    if (kit?.isActive == true) {
                        "Desativar Kit"
                    } else {
                        "Ativar Kit"
                    }
                )
            },
            text = {
                Text(
                    if (kit?.isActive == true) {
                        "Tem certeza que deseja desativar este kit?"
                    } else {
                        "Tem certeza que deseja ativar este kit?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        kit?.let {
                            viewModel.toggleKitStatus(it.id, !it.isActive)
                        }
                        showDeactivateDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun KitDetailsContent(
    kit: Kit,
    availability: Map<String, KitItemAvailability>?,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            KitHeader(kit = kit)
        }

        // Status Card
        item {
            StatusCard(
                isActive = kit.isActive,
                onToggleStatus = onToggleStatus
            )
        }

        // Descrição
        if (kit.description.isNotBlank()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Descrição",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = kit.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Resumo de Disponibilidade
        if (availability != null) {
            item {
                AvailabilitySummaryCard(availability = availability)
            }
        }

        // Produtos do Kit
        item {
            Text(
                text = "Produtos do Kit (${kit.items.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(kit.items) { item ->
            val itemAvailability = availability?.get(item.productId)
            KitProductCard(
                item = item,
                availability = itemAvailability
            )
        }

        // Informações de Sistema
        item {
            SystemInfoCard(kit = kit)
        }

        // Espaço extra
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun KitHeader(kit: Kit) {
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
            // Ícone
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
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Info
            Column {
                Text(
                    text = kit.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${kit.items.size} produtos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    isActive: Boolean,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isActive) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Cancel
                    },
                    contentDescription = null,
                    tint = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                Column {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Text(
                        text = if (isActive) "Ativo" else "Inativo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }

            OutlinedButton(onClick = onToggleStatus) {
                Text(if (isActive) "Desativar" else "Ativar")
            }
        }
    }
}

@Composable
private fun AvailabilitySummaryCard(
    availability: Map<String, KitItemAvailability>
) {
    val allAvailable = availability.values.all { it.isAvailable }
    val availableCount = availability.values.count { it.isAvailable }
    val totalCount = availability.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (allAvailable) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
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
                imageVector = if (allAvailable) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Warning
                },
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (allAvailable) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Column {
                Text(
                    text = if (allAvailable) {
                        "Kit Disponível"
                    } else {
                        "Kit Indisponível"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (allAvailable) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Text(
                    text = "$availableCount de $totalCount produtos disponíveis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (allAvailable) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }
    }
}

@Composable
private fun KitProductCard(
    item: KitItem,
    availability: KitItemAvailability?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Necessário: ${formatQuantity(item.quantity)} ${getUnitLabel(item.unit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (availability != null) {
                    Text(
                        text = "Disponível: ${formatQuantity(availability.availableStock)} ${getUnitLabel(item.unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (availability.isAvailable) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            // Badge de disponibilidade
            if (availability != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (availability.isAvailable) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (availability.isAvailable) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.Cancel
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (availability.isAvailable) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                        Text(
                            text = if (availability.isAvailable) "OK" else "Falta",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (availability.isAvailable) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemInfoCard(kit: Kit) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Informações de Sistema",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Criado em: ${formatDateTime(kit.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Última atualização: ${formatDateTime(kit.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "ID: ${kit.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper functions
private fun getUnitLabel(unit: com.ipca.lojasocial.domain.model.ProductUnit): String {
    return when (unit) {
        com.ipca.lojasocial.domain.model.ProductUnit.UNIT -> "un"
        com.ipca.lojasocial.domain.model.ProductUnit.KILOGRAM -> "kg"
        com.ipca.lojasocial.domain.model.ProductUnit.LITER -> "L"
        com.ipca.lojasocial.domain.model.ProductUnit.PACKAGE -> "pct"
    }
}

private fun formatQuantity(quantity: Double): String {
    return if (quantity % 1.0 == 0.0) {
        quantity.toInt().toString()
    } else {
        String.format("%.2f", quantity)
    }
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}
