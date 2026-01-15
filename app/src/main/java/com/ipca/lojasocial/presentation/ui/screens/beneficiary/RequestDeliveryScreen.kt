package com.ipca.lojasocial.presentation.ui.screens.beneficiary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.firebase.auth.FirebaseAuth
import com.ipca.lojasocial.domain.model.Kit
import com.ipca.lojasocial.presentation.viewmodel.BeneficiaryDashboardViewModel
import com.ipca.lojasocial.presentation.viewmodel.BeneficiaryDeliveryViewModel
import com.ipca.lojasocial.presentation.viewmodel.KitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDeliveryScreen(
    dashboardViewModel: BeneficiaryDashboardViewModel = hiltViewModel(),
    deliveryViewModel: BeneficiaryDeliveryViewModel = hiltViewModel(),
    kitViewModel: KitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val deliveryState by deliveryViewModel.uiState.collectAsState()
    val kitsState by kitViewModel.uiState.collectAsState()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var selectedKit by remember { mutableStateOf<Kit?>(null) }
    var requestNotes by remember { mutableStateOf("") }
    var showKitSelector by remember { mutableStateOf(false) }

    // Carregar kits disponíveis
    LaunchedEffect(Unit) {
        kitViewModel.loadKits()
        if (currentUserId.isNotBlank() && dashboardState.beneficiary == null) {
            dashboardViewModel.loadBeneficiaryData(currentUserId)
        }
    }

    // Navegar de volta em caso de sucesso
    LaunchedEffect(deliveryState.successMessage) {
        if (deliveryState.successMessage != null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar Entrega") },
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
            // Info Card
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
                        Icons.Default.Info,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            "Como funciona?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "1. Escolha o kit desejado\n" +
                                    "2. Adicione observações (opcional)\n" +
                                    "3. Envie a solicitação\n" +
                                    "4. Aguarde aprovação",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Kit Selector Card
            Card(
                onClick = { showKitSelector = true },
                modifier = Modifier.fillMaxWidth()
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
                            "Kit *",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            selectedKit?.name ?: "Selecionar kit",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedKit != null)
                                FontWeight.Bold else FontWeight.Normal
                        )
                        if (selectedKit != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${selectedKit!!.items.size} produtos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }

            // Request Notes
            OutlinedTextField(
                value = requestNotes,
                onValueChange = { requestNotes = it },
                label = { Text("Observações (opcional)") },
                placeholder = {
                    Text("Ex: Urgente, preferência de horário, etc.")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4,
                leadingIcon = { Icon(Icons.Default.Description, null) }
            )

            // Error Message
            if (deliveryState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
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
                            Icons.Default.Error,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            deliveryState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    val beneficiaryId = dashboardState.beneficiary?.id ?: ""
                    val kitId = selectedKit?.id ?: ""

                    deliveryViewModel.requestDelivery(
                        beneficiaryId = beneficiaryId,
                        kitId = kitId,
                        requestNotes = requestNotes
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedKit != null &&
                        dashboardState.beneficiary != null &&
                        !deliveryState.isLoading
            ) {
                if (deliveryState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Solicitação")
                }
            }

            Text(
                "* Campo obrigatório",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Kit Selector Dialog
    if (showKitSelector) {
        KitSelectorDialog(
            kits = kitsState.filteredKits.filter { it.isActive },
            onKitSelected = {
                selectedKit = it
                showKitSelector = false
            },
            onDismiss = { showKitSelector = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KitSelectorDialog(
    kits: List<Kit>,
    onKitSelected: (Kit) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = kits.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(16.dp)
            ) {
                Text(
                    "Escolher Kit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Nenhum kit disponível")
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered) { kit ->
                            KitCard(
                                kit = kit,
                                onClick = { onKitSelected(kit) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KitCard(
    kit: Kit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ChevronRight, null)
            }

            if (kit.description.isNotBlank()) {
                Text(
                    kit.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

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
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}