package com.ipca.lojasocial.presentation.ui.screens.beneficiary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.presentation.ui.components.IPCAButton
import com.ipca.lojasocial.presentation.ui.components.IPCATextField
import com.ipca.lojasocial.presentation.viewmodel.BeneficiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBeneficiaryScreen(
    beneficiaryId: String? = null,
    userId: String,
    viewModel: BeneficiaryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = beneficiaryId != null

    // Estados do formulário - Dados Pessoais
    var studentNumber by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Dados Académicos
    var academicDegree by remember { mutableStateOf(AcademicDegree.BACHELOR) }
    var course by remember { mutableStateOf("") }
    var academicYear by remember { mutableStateOf(1) }
    var availableCourses by remember { mutableStateOf(AcademicCourses.BACHELOR_COURSES) }

    // Morada
    var address by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    // Dados Sociais
    var nif by remember { mutableStateOf("") }
    var familySize by remember { mutableStateOf("1") }
    var monthlyIncome by remember { mutableStateOf("") }

    // Necessidades Especiais
    var hasSpecialNeeds by remember { mutableStateOf(false) }
    var specialNeedsDescription by remember { mutableStateOf("") }

    // Outros
    var observations by remember { mutableStateOf("") }

    // Menus
    var showDegreeMenu by remember { mutableStateOf(false) }
    var showCourseMenu by remember { mutableStateOf(false) }
    var showYearMenu by remember { mutableStateOf(false) }
    var isFormInitialized by remember { mutableStateOf(false) }

    // Atualizar cursos quando grau mudar
    LaunchedEffect(academicDegree) {
        availableCourses = AcademicCourses.getCoursesByDegree(academicDegree)
        if (!isFormInitialized) {
            course = "" // Limpar curso quando mudar grau (exceto no carregamento inicial)
        }
    }

    // Carregar beneficiário se for edição
    LaunchedEffect(beneficiaryId) {
        if (beneficiaryId != null) {
            viewModel.loadBeneficiaryById(beneficiaryId)
        }
    }

    // Preencher campos quando beneficiário carregar
    LaunchedEffect(uiState.selectedBeneficiary) {
        if (isEditMode && uiState.selectedBeneficiary != null && !isFormInitialized) {
            uiState.selectedBeneficiary?.let { ben ->
                studentNumber = ben.studentNumber
                name = ben.name
                email = ben.email
                phone = ben.phone
                academicDegree = ben.academicDegree
                course = ben.course
                academicYear = ben.academicYear
                address = ben.address
                zipCode = ben.zipCode
                city = ben.city
                nif = ben.nif ?: ""
                familySize = ben.familySize.toString()
                monthlyIncome = if (ben.monthlyIncome > 0) {
                    ben.monthlyIncome.toString()
                } else ""
                hasSpecialNeeds = ben.hasSpecialNeeds
                specialNeedsDescription = ben.specialNeedsDescription ?: ""
                observations = ben.observations
                isFormInitialized = true
            }
        }
    }

    // Navegar de volta em caso de sucesso
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Editar Beneficiário" else "Novo Beneficiário")
                },
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
            // SEÇÃO: Dados Pessoais
            SectionHeader("Dados Pessoais")

            IPCATextField(
                value = studentNumber,
                onValueChange = { studentNumber = it },
                label = "Número de Estudante *",
                placeholder = "Ex: 20240001",
                leadingIcon = Icons.Default.Badge,
                enabled = !isEditMode
            )

            IPCATextField(
                value = name,
                onValueChange = { name = it },
                label = "Nome Completo *",
                placeholder = "Ex: João Silva",
                leadingIcon = Icons.Default.Person
            )

            IPCATextField(
                value = email,
                onValueChange = { email = it },
                label = "Email *",
                placeholder = "joao.silva@ipca.pt",
                leadingIcon = Icons.Default.Email
            )

            IPCATextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Telefone *",
                placeholder = "+351 912 345 678",
                leadingIcon = Icons.Default.Phone
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // SEÇÃO: Dados Académicos
            SectionHeader("Dados Académicos")

            // Grau Académico
            ExposedDropdownMenuBox(
                expanded = showDegreeMenu,
                onExpandedChange = { showDegreeMenu = it }
            ) {
                OutlinedTextField(
                    value = academicDegree.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grau Académico *") },
                    leadingIcon = { Icon(Icons.Default.School, null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDegreeMenu)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = showDegreeMenu,
                    onDismissRequest = { showDegreeMenu = false }
                ) {
                    AcademicDegree.values().forEach { degree ->
                        DropdownMenuItem(
                            text = { Text(degree.displayName) },
                            onClick = {
                                academicDegree = degree
                                academicYear = 1
                                showDegreeMenu = false
                            }
                        )
                    }
                }
            }

            // Curso
            ExposedDropdownMenuBox(
                expanded = showCourseMenu,
                onExpandedChange = { showCourseMenu = it }
            ) {
                OutlinedTextField(
                    value = course,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Curso *") },
                    placeholder = { Text("Selecione o curso") },
                    leadingIcon = { Icon(Icons.Default.MenuBook, null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCourseMenu)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = showCourseMenu,
                    onDismissRequest = { showCourseMenu = false }
                ) {
                    availableCourses.forEach { courseName ->
                        DropdownMenuItem(
                            text = { Text(courseName) },
                            onClick = {
                                course = courseName
                                showCourseMenu = false
                            }
                        )
                    }
                }
            }

            // Ano Académico
            ExposedDropdownMenuBox(
                expanded = showYearMenu,
                onExpandedChange = { showYearMenu = it }
            ) {
                OutlinedTextField(
                    value = "$academicYear º Ano",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ano Académico *") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showYearMenu)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = showYearMenu,
                    onDismissRequest = { showYearMenu = false }
                ) {
                    AcademicDegree.getYears(academicDegree).forEach { year ->
                        DropdownMenuItem(
                            text = { Text("$year º Ano") },
                            onClick = {
                                academicYear = year
                                showYearMenu = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // SEÇÃO: Morada
            SectionHeader("Morada")

            IPCATextField(
                value = address,
                onValueChange = { address = it },
                label = "Endereço",
                placeholder = "Rua, nº, andar",
                leadingIcon = Icons.Default.Home
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IPCATextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = "Código Postal *",
                    placeholder = "4750-123",
                    leadingIcon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f)
                )

                IPCATextField(
                    value = city,
                    onValueChange = { city = it },
                    label = "Cidade",
                    placeholder = "Barcelos",
                    leadingIcon = Icons.Default.LocationCity,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // SEÇÃO: Dados Sociais
            SectionHeader("Dados Sociais")

            IPCATextField(
                value = nif,
                onValueChange = { nif = it },
                label = "NIF",
                placeholder = "123456789",
                leadingIcon = Icons.Default.CreditCard,
                isNumeric = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IPCATextField(
                    value = familySize,
                    onValueChange = { familySize = it },
                    label = "Agregado Familiar *",
                    placeholder = "1",
                    leadingIcon = Icons.Default.People,
                    isNumeric = true,
                    modifier = Modifier.weight(1f)
                )

                IPCATextField(
                    value = monthlyIncome,
                    onValueChange = { monthlyIncome = it },
                    label = "Rendimento Mensal",
                    placeholder = "500.00",
                    leadingIcon = Icons.Default.Euro,
                    isNumeric = true,
                    modifier = Modifier.weight(1f),
                    supportingText = "Per capita (€)"
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // SEÇÃO: Necessidades Especiais
            SectionHeader("Necessidades Especiais")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hasSpecialNeeds,
                    onCheckedChange = { hasSpecialNeeds = it }
                )
                Text("Tem necessidades especiais", style = MaterialTheme.typography.bodyLarge)
            }

            if (hasSpecialNeeds) {
                IPCATextField(
                    value = specialNeedsDescription,
                    onValueChange = { specialNeedsDescription = it },
                    label = "Descrição das Necessidades",
                    placeholder = "Descreva as necessidades especiais...",
                    leadingIcon = Icons.Default.AccessibleForward,
                    singleLine = false,
                    maxLines = 3
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // SEÇÃO: Observações
            SectionHeader("Observações")

            IPCATextField(
                value = observations,
                onValueChange = { observations = it },
                label = "Observações",
                placeholder = "Notas adicionais (opcional)",
                leadingIcon = Icons.Default.Notes,
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mensagem de erro
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "A sua candidatura será revista pela equipa da Loja Social. " +
                                "Será notificado por email sobre o resultado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Botões
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEditMode) {
                    IPCAButton(
                        onClick = { viewModel.deleteBeneficiary(beneficiaryId!!) },
                        text = "Eliminar",
                        modifier = Modifier.weight(1f),
                        loading = uiState.isLoading,
                        enabled = !uiState.isLoading,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                }

                IPCAButton(
                    onClick = {
                        val beneficiary = Beneficiary(
                            id = beneficiaryId ?: "",
                            userId = userId,
                            studentNumber = studentNumber,
                            name = name,
                            email = email,
                            phone = phone,
                            academicDegree = academicDegree,
                            course = course,
                            academicYear = academicYear,
                            address = address,
                            zipCode = zipCode,
                            city = city,
                            nif = nif.ifBlank { null },
                            familySize = familySize.toIntOrNull() ?: 1,
                            monthlyIncome = monthlyIncome.toDoubleOrNull() ?: 0.0,
                            hasSpecialNeeds = hasSpecialNeeds,
                            specialNeedsDescription = if (hasSpecialNeeds) {
                                specialNeedsDescription.ifBlank { null }
                            } else null,
                            observations = observations,
                            isActive = true,
                            registeredBy = userId
                        )

                        if (isEditMode) {
                            viewModel.updateBeneficiary(beneficiary)
                        } else {
                            viewModel.createBeneficiary(beneficiary, userId)
                        }
                    },
                    text = if (isEditMode) "Guardar" else "Submeter Candidatura",
                    modifier = Modifier.weight(1f),
                    loading = uiState.isLoading,
                    enabled = studentNumber.isNotBlank() &&
                            name.isNotBlank() &&
                            email.isNotBlank() &&
                            phone.isNotBlank() &&
                            course.isNotBlank() &&
                            zipCode.isNotBlank() &&
                            familySize.toIntOrNull() != null
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelection()
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}