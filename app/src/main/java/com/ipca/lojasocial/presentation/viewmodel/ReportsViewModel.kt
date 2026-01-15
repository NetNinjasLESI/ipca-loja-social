package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.usecase.reports.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * Estados da UI de Relatórios
 */
data class ReportsUiState(
    val dashboardStatistics: DashboardStatistics? = null,
    val inventoryReport: InventoryReport? = null,
    val deliveriesReport: DeliveriesReport? = null,
    val campaignsReport: CampaignsReport? = null,
    val beneficiariesReport: BeneficiariesReport? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para gestão de relatórios
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getDashboardStatisticsUseCase: GetDashboardStatisticsUseCase,
    private val getInventoryReportUseCase: GetInventoryReportUseCase,
    private val getDeliveriesReportUseCase: GetDeliveriesReportUseCase,
    private val getCampaignsReportUseCase: GetCampaignsReportUseCase,
    private val getBeneficiariesReportUseCase: GetBeneficiariesReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadDashboardStatistics()
    }

    /**
     * Carregar estatísticas do dashboard
     */
    fun loadDashboardStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getDashboardStatisticsUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            dashboardStatistics = result.data,
                            isLoading = false
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao carregar estatísticas"
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
     * Gerar relatório de inventário
     */
    fun generateInventoryReport(startDate: Date? = null, endDate: Date? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getInventoryReportUseCase(startDate, endDate)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            inventoryReport = result.data,
                            isLoading = false,
                            successMessage = "Relatório de inventário gerado"
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao gerar relatório"
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
     * Gerar relatório de entregas
     */
    fun generateDeliveriesReport(startDate: Date? = null, endDate: Date? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getDeliveriesReportUseCase(startDate, endDate)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            deliveriesReport = result.data,
                            isLoading = false,
                            successMessage = "Relatório de entregas gerado"
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao gerar relatório"
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
     * Gerar relatório de campanhas
     */
    fun generateCampaignsReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getCampaignsReportUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            campaignsReport = result.data,
                            isLoading = false,
                            successMessage = "Relatório de campanhas gerado"
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao gerar relatório"
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
     * Gerar relatório de beneficiários
     */
    fun generateBeneficiariesReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getBeneficiariesReportUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            beneficiariesReport = result.data,
                            isLoading = false,
                            successMessage = "Relatório de beneficiários gerado"
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao gerar relatório"
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
     * Limpar relatórios
     */
    fun clearReports() {
        _uiState.update {
            it.copy(
                inventoryReport = null,
                deliveriesReport = null,
                campaignsReport = null,
                beneficiariesReport = null
            )
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
