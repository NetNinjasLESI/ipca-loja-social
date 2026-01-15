package com.ipca.lojasocial.domain.model

/**
 * MODELO SIMPLIFICADO: Kit Customizado
 *
 * O beneficiário pode:
 * - Começar com um kit base (opcional)
 * - Adicionar/remover QUALQUER produto
 * - SEM limite de mudanças
 * - Montar o kit como quiser
 */
data class CustomKit(
    val baseKitId: String? = null,  // Opcional - pode começar do zero
    val baseKitName: String? = null,
    val selectedItems: List<CustomKitItem>,  // Lista final de produtos escolhidos
    val notes: String = ""  // Notas do beneficiário
) {
    fun isEmpty(): Boolean = selectedItems.isEmpty()

    fun totalItems(): Int = selectedItems.size

    fun isBasedOnKit(): Boolean = baseKitId != null
}

/**
 * Item de um kit customizado
 * Usa Int para quantity (UI simples) e ProductUnit para unidade
 */
data class CustomKitItem(
    val productId: String,
    val productName: String,
    val quantity: Int,  // ✅ Quantidade como Int (unidades inteiras)
    val unit: ProductUnit  // ✅ CORRIGIDO: ProductUnit (enum) ao invés de String
)

/**
 * Validação simples: apenas verifica stock
 */
sealed class CustomKitValidationResult {
    object Valid : CustomKitValidationResult()
    data class Invalid(val reason: String) : CustomKitValidationResult()
}