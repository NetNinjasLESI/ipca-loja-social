package com.ipca.lojasocial.presentation.ui.screens.beneficiary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.ipca.lojasocial.domain.model.Beneficiary
import com.ipca.lojasocial.presentation.ui.components.ErrorMessage
import com.ipca.lojasocial.presentation.ui.components.LoadingIndicator
import com.ipca.lojasocial.presentation.viewmodel.BeneficiaryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryDetailsScreen(
    beneficiaryId: String,
    viewModel: BeneficiaryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeactivateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(beneficiaryId) {
        viewModel.loadBeneficiaryById(beneficiaryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Beneficiário") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(beneficiaryId) }) {
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
            uiState.isLoadingDetails && uiState.selectedBeneficiary == null -> {
                LoadingIndicator()
            }

            uiState.error != null && uiState.selectedBeneficiary == null -> {
                ErrorMessage(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadBeneficiaryById(beneficiaryId) }
                )
            }

            uiState.selectedBeneficiary != null -> {
                BeneficiaryDetailsContent(
                    beneficiary = uiState.selectedBeneficiary!!,
                    onToggleStatus = { showDeactivateDialog = true },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    if (showDeactivateDialog) {
        val beneficiary = uiState.selectedBeneficiary
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = {
                Text(
                    if (beneficiary?.isActive == true) {
                        "Desativar Beneficiário"
                    } else {
                        "Ativar Beneficiário"
                    }
                )
            },
            text = {
                Text(
                    if (beneficiary?.isActive == true) {
                        "Tem certeza que deseja desativar este beneficiário? Ele não poderá mais receber entregas."
                    } else {
                        "Tem certeza que deseja ativar este beneficiário?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        beneficiary?.let {
                            viewModel.toggleBeneficiaryStatus(it.id, !it.isActive)
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
private fun BeneficiaryDetailsContent(
    beneficiary: Beneficiary,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { BeneficiaryHeader(beneficiary = beneficiary) }

        item {
            StatusCard(
                isActive = beneficiary.isActive,
                onToggleStatus = onToggleStatus
            )
        }

        item {
            InfoCard(
                title = "Informações Pessoais",
                icon = Icons.Default.Person
            ) {
                InfoRow(
                    icon = Icons.Default.Badge,
                    label = "Nº Estudante",
                    value = beneficiary.studentNumber
                )
                InfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = beneficiary.email
                )
                InfoRow(
                    icon = Icons.Default.Phone,
                    label = "Telefone",
                    value = beneficiary.phone
                )
                beneficiary.nif?.let {
                    InfoRow(
                        icon = Icons.Default.CreditCard,
                        label = "NIF",
                        value = it
                    )
                }
            }
        }

        // ✅ CORRIGIDO: Dados Académicos sem className
        item {
            InfoCard(
                title = "Dados Académicos",
                icon = Icons.Default.School
            ) {
                InfoRow(
                    icon = Icons.Default.School,
                    label = "Grau",
                    value = beneficiary.academicDegree.displayName
                )
                InfoRow(
                    icon = Icons.Default.MenuBook,
                    label = "Curso",
                    value = beneficiary.course
                )
                InfoRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Ano Académico",
                    value = "${beneficiary.academicYear}º Ano"
                )
            }
        }

        if (beneficiary.address.isNotBlank() ||
            beneficiary.zipCode.isNotBlank() ||
            beneficiary.city.isNotBlank()
        ) {
            item {
                InfoCard(
                    title = "Morada",
                    icon = Icons.Default.Home
                ) {
                    if (beneficiary.address.isNotBlank()) {
                        InfoRow(
                            icon = Icons.Default.Home,
                            label = "Endereço",
                            value = beneficiary.address
                        )
                    }
                    if (beneficiary.zipCode.isNotBlank()) {
                        InfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Código Postal",
                            value = beneficiary.zipCode
                        )
                    }
                    if (beneficiary.city.isNotBlank()) {
                        InfoRow(
                            icon = Icons.Default.LocationCity,
                            label = "Cidade",
                            value = beneficiary.city
                        )
                    }
                }
            }
        }

        item {
            InfoCard(
                title = "Dados Sociais",
                icon = Icons.Default.People
            ) {
                InfoRow(
                    icon = Icons.Default.People,
                    label = "Agregado Familiar",
                    value = "${beneficiary.familySize} pessoas"
                )
                if (beneficiary.monthlyIncome > 0) {
                    InfoRow(
                        icon = Icons.Default.Euro,
                        label = "Rendimento Mensal",
                        value = String.format("€ %.2f", beneficiary.monthlyIncome)
                    )
                }
            }
        }

        if (beneficiary.hasSpecialNeeds) {
            item {
                InfoCard(
                    title = "Necessidades Especiais",
                    icon = Icons.Default.AccessibleForward,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    beneficiary.specialNeedsDescription?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } ?: run {
                        Text(
                            text = "Sem descrição fornecida",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (beneficiary.observations.isNotBlank()) {
            item {
                InfoCard(
                    title = "Observações",
                    icon = Icons.Default.Notes
                ) {
                    Text(
                        text = beneficiary.observations,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        item {
            InfoCard(
                title = "Histórico de Entregas",
                icon = Icons.Default.LocalShipping
            ) {
                Text(
                    text = "Funcionalidade disponível após implementação do módulo de Entregas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        item { SystemInfoCard(beneficiary = beneficiary) }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun BeneficiaryHeader(beneficiary: Beneficiary) {
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
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = beneficiary.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = beneficiary.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = beneficiary.studentNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = beneficiary.course,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StatusCard(isActive: Boolean, onToggleStatus: () -> Unit) {
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
                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SystemInfoCard(beneficiary: Beneficiary) {
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
                text = "Registado em: ${formatDateTime(beneficiary.registeredAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Última atualização: ${formatDateTime(beneficiary.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "ID: ${beneficiary.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}