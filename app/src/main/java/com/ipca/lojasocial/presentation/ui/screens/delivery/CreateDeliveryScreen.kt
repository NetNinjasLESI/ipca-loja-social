package com.ipca.lojasocial.presentation.ui.screens.delivery

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
import com.ipca.lojasocial.domain.model.Beneficiary
import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.domain.model.Kit
import com.ipca.lojasocial.presentation.viewmodel.BeneficiaryViewModel
import com.ipca.lojasocial.presentation.viewmodel.DeliveryViewModel
import com.ipca.lojasocial.presentation.viewmodel.KitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeliveryScreen(
    userId: String,
    viewModel: DeliveryViewModel = hiltViewModel(),
    beneficiaryViewModel: BeneficiaryViewModel = hiltViewModel(),
    kitViewModel: KitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val beneficiariesState by beneficiaryViewModel.uiState.collectAsState()
    val kitsState by kitViewModel.uiState.collectAsState()

    var selectedBeneficiary by remember { mutableStateOf<Beneficiary?>(null) }
    var selectedKit by remember { mutableStateOf<Kit?>(null) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var notes by remember { mutableStateOf("") }
    var showBeneficiarySelector by remember { mutableStateOf(false) }
    var showKitSelector by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var kitAvailable by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        beneficiaryViewModel.loadBeneficiaries()
        kitViewModel.loadKits()
    }

    LaunchedEffect(selectedKit) {
        if (selectedKit != null) {
            kitViewModel.checkKitAvailability(selectedKit!!.id) { available ->
                kitAvailable = available
            }
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agendar Entrega") },
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
            Card(
                onClick = { showBeneficiarySelector = true },
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
                            "Beneficiário *",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            selectedBeneficiary?.name ?: "Selecionar beneficiário",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedBeneficiary != null) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }

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
                            fontWeight = if (selectedKit != null) FontWeight.Bold else FontWeight.Normal
                        )
                        if (kitAvailable != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (kitAvailable!!)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    if (kitAvailable!!) "✓ Stock disponível" else "✗ Sem stock",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (kitAvailable!!)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }

            if (kitAvailable == false) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Este kit não tem stock disponível. Não será possível agendar a entrega.",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Data de Início
            Card(
                onClick = { showStartDatePicker = true },
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
                            "Data de Início *",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            startDate?.let { formatDate(it) } ?: "Selecionar data de início",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (startDate != null) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Icon(Icons.Default.CalendarToday, null)
                }
            }

            // Data de Fim
            Card(
                onClick = {
                    if (startDate != null) {
                        showEndDatePicker = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = if (startDate == null) {
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                } else {
                    CardDefaults.cardColors()
                }
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
                            "Data de Fim *",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (startDate == null) {
                                "Selecione primeiro a data de início"
                            } else {
                                endDate?.let { formatDate(it) } ?: "Selecionar data de fim"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (endDate != null) FontWeight.Bold else FontWeight.Normal,
                            color = if (startDate == null) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    Icon(
                        Icons.Default.CalendarToday,
                        null,
                        tint = if (startDate == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3,
                leadingIcon = { Icon(Icons.Default.Description, null) }
            )

            if (uiState.error != null) {
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

            Button(
                onClick = {
                    val delivery = Delivery(
                        id = "",
                        beneficiaryId = selectedBeneficiary!!.id,
                        beneficiaryName = selectedBeneficiary!!.name,
                        kitId = selectedKit!!.id,
                        kitName = selectedKit!!.name,
                        scheduledDate = startDate!!,
                        status = DeliveryStatus.SCHEDULED,
                        notes = notes
                    )
                    viewModel.createDelivery(delivery, userId)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedBeneficiary != null &&
                        selectedKit != null &&
                        startDate != null &&
                        endDate != null &&
                        kitAvailable == true &&
                        !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Agendar Entrega")
                }
            }

            Text(
                "* Campos obrigatórios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showBeneficiarySelector) {
        BeneficiarySelectorDialog(
            beneficiaries = beneficiariesState.filteredBeneficiaries.filter { it.isActive },
            onBeneficiarySelected = {
                selectedBeneficiary = it
                showBeneficiarySelector = false
            },
            onDismiss = { showBeneficiarySelector = false }
        )
    }

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

    if (showStartDatePicker) {
        DatePickerDialog(
            title = "Data de Início",
            initialDate = startDate,
            minDate = getTodayAtMidnight(),
            onDateSelected = { date ->
                startDate = date
                // Se a data de fim for anterior à nova data de início, limpa
                if (endDate != null && endDate!!.before(date)) {
                    endDate = null
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker && startDate != null) {
        DatePickerDialog(
            title = "Data de Fim",
            initialDate = endDate,
            minDate = getNextDay(startDate!!),
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
private fun BeneficiarySelectorDialog(
    beneficiaries: List<Beneficiary>,
    onBeneficiarySelected: (Beneficiary) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = beneficiaries.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.studentNumber.contains(searchQuery, ignoreCase = true)
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
                    "Selecionar Beneficiário",
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
                        Text("Nenhum beneficiário encontrado")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered) { beneficiary ->
                            Card(
                                onClick = { onBeneficiarySelected(beneficiary) }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        beneficiary.name,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Nº ${beneficiary.studentNumber} • ${beneficiary.course}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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
                    "Selecionar Kit",
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
                        Text("Nenhum kit encontrado")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered) { kit ->
                            Card(
                                onClick = { onKitSelected(kit) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(kit.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${kit.items.size} produtos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    title: String,
    initialDate: Date?,
    minDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        if (initialDate != null && initialDate.after(minDate)) {
            time = initialDate
        } else {
            time = minDate
        }
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
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedCalendar = Calendar.getInstance().apply {
                            timeInMillis = millis
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateSelected(selectedCalendar.time)
                    }
                },
                enabled = datePickerState.selectedDateMillis != null
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
            title = {
                Text(
                    title,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            },
            headline = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Date(millis)
                    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("pt", "PT"))
                    Text(
                        sdf.format(date),
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
            }
        )
    }
}

// Funções auxiliares
private fun getTodayAtMidnight(): Date {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}

private fun getNextDay(date: Date): Date {
    return Calendar.getInstance().apply {
        time = date
        add(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "PT"))
    return sdf.format(date)
}