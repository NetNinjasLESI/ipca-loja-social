package com.ipca.lojasocial.presentation.ui.screens.auth.login

import android.R.attr.text
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ipca.lojasocial.R
import com.ipca.lojasocial.presentation.ui.components.*
import com.ipca.lojasocial.presentation.viewmodel.LoginViewModel

/**
 * Login Screen
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToPublicCampaigns: () -> Unit,
    onNavigateToUserDashboard: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Navigate based on user role when login is successful
    LaunchedEffect(uiState.isSuccess, uiState.currentUser) {
        android.util.Log.d("LoginScreen", "=== LaunchedEffect triggered ===")
        android.util.Log.d("LoginScreen", "isSuccess: ${uiState.isSuccess}")
        android.util.Log.d("LoginScreen", "currentUser: ${uiState.currentUser}")

        if (uiState.isSuccess && uiState.currentUser != null) {
            val user = uiState.currentUser
            val role = user?.role

            android.util.Log.d("LoginScreen", "User: ${user?.email}, Role: $role")

            when (role) {
                com.ipca.lojasocial.domain.model.UserRole.USER -> {
                    android.util.Log.d("LoginScreen", ">>> Calling onNavigateToUserDashboard()")
                    onNavigateToUserDashboard()
                }
                com.ipca.lojasocial.domain.model.UserRole.BENEFICIARY -> {
                    android.util.Log.d("LoginScreen", ">>> Calling onNavigateToHome()")
                    onNavigateToHome()
                }
                com.ipca.lojasocial.domain.model.UserRole.COLLABORATOR,
                com.ipca.lojasocial.domain.model.UserRole.ADMINISTRATOR -> {
                    android.util.Log.d("LoginScreen", ">>> Calling onNavigateToDashboard()")
                    onNavigateToDashboard()
                }

                else -> {}
            }
        } else {
            android.util.Log.e("LoginScreen", "NOT navigating - isSuccess: ${uiState.isSuccess}, user null: ${uiState.currentUser == null}")
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                 painter = painterResource(id = R.drawable.ipca_logo),
                 contentDescription = "IPCA Logo",
                 modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "LOJA SOCIAL",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Serviços de Ação Social do IPCA",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email field
            IPCATextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email Institucional",
                placeholder = "exemplo@ipca.pt",
                leadingIcon = Icons.Default.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                isError = uiState.error != null,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            IPCAPasswordField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Palavra-passe",
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.signIn()
                    }
                ),
                isError = uiState.error != null,
                errorMessage = uiState.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login button
            IPCAButton(
                text = "Entrar",
                onClick = { viewModel.signIn() },
                loading = uiState.isLoading,
                enabled = uiState.email.isNotBlank() && uiState.password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot password
            TextButton(
                onClick = { /* TODO: Navigate to password reset */ },
                enabled = !uiState.isLoading
            ) {
                Text("Esqueci-me da palavra-passe")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register section
            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ainda não tens conta?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            IPCAOutlinedButton(
                text = "Criar Conta",
                onClick = onNavigateToRegister,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info for public access
            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Informações públicas disponíveis sem login",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            IPCAOutlinedButton(
                text = "Ver Campanhas",
                onClick = onNavigateToPublicCampaigns,
                enabled = !uiState.isLoading
            )
        }
    }
}
