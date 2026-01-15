package com.ipca.lojasocial.domain.model

import java.util.Date

/**
 * Domain model representing a Stock Movement (entry or exit)
 */
data class StockMovement(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val type: MovementType = MovementType.ENTRY,
    val quantity: Double = 0.0,
    val unit: ProductUnit = ProductUnit.UNIT,
    val reason: String = "",
    val referenceDocument: String? = null,
    val performedBy: String = "",  // User ID
    val performedAt: Date = Date(),
    val notes: String = ""
)

enum class MovementType {
    ENTRY,          // Entrada
    EXIT,           // Saída
    ADJUSTMENT,     // Ajuste
    TRANSFER        // Transferência
}
