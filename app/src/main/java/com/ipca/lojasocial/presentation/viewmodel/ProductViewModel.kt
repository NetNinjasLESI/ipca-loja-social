package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.usecase.product.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados da UI de Produtos
 */
data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val lowStockProducts: List<Product> = emptyList(),
    val expiringProducts: List<Product> = emptyList(),
    val stockMovements: List<StockMovement> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ProductCategory? = null,
    val isLoading: Boolean = false,
    val isLoadingDetails: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para gestão de produtos e inventário
 */
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val createProductUseCase: CreateProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getLowStockProductsUseCase: GetLowStockProductsUseCase,
    private val getProductsNearExpiryUseCase: GetProductsNearExpiryUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val recordStockMovementUseCase: RecordStockMovementUseCase,
    private val getStockMovementsByProductUseCase: GetStockMovementsByProductUseCase,
    private val checkProductStockAvailableUseCase: CheckProductStockAvailableUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadLowStockProducts()
        loadExpiringProducts()
    }

    /**
     * Carregar todos os produtos
     */
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getAllProductsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                products = result.data,
                                filteredProducts = result.data,
                                isLoading = false
                            )
                        }
                        applyFilters()
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Erro ao carregar produtos"
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
     * Carregar produtos com stock baixo
     */
    private fun loadLowStockProducts() {
        viewModelScope.launch {
            getLowStockProductsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(lowStockProducts = result.data) }
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
    }

    /**
     * Carregar produtos perto da validade
     */
    private fun loadExpiringProducts() {
        viewModelScope.launch {
            getProductsNearExpiryUseCase(30).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(expiringProducts = result.data) }
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
    }

    /**
     * Pesquisar produtos
     */
    fun searchProducts(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            searchProductsUseCase(query).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                filteredProducts = result.data,
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
     * Filtrar por categoria
     */
    fun filterByCategory(category: ProductCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    /**
     * Aplicar filtros
     */
    private fun applyFilters() {
        val category = _uiState.value.selectedCategory
        val query = _uiState.value.searchQuery

        if (category != null) {
            viewModelScope.launch {
                getProductsByCategoryUseCase(category).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(filteredProducts = result.data)
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
            searchProducts(query)
        } else {
            _uiState.update {
                it.copy(filteredProducts = it.products)
            }
        }
    }

    fun loadProductDetails(productId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingDetails = true,
                    error = null,
                    selectedProduct = null // ✅ Limpar produto anterior
                )
            }

            when (val result = getProductByIdUseCase(productId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedProduct = result.data,
                            isLoadingDetails = false
                        )
                    }
                    // Carregar movimentações do produto
                    loadStockMovements(productId)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingDetails = false,
                            error = result.message ?: "Erro ao carregar produto"
                        )
                    }
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoadingDetails = true) }
                }
            }
        }
    }

    /**
     * ✅ MANTIDO: Para compatibilidade com código existente
     */
    fun getProductById(productId: String) {
        loadProductDetails(productId)
    }

    /**
     * Selecionar produto (para navegação rápida, sem recarregar)
     */
    fun selectProduct(product: Product) {
        _uiState.update { it.copy(selectedProduct = product) }
        loadStockMovements(product.id)
    }

    /**
     * Carregar movimentações de stock de um produto
     */
    private fun loadStockMovements(productId: String) {
        viewModelScope.launch {
            getStockMovementsByProductUseCase(productId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(stockMovements = result.data) }
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
    }

    /**
     * Criar novo produto
     */
    fun createProduct(product: Product, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val productWithUser = product.copy(createdBy = userId)

            when (val result = createProductUseCase(productWithUser)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Produto criado com sucesso"
                        )
                    }
                    // ✅ Recarregar lista de produtos
                    loadProducts()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao criar produto"
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
     * Atualizar produto
     */
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = updateProductUseCase(product)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Produto atualizado com sucesso",
                            selectedProduct = result.data // ✅ Atualizar com dados mais recentes
                        )
                    }
                    // ✅ Recarregar lista de produtos
                    loadProducts()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao atualizar produto"
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
     * Deletar produto
     */
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (deleteProductUseCase(productId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Produto removido com sucesso",
                            selectedProduct = null
                        )
                    }
                    // ✅ Recarregar lista de produtos
                    loadProducts()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao remover produto"
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
     * Buscar produto por código de barras
     */
    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getProductByBarcodeUseCase(barcode)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedProduct = result.data,
                            isLoading = false
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Produto não encontrado"
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
     * Registar movimentação de stock
     */
    fun recordStockMovement(movement: StockMovement) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (recordStockMovementUseCase(movement)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Movimentação registada com sucesso"
                        )
                    }
                    // ✅ Recarregar movimentações
                    loadStockMovements(movement.productId)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao registar movimentação"
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
     * Verificar disponibilidade de stock
     */
    fun checkStockAvailable(
        productId: String,
        quantity: Double,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = checkProductStockAvailableUseCase(productId, quantity)) {
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
     * Limpar seleção de produto
     */
    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedProduct = null,
                stockMovements = emptyList()
            )
        }
    }
}