package com.ipca.lojasocial.presentation.ui.screens.campaign

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ipca.lojasocial.domain.model.ProductCategory
import com.ipca.lojasocial.domain.model.ProductUnit

/**
 * ✅ Dialog para registar doações de produtos
 *
 * Este diálogo:
 * - Permite especificar produto, quantidade, unidade e categoria
 * - Integra automaticamente com o inventário
 * - Se o produto não existe, cria-o
 * - Se existe, adiciona quantidade ao stock
 * - Regista como movimentação de stock (ENTRY - Doação)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductDonationDialog(
    onConfirm: (
        productName: String,
        quantity: Double,
        unit: ProductUnit,
        category: ProductCategory,
        donorName: String?,
        donorEmail: String?,
        donorPhone: String?,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(ProductUnit.UNIT) }
    var selectedCategory by remember { mutableStateOf(ProductCategory.FOOD) }
    var expandedUnit by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    var donorName by remember { mutableStateOf("") }
    var donorEmail by remember { mutableStateOf("") }
    var donorPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isValid = productName.isNotBlank() &&
            quantity.toDoubleOrNull()?.let { it > 0 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            "O produto será adicionado ao inventário",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // ✅ Info card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "A doação será registada e o stock será automaticamente atualizado no inventário.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Informações do Produto
                Text(
                    "Informações do Produto",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nome do Produto *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.ShoppingBag, null) },
                    placeholder = { Text("Ex: Arroz, Óleo, Pasta de Dentes") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                quantity = it
                            }
                        },
                        label = { Text("Quantidade *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Numbers, null) },
                        placeholder = { Text("0") }
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedUnit,
                        onExpandedChange = { expandedUnit = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedUnit.getDisplayName(),
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
                            onDismissRequest = { expandedUnit = false }
                        ) {
                            ProductUnit.entries.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.getDisplayName()) },
                                    onClick = {
                                        selectedUnit = unit
                                        expandedUnit = false
                                    }
                                )
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.getDisplayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                        },
                        leadingIcon = {
                            Icon(selectedCategory.getIcon(), null)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        ProductCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(category.getIcon(), null)
                                        Text(category.getDisplayName())
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Informações do Doador (Opcional)
                Text(
                    "Informações do Doador (opcional)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = donorName,
                    onValueChange = { donorName = it },
                    label = { Text("Nome do Doador") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )

                OutlinedTextField(
                    value = donorEmail,
                    onValueChange = { donorEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, null) }
                )

                OutlinedTextField(
                    value = donorPhone,
                    onValueChange = { donorPhone = it },
                    label = { Text("Telefone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, null) }
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    leadingIcon = { Icon(Icons.Default.Note, null) },
                    placeholder = { Text("Ex: Arroz integral de 1kg") }
                )

                // Ações
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
                        onClick = {
                            if (isValid) {
                                onConfirm(
                                    productName,
                                    quantity.toDouble(),
                                    selectedUnit,
                                    selectedCategory,
                                    donorName.takeIf { it.isNotBlank() },
                                    donorEmail.takeIf { it.isNotBlank() },
                                    donorPhone.takeIf { it.isNotBlank() },
                                    notes
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isValid
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registar")
                    }
                }

                Text(
                    "* Campos obrigatórios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper Extensions
private fun ProductUnit.getDisplayName(): String {
    return when (this) {
        ProductUnit.UNIT -> "Unidade(s)"
        ProductUnit.KILOGRAM -> "Quilograma(s)"
        ProductUnit.LITER -> "Litro(s)"
        ProductUnit.PACKAGE -> "Embalagem(ns)"
    }
}

private fun ProductCategory.getDisplayName(): String {
    return when (this) {
        ProductCategory.FOOD -> "Alimentos"
        ProductCategory.HYGIENE -> "Higiene Pessoal"
        ProductCategory.CLEANING -> "Limpeza"
        ProductCategory.OTHER -> "Outros"
    }
}

private fun ProductCategory.getIcon(): androidx.compose.ui.graphics.vector.ImageVector {
    return when (this) {
        ProductCategory.FOOD -> Icons.Default.Restaurant
        ProductCategory.HYGIENE -> Icons.Default.CleanHands
        ProductCategory.CLEANING -> Icons.Default.Home
        ProductCategory.OTHER -> Icons.Default.MoreHoriz
    }
}
