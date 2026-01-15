package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.usecase.delivery.*
import com.ipca.lojasocial.domain.usecase.kit.GetActiveKitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomKitBuilderViewModel @Inject constructor(
    private val getActiveKitsUseCase: GetActiveKitsUseCase,
    private val getAvailableProductsUseCase: GetAvailableProductsUseCase,
    private val loadKitAsBaseUseCase: LoadKitAsBaseUseCase,
    private val validateCustomKitUseCase: ValidateCustomKitUseCase,
    private val requestCustomKitDeliveryUseCase: RequestCustomKitDeliveryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomKitBuilderUiState())
    val uiState: StateFlow<CustomKitBuilderUiState> = _uiState.asStateFlow()

    data class CustomKitBuilderUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        val currentStep: BuilderStep = BuilderStep.START,
        val availableKits: List<Kit> = emptyList(),
        val availableProducts: List<Product> = emptyList(),
        val customKit: CustomKit = CustomKit(selectedItems = emptyList()),
        val searchQuery: String = "",
        val selectedCategory: ProductCategory? = null,
        val validationError: String? = null
    )

    enum class BuilderStep {
        START,
        SELECT_PRODUCTS,
        REVIEW
    }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val kitsResult = getActiveKitsUseCase()) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val kits = (kitsResult.data as? List<Kit>) ?: emptyList()
                    _uiState.update { it.copy(availableKits = kits) }
                }
                else -> {
                    _uiState.update { it.copy(availableKits = emptyList()) }
                }
            }

            when (val productsResult = getAvailableProductsUseCase()) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val products = (productsResult.data as? List<Product>) ?: emptyList()
                    _uiState.update {
                        it.copy(
                            availableProducts = products,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = productsResult.message,
                            isLoading = false
                        )
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            error = "Erro ao carregar produtos",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun startFromScratch() {
        _uiState.update {
            it.copy(
                currentStep = BuilderStep.SELECT_PRODUCTS,
                customKit = CustomKit(selectedItems = emptyList())
            )
        }
    }

    fun startFromKit(kitId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = loadKitAsBaseUseCase(kitId)) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val customKit = result.data as? CustomKit
                    if (customKit != null) {
                        _uiState.update {
                            it.copy(
                                currentStep = BuilderStep.SELECT_PRODUCTS,
                                customKit = customKit,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                error = "Erro ao carregar kit",
                                isLoading = false
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            error = "Erro ao carregar kit",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun goToReview() {
        viewModelScope.launch {
            val currentKit = _uiState.value.customKit

            when (val result = validateCustomKitUseCase(currentKit)) {
                is Result.Success<*> -> {
                    when (result.data) {
                        is CustomKitValidationResult.Invalid -> {
                            _uiState.update {
                                it.copy(
                                    validationError = (result.data as CustomKitValidationResult.Invalid).reason
                                )
                            }
                        }
                        is CustomKitValidationResult.Valid -> {
                            _uiState.update {
                                it.copy(
                                    currentStep = BuilderStep.REVIEW,
                                    validationError = null
                                )
                            }
                        }
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(validationError = "Erro ao validar kit")
                    }
                }
            }
        }
    }

    fun goBack() {
        val currentStep = _uiState.value.currentStep
        _uiState.update {
            it.copy(
                currentStep = when (currentStep) {
                    BuilderStep.REVIEW -> BuilderStep.SELECT_PRODUCTS
                    BuilderStep.SELECT_PRODUCTS -> BuilderStep.START
                    BuilderStep.START -> BuilderStep.START
                },
                validationError = null
            )
        }
    }

    fun addProduct(product: Product, quantity: Int) {
        val currentKit = _uiState.value.customKit
        val currentItems = currentKit.selectedItems.toMutableList()

        val existingIndex = currentItems.indexOfFirst { it.productId == product.id }

        if (existingIndex >= 0) {
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(
                quantity = existingItem.quantity + quantity
            )
        } else {
            // ✅ CORRIGIDO: Usar CustomKitItem ao invés de KitItem
            currentItems.add(
                CustomKitItem(
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,  // Int é ok aqui
                    unit = product.unit
                )
            )
        }

        _uiState.update {
            it.copy(
                customKit = currentKit.copy(selectedItems = currentItems),
                validationError = null
            )
        }
    }

    fun removeProduct(productId: String) {
        val currentKit = _uiState.value.customKit
        val updatedItems = currentKit.selectedItems.filter { it.productId != productId }

        _uiState.update {
            it.copy(
                customKit = currentKit.copy(selectedItems = updatedItems)
            )
        }
    }

    fun updateProductQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeProduct(productId)
            return
        }

        val currentKit = _uiState.value.customKit
        val updatedItems = currentKit.selectedItems.map { item ->
            if (item.productId == productId) {
                item.copy(quantity = newQuantity)
            } else {
                item
            }
        }

        _uiState.update {
            it.copy(
                customKit = currentKit.copy(selectedItems = updatedItems)
            )
        }
    }

    fun updateNotes(notes: String) {
        val currentKit = _uiState.value.customKit
        _uiState.update {
            it.copy(
                customKit = currentKit.copy(notes = notes)
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: ProductCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun getFilteredProducts(): List<Product> {
        val state = _uiState.value
        var filtered = state.availableProducts

        if (state.selectedCategory != null) {
            filtered = filtered.filter { it.category == state.selectedCategory }
        }

        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(state.searchQuery, ignoreCase = true) ||
                        it.description.contains(state.searchQuery, ignoreCase = true)
            }
        }

        return filtered
    }

    fun submitCustomKit(beneficiaryId: String, additionalNotes: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentKit = _uiState.value.customKit

            when (val result = requestCustomKitDeliveryUseCase(
                beneficiaryId = beneficiaryId,
                customKit = currentKit,
                notes = additionalNotes
            )) {
                is Result.Success<*> -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Solicitação enviada com sucesso!"
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao enviar solicitação"
                        )
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro desconhecido"
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, validationError = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}