package com.ipca.lojasocial.presentation.ui.screens.reports

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
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.presentation.viewmodel.ReportsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedReport by remember { mutableStateOf<ReportType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatórios") },
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
        ) {
            if (selectedReport == null) {
                ReportSelectionView(
                    onReportSelected = { reportType ->
                        selectedReport = reportType
                        when (reportType) {
                            ReportType.INVENTORY -> viewModel.generateInventoryReport()
                            ReportType.DELIVERIES -> viewModel.generateDeliveriesReport()
                            ReportType.CAMPAIGNS -> viewModel.generateCampaignsReport()
                            ReportType.BENEFICIARIES -> viewModel.generateBeneficiariesReport()
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                ReportDetailsView(
                    reportType = selectedReport!!,
                    inventoryReport = uiState.inventoryReport,
                    deliveriesReport = uiState.deliveriesReport,
                    campaignsReport = uiState.campaignsReport,
                    beneficiariesReport = uiState.beneficiariesReport,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onBack = { selectedReport = null }
                )
            }
        }
    }
}

enum class ReportType {
    INVENTORY, DELIVERIES, CAMPAIGNS, BENEFICIARIES
}

@Composable
private fun ReportSelectionView(
    onReportSelected: (ReportType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Selecione o Relatório",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            ReportCard(
                title = "Relatório de Inventário",
                description = "Stock, categorias, movimentações",
                icon = Icons.Default.Inventory,
                color = MaterialTheme.colorScheme.primary,
                onClick = { onReportSelected(ReportType.INVENTORY) }
            )
        }

        item {
            ReportCard(
                title = "Relatório de Entregas",
                description = "Entregas por status, beneficiário, kit",
                icon = Icons.Default.LocalShipping,
                color = MaterialTheme.colorScheme.secondary,
                onClick = { onReportSelected(ReportType.DELIVERIES) }
            )
        }

        item {
            ReportCard(
                title = "Relatório de Campanhas",
                description = "Campanhas, doações, progresso",
                icon = Icons.Default.Campaign,
                color = MaterialTheme.colorScheme.tertiary,
                onClick = { onReportSelected(ReportType.CAMPAIGNS) }
            )
        }

        item {
            ReportCard(
                title = "Relatório de Beneficiários",
                description = "Beneficiários por curso, agregado familiar",
                icon = Icons.Default.People,
                color = MaterialTheme.colorScheme.error,
                onClick = { onReportSelected(ReportType.BENEFICIARIES) }
            )
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
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
                color = color,
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        icon,
                        null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = color
            )
        }
    }
}

