package com.ipca.lojasocial.presentation.ui.screens.collaborator.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ipca.lojasocial.presentation.viewmodel.ApplicationViewModel
import com.ipca.lojasocial.presentation.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaboratorDashboardScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: ApplicationViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load pending applications count
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadPendingApplications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard do Colaborador") },
                actions = {
                    IconButton(onClick = {
                        loginViewModel.logout()
                        onLogout()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sair"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Candidaturas (NOVO!)
            item {
                DashboardCard(
                    title = "Candidaturas",
                    description = "Rever candidaturas a beneficiário",
                    icon = Icons.Default.AssignmentInd,
                    onClick = { onNavigate("applications") },
                    badge = if (uiState.pendingCount > 0) uiState.pendingCount else null
                )
            }

            // Dashboard e Relatórios
            item {
                DashboardCard(
                    title = "Relatórios",
                    description = "Visualizar estatísticas e gerar relatórios",
                    icon = Icons.Default.Dashboard,
                    onClick = { onNavigate("dashboard") }
                )
            }

            item {
                DashboardCard(
                    title = "Inventário",
                    description = "Gerir produtos e stock",
                    icon = Icons.Default.Inventory,
                    onClick = { onNavigate("inventory") }
                )
            }

            item {
                DashboardCard(
                    title = "Beneficiários",
                    description = "Gerir beneficiários",
                    icon = Icons.Default.People,
                    onClick = { onNavigate("beneficiaries") }
                )
            }

            item {
                DashboardCard(
                    title = "Kits",
                    description = "Gerir cestas básicas",
                    icon = Icons.Default.ShoppingBasket,
                    onClick = { onNavigate("kits") }
                )
            }

            item {
                DashboardCard(
                    title = "Entregas",
                    description = "Gerir entregas aos beneficiários",
                    icon = Icons.Default.LocalShipping,
                    onClick = { onNavigate("deliveries") }
                )
            }

            item {
                DashboardCard(
                    title = "Campanhas",
                    description = "Gerir campanhas de angariação",
                    icon = Icons.Default.Campaign,
                    onClick = { onNavigate("campaigns") }
                )
            }
        }
    }
}

/**
 * Reusable Dashboard Card com Badge opcional
 */
@Composable
private fun DashboardCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    badge: Int? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Badge for pending count
            if (badge != null && badge > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        badge.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
