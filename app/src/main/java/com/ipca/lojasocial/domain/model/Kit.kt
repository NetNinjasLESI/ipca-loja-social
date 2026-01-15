package com.ipca.lojasocial.domain.model

import java.util.Date

/**
 * Domain model representing a Kit (set of products for delivery)
 */
data class Kit(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val items: List<KitItem> = emptyList(),
    val isActive: Boolean = true,
    val isPredefined: Boolean = false,
    val createdAt: Date = Date(),
    val createdBy: String = "",  // User ID
    val updatedAt: Date = Date()
)

/**
 * Domain model representing an item within a Kit
 */
data class KitItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Double = 0.0,
    val unit: ProductUnit = ProductUnit.UNIT
)
