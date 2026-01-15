package com.ipca.lojasocial.presentation.ui.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ipca.lojasocial.domain.model.Product
import com.ipca.lojasocial.domain.model.ProductCategory
import com.ipca.lojasocial.domain.model.ProductUnit
import com.ipca.lojasocial.presentation.ui.components.IPCAButton
import com.ipca.lojasocial.presentation.ui.components.IPCATextField
import com.ipca.lojasocial.presentation.viewmodel.ProductViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: String? = null,
    userId: String,
    scannedBarcode: String? = null,
    viewModel: ProductViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToScanner: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = productId != null

    // Estados do formulário
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ProductCategory.FOOD) }
    var barcode by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(ProductUnit.UNIT) }
    var currentStock by remember { mutableStateOf("") }
    var minimumStock by remember { mutableStateOf("") }
    var expiryDateStr by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<Date?>(null) }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFormInitialized by remember { mutableStateOf(false) }

    // ✅ Atualizar barcode quando receber código escaneado
    LaunchedEffect(scannedBarcode) {
        if (scannedBarcode != null && scannedBarcode.isNotBlank()) {
            barcode = scannedBarcode
        }
    }

    // ✅ CORREÇÃO: Carregar produto se for edição
    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.loadProductDetails(productId)
        }
    }

    // ✅ CORREÇÃO: Preencher formulário quando produto carregar
    LaunchedEffect(uiState.selectedProduct) {
        if (isEditMode && uiState.selectedProduct != null && !isFormInitialized) {
            uiState.selectedProduct?.let { product ->
                name = product.name
                description = product.description
                category = product.category
                barcode = product.barcode ?: ""
                unit = product.unit
                currentStock = product.currentStock.toString()
                minimumStock = product.minimumStock.toString()
                expiryDate = product.expiryDate
                expiryDateStr = product.expiryDate?.let { formatDate(it) } ?: ""
            }
        }
    }

    // Navegar de volta em caso de sucesso
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Editar Produto" else "Novo Produto")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ✅ Mostrar loading enquanto carrega produto em modo edição
            when {
                isEditMode && uiState.isLoadingDetails -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                    )
                }

                isEditMode && uiState.selectedProduct == null -> {
                    // ✅ Erro ao carregar produto
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Erro ao carregar produto",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Voltar")
                        }
                    }
                }

                else -> {
                    // ✅ Formulário
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Nome
                        IPCATextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Nome do Produto *",
                            placeholder = "Ex: Arroz Agulha",
                            leadingIcon = Icons.Default.ShoppingCart
                        )

                        // Descrição
                        IPCATextField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Descrição",
                            placeholder = "Descrição detalhada do produto",
                            leadingIcon = Icons.Default.Description,
                            singleLine = false,
                            maxLines = 3
                        )

                        // Categoria
                        ExposedDropdownMenuBox(
                            expanded = showCategoryMenu,
                            onExpandedChange = { showCategoryMenu = it }
                        ) {
                            OutlinedTextField(
                                value = getCategoryLabel(category),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoria *") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = showCategoryMenu
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors()
                            )

                            ExposedDropdownMenu(
                                expanded = showCategoryMenu,
                                onDismissRequest = { showCategoryMenu = false }
                            ) {
                                ProductCategory.entries.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(getCategoryLabel(cat)) },
                                        onClick = {
                                            category = cat
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Código de barras COM SCANNER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IPCATextField(
                                value = barcode,
                                onValueChange = { barcode = it },
                                label = "Código de Barras",
                                placeholder = "000000000000",
                                leadingIcon = Icons.Default.QrCode,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedButton(
                                onClick = onNavigateToScanner,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, "Scan")
                            }
                        }

                        // Unidade
                        ExposedDropdownMenuBox(
                            expanded = showUnitMenu,
                            onExpandedChange = { showUnitMenu = it }
                        ) {
                            OutlinedTextField(
                                value = getUnitLabel(unit),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unidade *") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = showUnitMenu
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors()
                            )

                            ExposedDropdownMenu(
                                expanded = showUnitMenu,
                                onDismissRequest = { showUnitMenu = false }
                            ) {
                                ProductUnit.entries.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(getUnitLabel(u)) },
                                        onClick = {
                                            unit = u
                                            showUnitMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Stocks
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IPCATextField(
                                value = currentStock,
                                onValueChange = { currentStock = it },
                                label = "Stock Atual *",
                                placeholder = "0",
                                leadingIcon = Icons.Default.Inventory2,
                                modifier = Modifier.weight(1f),
                                isNumeric = true
                            )

                            IPCATextField(
                                value = minimumStock,
                                onValueChange = { minimumStock = it },
                                label = "Stock Mínimo *",
                                placeholder = "0",
                                leadingIcon = Icons.Default.Warning,
                                modifier = Modifier.weight(1f),
                                isNumeric = true
                            )
                        }

                        // Data de validade com DatePicker
                        OutlinedTextField(
                            value = expiryDateStr,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Data de Validade") },
                            placeholder = { Text("Selecione a data") },
                            leadingIcon = {
                                Icon(Icons.Default.Event, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.CalendarMonth, "Selecionar data")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )

                        if (showDatePicker) {
                            DatePickerDialog(
                                onDateSelected = { selectedDate ->
                                    expiryDate = selectedDate
                                    expiryDateStr = selectedDate?.let { formatDate(it) } ?: ""
                                    showDatePicker = false
                                },
                                onDismiss = { showDatePicker = false },
                                initialDate = expiryDate
                            )
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
                                IPCAButton(
                                    onClick = {
                                        viewModel.deleteProduct(productId!!)
                                    },
                                    text = "Eliminar",
                                    modifier = Modifier.weight(1f),
                                    loading = uiState.isLoading,
                                    enabled = !uiState.isLoading,
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            }

                            IPCAButton(
                                onClick = {
                                    val product = Product(
                                        id = productId ?: "",
                                        name = name,
                                        description = description,
                                        category = category,
                                        barcode = barcode.ifBlank { null },
                                        unit = unit,
                                        currentStock = currentStock.toDoubleOrNull() ?: 0.0,
                                        minimumStock = minimumStock.toDoubleOrNull() ?: 0.0,
                                        expiryDate = expiryDate,
                                        isActive = true,
                                        createdBy = userId
                                    )

                                    if (isEditMode) {
                                        viewModel.updateProduct(product)
                                    } else {
                                        viewModel.createProduct(product, userId)
                                    }
                                },
                                text = if (isEditMode) "Guardar" else "Criar Produto",
                                modifier = Modifier.weight(if (isEditMode) 1f else 1f),
                                loading = uiState.isLoading,
                                enabled = name.isNotBlank() &&
                                        currentStock.toDoubleOrNull() != null &&
                                        minimumStock.toDoubleOrNull() != null
                            )
                        }

                        Text(
                            text = "* Campos obrigatórios",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // ✅ Limpar ao sair
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelection()
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

private fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    return String.format(
        "%02d/%02d/%04d",
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.YEAR)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Date?) -> Unit,
    onDismiss: () -> Unit,
    initialDate: Date? = null
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.time ?: System.currentTimeMillis()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(Date(it))
                } ?: onDateSelected(null)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}