package com.ipca.lojasocial.presentation.ui.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import com.ipca.lojasocial.domain.model.MovementType
import com.ipca.lojasocial.domain.model.Product
import com.ipca.lojasocial.domain.model.StockMovement
import com.ipca.lojasocial.presentation.ui.components.IPCAButton
import com.ipca.lojasocial.presentation.ui.components.IPCATextField
import com.ipca.lojasocial.presentation.viewmodel.ProductViewModel
import java.util.*

/**
 * Tela para registar movimentações de stock
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockMovementScreen(
    productId: String,
    userId: String,
    viewModel: ProductViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val product = uiState.selectedProduct

    // Estados do formulário
    var selectedType by remember { mutableStateOf(MovementType.ENTRY) }
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var referenceDocument by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isFormInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedProduct) {
        uiState.selectedProduct?.let { prod ->
            if (!isFormInitialized) {
                // Preencher campos se necessário
                isFormInitialized = true
            }
        }
    }

    // Carregar produto
    LaunchedEffect(productId) {
        viewModel.loadProductDetails(productId)
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
                title = { Text("Movimentar Stock") },
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
            // Informação do produto
            product?.let { prod ->
                ProductInfoCard(product = prod)
            }

            // Tipo de movimentação
            Text(
                text = "Tipo de Movimentação",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MovementType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type }
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = getMovementIcon(type),
                                contentDescription = null,
                                tint = getMovementColor(type),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = getMovementTypeLabel(type),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = getMovementTypeDescription(type),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (type != MovementType.values().last()) {
                            Divider()
                        }
                    }
                }
            }

            // Quantidade
            IPCATextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = "Quantidade *",
                placeholder = "0",
                leadingIcon = Icons.Default.Numbers,
                isNumeric = true,
                supportingText = product?.let {
                    "Stock atual: ${formatQuantity(it.currentStock)} " +
                            getUnitLabel(it.unit)
                }
            )

            // Validação de stock (para saídas)
            if (selectedType == MovementType.EXIT) {
                product?.let { prod ->
                    val requestedQty = quantity.toDoubleOrNull() ?: 0.0
                    if (requestedQty > prod.currentStock) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor =
                                MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Stock insuficiente!",
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // Motivo
            IPCATextField(
                value = reason,
                onValueChange = { reason = it },
                label = "Motivo *",
                placeholder = getReasonPlaceholder(selectedType),
                leadingIcon = Icons.Default.Description,
                singleLine = false,
                maxLines = 2
            )

            // Documento de referência
            IPCATextField(
                value = referenceDocument,
                onValueChange = { referenceDocument = it },
                label = "Documento de Referência",
                placeholder = "Ex: Fatura #123, Doação #456",
                leadingIcon = Icons.Default.Receipt
            )

            // Notas
            IPCATextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas",
                placeholder = "Observações adicionais (opcional)",
                leadingIcon = Icons.Default.Notes,
                singleLine = false,
                maxLines = 3
            )

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

            Spacer(modifier = Modifier.height(8.dp))

            // Botão de registar
            IPCAButton(
                onClick = {
                    product?.let { prod ->
                        val movement = StockMovement(
                            productId = prod.id,
                            productName = prod.name,
                            type = selectedType,
                            quantity = quantity.toDoubleOrNull() ?: 0.0,
                            unit = prod.unit,
                            reason = reason,
                            referenceDocument = referenceDocument.ifBlank { null },
                            performedBy = userId,
                            performedAt = Date(),
                            notes = notes
                        )
                        viewModel.recordStockMovement(movement)
                    }
                },
                text = "Registar Movimentação",
                modifier = Modifier.fillMaxWidth(),
                loading = uiState.isLoading,  // ✅ CORRECT
                enabled = quantity.toDoubleOrNull() != null &&
                        quantity.toDoubleOrNull()!! > 0 &&
                        reason.isNotBlank() &&
                        product != null &&
                        (selectedType != MovementType.EXIT ||
                                quantity.toDoubleOrNull()!! <= product.currentStock)
            )

            Text(
                text = "* Campos obrigatórios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = getCategoryLabel(product.category),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Stock Atual",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatQuantity(product.currentStock),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = " ${getUnitLabel(product.unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// Helper functions
private fun getCategoryLabel(
    category: com.ipca.lojasocial.domain.model.ProductCategory
): String {
    return when (category) {
        com.ipca.lojasocial.domain.model.ProductCategory.FOOD -> "Alimentação"
        com.ipca.lojasocial.domain.model.ProductCategory.HYGIENE -> "Higiene"
        com.ipca.lojasocial.domain.model.ProductCategory.CLEANING -> "Limpeza"
        com.ipca.lojasocial.domain.model.ProductCategory.OTHER -> "Outros"
    }
}

private fun getUnitLabel(
    unit: com.ipca.lojasocial.domain.model.ProductUnit
): String {
    return when (unit) {
        com.ipca.lojasocial.domain.model.ProductUnit.UNIT -> "un"
        com.ipca.lojasocial.domain.model.ProductUnit.KILOGRAM -> "kg"
        com.ipca.lojasocial.domain.model.ProductUnit.LITER -> "L"
        com.ipca.lojasocial.domain.model.ProductUnit.PACKAGE -> "pct"
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

private fun getMovementIcon(
    type: MovementType
): androidx.compose.ui.graphics.vector.ImageVector {
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

private fun getMovementTypeDescription(type: MovementType): String {
    return when (type) {
        MovementType.ENTRY -> "Adicionar stock (compra, doação, etc.)"
        MovementType.EXIT -> "Remover stock (entrega, venda, etc.)"
        MovementType.ADJUSTMENT -> "Corrigir stock (contagem, erro, etc.)"
        MovementType.TRANSFER -> "Transferir para outro local"
    }
}

private fun getReasonPlaceholder(type: MovementType): String {
    return when (type) {
        MovementType.ENTRY -> "Ex: Compra, Doação recebida"
        MovementType.EXIT -> "Ex: Entrega a beneficiário, Venda"
        MovementType.ADJUSTMENT -> "Ex: Contagem física, Correção de erro"
        MovementType.TRANSFER -> "Ex: Transferir para armazém B"
    }
}

private fun formatQuantity(quantity: Double): String {
    return if (quantity % 1.0 == 0.0) {
        quantity.toInt().toString()
    } else {
        String.format("%.2f", quantity)
    }
}
