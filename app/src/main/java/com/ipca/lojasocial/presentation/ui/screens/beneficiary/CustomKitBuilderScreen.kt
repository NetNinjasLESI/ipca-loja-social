package com.ipca.lojasocial.presentation.ui.screens.beneficiary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.presentation.viewmodel.CustomKitBuilderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomKitBuilderScreen(
    beneficiaryId: String,
    onNavigateBack: () -> Unit,
    viewModel: CustomKitBuilderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(1500)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState.currentStep) {
                            CustomKitBuilderViewModel.BuilderStep.START -> "Montar Kit"
                            CustomKitBuilderViewModel.BuilderStep.SELECT_PRODUCTS -> "Adicionar Produtos"
                            CustomKitBuilderViewModel.BuilderStep.REVIEW -> "Confirmar"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep == CustomKitBuilderViewModel.BuilderStep.START) {
                            onNavigateBack()
                        } else {
                            viewModel.goBack()
                        }
                    }) {
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState.currentStep) {
                CustomKitBuilderViewModel.BuilderStep.START -> {
                    StartStep(
                        availableKits = uiState.availableKits,
                        isLoading = uiState.isLoading,
                        onStartFromScratch = { viewModel.startFromScratch() },
                        onStartFromKit = { viewModel.startFromKit(it) }
                    )
                }
                CustomKitBuilderViewModel.BuilderStep.SELECT_PRODUCTS -> {
                    SelectProductsStep(
                        customKit = uiState.customKit,
                        availableProducts = viewModel.getFilteredProducts(),
                        searchQuery = uiState.searchQuery,
                        selectedCategory = uiState.selectedCategory,
                        validationError = uiState.validationError,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onAddProduct = { product, quantity ->
                            viewModel.addProduct(product, quantity)
                        },
                        onRemoveProduct = { viewModel.removeProduct(it) },
                        onUpdateQuantity = { productId, quantity ->
                            viewModel.updateProductQuantity(productId, quantity)
                        },
                        onContinue = { viewModel.goToReview() }
                    )
                }
                CustomKitBuilderViewModel.BuilderStep.REVIEW -> {
                    ReviewStep(
                        customKit = uiState.customKit,
                        isLoading = uiState.isLoading,
                        onNotesChange = { viewModel.updateNotes(it) },
                        onSubmit = { notes ->
                            viewModel.submitCustomKit(beneficiaryId, notes)
                        }
                    )
                }
            }

            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(uiState.error!!)
                }
            }

            if (uiState.successMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Text(uiState.successMessage!!)
                    }
                }
            }
        }
    }
}

// EXTENSÕES PARA CONVERSÃO DE NOMES
fun ProductCategory.getDisplayName(): String {
    return when (this) {
        ProductCategory.FOOD -> "Comida"
        ProductCategory.HYGIENE -> "Higiene"
        ProductCategory.CLEANING -> "Limpeza"
        ProductCategory.OTHER -> "Outros"
    }
}

fun ProductUnit.getDisplayName(): String {
    return when (this) {
        ProductUnit.KILOGRAM -> "kg"
        ProductUnit.LITER -> "L"
        ProductUnit.UNIT -> "un"
        ProductUnit.PACKAGE -> "pacote(s)"
    }
}

@Composable
private fun StartStep(
    availableKits: List<Kit>,
    isLoading: Boolean,
    onStartFromScratch: () -> Unit,
    onStartFromKit: (String) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Como deseja montar o seu kit?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Escolha produtos livremente ou comece com um kit base",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                onClick = onStartFromScratch,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Começar do Zero",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Escolha os produtos que precisa",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ArrowForward, null)
                }
            }
        }

        if (availableKits.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        "OU",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }

            item {
                Text(
                    "Começar com Kit Base",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(availableKits) { kit ->
            KitBaseCard(
                kit = kit,
                onClick = { onStartFromKit(kit.id) }
            )
        }
    }
}

