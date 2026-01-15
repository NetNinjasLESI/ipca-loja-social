package com.ipca.lojasocial.domain.model

import java.util.Date

/**
 * Domain model representing a Delivery to a beneficiary
 */
data class Delivery(
    val id: String = "",
    val beneficiaryId: String = "",
    val beneficiaryName: String = "",
    val kitId: String = "",
    val kitName: String = "",
    val scheduledDate: Date = Date(),
    val status: DeliveryStatus = DeliveryStatus.PENDING_APPROVAL,
    val notes: String = "",

    // Request fields (quando beneficiário solicita)
    val requestedDate: Date? = null,
    val requestNotes: String = "",

    // Approval fields
    val approvedDate: Date? = null,
    val approvedBy: String? = null,
    val rejectedDate: Date? = null,
    val rejectedBy: String? = null,
    val rejectionReason: String? = null,

    // Confirmation fields
    val confirmedDate: Date? = null,
    val confirmedBy: String? = null,

    // Cancellation fields
    val cancelledDate: Date? = null,
    val cancelledBy: String? = null,
    val cancellationReason: String? = null,

    // Timestamps
    val createdAt: Date = Date(),
    val createdBy: String = "",
    val updatedAt: Date = Date()
)

/**
 * Estados do fluxo de entrega
 */
enum class DeliveryStatus {
    PENDING_APPROVAL,   // Aguardando aprovação do colaborador
    APPROVED,           // Aprovada, aguarda agendamento
    REJECTED,           // Rejeitada pelo colaborador
    SCHEDULED,          // Agendada (com data definida)
    CONFIRMED,          // Confirmada (entregue, stock deduzido)
    CANCELLED           // Cancelada
}