package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.usecase.delivery.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeliveriesUiState(
    val deliveries: List<Delivery> = emptyList(),
    val filteredDeliveries: List<Delivery> = emptyList(),
    val selectedDelivery: Delivery? = null,
    val statistics: DeliveryStatistics? = null,
    val upcomingDeliveries: List<Delivery> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: DeliveryStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class DeliveryViewModel @Inject constructor(
    private val getAllDeliveriesUseCase: GetAllDeliveriesUseCase,
    private val getDeliveryByIdUseCase: GetDeliveryByIdUseCase,
    private val createDeliveryUseCase: CreateDeliveryUseCase,
    private val confirmDeliveryUseCase: ConfirmDeliveryUseCase,
    private val cancelDeliveryUseCase: CancelDeliveryUseCase,
    private val getDeliveriesByBeneficiaryUseCase: GetDeliveriesByBeneficiaryUseCase,
    private val getDeliveriesByStatusUseCase: GetDeliveriesByStatusUseCase,
    private val searchDeliveriesUseCase: SearchDeliveriesUseCase,
    private val getDeliveryStatisticsUseCase: GetDeliveryStatisticsUseCase,
    private val validateDeliveryConfirmationUseCase: ValidateDeliveryConfirmationUseCase,
    private val getUpcomingDeliveriesUseCase: GetUpcomingDeliveriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveriesUiState())
    val uiState: StateFlow<DeliveriesUiState> = _uiState.asStateFlow()

    init {
        loadDeliveries()
        loadStatistics()
        loadUpcomingDeliveries()
    }

    fun loadDeliveries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getAllDeliveriesUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                deliveries = result.data,
                                filteredDeliveries = result.data,
                                isLoading = false
                            )
                        }
                        applyFilters()
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Erro ao carregar entregas"
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
            when (val result = getDeliveryStatisticsUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(statistics = result.data) }
                }
                else -> {}
            }
        }
    }

    fun loadUpcomingDeliveries() {
        viewModelScope.launch {
            when (val result = getUpcomingDeliveriesUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(upcomingDeliveries = result.data) }
                }
                else -> {}
            }
        }
    }

    fun searchDeliveries(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            searchDeliveriesUseCase(query).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(filteredDeliveries = result.data, isLoading = false)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun filterByStatus(status: DeliveryStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
        applyFilters()
    }

    private fun applyFilters() {
        val status = _uiState.value.selectedStatus
        val query = _uiState.value.searchQuery

        if (status != null) {
            viewModelScope.launch {
                getDeliveriesByStatusUseCase(status).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update { it.copy(filteredDeliveries = result.data) }
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
            searchDeliveries(query)
        } else {
            _uiState.update { it.copy(filteredDeliveries = it.deliveries) }
        }
    }

    fun selectDelivery(delivery: Delivery) {
        _uiState.update { it.copy(selectedDelivery = delivery) }
    }

    fun loadDeliveryById(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getDeliveryByIdUseCase(id)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(selectedDelivery = result.data, isLoading = false)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message ?: "Entrega não encontrada")
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun createDelivery(delivery: Delivery, userId: String) {

        if (userId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Utilizador não autenticado"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val deliveryWithUser = delivery.copy(createdBy = userId)

            when (val result = createDeliveryUseCase(deliveryWithUser)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Entrega agendada com sucesso",
                            selectedDelivery = null
                        )
                    }
                    loadStatistics()
                    loadUpcomingDeliveries()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao criar entrega"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun confirmDelivery(deliveryId: String, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (confirmDeliveryUseCase(deliveryId, userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Entrega confirmada com sucesso. Stock atualizado automaticamente.",
                            selectedDelivery = null
                        )
                    }
                    loadStatistics()
                    loadUpcomingDeliveries()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Erro ao confirmar entrega")
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun cancelDelivery(deliveryId: String, userId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (cancelDeliveryUseCase(deliveryId, userId, reason)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Entrega cancelada",
                            selectedDelivery = null
                        )
                    }
                    loadStatistics()
                    loadUpcomingDeliveries()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Erro ao cancelar entrega")
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun loadDeliveriesByBeneficiary(beneficiaryId: String) {
        viewModelScope.launch {
            getDeliveriesByBeneficiaryUseCase(beneficiaryId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(filteredDeliveries = result.data) }
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
    }

    fun validateDeliveryConfirmation(deliveryId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            when (val result = validateDeliveryConfirmationUseCase(deliveryId)) {
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
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedDelivery = null) }
    }
}