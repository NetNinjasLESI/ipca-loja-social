package com.ipca.lojasocial.domain.model

import java.util.Date

/**
 * ✅ Product Donation - Doação de produto registada
 * Contém informações do doador e está ligada a uma campanha
 * IMPORTANTE: Esta é a versão COMPLETA com integração ao inventário
 */
data class ProductDonation(
    val id: String = "",
    val campaignId: String = "",
    val productName: String = "",
    val quantity: Double = 0.0,
    val unit: ProductUnit = ProductUnit.UNIT,
    val category: ProductCategory = ProductCategory.OTHER,
    val donorName: String? = null,
    val donorEmail: String? = null,
    val donorPhone: String? = null,
    val donatedAt: Date = Date(),
    val notes: String = "",
    val inventoryProductId: String = "" // ID do produto no inventário
)
