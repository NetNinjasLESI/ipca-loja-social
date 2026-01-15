package com.ipca.lojasocial.domain.usecase.campaign

import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.CampaignRepository
import com.ipca.lojasocial.domain.repository.ProductRepository
import java.util.Date
import javax.inject.Inject

/**
 * ✅ NOVO UseCase: Registar doação de produto E adicionar ao inventário
 *
 * Fluxo:
 * 1. Registar doação na campanha
 * 2. Verificar se produto existe no inventário
 * 3. Se não existir, criar produto
 * 4. Adicionar stock ao inventário
 * 5. Registar movimentação de stock
 */
class AddProductDonationWithInventoryUseCase @Inject constructor(
    private val campaignRepository: CampaignRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        campaignId: String,
        productName: String,
        quantity: Double,
        unit: ProductUnit,
        category: ProductCategory,
        donorName: String?,
        donorEmail: String?,
        donorPhone: String?,
        notes: String,
        userId: String
    ): Result<ProductDonationResult> {
        return try {
            // 1. Registar doação na campanha
            val donationResult = campaignRepository.addProductDonation(
                campaignId = campaignId,
                productName = productName,
                donorName = donorName,
                donorEmail = donorEmail,
                donorPhone = donorPhone,
                notes = "$notes | Quantidade: $quantity ${unit.getDisplayName()}"
            )

            if (donationResult is Result.Error) {
                return Result.Error(
                    donationResult.exception,
                    "Erro ao registar doação: ${donationResult.message}"
                )
            }

            val donationId = (donationResult as Result.Success).data

            // 2. Procurar produto existente no inventário (por nome e categoria)
            var existingProduct: Product? = null

            productRepository.getAllProducts().collect { result ->
                if (result is Result.Success) {
                    existingProduct = result.data.firstOrNull { product ->
                        product.name.equals(productName, ignoreCase = true) &&
                                product.category == category &&
                                product.isActive
                    }
                }
            }

            // 3. Se produto não existe, criar novo
            val productId = if (existingProduct != null) {
                existingProduct!!.id
            } else {
                val newProduct = Product(
                    name = productName,
                    description = "Produto adicionado via campanha",
                    category = category,
                    unit = unit,
                    currentStock = 0.0,
                    minimumStock = 10.0, // Valor padrão
                    isActive = true,
                    createdBy = userId
                )

                val createResult = productRepository.createProduct(newProduct)

                if (createResult is Result.Error) {
                    return Result.Error(
                        createResult.exception,
                        "Erro ao criar produto no inventário: ${createResult.message}"
                    )
                }

                (createResult as Result.Success).data.id
            }

            // 4. Adicionar stock ao inventário
            val movement = StockMovement(
                productId = productId,
                productName = productName,
                type = MovementType.ENTRY,
                quantity = quantity,
                unit = unit,
                reason = "Doação de Campanha",
                referenceDocument = "CAMP-$campaignId / DON-$donationId",
                performedBy = userId,
                performedAt = Date(),
                notes = "Doador: ${donorName ?: "Anónimo"} | $notes"
            )

            val movementResult = productRepository.recordStockMovement(movement)

            if (movementResult is Result.Error) {
                return Result.Error(
                    movementResult.exception,
                    "Erro ao adicionar stock: ${movementResult.message}"
                )
            }

            // 5. Sucesso!
            Result.Success(
                ProductDonationResult(
                    donationId = donationId,
                    productId = productId,
                    productCreated = existingProduct == null,
                    stockAdded = quantity
                )
            )

        } catch (e: Exception) {
            Result.Error(e, "Erro ao processar doação: ${e.message}")
        }
    }
}

/**
 * Resultado da operação de doação
 */
data class ProductDonationResult(
    val donationId: String,
    val productId: String,
    val productCreated: Boolean,
    val stockAdded: Double
)

/**
 * Helper para obter nome de unidade em português
 */
private fun ProductUnit.getDisplayName(): String {
    return when (this) {
        ProductUnit.UNIT -> "unidade(s)"
        ProductUnit.KILOGRAM -> "kg"
        ProductUnit.LITER -> "litro(s)"
        ProductUnit.PACKAGE -> "embalagem(ns)"
    }
}