package com.ipca.lojasocial.domain.usecase.product

import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para obter produtos com stock baixo
 */
class GetLowStockProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<Result<List<Product>>> {
        return repository.getProductsWithLowStock()
    }
}

/**
 * Use case para obter produtos perto da validade
 */
class GetProductsNearExpiryUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(daysThreshold: Int = 30): Flow<Result<List<Product>>> {
        return repository.getProductsNearExpiry(daysThreshold)
    }
}

/**
 * Use case para obter produtos por categoria
 */
class GetProductsByCategoryUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(category: ProductCategory): Flow<Result<List<Product>>> {
        return repository.getProductsByCategory(category)
    }
}

/**
 * Use case para buscar produto por código de barras
 */
class GetProductByBarcodeUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): Result<Product> {
        if (barcode.isBlank()) {
            return Result.Error(Exception("Código de barras inválido"))
        }
        return repository.getProductByBarcode(barcode)
    }
}

/**
 * Use case para registar movimentação de stock
 */
class RecordStockMovementUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(movement: StockMovement): Result<Unit> {
        // Validações
        if (movement.productId.isBlank()) {
            return Result.Error(Exception("ID do produto é obrigatório"))
        }

        if (movement.quantity <= 0) {
            return Result.Error(Exception("Quantidade deve ser maior que zero"))
        }

        if (movement.reason.isBlank()) {
            return Result.Error(Exception("Motivo da movimentação é obrigatório"))
        }

        if (movement.performedBy.isBlank()) {
            return Result.Error(Exception("Utilizador não identificado"))
        }

        return repository.recordStockMovement(movement)
    }
}

/**
 * Use case para obter histórico de movimentações de um produto
 */
class GetStockMovementsByProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(productId: String): Flow<Result<List<StockMovement>>> {
        if (productId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Result.Error(Exception("ID do produto inválido")))
            }
        }
        return repository.getStockMovementsByProduct(productId)
    }
}

/**
 * Use case para obter todas as movimentações de stock
 */
class GetAllStockMovementsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<Result<List<StockMovement>>> {
        return repository.getAllStockMovements()
    }
}

/**
 * Use case para verificar se produto tem stock disponível
 */
class CheckProductStockAvailableUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(
        productId: String,
        requiredQuantity: Double
    ): Result<Boolean> {
        if (productId.isBlank()) {
            return Result.Error(Exception("ID do produto inválido"))
        }

        if (requiredQuantity <= 0) {
            return Result.Error(Exception("Quantidade deve ser maior que zero"))
        }

        return when (val result = repository.getProductById(productId)) {
            is Result.Success -> {
                val hasStock = result.data.currentStock >= requiredQuantity
                Result.Success(hasStock)
            }

            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }
}
