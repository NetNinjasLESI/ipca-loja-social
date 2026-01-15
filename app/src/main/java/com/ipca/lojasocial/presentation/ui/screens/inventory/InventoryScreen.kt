package com.ipca.lojasocial.presentation.ui.screens.inventory

import androidx.compose.foundation.background
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
import com.ipca.lojasocial.domain.model.Product
import com.ipca.lojasocial.domain.model.ProductCategory
import com.ipca.lojasocial.presentation.ui.components.EmptyState
import com.ipca.lojasocial.presentation.ui.components.ErrorMessage
import com.ipca.lojasocial.presentation.ui.components.LoadingIndicator
import com.ipca.lojasocial.presentation.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela principal de gestão de inventário
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: ProductViewModel = hiltViewModel(),
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    onNavigateToStockMovement: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventário") },
                actions = {
                    // Botão de filtro
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtrar"
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
                onClick = onNavigateToAddProduct,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Produto")
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
                    viewModel.searchProducts(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Chips de alertas
            AlertChips(
                lowStockCount = uiState.lowStockProducts.size,
                expiringCount = uiState.expiringProducts.size,
                onLowStockClick = { /* TODO: Filtrar low stock */ },
                onExpiringClick = { /* TODO: Filtrar expiring */ },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de produtos
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }

                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error ?: "Erro desconhecido",
                        onRetry = { viewModel.loadProducts() }
                    )
                }

                uiState.filteredProducts.isEmpty() -> {
                    EmptyState(
                        message = if (searchQuery.isNotBlank()) {
                            "Nenhum produto encontrado"
                        } else {
                            "Nenhum produto cadastrado"
                        },
                        icon = Icons.Default.Inventory
                    )
                }

                else -> {
                    ProductsList(
                        products = uiState.filteredProducts,
                        onProductClick = { onNavigateToProductDetails(it.id) },
                        onStockClick = { onNavigateToStockMovement(it.id) }
                    )
                }
            }
        }
    }

    // Diálogo de filtro
    if (showFilterDialog) {
        FilterDialog(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { category ->
                viewModel.filterByCategory(category)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
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
        placeholder = { Text("Pesquisar produtos...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun AlertChips(
    lowStockCount: Int,
    expiringCount: Int,
    onLowStockClick: () -> Unit,
    onExpiringClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (lowStockCount > 0) {
            FilterChip(
                selected = false,
                onClick = onLowStockClick,
                label = { Text("Stock Baixo ($lowStockCount)") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }

        if (expiringCount > 0) {
            FilterChip(
                selected = false,
                onClick = onExpiringClick,
                label = { Text("A Expirar ($expiringCount)") },
                leadingIcon = {
                    Icon(
                        Icons.Default.EventBusy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

@Composable
private fun ProductsList(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onStockClick: (Product) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                onClick = { onProductClick(product) },
                onStockClick = { onStockClick(product) }
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onStockClick: () -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getCategoryLabel(product.category),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status badge
                StockStatusBadge(product = product)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Stock Atual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatQuantity(product.currentStock),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = getStockColor(product)
                        )
                        Text(
                            text = " ${getUnitLabel(product.unit)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Text(
                        text = "Mínimo: ${formatQuantity(product.minimumStock)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Botão de movimentação
                OutlinedButton(
                    onClick = onStockClick,
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Movimentar")
                }
            }

            // Expiry date (se existir)
            product.expiryDate?.let { date ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Validade: ${formatDate(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isNearExpiry(date)) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StockStatusBadge(product: Product) {
    val (text, color) = when {
        product.currentStock <= 0 -> "Esgotado" to MaterialTheme.colorScheme.error
        product.currentStock <= product.minimumStock ->
            "Stock Baixo" to MaterialTheme.colorScheme.error
        else -> "Disponível" to MaterialTheme.colorScheme.primary
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
private fun FilterDialog(
    selectedCategory: ProductCategory?,
    onCategorySelected: (ProductCategory?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar por Categoria") },
        text = {
            Column {
                // Opção "Todas"
                FilterOption(
                    text = "Todas as Categorias",
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Categorias
                ProductCategory.values().forEach { category ->
                    FilterOption(
                        text = getCategoryLabel(category),
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) }
                    )
                }
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
private fun FilterOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
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

private fun getUnitLabel(unit: com.ipca.lojasocial.domain.model.ProductUnit): String {
    return when (unit) {
        com.ipca.lojasocial.domain.model.ProductUnit.UNIT -> "un"
        com.ipca.lojasocial.domain.model.ProductUnit.KILOGRAM -> "kg"
        com.ipca.lojasocial.domain.model.ProductUnit.LITER -> "L"
        com.ipca.lojasocial.domain.model.ProductUnit.PACKAGE -> "pct"
    }
}

@Composable
private fun getStockColor(product: Product): androidx.compose.ui.graphics.Color {
    return when {
        product.currentStock <= 0 -> MaterialTheme.colorScheme.error
        product.currentStock <= product.minimumStock ->
            MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
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
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "PT"))
    return sdf.format(date)
}

private fun isNearExpiry(date: Date): Boolean {
    val daysUntilExpiry = ((date.time - Date().time) / (1000 * 60 * 60 * 24))
    return daysUntilExpiry <= 30
}
