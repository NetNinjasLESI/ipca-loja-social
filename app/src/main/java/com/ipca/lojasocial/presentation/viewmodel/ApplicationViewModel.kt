package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.ApplicationStatus
import com.ipca.lojasocial.domain.model.BeneficiaryApplication
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.usecase.application.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados da UI de Candidaturas
 */
data class ApplicationsUiState(
    val applications: List<BeneficiaryApplication> = emptyList(),
    val myApplication: BeneficiaryApplication? = null,
    val selectedApplication: BeneficiaryApplication? = null,
    val hasExistingApplication: Boolean = false,
    val pendingCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ✅ ViewModel CORRIGIDO com filtro duplo (UseCase + Local)
 */
@HiltViewModel
class ApplicationViewModel @Inject constructor(
    private val getAllApplicationsUseCase: GetAllApplicationsUseCase,
    private val getApplicationByIdUseCase: GetApplicationByIdUseCase,
    private val getApplicationByUserIdUseCase: GetApplicationByUserIdUseCase,
    private val getApplicationsByStatusUseCase: GetApplicationsByStatusUseCase,
    private val createApplicationUseCase: CreateApplicationUseCase,
    private val approveApplicationUseCase: ApproveApplicationUseCase,
    private val rejectApplicationUseCase: RejectApplicationUseCase,
    private val hasExistingApplicationUseCase: HasExistingApplicationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationsUiState())
    val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

    /**
     * Carregar todas as candidaturas
     */
    fun loadAllApplications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getAllApplicationsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        android.util.Log.d("AppViewModel", "LoadAll: Total = ${result.data.size}")

                        val pending = result.data.count {
                            it.status == ApplicationStatus.PENDING
                        }

                        _uiState.update {
                            it.copy(
                                applications = result.data,
                                pendingCount = pending,
                                isLoading = false
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Erro ao carregar candidaturas"
                            )
                        }
                    }

                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    /**
     * Carregar candidaturas pendentes
     */
    fun loadPendingApplications() {
        loadApplicationsByStatus(ApplicationStatus.PENDING)
    }

    /**
     * ✅ CORRIGIDO: Carregar candidaturas por status com FILTRO DUPLO
     * 1. Primeiro tenta usar o UseCase específico
     * 2. Se retornar dados errados, filtra localmente
     */
    fun loadApplicationsByStatus(status: ApplicationStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            android.util.Log.d("AppViewModel", "=== CARREGANDO STATUS: $status ===")

            getApplicationsByStatusUseCase(status).collect { result ->
                when (result) {
                    is Result.Success -> {
                        android.util.Log.d("AppViewModel", "UseCase retornou: ${result.data.size} candidaturas")

                        // ✅ LOG detalhado do que veio do UseCase
                        result.data.forEachIndexed { index, app ->
                            android.util.Log.d("AppViewModel", "  [$index] ${app.userName}: ${app.status}")
                        }

                        // ✅ FILTRO DUPLO: Garantir que só mostramos o status correto
                        val filteredApplications = result.data.filter { app ->
                            val matches = app.status == status
                            if (!matches) {
                                android.util.Log.w(
                                    "AppViewModel",
                                    "⚠️ FILTRADO: ${app.userName} tem status ${app.status} mas queríamos $status"
                                )
                            }
                            matches
                        }

                        android.util.Log.d("AppViewModel", "Após filtro local: ${filteredApplications.size} candidaturas")

                        // Atualizar pendingCount
                        val pendingCount = if (status == ApplicationStatus.PENDING) {
                            filteredApplications.size
                        } else {
                            // Manter o valor atual se não estamos carregando PENDING
                            _uiState.value.pendingCount
                        }

                        _uiState.update {
                            it.copy(
                                applications = filteredApplications,
                                pendingCount = pendingCount,
                                isLoading = false
                            )
                        }

                        android.util.Log.d("AppViewModel", "Estado atualizado com ${filteredApplications.size} candidaturas")
                    }

                    is Result.Error -> {
                        android.util.Log.e("AppViewModel", "Erro: ${result.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }

                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    /**
     * Carregar minha candidatura (como USER)
     */
    fun loadMyApplication(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getApplicationByUserIdUseCase(userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            myApplication = result.data,
                            hasExistingApplication = true,
                            isLoading = false
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            myApplication = null,
                            hasExistingApplication = false,
                            isLoading = false
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * Carregar candidatura por ID
     */
    fun loadApplicationById(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getApplicationByIdUseCase(id)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedApplication = result.data,
                            isLoading = false
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Candidatura não encontrada"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * Criar nova candidatura
     */
    fun createApplication(application: BeneficiaryApplication) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = createApplicationUseCase(application)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Candidatura submetida com sucesso!",
                            myApplication = result.data,
                            hasExistingApplication = true
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao submeter candidatura"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * Aprovar candidatura
     */
    fun approveApplication(
        applicationId: String,
        reviewedBy: String,
        reviewedByName: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (approveApplicationUseCase(applicationId, reviewedBy, reviewedByName)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Candidatura aprovada! Utilizador é agora beneficiário.",
                            selectedApplication = null
                        )
                    }
                    loadPendingApplications()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao aprovar candidatura"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * Rejeitar candidatura
     */
    fun rejectApplication(
        applicationId: String,
        reviewedBy: String,
        reviewedByName: String,
        reason: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (rejectApplicationUseCase(applicationId, reviewedBy, reviewedByName, reason)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Candidatura rejeitada",
                            selectedApplication = null
                        )
                    }
                    loadPendingApplications()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao rejeitar candidatura"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * Verificar se utilizador já tem candidatura
     */
    fun checkExistingApplication(userId: String) {
        viewModelScope.launch {
            when (val result = hasExistingApplicationUseCase(userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(hasExistingApplication = result.data)
                    }
                    if (result.data) {
                        loadMyApplication(userId)
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(hasExistingApplication = false)
                    }
                }

                is Result.Loading -> {}
            }
        }
    }

    /**
     * Limpar mensagens
     */
    fun clearMessages() {
        _uiState.update {
            it.copy(error = null, successMessage = null)
        }
    }

    /**
     * Limpar seleção
     */
    fun clearSelection() {
        _uiState.update {
            it.copy(selectedApplication = null)
        }
    }
}
