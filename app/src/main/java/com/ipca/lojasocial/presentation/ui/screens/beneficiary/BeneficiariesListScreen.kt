package com.ipca.lojasocial.presentation.ui.screens.beneficiary

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
import com.ipca.lojasocial.domain.model.Beneficiary
import com.ipca.lojasocial.presentation.ui.components.EmptyState
import com.ipca.lojasocial.presentation.ui.components.ErrorMessage
import com.ipca.lojasocial.presentation.ui.components.LoadingIndicator
import com.ipca.lojasocial.presentation.viewmodel.BeneficiaryViewModel

/**
 * Tela principal de gestão de beneficiários
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariesListScreen(
    viewModel: BeneficiaryViewModel = hiltViewModel(),
    onNavigateToAddBeneficiary: () -> Unit,
    onNavigateToBeneficiaryDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showStatsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beneficiários") },
                actions = {
                    // Estatísticas
                    IconButton(onClick = { showStatsDialog = true }) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text("${uiState.statistics?.active ?: 0}")
                                }
                            }
                        ) {
                            Icon(Icons.Default.People, "Estatísticas")
                        }
                    }
                    // Toggle ativo/inativo
                    IconButton(
                        onClick = { viewModel.toggleShowOnlyActive() }
                    ) {
                        Icon(
                            if (uiState.showOnlyActive) {
                                Icons.Default.ToggleOn
                            } else {
                                Icons.Default.ToggleOff
                            },
                            contentDescription = "Filtrar ativos"
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
                onClick = onNavigateToAddBeneficiary,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, "Adicionar Beneficiário")
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
                    viewModel.searchBeneficiaries(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Estatísticas rápidas
            uiState.statistics?.let { stats ->
                StatsRow(
                    total = stats.active,
                    withSpecialNeeds = stats.withSpecialNeeds,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de beneficiários
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }

                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error ?: "Erro desconhecido",
                        onRetry = { viewModel.loadBeneficiaries() }
                    )
                }

                uiState.filteredBeneficiaries.isEmpty() -> {
                    EmptyState(
                        message = if (searchQuery.isNotBlank()) {
                            "Nenhum beneficiário encontrado"
                        } else {
                            "Nenhum beneficiário cadastrado"
                        },
                        icon = Icons.Default.PersonOff
                    )
                }

                else -> {
                    BeneficiariesList(
                        beneficiaries = uiState.filteredBeneficiaries,
                        onBeneficiaryClick = {
                            onNavigateToBeneficiaryDetails(it.id)
                        }
                    )
                }
            }
        }
    }

    // Diálogo de estatísticas
    if (showStatsDialog) {
        StatisticsDialog(
            statistics = uiState.statistics,
            onDismiss = { showStatsDialog = false }
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
        placeholder = { Text("Pesquisar por nome, nº ou email...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Limpar")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun StatsRow(
    total: Int,
    withSpecialNeeds: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip(
            icon = Icons.Default.People,
            label = "Total",
            value = total.toString(),
            color = MaterialTheme.colorScheme.primary
        )

        if (withSpecialNeeds > 0) {
            StatChip(
                icon = Icons.Default.AccessibleForward,
                label = "Necessidades Especiais",
                value = withSpecialNeeds.toString(),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "$value $label",
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BeneficiariesList(
    beneficiaries: List<Beneficiary>,
    onBeneficiaryClick: (Beneficiary) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(beneficiaries) { beneficiary ->
            BeneficiaryCard(
                beneficiary = beneficiary,
                onClick = { onBeneficiaryClick(beneficiary) }
            )
        }
    }
}

// Em BeneficiariesListScreen.kt
// Substitua o componente BeneficiaryCard por este:

@Composable
private fun BeneficiaryCard(
    beneficiary: Beneficiary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar e info
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = beneficiary.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Informações
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = beneficiary.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Badge,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = beneficiary.studentNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = beneficiary.course,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // ✅ CORRIGIDO: Removido className
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${beneficiary.academicYear}º Ano • ${beneficiary.academicDegree.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (beneficiary.hasSpecialNeeds) {
                            Badge(
                                containerColor =
                                    MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AccessibleForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        "Nec. Especiais",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        if (!beneficiary.isActive) {
                            Badge(
                                containerColor =
                                    MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    "Inativo",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Ícone de navegação
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver detalhes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatisticsDialog(
    statistics: com.ipca.lojasocial.domain.usecase.beneficiary.BeneficiaryStatistics?,
    onDismiss: () -> Unit
) {
    if (statistics == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Estatísticas de Beneficiários") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatRow("Total", statistics.total.toString())
                StatRow("Ativos", statistics.active.toString())
                StatRow("Inativos", statistics.inactive.toString())
                StatRow(
                    "Com Necessidades Especiais",
                    statistics.withSpecialNeeds.toString()
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "Por Curso",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                statistics.byCourse.forEach { (course, count) ->
                    StatRow(course, count.toString())
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "Por Ano Académico",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                statistics.byAcademicYear.forEach { (year, count) ->
                    StatRow("${year}º Ano", count.toString())
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
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
