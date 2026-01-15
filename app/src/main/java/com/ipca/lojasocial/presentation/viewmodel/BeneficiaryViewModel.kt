package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.Beneficiary
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.usecase.beneficiary.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados da UI de Beneficiários
 */
data class BeneficiariesUiState(
    val beneficiaries: List<Beneficiary> = emptyList(),
    val filteredBeneficiaries: List<Beneficiary> = emptyList(),
    val selectedBeneficiary: Beneficiary? = null,
    val statistics: BeneficiaryStatistics? = null,
    val searchQuery: String = "",
    val selectedCourse: String? = null,
    val showOnlyActive: Boolean = true,
    val isLoading: Boolean = false,
    val isLoadingDetails: Boolean = false,  // ✅ ADICIONADO
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para gestão de beneficiários
 */
@HiltViewModel
class BeneficiaryViewModel @Inject constructor(
    private val getAllBeneficiariesUseCase: GetAllBeneficiariesUseCase,
    private val getActiveBeneficiariesUseCase: GetActiveBeneficiariesUseCase,
    private val getBeneficiaryByIdUseCase: GetBeneficiaryByIdUseCase,
    private val getBeneficiaryByUserIdUseCase: GetBeneficiaryByUserIdUseCase,
    private val getBeneficiaryByStudentNumberUseCase: GetBeneficiaryByStudentNumberUseCase,
    private val createBeneficiaryUseCase: CreateBeneficiaryUseCase,
    private val updateBeneficiaryUseCase: UpdateBeneficiaryUseCase,
    private val deleteBeneficiaryUseCase: DeleteBeneficiaryUseCase,
    private val searchBeneficiariesUseCase: SearchBeneficiariesUseCase,
    private val getBeneficiariesByCourseUseCase: GetBeneficiariesByCourseUseCase,
    private val validateBeneficiaryActiveUseCase: ValidateBeneficiaryActiveUseCase,
    private val toggleBeneficiaryStatusUseCase: ToggleBeneficiaryStatusUseCase,
    private val getBeneficiaryStatisticsUseCase: GetBeneficiaryStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiariesUiState())
    val uiState: StateFlow<BeneficiariesUiState> = _uiState.asStateFlow()

    init {
        loadBeneficiaries()
        loadStatistics()
    }

    fun loadBeneficiaries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val flow = if (_uiState.value.showOnlyActive) {
                getActiveBeneficiariesUseCase()
            } else {
                getAllBeneficiariesUseCase()
            }

            flow.collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                beneficiaries = result.data,
                                filteredBeneficiaries = result.data,
                                isLoading = false
                            )
                        }
                        applyFilters()
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Erro ao carregar beneficiários"
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

    fun loadStatistics() {
        viewModelScope.launch {
            when (val result = getBeneficiaryStatisticsUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(statistics = result.data) }
                }
                is Result.Error -> {}
                is Result.Loading -> {}
            }
        }
    }

    fun searchBeneficiaries(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            searchBeneficiariesUseCase(query).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                filteredBeneficiaries = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
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

    fun filterByCourse(course: String?) {
        _uiState.update { it.copy(selectedCourse = course) }
        applyFilters()
    }

    fun toggleShowOnlyActive() {
        _uiState.update { it.copy(showOnlyActive = !it.showOnlyActive) }
        loadBeneficiaries()
    }

    private fun applyFilters() {
        val course = _uiState.value.selectedCourse
        val query = _uiState.value.searchQuery

        if (course != null) {
            viewModelScope.launch {
                getBeneficiariesByCourseUseCase(course).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(filteredBeneficiaries = result.data)
                            }
                        }
                        is Result.Error -> {
                            _uiState.update { it.copy(error = result.message) }
                        }
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            }
        } else if (query.isNotBlank()) {
            searchBeneficiaries(query)
        } else {
            _uiState.update {
                it.copy(filteredBeneficiaries = it.beneficiaries)
            }
        }
    }

    fun selectBeneficiary(beneficiary: Beneficiary) {
        _uiState.update { it.copy(selectedBeneficiary = beneficiary) }
    }

    // ✅ CORRIGIDO: Usa isLoadingDetails
    fun loadBeneficiaryById(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetails = true, error = null, selectedBeneficiary = null) }

            when (val result = getBeneficiaryByIdUseCase(id)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedBeneficiary = result.data,
                            isLoadingDetails = false
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingDetails = false,
                            error = result.message ?: "Beneficiário não encontrado"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoadingDetails = true) }
                }
            }
        }
    }

    fun createBeneficiary(beneficiary: Beneficiary, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val beneficiaryWithUser = beneficiary.copy(registeredBy = userId)

            when (val result = createBeneficiaryUseCase(beneficiaryWithUser)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Beneficiário criado com sucesso",
                            selectedBeneficiary = null
                        )
                    }
                    loadBeneficiaries()
                    loadStatistics()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao criar beneficiário"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun updateBeneficiary(beneficiary: Beneficiary) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (updateBeneficiaryUseCase(beneficiary)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Beneficiário atualizado com sucesso",
                            selectedBeneficiary = null
                        )
                    }
                    loadBeneficiaries()
                    loadStatistics()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao atualizar beneficiário"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun deleteBeneficiary(beneficiaryId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (deleteBeneficiaryUseCase(beneficiaryId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Beneficiário removido com sucesso",
                            selectedBeneficiary = null
                        )
                    }
                    loadBeneficiaries()
                    loadStatistics()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao remover beneficiário"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun toggleBeneficiaryStatus(beneficiaryId: String, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (toggleBeneficiaryStatusUseCase(beneficiaryId, isActive)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = if (isActive) {
                                "Beneficiário ativado"
                            } else {
                                "Beneficiário desativado"
                            }
                        )
                    }
                    loadBeneficiaries()
                    loadStatistics()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao alterar status"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun findByStudentNumber(studentNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getBeneficiaryByStudentNumberUseCase(studentNumber)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedBeneficiary = result.data,
                            isLoading = false
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Beneficiário não encontrado"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun validateBeneficiaryActive(
        beneficiaryId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = validateBeneficiaryActiveUseCase(beneficiaryId)) {
                is Result.Success -> {
                    onResult(result.data)
                }

                is Result.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                    onResult(false)
                }

                is Result.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                error = null,
                successMessage = null
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(selectedBeneficiary = null)
        }
    }
}