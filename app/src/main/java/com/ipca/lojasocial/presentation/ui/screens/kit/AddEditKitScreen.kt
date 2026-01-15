package com.ipca.lojasocial.presentation.ui.screens.kit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.ipca.lojasocial.domain.model.KitItem
import com.ipca.lojasocial.domain.model.Product
import com.ipca.lojasocial.presentation.ui.components.IPCAButton
import com.ipca.lojasocial.presentation.ui.components.IPCATextField
import com.ipca.lojasocial.presentation.viewmodel.KitViewModel
import com.ipca.lojasocial.presentation.viewmodel.ProductViewModel

/**
 * Tela para adicionar ou editar kit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditKitScreen(
    kitId: String? = null,
    userId: String,
    viewModel: KitViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val productsState by productViewModel.uiState.collectAsState()
    val isEditMode = kitId != null

    // Estados do formulário
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<KitItem>>(emptyList()) }

    var showProductSelector by remember { mutableStateOf(false) }

    // Carregar produtos disponíveis
    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
    }

    // Carregar kit se for edição
    LaunchedEffect(kitId) {
        if (kitId != null) {
            viewModel.loadKitById(kitId)
        }
    }

    // Preencher campos quando kit carregar
    LaunchedEffect(uiState.selectedKit) {
        uiState.selectedKit?.let { kit ->
            name = kit.name
            description = kit.description
            items = kit.items
        }
    }

    // Navegar de volta em caso de sucesso
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Editar Kit" else "Novo Kit")
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nome
            IPCATextField(
                value = name,
                onValueChange = { name = it },
                label = "Nome do Kit *",
                placeholder = "Ex: Kit Família 4 Pessoas",
                leadingIcon = Icons.Default.Inventory
            )

            // Descrição
            IPCATextField(
                value = description,
                onValueChange = { description = it },
                label = "Descrição",
                placeholder = "Descrição do kit",
                leadingIcon = Icons.Default.Description,
                singleLine = false,
                maxLines = 3
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Seção de Produtos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Produtos do Kit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${items.size} itens",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Lista de produtos adicionados
            if (items.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nenhum produto adicionado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items.forEach { item ->
                    KitItemCard(
                        item = item,
                        onRemove = { items = items - item },
                        onQuantityChange = { newQuantity ->
                            items = items.map {
                                if (it.productId == item.productId) {
                                    it.copy(quantity = newQuantity)
                                } else it
                            }
                        }
                    )
                }
            }

            // Botão adicionar produto
            OutlinedButton(
                onClick = { showProductSelector = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar Produto")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mensagem de erro
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Botões
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEditMode) {
                    OutlinedButton(
                        onClick = {
                            // TODO: Confirmar antes de deletar
                            viewModel.deleteKit(kitId!!)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar")
                    }
                }

                IPCAButton(
                    onClick = {
                        val kit = Kit(
                            id = kitId ?: "",
                            name = name,
                            description = description,
                            items = items,
                            isActive = true,
                            createdBy = userId
                        )

                        if (isEditMode) {
                            viewModel.updateKit(kit)
                        } else {
                            viewModel.createKit(kit, userId)
                        }
                    },
                    text = if (isEditMode) "Guardar" else "Criar Kit",
                    modifier = Modifier.weight(if (isEditMode) 1f else 1f),
                    enabled = name.isNotBlank() && items.isNotEmpty() && !uiState.isLoading
                )
            }

            Text(
                text = "* Campos obrigatórios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialog de seleção de produtos
    if (showProductSelector) {
        ProductSelectorDialog(
            products = productsState.filteredProducts.filter { product ->
                // Não mostrar produtos já adicionados
                items.none { it.productId == product.id }
            },
            onProductSelected = { product, quantity ->
                items = items + KitItem(
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,
                    unit = product.unit
                )
                showProductSelector = false
            },
            onDismiss = { showProductSelector = false }
        )
    }
}

@Composable
private fun KitItemCard(
    item: KitItem,
    onRemove: () -> Unit,
    onQuantityChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${formatQuantity(item.quantity)} ${getUnitLabel(item.unit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão editar quantidade
                IconButton(
                    onClick = {
                        // TODO: Mostrar dialog para editar quantidade
                    }
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar quantidade",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Botão remover
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remover",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductSelectorDialog(
    products: List<Product>,
    onProductSelected: (Product, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = if (searchQuery.isBlank()) {
        products
    } else {
        products.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Produto ao Kit") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // ✅ FIX: Cópia local do selectedProduct para evitar smart cast
                val currentSelectedProduct = selectedProduct

                if (currentSelectedProduct == null) {
                    // Fase 1: Selecionar produto
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Pesquisar") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredProducts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhum produto disponível")
                        }
                    } else {
                        LazyColumn {
                            items(filteredProducts) { product ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    onClick = { selectedProduct = product }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = product.name,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "Stock: ${formatQuantity(product.currentStock)} ${getUnitLabel(product.unit)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Fase 2: Inserir quantidade
                    Text(
                        text = "Produto: ${currentSelectedProduct.name}",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantidade *") },
                        leadingIcon = {
                            Icon(Icons.Default.Numbers, null)
                        },
                        suffix = {
                            Text(getUnitLabel(currentSelectedProduct.unit))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Stock disponível: ${formatQuantity(currentSelectedProduct.currentStock)} ${getUnitLabel(currentSelectedProduct.unit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            // ✅ FIX: Cópia local para evitar smart cast
            val currentSelectedProduct = selectedProduct

            if (currentSelectedProduct != null) {
                TextButton(
                    onClick = {
                        val qty = quantity.toDoubleOrNull()
                        if (qty != null && qty > 0) {
                            onProductSelected(currentSelectedProduct, qty)
                        }
                    },
                    enabled = quantity.toDoubleOrNull() != null &&
                            quantity.toDoubleOrNull()!! > 0
                ) {
                    Text("Adicionar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (selectedProduct != null) {
                    selectedProduct = null
                    quantity = ""
                } else {
                    onDismiss()
                }
            }) {
                Text(if (selectedProduct != null) "Voltar" else "Cancelar")
            }
        }
    )
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