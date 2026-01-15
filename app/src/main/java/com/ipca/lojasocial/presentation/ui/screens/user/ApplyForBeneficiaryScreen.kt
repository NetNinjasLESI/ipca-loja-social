package com.ipca.lojasocial.presentation.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.presentation.viewmodel.ApplicationViewModel
import com.ipca.lojasocial.presentation.viewmodel.LoginViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyForBeneficiaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApplicationViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel() // ✅ ADICIONADO
) {
    val uiState by viewModel.uiState.collectAsState()
    val loginUiState by loginViewModel.uiState.collectAsState() // ✅ ADICIONADO
    val currentUser = FirebaseAuth.getInstance().currentUser

    // ✅ IGUAL AO DASHBOARD - Buscar nome da BD
    val currentUserName = loginUiState.currentUser?.name
        ?: currentUser?.email?.substringBefore("@")
        ?: "Utilizador"

    // Form fields
    var studentNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nif by remember { mutableStateOf("") }
    var academicDegree by remember { mutableStateOf(AcademicDegree.BACHELOR) }
    var course by remember { mutableStateOf("") }
    var academicYear by remember { mutableStateOf(1) }
    var availableCourses by remember { mutableStateOf(AcademicCourses.BACHELOR_COURSES) }
    var address by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var familySize by remember { mutableStateOf("1") }
    var monthlyIncome by remember { mutableStateOf("") }
    var hasSpecialNeeds by remember { mutableStateOf(false) }
    var specialNeedsDescription by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }

    var showDegreeMenu by remember { mutableStateOf(false) }
    var showCourseMenu by remember { mutableStateOf(false) }
    var showYearMenu by remember { mutableStateOf(false) }

    LaunchedEffect(academicDegree) {
        availableCourses = AcademicCourses.getCoursesByDegree(academicDegree)
        course = ""
        academicYear = 1
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidatura a Beneficiário") },
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
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "A tua candidatura será revista pela equipa da Loja Social.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Personal Information Section
            SectionHeader("Informações Pessoais")

            OutlinedTextField(
                value = studentNumber,
                onValueChange = { studentNumber = it },
                label = { Text("Número de Estudante *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Badge, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefone *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = nif,
                onValueChange = { nif = it },
                label = { Text("NIF *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Academic Information Section
            SectionHeader("Informações Académicas")

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

            // Address Information Section
            SectionHeader("Morada")

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Morada *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Home, null) },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = { Text("Código Postal *") },
                    modifier = Modifier.weight(0.6f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Cidade *") },
                    modifier = Modifier.weight(0.4f),
                    singleLine = true
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Family Information Section
            SectionHeader("Informações Familiares")

            OutlinedTextField(
                value = familySize,
                onValueChange = {
                    if (it.isEmpty() || it.toIntOrNull() != null && it.toInt() >= 1) {
                        familySize = it
                    }
                },
                label = { Text("Agregado Familiar *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.People, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                supportingText = { Text("Número de pessoas no agregado") }
            )

            OutlinedTextField(
                value = monthlyIncome,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d+\\.?\\d*$"))) {
                        monthlyIncome = newValue
                    }
                },
                label = { Text("Rendimento Mensal (€)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Euro, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                supportingText = { Text("Rendimento mensal do agregado") }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Special Needs Section
            SectionHeader("Necessidades Especiais")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Tem necessidades especiais?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = hasSpecialNeeds,
                    onCheckedChange = { hasSpecialNeeds = it }
                )
            }

            if (hasSpecialNeeds) {
                OutlinedTextField(
                    value = specialNeedsDescription,
                    onValueChange = { specialNeedsDescription = it },
                    label = { Text("Descrever necessidades especiais") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Observations Section
            SectionHeader("Observações (Opcional)")

            OutlinedTextField(
                value = observations,
                onValueChange = { observations = it },
                label = { Text("Informações adicionais") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                supportingText = { Text("Qualquer informação adicional relevante") }
            )

            // Error message
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

            // Submit Button
            Button(
                onClick = {
                    val application = BeneficiaryApplication(
                        userId = currentUser?.uid ?: "",
                        userName = currentUserName, // ✅ IGUAL AO DASHBOARD
                        userEmail = currentUser?.email ?: "",
                        studentNumber = studentNumber,
                        phone = phone,
                        nif = nif,
                        academicDegree = academicDegree,
                        course = course,
                        academicYear = academicYear,
                        address = address,
                        zipCode = zipCode,
                        city = city,
                        familySize = familySize.toIntOrNull() ?: 1,
                        monthlyIncome = monthlyIncome.toDoubleOrNull() ?: 0.0,
                        hasSpecialNeeds = hasSpecialNeeds,
                        specialNeedsDescription = if (hasSpecialNeeds) specialNeedsDescription else "",
                        observations = observations,
                        appliedAt = Date()
                    )
                    viewModel.createApplication(application)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading &&
                        studentNumber.isNotBlank() &&
                        phone.isNotBlank() &&
                        nif.isNotBlank() &&
                        course.isNotBlank() &&
                        address.isNotBlank() &&
                        zipCode.isNotBlank() &&
                        city.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submeter Candidatura")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}