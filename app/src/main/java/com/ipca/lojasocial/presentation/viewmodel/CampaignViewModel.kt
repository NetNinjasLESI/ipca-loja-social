package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.usecase.campaign.*
import com.ipca.lojasocial.domain.usecase.product.GetAllProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados da UI de Campanhas
 */
data class CampaignUiState(
    val campaigns: List<Campaign> = emptyList(),
    val filteredCampaigns: List<Campaign> = emptyList(),
    val selectedCampaign: Campaign? = null,
    val productDonations: List<ProductDonation> = emptyList(),
    val selectedStatus: CampaignStatus? = null,
    val statistics: CampaignStatistics? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ✅ ViewModel ATUALIZADO com seleção obrigatória de produto
 */
@HiltViewModel
class CampaignViewModel @Inject constructor(
    private val getAllCampaignsUseCase: GetAllCampaignsUseCase,
    private val getCampaignByIdUseCase: GetCampaignByIdUseCase,
    private val createCampaignUseCase: CreateCampaignUseCase,
    private val updateCampaignUseCase: UpdateCampaignUseCase,
    private val deleteCampaignUseCase: DeleteCampaignUseCase,
    private val activateCampaignUseCase: ActivateCampaignUseCase,
    private val completeCampaignUseCase: CompleteCampaignUseCase,
    private val getActiveCampaignsUseCase: GetActiveCampaignsUseCase,
    private val searchCampaignsUseCase: SearchCampaignsUseCase,
    private val getProductDonationsByCampaignUseCase: GetProductDonationsByCampaignUseCase,
    private val getCampaignStatisticsUseCase: GetCampaignStatisticsUseCase,
    private val addProductDonationWithInventoryUseCase: AddProductDonationWithInventoryUseCase,
    private val getAllProductsUseCase: GetAllProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampaignUiState())
    val uiState: StateFlow<CampaignUiState> = _uiState.asStateFlow()

    private val _inventoryProducts = MutableStateFlow<List<Product>>(emptyList())
    val inventoryProducts: StateFlow<List<Product>> = _inventoryProducts.asStateFlow()

    init {
        loadCampaigns()
        loadInventoryProducts()
        loadStatistics()
    }

    private fun loadInventoryProducts() {
        viewModelScope.launch {
            getAllProductsUseCase().collect { result ->
                if (result is Result.Success) {
                    _inventoryProducts.value = result.data.filter { it.isActive }
                }
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            when (val result = getCampaignStatisticsUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(statistics = result.data) }
                }
                is Result.Error -> {
                    // Log error silently
                }
                else -> {}
            }
        }
    }

    fun loadCampaigns() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getAllCampaignsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                campaigns = result.data,
                                filteredCampaigns = filterCampaigns(result.data, it.selectedStatus),
                                isLoading = false
                            )
                        }
                        loadStatistics()
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Erro ao carregar campanhas"
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

    fun filterByStatus(status: CampaignStatus?) {
        _uiState.update {
            it.copy(
                selectedStatus = status,
                filteredCampaigns = filterCampaigns(it.campaigns, status)
            )
        }
    }

    private fun filterCampaigns(campaigns: List<Campaign>, status: CampaignStatus?): List<Campaign> {
        return if (status == null) {
            campaigns
        } else {
            campaigns.filter { it.status == status }
        }
    }

    fun loadCampaignById(campaignId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getCampaignByIdUseCase(campaignId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedCampaign = result.data,
                            isLoading = false
                        )
                    }
                    loadProductDonations(campaignId)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao carregar campanha"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun loadProductDonations(campaignId: String) {
        viewModelScope.launch {
            getProductDonationsByCampaignUseCase(campaignId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(productDonations = result.data) }
                    }

                    is Result.Error -> {
                        // Log error silently
                    }

                    is Result.Loading -> {
                        // No action needed
                    }
                }
            }
        }
    }

    fun createCampaign(campaign: Campaign, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val campaignWithUser = campaign.copy(createdBy = userId)

            when (val result = createCampaignUseCase(campaignWithUser)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Campanha criada com sucesso",
                            selectedCampaign = result.data
                        )
                    }
                    loadCampaigns()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao criar campanha"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun updateCampaign(campaign: Campaign) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (updateCampaignUseCase(campaign)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Campanha atualizada"
                        )
                    }
                    loadCampaigns()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao atualizar campanha"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun deleteCampaign(campaignId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (deleteCampaignUseCase(campaignId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Campanha eliminada",
                            selectedCampaign = null
                        )
                    }
                    loadCampaigns()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao eliminar campanha"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun activateCampaign(campaignId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (activateCampaignUseCase(campaignId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Campanha ativada"
                        )
                    }
                    loadCampaigns()
                    loadCampaignById(campaignId)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao ativar campanha"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun completeCampaign(campaignId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (completeCampaignUseCase(campaignId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Campanha concluída"
                        )
                    }
                    loadCampaigns()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao completar campanha"
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
     * ✅ NOVO: Registar doação com produto EXISTENTE do inventário
     */
    fun registerProductDonationFromExisting(
        campaignId: String,
        product: Product,
        quantity: Double,
        donorName: String?,
        donorEmail: String?,
        donorPhone: String?,
        notes: String,
        userId: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = addProductDonationWithInventoryUseCase(
                campaignId = campaignId,
                productName = product.name,
                quantity = quantity,
                unit = product.unit,
                category = product.category,
                donorName = donorName,
                donorEmail = donorEmail,
                donorPhone = donorPhone,
                notes = notes,
                userId = userId
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Doação registada! Stock atualizado (+${quantity} ${product.unit.getDisplayName()})"
                        )
                    }
                    loadProductDonations(campaignId)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao registar doação"
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
     * ✅ NOVO: Registar doação criando NOVO produto no inventário
     */
    fun registerProductDonationWithNewProduct(
        campaignId: String,
        productName: String,
        productDescription: String,
        category: ProductCategory,
        barcode: String?,
        unit: ProductUnit,
        minimumStock: Double,
        quantity: Double,
        donorName: String?,
        donorEmail: String?,
        donorPhone: String?,
        notes: String,
        userId: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = addProductDonationWithInventoryUseCase(
                campaignId = campaignId,
                productName = productName,
                quantity = quantity,
                unit = unit,
                category = category,
                donorName = donorName,
                donorEmail = donorEmail,
                donorPhone = donorPhone,
                notes = notes,
                userId = userId
            )) {
                is Result.Success -> {
                    val donationResult = result.data

                    val message = if (donationResult.productCreated) {
                        "Doação registada! Produto criado no inventário (+${quantity} ${unit.getDisplayName()})"
                    } else {
                        "Doação registada! Stock atualizado (+${quantity} ${unit.getDisplayName()})"
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = message
                        )
                    }

                    loadProductDonations(campaignId)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao registar doação"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun searchCampaigns(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            searchCampaignsUseCase(query).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                campaigns = result.data,
                                filteredCampaigns = filterCampaigns(result.data, it.selectedStatus),
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
            it.copy(
                selectedCampaign = null,
                productDonations = emptyList()
            )
        }
    }
}

// Helper Extension
private fun ProductUnit.getDisplayName(): String {
    return when (this) {
        ProductUnit.UNIT -> "un"
        ProductUnit.KILOGRAM -> "kg"
        ProductUnit.LITER -> "L"
        ProductUnit.PACKAGE -> "pct"
    }
}
