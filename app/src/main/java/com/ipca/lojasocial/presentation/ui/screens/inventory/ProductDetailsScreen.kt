package com.ipca.lojasocial.presentation.ui.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ipca.lojasocial.domain.model.Product
import com.ipca.lojasocial.domain.model.ProductCategory
import com.ipca.lojasocial.domain.model.ProductUnit
import com.ipca.lojasocial.domain.model.MovementType
import com.ipca.lojasocial.domain.model.StockMovement
import com.ipca.lojasocial.presentation.ui.components.LoadingIndicator
import com.ipca.lojasocial.presentation.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de detalhes do produto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    productId: String,
    viewModel: ProductViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToStockMovement: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val product = uiState.selectedProduct

    // ✅ FIXED: Fetch full product from database instead of empty Product object
    LaunchedEffect(productId) {
        viewModel.getProductById(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Produto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToEdit(productId) }
                    ) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToStockMovement(productId) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.TrendingUp, "Movimentar")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading && product == null) {
            LoadingIndicator()
        } else if (product != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Informações básicas
                item {
                    ProductInfoCard(product = product)
                }

                // Stock
                item {
                    StockCard(product = product)
                }

                // Histórico de movimentações
                item {
                    Text(
                        text = "Histórico de Movimentações",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.stockMovements) { movement ->
                    StockMovementItem(movement = movement)
                }

                if (uiState.stockMovements.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sem movimentações registadas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Espaço extra no final para o FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } else {
            // Erro ao carregar
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Erro ao carregar produto",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductInfoCard(product: Product) {
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
            // Categoria badge
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = getCategoryLabel(product.category),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Nome
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Descrição
            if (product.description.isNotBlank()) {
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

            // Detalhes em grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    icon = Icons.Default.Scale,
                    label = "Unidade",
                    value = getUnitLabel(product.unit)
                )

                if (product.barcode != null) {
                    DetailItem(
                        icon = Icons.Default.QrCode,
                        label = "Código de Barras",
                        value = product.barcode!!
                    )
                }
            }

            // Data de validade se existir
            product.expiryDate?.let { expiryDate ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Validade: ${formatDate(expiryDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun StockCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Stock",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Stock Atual
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Stock Atual",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatQuantity(product.currentStock),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (product.currentStock <= product.minimumStock) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        Text(
                            text = getUnitAbbreviation(product.unit),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stock Mínimo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Stock Mínimo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatQuantity(product.minimumStock),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = getUnitAbbreviation(product.unit),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Alerta de stock baixo
            if (product.currentStock <= product.minimumStock) {
                LinearProgressIndicator(
                    progress = { (product.currentStock / product.minimumStock).toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error,
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Esgotado",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LinearProgressIndicator(
                    progress = { (product.currentStock / (product.minimumStock * 2)).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StockMovementItem(movement: StockMovement) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = getMovementIcon(movement.type),
                    contentDescription = null,
                    tint = getMovementColor(movement.type),
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = getMovementTypeLabel(movement.type),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = movement.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatDateTime(movement.performedAt)} • Por ${movement.performedBy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (movement.type == MovementType.ENTRY) "+" else "-"}${formatQuantity(movement.quantity)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = getMovementColor(movement.type)
                )
                Text(
                    text = getUnitAbbreviation(movement.unit),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper functions
private fun getCategoryLabel(category: ProductCategory): String {
    return when (category) {
        ProductCategory.FOOD -> "Alimentação"
        ProductCategory.HYGIENE -> "Higiene"
        ProductCategory.CLEANING -> "Limpeza"
        ProductCategory.OTHER -> "Outros"
    }
}

private fun getUnitLabel(unit: ProductUnit): String {
    return when (unit) {
        ProductUnit.UNIT -> "Unidade (un)"
        ProductUnit.KILOGRAM -> "Quilograma (kg)"
        ProductUnit.LITER -> "Litro (L)"
        ProductUnit.PACKAGE -> "Pacote (pct)"
    }
}

private fun getUnitAbbreviation(unit: ProductUnit): String {
    return when (unit) {
        ProductUnit.UNIT -> "un"
        ProductUnit.KILOGRAM -> "kg"
        ProductUnit.LITER -> "L"
        ProductUnit.PACKAGE -> "pct"
    }
}

@Composable
private fun getMovementColor(type: MovementType): androidx.compose.ui.graphics.Color {
    return when (type) {
        MovementType.ENTRY -> MaterialTheme.colorScheme.primary
        MovementType.EXIT -> MaterialTheme.colorScheme.error
        MovementType.ADJUSTMENT -> MaterialTheme.colorScheme.tertiary
        MovementType.TRANSFER -> MaterialTheme.colorScheme.secondary
    }
}

private fun getMovementIcon(type: MovementType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        MovementType.ENTRY -> Icons.Default.TrendingUp
        MovementType.EXIT -> Icons.Default.TrendingDown
        MovementType.ADJUSTMENT -> Icons.Default.Edit
        MovementType.TRANSFER -> Icons.Default.SwapHoriz
    }
}

private fun getMovementTypeLabel(type: MovementType): String {
    return when (type) {
        MovementType.ENTRY -> "Entrada"
        MovementType.EXIT -> "Saída"
        MovementType.ADJUSTMENT -> "Ajuste"
        MovementType.TRANSFER -> "Transferência"
    }
}

private fun formatQuantity(quantity: Double): String {
    return if (quantity % 1.0 == 0.0) {
        quantity.toInt().toString()
    } else {
        String.format("%.2f", quantity)
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(date)
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(date)
}