@Composable
private fun KitBaseCard(
    kit: Kit,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    kit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
            }

            Text(
                kit.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Inventory,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${kit.items.size} produtos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    " • Pode adicionar ou remover produtos depois",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SelectProductsStep(
    customKit: CustomKit,
    availableProducts: List<Product>,
    searchQuery: String,
    selectedCategory: ProductCategory?,
    validationError: String?,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (ProductCategory?) -> Unit,
    onAddProduct: (Product, Int) -> Unit,
    onRemoveProduct: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CartSummary(
            customKit = customKit,
            onRemoveProduct = onRemoveProduct,
            onUpdateQuantity = onUpdateQuantity
        )

        HorizontalDivider()

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Procurar produtos...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, "Limpar")
                                }
                            }
                        }
                    )
                }

                item {
                    CategoryFilter(
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected
                    )
                }

                items(availableProducts) { product ->
                    val itemInCart = customKit.selectedItems.find { it.productId == product.id }
                    ProductCard(
                        product = product,
                        quantityInCart = itemInCart?.quantity ?: 0,
                        onAddToCart = { quantity ->
                            onAddProduct(product, quantity)
                        }
                    )
                }

                if (availableProducts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhum produto encontrado",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (validationError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        validationError,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = customKit.selectedItems.isNotEmpty()
            ) {
                Text("Continuar para Revisão")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null)
            }
        }
    }
}

@Composable
private fun CartSummary(
    customKit: CustomKit,
    onRemoveProduct: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ShoppingCart, null)
                    Text(
                        "Meu Kit (${customKit.totalItems()} produtos)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        if (isExpanded) "Recolher" else "Expandir"
                    )
                }
            }

            if (isExpanded && customKit.selectedItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                customKit.selectedItems.forEach { item ->
                    CartItemRow(
                        item = item,
                        onRemove = { onRemoveProduct(item.productId) },
                        onQuantityChange = { newQty ->
                            onUpdateQuantity(item.productId, newQty)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CustomKitItem,
    onRemove: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onQuantityChange(item.quantity - 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, "Diminuir", modifier = Modifier.size(16.dp))
            }

            Text(
                "${item.quantity} ${item.unit.getDisplayName()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.widthIn(min = 60.dp)
            )

            IconButton(
                onClick = { onQuantityChange(item.quantity + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, "Aumentar", modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    "Remover",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    selectedCategory: ProductCategory?,
    onCategorySelected: (ProductCategory?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Todos") }
            )
        }

        items(ProductCategory.entries.toList()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.getDisplayName()) }
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    quantityInCart: Int,
    onAddToCart: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showDialog = true }
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
                    product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Stock: ${product.currentStock.toInt()} ${product.unit.getDisplayName()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (quantityInCart > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            quantityInCart.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Icon(Icons.Default.Add, "Adicionar", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showDialog) {
        AddProductDialog(
            product = product,
            currentQuantity = quantityInCart,
            onDismiss = { showDialog = false },
            onConfirm = { quantity ->
                onAddToCart(quantity)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AddProductDialog(
    product: Product,
    currentQuantity: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(currentQuantity.coerceAtLeast(1)) }
    val maxStock = product.currentStock.toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(product.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Disponível: $maxStock ${product.unit.getDisplayName()}")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        enabled = quantity > 1
                    ) {
                        Icon(Icons.Default.Remove, "Diminuir")
                    }

                    Text(
                        "$quantity ${product.unit.getDisplayName()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { if (quantity < maxStock) quantity++ },
                        enabled = quantity < maxStock
                    ) {
                        Icon(Icons.Default.Add, "Aumentar")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(quantity) },
                enabled = quantity > 0 && quantity <= maxStock
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ReviewStep(
    customKit: CustomKit,
    isLoading: Boolean,
    onNotesChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    var additionalNotes by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Revisar Kit",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
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
                    if (customKit.isBasedOnKit()) {
                        Text(
                            "Baseado em: ${customKit.baseKitName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        "${customKit.totalItems()} produtos selecionados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Text(
                "Produtos:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(customKit.selectedItems) { item ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        item.productName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "${item.quantity} ${item.unit.getDisplayName()}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = additionalNotes,
                onValueChange = { additionalNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Observações (opcional)") },
                placeholder = { Text("Ex: Preferências, alergias, etc.") },
                minLines = 3,
                maxLines = 5
            )
        }

        item {
            Button(
                onClick = { onSubmit(additionalNotes) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Solicitação")
                }
            }
        }
    }
}