@Composable
private fun ReportDetailsView(
    reportType: ReportType,
    inventoryReport: InventoryReport?,
    deliveriesReport: DeliveriesReport?,
    campaignsReport: CampaignsReport?,
    beneficiariesReport: BeneficiariesReport?,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                    Text(
                        when (reportType) {
                            ReportType.INVENTORY -> "Relatório de Inventário"
                            ReportType.DELIVERIES -> "Relatório de Entregas"
                            ReportType.CAMPAIGNS -> "Relatório de Campanhas"
                            ReportType.BENEFICIARIES -> "Relatório de Beneficiários"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error)
                }
            }

            else -> {
                when (reportType) {
                    ReportType.INVENTORY -> inventoryReport?.let {
                        InventoryReportView(it)
                    }
                    ReportType.DELIVERIES -> deliveriesReport?.let {
                        DeliveriesReportView(it)
                    }
                    ReportType.CAMPAIGNS -> campaignsReport?.let {
                        CampaignsReportView(it)
                    }
                    ReportType.BENEFICIARIES -> beneficiariesReport?.let {
                        BeneficiariesReportView(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryReportView(report: InventoryReport) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReportSummaryCard(
                items = listOf(
                    "Total de Produtos" to report.totalProducts.toString(),
                    "Stock Total" to String.format("%.1f unidades", report.totalStock),
                    "Valor Total" to String.format("%.2f€", report.totalValue),
                    "Produtos em Falta" to report.lowStockProducts.size.toString()
                )
            )
        }

        item {
            SectionHeader("Produtos por Categoria")
        }

        item {
            ChartCard(report.productsByCategory)
        }

        item {
            SectionHeader("Stock por Categoria")
        }

        item {
            report.stockByCategory.forEach { (category, stock) ->
                DataRow(
                    label = category,
                    value = String.format("%.1f", stock)
                )
            }
        }

        item {
            SectionHeader("Top Produtos")
        }

        items(report.topProducts) { product ->
            ProductCard(product)
        }

        if (report.lowStockProducts.isNotEmpty()) {
            item {
                SectionHeader("Produtos em Falta", color = MaterialTheme.colorScheme.error)
            }

            items(report.lowStockProducts) { product ->
                ProductCard(product, isLowStock = true)
            }
        }

        item {
            SectionHeader("Movimentações")
        }

        item {
            ReportSummaryCard(
                items = listOf(
                    "Total" to report.totalMovements.toString(),
                    "Entradas" to report.entries.toString(),
                    "Saídas" to report.exits.toString(),
                    "Ajustes" to report.adjustments.toString()
                )
            )
        }

        item {
            Text(
                "Gerado em: ${formatDateTime(report.generatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeliveriesReportView(report: DeliveriesReport) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReportSummaryCard(
                items = listOf(
                    "Total" to report.totalDeliveries.toString(),
                    "Agendadas" to report.scheduled.toString(),
                    "Confirmadas" to report.confirmed.toString(),
                    "Canceladas" to report.cancelled.toString()
                )
            )
        }

        item {
            SectionHeader("Top Beneficiários")
        }

        item {
            report.deliveriesByBeneficiary.forEach { (name, count) ->
                DataRow(label = name, value = "$count entregas")
            }
        }

        item {
            SectionHeader("Top Kits")
        }

        item {
            report.deliveriesByKit.forEach { (name, count) ->
                DataRow(label = name, value = "$count entregas")
            }
        }

        item {
            Text(
                "Gerado em: ${formatDateTime(report.generatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CampaignsReportView(report: CampaignsReport) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReportSummaryCard(
                items = listOf(
                    "Total" to report.totalCampaigns.toString(),
                    "Ativas" to report.active.toString(),
                    "Rascunhos" to report.draft.toString(),
                    "Concluídas" to report.completed.toString()
                )
            )
        }

        item {
            ReportSummaryCard(
                items = listOf(
                    "Meta Total" to String.format("%.2f€", report.totalGoal),
                    "Angariado" to String.format("%.2f€", report.totalRaised),
                    "Doações" to report.totalDonations.toString(),
                    "Média/Doação" to String.format("%.2f€", report.averageDonation)
                )
            )
        }

        item {
            SectionHeader("Top Campanhas")
        }

        items(report.topCampaigns) { campaign ->
            CampaignCard(campaign)
        }

        item {
            Text(
                "Gerado em: ${formatDateTime(report.generatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BeneficiariesReportView(report: BeneficiariesReport) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReportSummaryCard(
                items = listOf(
                    "Total" to report.totalBeneficiaries.toString(),
                    "Ativos" to report.active.toString(),
                    "Inativos" to report.inactive.toString()
                )
            )
        }

        item {
            SectionHeader("Por Curso")
        }

        item {
            ChartCard(report.byCourse)
        }

        item {
            SectionHeader("Por Agregado Familiar")
        }

        item {
            ChartCard(report.byHouseholdSize)
        }

        item {
            SectionHeader("Top Beneficiários (Entregas)")
        }

        items(report.topBeneficiaries) { beneficiary ->
            DataRow(
                label = beneficiary.name,
                value = "${beneficiary.deliveriesCount} entregas"
            )
        }

        item {
            Text(
                "Gerado em: ${formatDateTime(report.generatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper Composables

@Composable
private fun SectionHeader(
    text: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
private fun ReportSummaryCard(items: List<Pair<String, String>>) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartCard(data: Map<String, Int>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEach { (label, value) ->
                DataRow(label = label, value = value.toString())
            }
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(
            value,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Divider()
}

@Composable
private fun ProductCard(product: ProductStockInfo, isLowStock: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isLowStock) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text(
                    product.category,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "${product.currentStock} ${product.unit}",
                fontWeight = FontWeight.Bold,
                color = if (isLowStock) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CampaignCard(campaign: CampaignPerformance) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(campaign.title, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Angariado:", style = MaterialTheme.typography.bodySmall)
                Text(
                    "${String.format("%.2f", campaign.raised)}€ / ${String.format("%.2f", campaign.goal)}€",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = campaign.progress / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "${campaign.progress}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
    return sdf.format(date)
}
