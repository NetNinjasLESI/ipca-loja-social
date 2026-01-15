package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.Beneficiary
import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.usecase.beneficiary.GetBeneficiaryByUserIdUseCase
import com.ipca.lojasocial.domain.usecase.delivery.GetDeliveriesByBeneficiaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados da UI do Dashboard do Beneficiário
 */
data class BeneficiaryDashboardUiState(
    val beneficiary: Beneficiary? = null,
    val allDeliveries: List<Delivery> = emptyList(),
    val upcomingDelivery: Delivery? = null,
    val totalDeliveries: Int = 0,
    val confirmedDeliveries: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para o Dashboard do Beneficiário
 */
@HiltViewModel
class BeneficiaryDashboardViewModel @Inject constructor(
    private val getBeneficiaryByUserIdUseCase: GetBeneficiaryByUserIdUseCase,
    private val getDeliveriesByBeneficiaryUseCase: GetDeliveriesByBeneficiaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiaryDashboardUiState())
    val uiState: StateFlow<BeneficiaryDashboardUiState> = _uiState.asStateFlow()

    /**
     * Carregar dados do beneficiário logado
     */
    fun loadBeneficiaryData(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Buscar dados do beneficiário
            when (val beneficiaryResult = getBeneficiaryByUserIdUseCase(userId)) {
                is Result.Success -> {
                    val beneficiary = beneficiaryResult.data
                    _uiState.update { 
                        it.copy(
                            beneficiary = beneficiary,
                            isLoading = false
                        ) 
                    }

                    // 2. Buscar entregas do beneficiário
                    loadDeliveries(beneficiary.id)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = beneficiaryResult.message ?: "Erro ao carregar dados"
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
     * Carregar entregas do beneficiário
     */
    private fun loadDeliveries(beneficiaryId: String) {
        viewModelScope.launch {
            getDeliveriesByBeneficiaryUseCase(beneficiaryId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val deliveries = result.data
                        
                        // Calcular estatísticas
                        val total = deliveries.size
                        val confirmed = deliveries.count { 
                            it.status == DeliveryStatus.CONFIRMED 
                        }
                        
                        // Próxima entrega agendada (mais recente)
                        val upcoming = deliveries
                            .filter { it.status == DeliveryStatus.SCHEDULED }
                            .sortedBy { it.scheduledDate }
                            .firstOrNull()

                        _uiState.update {
                            it.copy(
                                allDeliveries = deliveries,
                                upcomingDelivery = upcoming,
                                totalDeliveries = total,
                                confirmedDeliveries = confirmed,
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
     * Refresh dos dados
     */
    fun refresh(userId: String) {
        loadBeneficiaryData(userId)
    }

    /**
     * Limpar mensagens de erro
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
