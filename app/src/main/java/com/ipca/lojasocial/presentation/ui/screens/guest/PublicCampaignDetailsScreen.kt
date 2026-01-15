package com.ipca.lojasocial.presentation.ui.screens.guest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.presentation.viewmodel.CampaignViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ Tela pública SIMPLIFICADA - SEM produtos necessários
 * Apenas informações básicas da campanha
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicCampaignDetailsScreen(
    campaignId: String,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: CampaignViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(campaignId) {
        viewModel.loadCampaignById(campaignId)
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
                    TextButton(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Entrar")
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
        when {
            uiState.isLoading && uiState.selectedCampaign == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.selectedCampaign != null -> {
                SimpleCampaignDetailsContent(
                    campaign = uiState.selectedCampaign!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Campanha não encontrada")
                }
            }
        }
    }
}

@Composable
private fun SimpleCampaignDetailsContent(
    campaign: Campaign,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image
        item {
            if (campaign.imageUrl?.isNotBlank() == true) {
                AsyncImage(
                    model = campaign.imageUrl,
                    contentDescription = campaign.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.ShoppingBasket,
                            null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Title Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        campaign.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    if (campaign.status == CampaignStatus.ACTIVE) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "ATIVA - Aceitando Doações",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Description
        item {
            Card {
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
                            Icons.Default.Description,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Sobre a Campanha",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider()
                    Text(
                        campaign.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Dates
        item {
            Card {
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
                            Icons.Default.CalendarMonth,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Período de Recolha",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider()

                    InfoRow("Início", formatDate(campaign.startDate))
                    InfoRow("Fim", formatDate(campaign.endDate))

                    val daysRemaining = calculateDaysRemaining(campaign.endDate)
                    if (daysRemaining > 0 && campaign.status == CampaignStatus.ACTIVE) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                "Faltam $daysRemaining dias para terminar",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        // ✅ How to Help - SIMPLIFICADO
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
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
                            Icons.Default.Favorite,
                            null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "Como Posso Ajudar?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider()

                    Text(
                        "Esta campanha aceita doações de produtos diversos.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        "Pode doar alimentos, produtos de higiene, produtos de limpeza e outros itens essenciais.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        "Qualquer quantidade é bem-vinda! Entre em contacto com os Serviços de Ação Social do IPCA para combinar a entrega.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Contact Info
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                            Icons.Default.ContactMail,
                            null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            "Informações de Contacto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider()
                    Text(
                        "Para mais informações ou para doar, entre em contacto:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "📧 Email: social@ipca.pt",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "📞 Telefone: +351 253 802 260",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "PT"))
    return sdf.format(date)
}

private fun calculateDaysRemaining(endDate: Date): Long {
    val diff = endDate.time - System.currentTimeMillis()
    return diff / (1000 * 60 * 60 * 24)
}
