package com.ipca.lojasocial.domain.repository

import com.ipca.lojasocial.domain.model.Product
import com.ipca.lojasocial.domain.model.ProductCategory
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.model.StockMovement
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for product and inventory operations
 */
interface ProductRepository {
    
    /**
     * Get all products
     */
    fun getAllProducts(): Flow<Result<List<Product>>>
    
    /**
     * Get product by ID
     */
    suspend fun getProductById(id: String): Result<Product>
    
    /**
     * Get product by barcode
     */
    suspend fun getProductByBarcode(barcode: String): Result<Product>
    
    /**
     * Create a new product
     */
    suspend fun createProduct(product: Product): Result<Product>
    
    /**
     * Update product information
     */
    suspend fun updateProduct(product: Product): Result<Product>
    
    /**
     * Delete product
     */
    suspend fun deleteProduct(id: String): Result<Unit>
    
    /**
     * Get products by category
     */
    fun getProductsByCategory(category: ProductCategory): Flow<Result<List<Product>>>
    
    /**
     * Get products with low stock
     */
    fun getProductsWithLowStock(): Flow<Result<List<Product>>>
    
    /**
     * Get products near expiry
     */
    fun getProductsNearExpiry(daysThreshold: Int): Flow<Result<List<Product>>>
    
    /**
     * Search products by name
     */
    fun searchProducts(query: String): Flow<Result<List<Product>>>
    
    /**
     * Record stock movement
     */
    suspend fun recordStockMovement(movement: StockMovement): Result<Unit>
    
    /**
     * Get stock movements for a product
     */
    fun getStockMovementsByProduct(productId: String): Flow<Result<List<StockMovement>>>
    
    /**
     * Get all stock movements
     */
    fun getAllStockMovements(): Flow<Result<List<StockMovement>>>
}
