package com.ipca.lojasocial.domain.model

import java.util.Date

/**
 * Domain model representing a Product in inventory
 */
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: ProductCategory = ProductCategory.OTHER,
    val barcode: String? = null,
    val unit: ProductUnit = ProductUnit.UNIT,
    val currentStock: Double = 0.0,
    val minimumStock: Double = 0.0,
    val expiryDate: Date? = null,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val createdBy: String = "",  // User ID
    val updatedAt: Date = Date()
)

enum class ProductCategory {
    FOOD,           // Alimentar
    HYGIENE,        // Higiene
    CLEANING,       // Limpeza
    OTHER           // Outro
}

enum class ProductUnit {
    UNIT,           // Unidade
    KILOGRAM,       // Quilograma
    LITER,          // Litro
    PACKAGE         // Embalagem
}
