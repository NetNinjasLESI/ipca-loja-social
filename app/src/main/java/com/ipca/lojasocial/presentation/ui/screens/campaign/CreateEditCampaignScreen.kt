package com.ipca.lojasocial.presentation.ui.screens.campaign

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
 * ✅ Tela SIMPLIFICADA de criação/edição de campanhas
 * - Sem gestão de produtos necessários
 * - Apenas informações básicas da campanha
 * - Doações são registadas depois, livremente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditCampaignScreen(
    campaignId: String? = null,
    userId: String,
    viewModel: CampaignViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = campaignId != null

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember {
        mutableStateOf(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time)
    }
    var endDate by remember {
        mutableStateOf(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time)
    }
    var imageUrl by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(campaignId) {
        if (isEditMode && campaignId != null) {
            viewModel.loadCampaignById(campaignId)
        }
    }

    LaunchedEffect(uiState.selectedCampaign) {
        uiState.selectedCampaign?.let { campaign ->
            title = campaign.title
            description = campaign.description
            startDate = campaign.startDate
            endDate = campaign.endDate
            imageUrl = campaign.imageUrl ?: ""
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage == "Campanha criada com sucesso" ||
            uiState.successMessage == "Campanha atualizada" ||
            uiState.successMessage == "Campanha eliminada"
        ) {
            onNavigateBack()
        }
    }

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    val isFormValid = title.isNotBlank() &&
            description.isNotBlank() &&
            !startDate.before(today) &&
            endDate.after(startDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Campanha" else "Nova Campanha") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            if (isEditMode) {
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
                                Icons.Default.Edit,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Modo de Edição",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "A editar campanha existente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // ✅ Info: Doações livres
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text(
                                "Campanha de Recolha Livre",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Os doadores podem doar qualquer produto. Todas as doações são automaticamente adicionadas ao inventário.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Basic Info
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da Campanha *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Campaign, null) },
                    isError = title.isBlank() && title.isNotEmpty()
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    placeholder = {
                        Text("Descreva o objetivo da campanha e que tipo de produtos são mais necessários")
                    }
                )
            }

            // Dates
            item {
                Card(onClick = { showStartDatePicker = true }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Data de Início *",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatDate(startDate),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (startDate.before(today)) {
                                Text(
                                    "⚠ Data deve ser futura",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Icon(Icons.Default.CalendarToday, null)
                    }
                }
            }

            item {
                Card(onClick = { showEndDatePicker = true }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Data de Fim *",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatDate(endDate),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (!endDate.after(startDate)) {
                                Text(
                                    "⚠ Deve ser após data início",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Icon(Icons.Default.Event, null)
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL da Imagem (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Image, null) }
                )
            }

            // Summary
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
                        Text(
                            "Resumo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Duração: ${calculateDuration(startDate, endDate)} dias")
                        Text("Tipo: Recolha de Produtos")
                    }
                }
            }

            // Error
            if (uiState.error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            uiState.error!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Actions
            item {
                Button(
                    onClick = {
                        val campaign = Campaign(
                            id = campaignId ?: "",
                            title = title,
                            description = description,
                            goalType = CampaignGoalType.PRODUCT,
                            startDate = startDate,
                            endDate = endDate,
                            imageUrl = imageUrl.takeIf { it.isNotBlank() },
                            createdBy = userId
                        )

                        if (isEditMode) {
                            viewModel.updateCampaign(campaign)
                        } else {
                            viewModel.createCampaign(campaign, userId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEditMode) "Guardar" else "Criar Campanha")
                    }
                }
            }

            if (isEditMode) {
                item {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar Campanha")
                    }
                }
            }

            item {
                Text(
                    "* Campos obrigatórios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Dialogs
    if (showDeleteDialog && campaignId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("Eliminar Campanha") },
            text = { Text("Tem certeza? Esta ação não pode ser revertida.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCampaign(campaignId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            title = "Data de Início",
            initialDate = startDate,
            minDate = today,
            onDateSelected = { date ->
                startDate = date
                if (endDate.before(date) || endDate == date) {
                    endDate = Calendar.getInstance().apply {
                        time = date
                        add(Calendar.DAY_OF_MONTH, 1)
                    }.time
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        val minEndDate = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.DAY_OF_MONTH, 1)
        }.time

        DatePickerDialog(
            title = "Data de Fim",
            initialDate = if (endDate.after(minEndDate)) endDate else minEndDate,
            minDate = minEndDate,
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    title: String,
    initialDate: Date,
    minDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        time = if (initialDate.after(minDate)) initialDate else minDate
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= minDate.time
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(Date(it))
                    }
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = { Text(title, modifier = Modifier.padding(start = 24.dp, top = 16.dp)) }
        )
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "PT"))
    return sdf.format(date)
}

private fun calculateDuration(startDate: Date, endDate: Date): Long {
    val diff = endDate.time - startDate.time
    return (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
}
