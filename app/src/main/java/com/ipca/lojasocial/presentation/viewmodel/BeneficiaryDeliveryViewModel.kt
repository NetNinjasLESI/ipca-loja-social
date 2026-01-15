package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.usecase.delivery.RequestDeliveryUseCase
import com.ipca.lojasocial.domain.usecase.delivery.CancelDeliveryUseCase
import com.ipca.lojasocial.domain.usecase.delivery.GetDeliveriesByBeneficiaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados da UI para beneficiário gerenciar entregas
 */
data class BeneficiaryDeliveryUiState(
    val myRequests: List<Delivery> = emptyList(),
    val pendingRequests: List<Delivery> = emptyList(),
    val approvedRequests: List<Delivery> = emptyList(),
    val scheduledDeliveries: List<Delivery> = emptyList(),
    val completedDeliveries: List<Delivery> = emptyList(),
    val rejectedRequests: List<Delivery> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para beneficiário gerenciar suas entregas
 */
@HiltViewModel
class BeneficiaryDeliveryViewModel @Inject constructor(
    private val requestDeliveryUseCase: RequestDeliveryUseCase,
    private val cancelDeliveryUseCase: CancelDeliveryUseCase,
    private val getDeliveriesByBeneficiaryUseCase: GetDeliveriesByBeneficiaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiaryDeliveryUiState())
    val uiState: StateFlow<BeneficiaryDeliveryUiState> = _uiState.asStateFlow()

    /**
     * Carregar minhas solicitações/entregas
     */
    fun loadMyDeliveries(beneficiaryId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getDeliveriesByBeneficiaryUseCase(beneficiaryId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val all = result.data

                        // Organizar por status
                        val pending = all.filter {
                            it.status == DeliveryStatus.PENDING_APPROVAL
                        }
                        val approved = all.filter {
                            it.status == DeliveryStatus.APPROVED
                        }
                        val scheduled = all.filter {
                            it.status == DeliveryStatus.SCHEDULED
                        }
                        val completed = all.filter {
                            it.status == DeliveryStatus.CONFIRMED
                        }
                        val rejected = all.filter {
                            it.status == DeliveryStatus.REJECTED
                        }

                        _uiState.update {
                            it.copy(
                                myRequests = all,
                                pendingRequests = pending,
                                approvedRequests = approved,
                                scheduledDeliveries = scheduled,
                                completedDeliveries = completed,
                                rejectedRequests = rejected,
                                isLoading = false
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Erro ao carregar solicitações"
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
     * Solicitar nova entrega
     */
    fun requestDelivery(
        beneficiaryId: String,
        kitId: String,
        requestNotes: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (
                val result = requestDeliveryUseCase(
                    beneficiaryId = beneficiaryId,
                    kitId = kitId,
                    requestNotes = requestNotes
                )
            ) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Solicitação enviada com sucesso! " +
                                    "Aguarde aprovação."
                        )
                    }
                    // Recarregar lista
                    loadMyDeliveries(beneficiaryId)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao enviar solicitação"
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
     * Cancelar solicitação/entrega
     */
    fun cancelDelivery(
        deliveryId: String,
        beneficiaryId: String,
        reason: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (
                val result = cancelDeliveryUseCase(
                    deliveryId = deliveryId,
                    userId = beneficiaryId,
                    reason = reason
                )
            ) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Solicitação cancelada"
                        )
                    }
                    // Recarregar lista
                    loadMyDeliveries(beneficiaryId)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao cancelar"
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
     * Limpar mensagens
     */
    fun clearMessages() {
        _uiState.update {
            it.copy(error = null, successMessage = null)
        }
    }
}