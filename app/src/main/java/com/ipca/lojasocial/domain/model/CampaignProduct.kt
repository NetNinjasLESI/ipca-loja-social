package com.ipca.lojasocial.domain.model

/**
 * Produto necessário numa campanha de recolha
 * SIMPLES: Apenas lista o que é preciso (sem quantidades/progresso)
 * 
 * USA O ProductCategory QUE JÁ EXISTE NO Product.kt!
 */
data class CampaignProduct(
    val id: String = "",
    val campaignId: String = "",
    val productName: String = "", // Ex: "Arroz", "Óleo", "Pasta de Dentes"
    val category: ProductCategory = ProductCategory.FOOD, // ← USA O EXISTENTE!
    val priority: CampaignPriority = CampaignPriority.NORMAL, // ← RENOMEADO!
    val notes: String = "" // Ex: "De preferência integral"
)

/**
 * Prioridade do produto na campanha
 * RENOMEADO para CampaignPriority para evitar conflitos
 */
enum class CampaignPriority {
    HIGH,       // Urgente/Prioritário
    NORMAL,     // Normal
    LOW         // Opcional
}
