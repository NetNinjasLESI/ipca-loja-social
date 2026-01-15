package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.usecase.delivery.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * Estados da UI para colaborador gerenciar solicitações
 */
data class CollaboratorDeliveryUiState(
    val pendingRequests: List<Delivery> = emptyList(),
    val approvedRequests: List<Delivery> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para colaborador gerenciar solicitações de entregas
 */
@HiltViewModel
class CollaboratorDeliveryViewModel @Inject constructor(
    private val getPendingDeliveryRequestsUseCase: GetPendingDeliveryRequestsUseCase,
    private val getApprovedDeliveriesUseCase: GetApprovedDeliveriesUseCase,
    private val approveDeliveryRequestUseCase: ApproveDeliveryRequestUseCase,
    private val rejectDeliveryRequestUseCase: RejectDeliveryRequestUseCase,
    private val scheduleDeliveryUseCase: ScheduleDeliveryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollaboratorDeliveryUiState())
    val uiState: StateFlow<CollaboratorDeliveryUiState> = _uiState.asStateFlow()

    /**
     * Carregar solicitações pendentes
     */
    fun loadPendingRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getPendingDeliveryRequestsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                pendingRequests = result.data,
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
     * Carregar entregas aprovadas (aguardando agendamento)
     */
    fun loadApprovedRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getApprovedDeliveriesUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                approvedRequests = result.data,
                                isLoading = false
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Erro ao carregar aprovadas"
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
     * Aprovar solicitação
     */
    fun approveRequest(deliveryId: String, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (approveDeliveryRequestUseCase(deliveryId, userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Solicitação aprovada!"
                        )
                    }
                    // Recarregar listas
                    loadPendingRequests()
                    loadApprovedRequests()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao aprovar solicitação"
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
     * Rejeitar solicitação
     */
    fun rejectRequest(deliveryId: String, userId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (rejectDeliveryRequestUseCase(deliveryId, userId, reason)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Solicitação rejeitada"
                        )
                    }
                    // Recarregar lista
                    loadPendingRequests()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao rejeitar solicitação"
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
     * Agendar entrega
     */
    fun scheduleDelivery(
        deliveryId: String,
        scheduledDate: Date,
        notes: String,
        userId: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (scheduleDeliveryUseCase(deliveryId, scheduledDate, notes, userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Entrega agendada com sucesso!"
                        )
                    }
                    // Recarregar lista
                    loadApprovedRequests()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao agendar entrega"
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