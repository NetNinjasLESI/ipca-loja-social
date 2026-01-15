package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.data.repository.KitItemAvailability
import com.ipca.lojasocial.domain.model.Kit
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.usecase.kit.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados da UI de Kits
 */
data class KitsUiState(
    val kits: List<Kit> = emptyList(),
    val filteredKits: List<Kit> = emptyList(),
    val selectedKit: Kit? = null,
    val kitAvailability: Map<String, KitItemAvailability>? = null,
    val statistics: KitStatistics? = null,
    val searchQuery: String = "",
    val showOnlyActive: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para gestão de kits
 */
@HiltViewModel
class KitViewModel @Inject constructor(
    private val getAllKitsUseCase: GetAllKitsUseCase,
    private val getActiveKitsUseCase: GetActiveKitsUseCase,
    private val getKitByIdUseCase: GetKitByIdUseCase,
    private val createKitUseCase: CreateKitUseCase,
    private val updateKitUseCase: UpdateKitUseCase,
    private val deleteKitUseCase: DeleteKitUseCase,
    private val searchKitsUseCase: SearchKitsUseCase,
    private val checkKitAvailabilityUseCase: CheckKitAvailabilityUseCase,
    private val getKitAvailabilityDetailsUseCase: GetKitAvailabilityDetailsUseCase,
    private val validateKitActiveUseCase: ValidateKitActiveUseCase,
    private val toggleKitStatusUseCase: ToggleKitStatusUseCase,
    private val getKitStatisticsUseCase: GetKitStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(KitsUiState())
    val uiState: StateFlow<KitsUiState> = _uiState.asStateFlow()

    init {
        loadKits()
        loadStatistics()
    }

    /**
     * Carregar todos os kits
     */
    fun loadKits() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val flow = if (_uiState.value.showOnlyActive) {
                getActiveKitsUseCase()
            } else {
                getAllKitsUseCase()
            }

            flow.collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                kits = result.data,
                                filteredKits = result.data,
                                isLoading = false
                            )
                        }
                        applyFilters()
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Erro ao carregar kits"
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
     * Carregar estatísticas
     */
    fun loadStatistics() {
        viewModelScope.launch {
            when (val result = getKitStatisticsUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(statistics = result.data) }
                }

                is Result.Error -> {
                    // Log error but don't show to user
                }

                is Result.Loading -> {
                    // No action needed
                }
            }
        }
    }

    /**
     * Pesquisar kits
     */
    fun searchKits(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            searchKitsUseCase(query).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                filteredKits = result.data,
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

    /**
     * Toggle mostrar apenas ativos
     */
    fun toggleShowOnlyActive() {
        _uiState.update { it.copy(showOnlyActive = !it.showOnlyActive) }
        loadKits()
    }

    /**
     * Aplicar filtros
     */
    private fun applyFilters() {
        val query = _uiState.value.searchQuery

        if (query.isNotBlank()) {
            searchKits(query)
        } else {
            _uiState.update {
                it.copy(filteredKits = it.kits)
            }
        }
    }

    /**
     * Selecionar kit
     */
    fun selectKit(kit: Kit) {
        _uiState.update { it.copy(selectedKit = kit) }
        loadKitAvailability(kit.id)
    }

    /**
     * Carregar kit por ID
     */
    fun loadKitById(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getKitByIdUseCase(id)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedKit = result.data,
                            isLoading = false
                        )
                    }
                    loadKitAvailability(id)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Kit não encontrado"
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
     * Carregar disponibilidade do kit
     */
    private fun loadKitAvailability(kitId: String) {
        viewModelScope.launch {
            when (val result = getKitAvailabilityDetailsUseCase(kitId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(kitAvailability = result.data) }
                }

                is Result.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }

                is Result.Loading -> {
                    // No action needed
                }
            }
        }
    }

    /**
     * Criar novo kit
     */
    fun createKit(kit: Kit, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val kitWithUser = kit.copy(createdBy = userId)

            when (val result = createKitUseCase(kitWithUser)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Kit criado com sucesso",
                            selectedKit = null
                        )
                    }
                    loadStatistics()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao criar kit"
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
     * Atualizar kit
     */
    fun updateKit(kit: Kit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (updateKitUseCase(kit)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Kit atualizado com sucesso",
                            selectedKit = null
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao atualizar kit"
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
     * Deletar kit
     */
    fun deleteKit(kitId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (deleteKitUseCase(kitId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Kit removido com sucesso",
                            selectedKit = null
                        )
                    }
                    loadStatistics()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao remover kit"
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
     * Toggle status do kit
     */
    fun toggleKitStatus(kitId: String, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (toggleKitStatusUseCase(kitId, isActive)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = if (isActive) {
                                "Kit ativado"
                            } else {
                                "Kit desativado"
                            }
                        )
                    }
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

    /**
     * Verificar se kit está disponível
     */
    fun checkKitAvailability(
        kitId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = checkKitAvailabilityUseCase(kitId)) {
                is Result.Success -> {
                    onResult(result.data)
                }

                is Result.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                    onResult(false)
                }

                is Result.Loading -> {
                    // No action needed
                }
            }
        }
    }

    /**
     * Validar se kit está ativo
     */
    fun validateKitActive(
        kitId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = validateKitActiveUseCase(kitId)) {
                is Result.Success -> {
                    onResult(result.data)
                }

                is Result.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                    onResult(false)
                }

                is Result.Loading -> {
                    // No action needed
                }
            }
        }
    }

    /**
     * Limpar mensagens
     */
    fun clearMessages() {
        _uiState.update {
            it.copy(
                error = null,
                successMessage = null
            )
        }
    }

    /**
     * Limpar seleção de kit
     */
    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedKit = null,
                kitAvailability = null
            )
        }
    }
}
