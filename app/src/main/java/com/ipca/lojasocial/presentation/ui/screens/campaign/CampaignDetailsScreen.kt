package com.ipca.lojasocial.presentation.ui.screens.campaign

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.ipca.lojasocial.presentation.viewmodel.CampaignViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ Tela de detalhes da campanha com lista de doações
 * ATUALIZADO: Agora usa RegisterProductDonationEnhancedDialog que OBRIGA
 * a selecionar produto existente ou criar novo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailsScreen(
    campaignId: String,
    userId: String,
    viewModel: CampaignViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val campaign = uiState.selectedCampaign
    val donations = uiState.productDonations
    val availableProducts by viewModel.inventoryProducts.collectAsState()

    var showDonationDialog by remember { mutableStateOf(false) }
    var showActivateDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(campaignId) {
        viewModel.loadCampaignById(campaignId)
    }

    // Limpar mensagem de sucesso após 3 segundos
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Campanha") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    if (campaign != null) {
                        IconButton(onClick = { onNavigateToEdit(campaign.id) }) {
                            Icon(Icons.Default.Edit, "Editar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (campaign?.status == CampaignStatus.ACTIVE) {
                ExtendedFloatingActionButton(
                    onClick = { showDonationDialog = true },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    text = { Text("Registar Doação") }
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (campaign == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Campanha não encontrada")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campaign Info Card
                item {
                    CampaignInfoCard(
                        campaign = campaign,
                        onActivate = { showActivateDialog = true },
                        onComplete = { showCompleteDialog = true }
                    )
                }

                // Success Message
                if (uiState.successMessage != null) {
                    item {
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    uiState.successMessage!!,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Donations Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Doações Recebidas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${donations.size} doação${if (donations.size != 1) "ões" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Donations List
                if (donations.isEmpty()) {
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
                                        Icons.Default.Favorite,
                                        null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("Nenhuma doação registada")
                                    if (campaign.status == CampaignStatus.ACTIVE) {
                                        Text(
                                            "Clique no botão para registar a primeira doação",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(donations) { donation ->
                        DonationCard(donation = donation)
                    }
                }
            }
        }
    }

    // ✅ NOVO DIÁLOGO COM SELEÇÃO OBRIGATÓRIA DE PRODUTO
    if (showDonationDialog) {
        RegisterProductDonationEnhancedDialog(
            availableProducts = availableProducts,
            onSelectExisting = { product, quantity, donorName, donorEmail, donorPhone, notes ->
                viewModel.registerProductDonationFromExisting(
                    campaignId = campaignId,
                    product = product,
                    quantity = quantity,
                    donorName = donorName,
                    donorEmail = donorEmail,
                    donorPhone = donorPhone,
                    notes = notes,
                    userId = userId
                )
                showDonationDialog = false
            },
            onCreateNew = { name, description, category, barcode, unit, minimumStock, quantity, donorName, donorEmail, donorPhone, notes ->
                viewModel.registerProductDonationWithNewProduct(
                    campaignId = campaignId,
                    productName = name,
                    productDescription = description,
                    category = category,
                    barcode = barcode,
                    unit = unit,
                    minimumStock = minimumStock,
                    quantity = quantity,
                    donorName = donorName,
                    donorEmail = donorEmail,
                    donorPhone = donorPhone,
                    notes = notes,
                    userId = userId
                )
                showDonationDialog = false
            },
            onDismiss = { showDonationDialog = false }
        )
    }

    if (showActivateDialog) {
        AlertDialog(
            onDismissRequest = { showActivateDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null) },
            title = { Text("Ativar Campanha") },
            text = { Text("Deseja ativar esta campanha? Ela ficará visível para doadores.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.activateCampaign(campaignId)
                    showActivateDialog = false
                }) {
                    Text("Ativar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showActivateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            icon = { Icon(Icons.Default.Done, null) },
            title = { Text("Concluir Campanha") },
            text = { Text("Deseja marcar esta campanha como concluída?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.completeCampaign(campaignId)
                    showCompleteDialog = false
                }) {
                    Text("Concluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CampaignInfoCard(
    campaign: Campaign,
    onActivate: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (campaign.status) {
                CampaignStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                CampaignStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (campaign.status) {
                    CampaignStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                    CampaignStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                    CampaignStatus.DRAFT -> MaterialTheme.colorScheme.secondary
                    CampaignStatus.CANCELLED -> MaterialTheme.colorScheme.error
                }
            ) {
                Text(
                    text = when (campaign.status) {
                        CampaignStatus.ACTIVE -> "ATIVA"
                        CampaignStatus.COMPLETED -> "CONCLUÍDA"
                        CampaignStatus.DRAFT -> "RASCUNHO"
                        CampaignStatus.CANCELLED -> "CANCELADA"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Title
            Text(
                campaign.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Description
            Text(
                campaign.description,
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            // Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Início",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatDate(campaign.startDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Fim",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatDate(campaign.endDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons
            if (campaign.status == CampaignStatus.DRAFT) {
                Button(
                    onClick = onActivate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ativar Campanha")
                }
            } else if (campaign.status == CampaignStatus.ACTIVE) {
                OutlinedButton(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Done, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Marcar como Concluída")
                }
            }
        }
    }
}

@Composable
private fun DonationCard(donation: ProductDonation) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    donation.category.getIcon(),
                    null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        donation.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${donation.quantity} ${donation.unit.getDisplayName()} • ${donation.category.getDisplayName()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (donation.donorName != null) {
                HorizontalDivider()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Doador: ${donation.donorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (donation.notes.isNotBlank()) {
                Text(
                    donation.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                formatDateTime(donation.donatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper Functions
private fun ProductCategory.getIcon(): androidx.compose.ui.graphics.vector.ImageVector {
    return when (this) {
        ProductCategory.FOOD -> Icons.Default.Restaurant
        ProductCategory.HYGIENE -> Icons.Default.CleanHands
        ProductCategory.CLEANING -> Icons.Default.Home
        ProductCategory.OTHER -> Icons.Default.MoreHoriz
    }
}

private fun ProductCategory.getDisplayName(): String {
    return when (this) {
        ProductCategory.FOOD -> "Alimentos"
        ProductCategory.HYGIENE -> "Higiene"
        ProductCategory.CLEANING -> "Limpeza"
        ProductCategory.OTHER -> "Outros"
    }
}

private fun ProductUnit.getDisplayName(): String {
    return when (this) {
        ProductUnit.UNIT -> "un"
        ProductUnit.KILOGRAM -> "kg"
        ProductUnit.LITER -> "L"
        ProductUnit.PACKAGE -> "pct"
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "PT"))
    return sdf.format(date)
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}
