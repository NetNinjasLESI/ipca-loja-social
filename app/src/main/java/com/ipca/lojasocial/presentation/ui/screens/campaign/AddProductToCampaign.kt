package com.ipca.lojasocial.presentation.ui.screens.campaign

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
import com.ipca.lojasocial.domain.model.*
import java.util.*

/**
 * ✅ Dialog COMPLETO para adicionar produtos à campanha
 * - Opção 1: Selecionar produto existente do inventário
 * - Opção 2: Criar novo produto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductToCampaignDialog(
    existingProducts: List<Product>,
    onSelectExisting: (Product, CampaignPriority, String) -> Unit,
    onCreateNew: (
        name: String,
        description: String,
        category: ProductCategory,
        barcode: String?,
        unit: ProductUnit,
        minimumStock: Double,
        priority: CampaignPriority,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(AddProductMode.SELECT_EXISTING) }

    // Estados para produto existente
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedPriority by remember { mutableStateOf(CampaignPriority.NORMAL) }
    var productNotes by remember { mutableStateOf("") }

    // Estados para criar novo produto
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf(ProductCategory.FOOD) }
    var newBarcode by remember { mutableStateOf("") }
    var newUnit by remember { mutableStateOf(ProductUnit.UNIT) }
    var newMinStock by remember { mutableStateOf("10") }
    var newPriority by remember { mutableStateOf(CampaignPriority.NORMAL) }
    var newNotes by remember { mutableStateOf("") }

    var expandedCategory by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }
    var expandedPriority by remember { mutableStateOf(false) }
    var expandedNewPriority by remember { mutableStateOf(false) }

    val filteredProducts = remember(searchQuery, existingProducts) {
        if (searchQuery.isBlank()) {
            existingProducts
        } else {
            existingProducts.filter { product ->
                product.name.contains(searchQuery, ignoreCase = true) ||
                        product.description.contains(searchQuery, ignoreCase = true) ||
                        product.barcode?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AddShoppingCart,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "Adicionar Produto",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Fechar")
                    }
                }

                HorizontalDivider()

                // Mode Selector (Tabs)
                TabRow(
                    selectedTabIndex = selectedMode.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedMode == AddProductMode.SELECT_EXISTING,
                        onClick = { selectedMode = AddProductMode.SELECT_EXISTING },
                        text = { Text("Selecionar Existente") },
                        icon = { Icon(Icons.Default.Inventory, null) }
                    )
                    Tab(
                        selected = selectedMode == AddProductMode.CREATE_NEW,
                        onClick = { selectedMode = AddProductMode.CREATE_NEW },
                        text = { Text("Criar Novo") },
                        icon = { Icon(Icons.Default.Add, null) }
                    )
                }

                // Content
                when (selectedMode) {
                    AddProductMode.SELECT_EXISTING -> {
                        SelectExistingProductContent(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            filteredProducts = filteredProducts,
                            selectedProduct = selectedProduct,
                            onProductSelected = { selectedProduct = it },
                            selectedPriority = selectedPriority,
                            expandedPriority = expandedPriority,
                            onExpandedPriorityChange = { expandedPriority = it },
                            onPrioritySelected = {
                                selectedPriority = it
                                expandedPriority = false
                            },
                            productNotes = productNotes,
                            onNotesChange = { productNotes = it },
                            onConfirm = {
                                selectedProduct?.let { product ->
                                    onSelectExisting(product, selectedPriority, productNotes)
                                }
                            },
                            onDismiss = onDismiss,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AddProductMode.CREATE_NEW -> {
                        CreateNewProductContent(
                            name = newName,
                            onNameChange = { newName = it },
                            description = newDescription,
                            onDescriptionChange = { newDescription = it },
                            category = newCategory,
                            expandedCategory = expandedCategory,
                            onExpandedCategoryChange = { expandedCategory = it },
                            onCategorySelected = {
                                newCategory = it
                                expandedCategory = false
                            },
                            barcode = newBarcode,
                            onBarcodeChange = { newBarcode = it },
                            unit = newUnit,
                            expandedUnit = expandedUnit,
                            onExpandedUnitChange = { expandedUnit = it },
                            onUnitSelected = {
                                newUnit = it
                                expandedUnit = false
                            },
                            minStock = newMinStock,
                            onMinStockChange = { newMinStock = it },
                            priority = newPriority,
                            expandedPriority = expandedNewPriority,
                            onExpandedPriorityChange = { expandedNewPriority = it },
                            onPrioritySelected = {
                                newPriority = it
                                expandedNewPriority = false
                            },
                            notes = newNotes,
                            onNotesChange = { newNotes = it },
                            onConfirm = {
                                onCreateNew(
                                    newName,
                                    newDescription,
                                    newCategory,
                                    newBarcode.takeIf { it.isNotBlank() },
                                    newUnit,
                                    newMinStock.toDoubleOrNull() ?: 10.0,
                                    newPriority,
                                    newNotes
                                )
                            },
                            onDismiss = onDismiss,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

enum class AddProductMode {
    SELECT_EXISTING,
    CREATE_NEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectExistingProductContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredProducts: List<Product>,
    selectedProduct: Product?,
    onProductSelected: (Product) -> Unit,
    selectedPriority: CampaignPriority,
    expandedPriority: Boolean,
    onExpandedPriorityChange: (Boolean) -> Unit,
    onPrioritySelected: (CampaignPriority) -> Unit,
    productNotes: String,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Pesquisar produto") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Nome, descrição ou código de barras") }
            )

            // Selected Product Card
            selectedProduct?.let { product ->
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
                                "Produto Selecionado",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            getCategoryLabel(product.category),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Priority
            ExposedDropdownMenuBox(
                expanded = expandedPriority,
                onExpandedChange = onExpandedPriorityChange
            ) {
                OutlinedTextField(
                    value = getPriorityLabel(selectedPriority),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Prioridade") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority)
                    },
                    leadingIcon = {
                        Icon(getPriorityIcon(selectedPriority), null)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedPriority,
                    onDismissRequest = { onExpandedPriorityChange(false) }
                ) {
                    CampaignPriority.entries.forEach { priority ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(getPriorityIcon(priority), null)
                                    Text(getPriorityLabel(priority))
                                }
                            },
                            onClick = { onPrioritySelected(priority) }
                        )
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = productNotes,
                onValueChange = onNotesChange,
                label = { Text("Observações (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                placeholder = { Text("Ex: De preferência integral") }
            )
        }

        // Product List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredProducts.isEmpty()) {
                item {
                    Card {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text("Nenhum produto encontrado")
                                Text(
                                    "Tente criar um novo produto",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredProducts) { product ->
                    ProductListItem(
                        product = product,
                        isSelected = selectedProduct?.id == product.id,
                        onClick = { onProductSelected(product) }
                    )
                }
            }
        }

        // Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                enabled = selectedProduct != null
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateNewProductContent(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    category: ProductCategory,
    expandedCategory: Boolean,
    onExpandedCategoryChange: (Boolean) -> Unit,
    onCategorySelected: (ProductCategory) -> Unit,
    barcode: String,
    onBarcodeChange: (String) -> Unit,
    unit: ProductUnit,
    expandedUnit: Boolean,
    onExpandedUnitChange: (Boolean) -> Unit,
    onUnitSelected: (ProductUnit) -> Unit,
    minStock: String,
    onMinStockChange: (String) -> Unit,
    priority: CampaignPriority,
    expandedPriority: Boolean,
    onExpandedPriorityChange: (Boolean) -> Unit,
    onPrioritySelected: (CampaignPriority) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isValid = name.isNotBlank() && minStock.toDoubleOrNull() != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Criar Novo Produto no Inventário",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nome do Produto *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.ShoppingBag, null) },
            placeholder = { Text("Ex: Arroz Agulha") }
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
            leadingIcon = { Icon(Icons.Default.Description, null) }
        )

        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = onExpandedCategoryChange
        ) {
            OutlinedTextField(
                value = getCategoryLabel(category),
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoria *") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                },
                leadingIcon = { Icon(getCategoryIcon(category), null) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { onExpandedCategoryChange(false) }
            ) {
                ProductCategory.entries.forEach { cat ->
                    DropdownMenuItem(
                        text = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(getCategoryIcon(cat), null)
                                Text(getCategoryLabel(cat))
                            }
                        },
                        onClick = { onCategorySelected(cat) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = barcode,
            onValueChange = onBarcodeChange,
            label = { Text("Código de Barras") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.QrCode, null) }
        )

        ExposedDropdownMenuBox(
            expanded = expandedUnit,
            onExpandedChange = onExpandedUnitChange
        ) {
            OutlinedTextField(
                value = getUnitLabel(unit),
                onValueChange = {},
                readOnly = true,
                label = { Text("Unidade *") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedUnit,
                onDismissRequest = { onExpandedUnitChange(false) }
            ) {
                ProductUnit.entries.forEach { u ->
                    DropdownMenuItem(
                        text = { Text(getUnitLabel(u)) },
                        onClick = { onUnitSelected(u) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = minStock,
            onValueChange = {
                if (it.isEmpty() || it.toDoubleOrNull() != null) {
                    onMinStockChange(it)
                }
            },
            label = { Text("Stock Mínimo *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Warning, null) },
            placeholder = { Text("10") }
        )

        HorizontalDivider()

        Text(
            "Informações da Campanha",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        ExposedDropdownMenuBox(
            expanded = expandedPriority,
            onExpandedChange = onExpandedPriorityChange
        ) {
            OutlinedTextField(
                value = getPriorityLabel(priority),
                onValueChange = {},
                readOnly = true,
                label = { Text("Prioridade") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority)
                },
                leadingIcon = { Icon(getPriorityIcon(priority), null) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedPriority,
                onDismissRequest = { onExpandedPriorityChange(false) }
            ) {
                CampaignPriority.entries.forEach { p ->
                    DropdownMenuItem(
                        text = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(getPriorityIcon(p), null)
                                Text(getPriorityLabel(p))
                            }
                        },
                        onClick = { onPrioritySelected(p) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Observações") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
            placeholder = { Text("Ex: De preferência integral") }
        )

        Text(
            "* Campos obrigatórios",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                enabled = isValid
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Criar e Adicionar")
            }
        }
    }
}

@Composable
private fun ProductListItem(
    product: Product,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
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
                getCategoryIcon(product.category),
                null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    getCategoryLabel(product.category),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                product.barcode?.let { barcode ->
                    Text(
                        "Código: $barcode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Helper functions
private fun getCategoryLabel(category: ProductCategory): String {
    return when (category) {
        ProductCategory.FOOD -> "Alimentos"
        ProductCategory.HYGIENE -> "Higiene Pessoal"
        ProductCategory.CLEANING -> "Limpeza"
        ProductCategory.OTHER -> "Outros"
    }
}

private fun getCategoryIcon(category: ProductCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        ProductCategory.FOOD -> Icons.Default.Restaurant
        ProductCategory.HYGIENE -> Icons.Default.CleanHands
        ProductCategory.CLEANING -> Icons.Default.Home
        ProductCategory.OTHER -> Icons.Default.MoreHoriz
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

private fun getPriorityLabel(priority: CampaignPriority): String {
    return when (priority) {
        CampaignPriority.HIGH -> "Alta (Urgente)"
        CampaignPriority.NORMAL -> "Normal"
        CampaignPriority.LOW -> "Baixa (Opcional)"
    }
}

private fun getPriorityIcon(priority: CampaignPriority): androidx.compose.ui.graphics.vector.ImageVector {
    return when (priority) {
        CampaignPriority.HIGH -> Icons.Default.PriorityHigh
        CampaignPriority.NORMAL -> Icons.Default.Remove
        CampaignPriority.LOW -> Icons.Default.KeyboardArrowDown
    }
}