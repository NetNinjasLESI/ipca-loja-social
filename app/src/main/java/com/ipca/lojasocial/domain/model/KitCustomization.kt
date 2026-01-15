package com.ipca.lojasocial.domain.model

/**
 * Representa uma customização de kit feita por um beneficiário
 */
data class KitCustomization(
    val baseKitId: String = "",
    val baseKitName: String = "",
    val removedItems: List<KitItem> = emptyList(),
    val addedItems: List<KitItem> = emptyList(),
    val customizationNotes: String = ""
) {
    /**
     * Verifica se há customizações
     */
    fun hasCustomizations(): Boolean {
        return removedItems.isNotEmpty() || addedItems.isNotEmpty()
    }

    /**
     * Conta o total de mudanças
     */
    fun totalChanges(): Int {
        return removedItems.size + addedItems.size
    }

    /**
     * Obtém a lista final de itens após customização
     */
    fun getFinalItems(baseKit: Kit): List<KitItem> {
        // Remove os itens marcados para remoção
        val remainingItems = baseKit.items.filterNot { baseItem ->
            removedItems.any { it.productId == baseItem.productId }
        }

        // Adiciona os novos itens
        return remainingItems + addedItems
    }
}

/**
 * Resultado da validação de customização
 */
sealed class CustomizationValidationResult {
    object Valid : CustomizationValidationResult()
    data class Invalid(val reason: String) : CustomizationValidationResult()
}

/**
 * Regras de customização de kits
 */
object KitCustomizationRules {
    const val MAX_SUBSTITUTIONS = 3
    const val MAX_VALUE_PERCENTAGE_INCREASE = 0.0 // Não pode aumentar valor
}
