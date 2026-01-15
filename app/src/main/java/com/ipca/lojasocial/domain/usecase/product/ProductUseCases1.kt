package com.ipca.lojasocial.domain.usecase.product

import com.ipca.lojasocial.domain.model.Product
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para obter todos os produtos
 */
class GetAllProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<Result<List<Product>>> {
        return repository.getAllProducts()
    }
}

/**
 * Use case para obter produto por ID
 */
class GetProductByIdUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String): Result<Product> {
        if (productId.isBlank()) {
            return Result.Error(Exception("ID do produto inválido"))
        }
        return repository.getProductById(productId)
    }
}

/**
 * Use case para criar novo produto
 */
class CreateProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Result<Product> {
        // Validações
        if (product.name.isBlank()) {
            return Result.Error(Exception("Nome do produto é obrigatório"))
        }

        if (product.minimumStock < 0) {
            return Result.Error(Exception("Stock mínimo não pode ser negativo"))
        }

        if (product.currentStock < 0) {
            return Result.Error(Exception("Stock atual não pode ser negativo"))
        }

        return repository.createProduct(product)
    }
}

/**
 * Use case para atualizar produto
 */
class UpdateProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Result<Product> {
        // Validações
        if (product.id.isBlank()) {
            return Result.Error(Exception("ID do produto é obrigatório"))
        }

        if (product.name.isBlank()) {
            return Result.Error(Exception("Nome do produto é obrigatório"))
        }

        if (product.minimumStock < 0) {
            return Result.Error(Exception("Stock mínimo não pode ser negativo"))
        }

        if (product.currentStock < 0) {
            return Result.Error(Exception("Stock atual não pode ser negativo"))
        }

        return repository.updateProduct(product)
    }
}

/**
 * Use case para deletar produto (soft delete)
 */
class DeleteProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String): Result<Unit> {
        if (productId.isBlank()) {
            return Result.Error(Exception("ID do produto inválido"))
        }

        return repository.deleteProduct(productId)
    }
}

/**
 * Use case para pesquisar produtos
 */
class SearchProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(query: String): Flow<Result<List<Product>>> {
        if (query.isBlank()) {
            return repository.getAllProducts()
        }
        return repository.searchProducts(query)
    }
}
