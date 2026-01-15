package com.ipca.lojasocial.domain.usecase.delivery

import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.DeliveryRepository
import com.ipca.lojasocial.domain.repository.KitRepository
import com.ipca.lojasocial.domain.repository.BeneficiaryRepository
import com.ipca.lojasocial.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject

/**
 * SIMPLIFICADO: Validar kit customizado
 * Única regra: produtos devem ter stock disponível
 */
class ValidateCustomKitUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        customKit: CustomKit
    ): Result<CustomKitValidationResult> {
        try {
            // 1. Verificar se tem itens
            if (customKit.isEmpty()) {
                return Result.Success(
                    CustomKitValidationResult.Invalid("Adicione pelo menos um produto")
                )
            }

            // 2. Validar cada produto
            for (item in customKit.selectedItems) {
                when (val productResult = productRepository.getProductById(item.productId)) {
                    is Result.Success<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val product = productResult.data as? Product

                        if (product == null) {
                            return Result.Success(
                                CustomKitValidationResult.Invalid(
                                    "Produto ${item.productName} não encontrado"
                                )
                            )
                        }

                        // Produto deve estar ativo
                        if (!product.isActive) {
                            return Result.Success(
                                CustomKitValidationResult.Invalid(
                                    "Produto ${item.productName} não está mais disponível"
                                )
                            )
                        }

                        // Verificar stock - ✅ CORRIGIDO: converter Int para Double
                        if (product.currentStock < item.quantity.toDouble()) {
                            return Result.Success(
                                CustomKitValidationResult.Invalid(
                                    "Stock insuficiente para ${item.productName}. " +
                                            "Disponível: ${product.currentStock.toInt()} ${item.unit}"
                                )
                            )
                        }
                    }
                    else -> return Result.Success(
                        CustomKitValidationResult.Invalid(
                            "Produto ${item.productName} não encontrado"
                        )
                    )
                }
            }

            return Result.Success(CustomKitValidationResult.Valid)

        } catch (e: Exception) {
            return Result.Error(e, "Erro ao validar kit: ${e.message}")
        }
    }
}

/**
 * SIMPLIFICADO: Solicitar entrega com kit customizado
 * Beneficiário pode criar kit livremente
 */
class RequestCustomKitDeliveryUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val kitRepository: KitRepository,
    private val beneficiaryRepository: BeneficiaryRepository,
    private val validateCustomKitUseCase: ValidateCustomKitUseCase
) {
    suspend operator fun invoke(
        beneficiaryId: String,
        customKit: CustomKit,
        notes: String = ""
    ): Result<Delivery> {

        if (beneficiaryId.isBlank()) {
            return Result.Error(Exception("Beneficiário inválido"))
        }

        // 1. Validar beneficiário
        val beneficiary = when (
            val result = beneficiaryRepository.getBeneficiaryById(beneficiaryId)
        ) {
            is Result.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                val ben = result.data as? Beneficiary
                if (ben == null || !ben.isActive) {
                    return Result.Error(Exception("Beneficiário não encontrado ou inativo"))
                }
                ben
            }
            else -> return Result.Error(Exception("Beneficiário não encontrado"))
        }

        // 2. Validar kit customizado
        when (val validationResult = validateCustomKitUseCase(customKit)) {
            is Result.Success<*> -> {
                when (validationResult.data) {
                    is CustomKitValidationResult.Invalid -> {
                        return Result.Error(
                            Exception((validationResult.data as CustomKitValidationResult.Invalid).reason)
                        )
                    }
                    is CustomKitValidationResult.Valid -> {
                        // OK, continuar
                    }
                }
            }
            is Result.Error -> {
                return Result.Error(
                    validationResult.exception,
                    validationResult.message ?: "Erro na validação"
                )
            }
            else -> {
                return Result.Error(Exception("Erro ao validar kit"))
            }
        }

        // 3. Criar nome do kit
        val kitName = if (customKit.isBasedOnKit()) {
            "${customKit.baseKitName} (Personalizado - ${customKit.totalItems()} itens)"
        } else {
            "Kit Personalizado (${customKit.totalItems()} itens)"
        }

        // 4. Criar descrição detalhada
        val detailedNotes = buildString {
            if (notes.isNotBlank()) {
                append("$notes\n\n")
            }

            append("KIT PERSONALIZADO:\n")

            if (customKit.isBasedOnKit()) {
                append("Baseado em: ${customKit.baseKitName}\n\n")
            }

            append("Produtos selecionados:\n")
            customKit.selectedItems.forEach { item ->
                append("- ${item.productName} (${item.quantity} ${item.unit})\n")
            }

            if (customKit.notes.isNotBlank()) {
                append("\nObservações: ${customKit.notes}")
            }
        }

        // 5. Criar delivery
        val delivery = Delivery(
            beneficiaryId = beneficiaryId,
            beneficiaryName = beneficiary.name,
            kitId = customKit.baseKitId ?: "custom",
            kitName = kitName,
            status = DeliveryStatus.PENDING_APPROVAL,
            requestedDate = Date(),
            requestNotes = detailedNotes,
            createdAt = Date(),
            createdBy = beneficiaryId,
            updatedAt = Date()
        )

        return deliveryRepository.createDelivery(delivery)
    }
}

/**
 * SIMPLIFICADO: Obter produtos disponíveis
 * Retorna todos os produtos ativos com stock
 */
class GetAvailableProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): Result<List<Product>> {
        try {
            when (val result = productRepository.getAllProducts().first()) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val allProducts = (result.data as? List<Product>) ?: emptyList()
                    val availableProducts = allProducts.filter { product ->
                        product.isActive && product.currentStock > 0
                    }
                    return Result.Success(availableProducts)
                }
                is Result.Error -> {
                    return Result.Error(result.exception, result.message)
                }
                else -> {
                    return Result.Error(Exception("Erro ao buscar produtos"))
                }
            }
        } catch (e: Exception) {
            return Result.Error(e, "Erro ao obter produtos: ${e.message}")
        }
    }
}

/**
 * OPCIONAL: Carregar kit base para começar
 */
class LoadKitAsBaseUseCase @Inject constructor(
    private val kitRepository: KitRepository
) {
    suspend operator fun invoke(kitId: String): Result<CustomKit> {
        try {
            when (val result = kitRepository.getKitById(kitId)) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val kit =
                        result.data as? Kit ?: return Result.Error(Exception("Kit não encontrado"))

                    // ✅ CORRIGIDO: Converter KitItem para CustomKitItem
                    val customItems = kit.items.map { kitItem ->
                        CustomKitItem(
                            productId = kitItem.productId,
                            productName = kitItem.productName,
                            quantity = kitItem.quantity.toInt(),  // Double → Int
                            unit = kitItem.unit
                        )
                    }

                    val customKit = CustomKit(
                        baseKitId = kit.id,
                        baseKitName = kit.name,
                        selectedItems = customItems,
                        notes = ""
                    )
                    return Result.Success(customKit)
                }
                is Result.Error -> {
                    return Result.Error(result.exception, result.message)
                }
                else -> {
                    return Result.Error(Exception("Kit não encontrado"))
                }
            }
        } catch (e: Exception) {
            return Result.Error(e, "Erro ao carregar kit: ${e.message}")
        }
    }
}