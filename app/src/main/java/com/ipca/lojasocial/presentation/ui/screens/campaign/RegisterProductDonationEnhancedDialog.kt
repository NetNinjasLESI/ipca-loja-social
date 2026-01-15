package com.ipca.lojasocial.presentation.ui.screens.campaign

import androidx.compose.foundation.clickable
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

/**
 * ✅ Dialog OBRIGATÓRIO de seleção de produto ao registar doação
 * - DEVE selecionar produto existente OU criar novo
 * - Não permite apenas escrever nome do produto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductDonationEnhancedDialog(
    availableProducts: List<Product>,
    onSelectExisting: (
        product: Product,
        quantity: Double,
        donorName: String?,
        donorEmail: String?,
        donorPhone: String?,
        notes: String
    ) -> Unit,
    onCreateNew: (
        name: String,
        description: String,
        category: ProductCategory,
        barcode: String?,
        unit: ProductUnit,
        minimumStock: Double,
        quantity: Double,
        donorName: String?,
        donorEmail: String?,
        donorPhone: String?,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(DonationStep.SELECT_PRODUCT) }
    var selectedMode by remember { mutableStateOf(ProductSelectionMode.EXISTING) }

    // Produto existente
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Criar novo produto
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf(ProductCategory.FOOD) }
    var newBarcode by remember { mutableStateOf("") }
    var newUnit by remember { mutableStateOf(ProductUnit.UNIT) }
    var newMinStock by remember { mutableStateOf("10") }

    // Quantidade da doação
    var quantity by remember { mutableStateOf("") }

    // Informações do doador
    var donorName by remember { mutableStateOf("") }
    var donorEmail by remember { mutableStateOf("") }
    var donorPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Dropdowns
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }

    val filteredProducts = remember(searchQuery, availableProducts) {
        if (searchQuery.isBlank()) {
            availableProducts
        } else {
            availableProducts.filter { product ->
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
            Column(modifier = Modifier.fillMaxSize()) {
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
                            Icons.Default.Favorite,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                "Registar Doação",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                getStepTitle(currentStep),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Fechar")
                    }
                }

                HorizontalDivider()

                // Stepper
                LinearProgressIndicator(
                    progress = { (currentStep.ordinal + 1) / 3f },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Content based on step
                when (currentStep) {
                    DonationStep.SELECT_PRODUCT -> {
                        SelectProductStep(
                            selectedMode = selectedMode,
                            onModeChange = { selectedMode = it },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            filteredProducts = filteredProducts,
                            selectedProduct = selectedProduct,
                            onProductSelected = { selectedProduct = it },
                            newName = newName,
                            onNewNameChange = { newName = it },
                            newDescription = newDescription,
                            onNewDescriptionChange = { newDescription = it },
                            newCategory = newCategory,
                            expandedCategory = expandedCategory,
                            onExpandedCategoryChange = { expandedCategory = it },
                            onCategorySelected = {
                                newCategory = it
                                expandedCategory = false
                            },
                            newBarcode = newBarcode,
                            onNewBarcodeChange = { newBarcode = it },
                            newUnit = newUnit,
                            expandedUnit = expandedUnit,
                            onExpandedUnitChange = { expandedUnit = it },
                            onUnitSelected = {
                                newUnit = it
                                expandedUnit = false
                            },
                            newMinStock = newMinStock,
                            onNewMinStockChange = { newMinStock = it },
                            onNext = {
                                currentStep = DonationStep.QUANTITY
                            },
                            onCancel = onDismiss,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    DonationStep.QUANTITY -> {
                        QuantityStep(
                            selectedProduct = selectedProduct,
                            selectedMode = selectedMode,
                            newName = newName,
                            newUnit = newUnit,
                            quantity = quantity,
                            onQuantityChange = { quantity = it },
                            onNext = {
                                currentStep = DonationStep.DONOR_INFO
                            },
                            onBack = {
                                currentStep = DonationStep.SELECT_PRODUCT
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    DonationStep.DONOR_INFO -> {
                        DonorInfoStep(
                            donorName = donorName,
                            onDonorNameChange = { donorName = it },
                            donorEmail = donorEmail,
                            onDonorEmailChange = { donorEmail = it },
                            donorPhone = donorPhone,
                            onDonorPhoneChange = { donorPhone = it },
                            notes = notes,
                            onNotesChange = { notes = it },
                            onConfirm = {
                                val qty = quantity.toDoubleOrNull() ?: 0.0

                                if (selectedMode == ProductSelectionMode.EXISTING && selectedProduct != null) {
                                    onSelectExisting(
                                        selectedProduct!!,
                                        qty,
                                        donorName.takeIf { it.isNotBlank() },
                                        donorEmail.takeIf { it.isNotBlank() },
                                        donorPhone.takeIf { it.isNotBlank() },
                                        notes
                                    )
                                } else {
                                    onCreateNew(
                                        newName,
                                        newDescription,
                                        newCategory,
                                        newBarcode.takeIf { it.isNotBlank() },
                                        newUnit,
                                        newMinStock.toDoubleOrNull() ?: 10.0,
                                        qty,
                                        donorName.takeIf { it.isNotBlank() },
                                        donorEmail.takeIf { it.isNotBlank() },
                                        donorPhone.takeIf { it.isNotBlank() },
                                        notes
                                    )
                                }
                            },
                            onBack = {
                                currentStep = DonationStep.QUANTITY
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

enum class DonationStep {
    SELECT_PRODUCT,
    QUANTITY,
    DONOR_INFO
}

enum class ProductSelectionMode {
    EXISTING,
    CREATE_NEW
}

private fun getStepTitle(step: DonationStep): String {
    return when (step) {
        DonationStep.SELECT_PRODUCT -> "Passo 1 de 3: Selecionar/Criar Produto"
        DonationStep.QUANTITY -> "Passo 2 de 3: Quantidade"
        DonationStep.DONOR_INFO -> "Passo 3 de 3: Informações do Doador"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectProductStep(
    selectedMode: ProductSelectionMode,
    onModeChange: (ProductSelectionMode) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredProducts: List<Product>,
    selectedProduct: Product?,
    onProductSelected: (Product) -> Unit,
    newName: String,
    onNewNameChange: (String) -> Unit,
    newDescription: String,
    onNewDescriptionChange: (String) -> Unit,
    newCategory: ProductCategory,
    expandedCategory: Boolean,
    onExpandedCategoryChange: (Boolean) -> Unit,
    onCategorySelected: (ProductCategory) -> Unit,
    newBarcode: String,
    onNewBarcodeChange: (String) -> Unit,
    newUnit: ProductUnit,
    expandedUnit: Boolean,
    onExpandedUnitChange: (Boolean) -> Unit,
    onUnitSelected: (ProductUnit) -> Unit,
    newMinStock: String,
    onNewMinStockChange: (String) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isValid = when (selectedMode) {
        ProductSelectionMode.EXISTING -> selectedProduct != null
        ProductSelectionMode.CREATE_NEW -> newName.isNotBlank() && newMinStock.toDoubleOrNull() != null
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode selector
        TabRow(
            selectedTabIndex = selectedMode.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedMode == ProductSelectionMode.EXISTING,
                onClick = { onModeChange(ProductSelectionMode.EXISTING) },
                text = { Text("Produto Existente") },
                icon = { Icon(Icons.Default.Inventory, null) }
            )
            Tab(
                selected = selectedMode == ProductSelectionMode.CREATE_NEW,
                onClick = { onModeChange(ProductSelectionMode.CREATE_NEW) },
                text = { Text("Criar Novo") },
                icon = { Icon(Icons.Default.Add, null) }
            )
        }

        // Content based on mode
        when (selectedMode) {
            ProductSelectionMode.EXISTING -> {
                ExistingProductContent(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    filteredProducts = filteredProducts,
                    selectedProduct = selectedProduct,
                    onProductSelected = onProductSelected,
                    modifier = Modifier.weight(1f)
                )
            }

            ProductSelectionMode.CREATE_NEW -> {
                CreateNewProductContent(
                    name = newName,
                    onNameChange = onNewNameChange,
                    description = newDescription,
                    onDescriptionChange = onNewDescriptionChange,
                    category = newCategory,
                    expandedCategory = expandedCategory,
                    onExpandedCategoryChange = onExpandedCategoryChange,
                    onCategorySelected = onCategorySelected,
                    barcode = newBarcode,
                    onBarcodeChange = onNewBarcodeChange,
                    unit = newUnit,
                    expandedUnit = expandedUnit,
                    onExpandedUnitChange = onExpandedUnitChange,
                    onUnitSelected = onUnitSelected,
                    minStock = newMinStock,
                    onMinStockChange = onNewMinStockChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = isValid
            ) {
                Text("Seguinte")
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ExistingProductContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredProducts: List<Product>,
    selectedProduct: Product?,
    onProductSelected: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
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

        selectedProduct?.let { product ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                        Icons.Default.CheckCircle,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            "Produto Selecionado",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${getCategoryLabel(product.category)} • ${getUnitLabel(product.unit)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nome do Produto *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.ShoppingBag, null) }
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
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
            leadingIcon = { Icon(Icons.Default.Warning, null) }
        )
    }
}

@Composable
private fun QuantityStep(
    selectedProduct: Product?,
    selectedMode: ProductSelectionMode,
    newName: String,
    newUnit: ProductUnit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isValid = quantity.toDoubleOrNull()?.let { it > 0 } == true
    val unit = selectedProduct?.unit ?: newUnit
    val productName = selectedProduct?.name ?: newName

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                Text(
                    "Produto",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    productName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Unidade: ${getUnitLabel(unit)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = {
                if (it.isEmpty() || it.toDoubleOrNull() != null) {
                    onQuantityChange(it)
                }
            },
            label = { Text("Quantidade *") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Numbers, null) },
            suffix = { Text(getUnitAbbreviation(unit)) },
            placeholder = { Text("0") }
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(18.dp))
                Text("Voltar")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = isValid
            ) {
                Text("Seguinte")
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun DonorInfoStep(
    donorName: String,
    onDonorNameChange: (String) -> Unit,
    donorEmail: String,
    onDonorEmailChange: (String) -> Unit,
    donorPhone: String,
    onDonorPhoneChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Informações do Doador (opcional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = donorName,
            onValueChange = onDonorNameChange,
            label = { Text("Nome do Doador") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )

        OutlinedTextField(
            value = donorEmail,
            onValueChange = onDonorEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Email, null) }
        )

        OutlinedTextField(
            value = donorPhone,
            onValueChange = onDonorPhoneChange,
            label = { Text("Telefone") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Phone, null) }
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Observações") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            leadingIcon = { Icon(Icons.Default.Note, null) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(18.dp))
                Text("Voltar")
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Confirmar")
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
                    "${getCategoryLabel(product.category)} • ${getUnitLabel(product.unit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                product.barcode?.let {
                    Text(
                        "Código: $it",
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
        ProductUnit.UNIT -> "Unidade(s)"
        ProductUnit.KILOGRAM -> "Quilograma(s)"
        ProductUnit.LITER -> "Litro(s)"
        ProductUnit.PACKAGE -> "Embalagem(ns)"
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
