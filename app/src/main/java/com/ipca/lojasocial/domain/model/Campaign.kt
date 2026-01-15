package com.ipca.lojasocial.domain.model

import java.util.Date
import java.util.Calendar

/**
 * Domain model representing a Campaign
 * ✅ SIMPLIFICADO: Campanhas já não têm produtos necessários
 * Os doadores doam o que querem, e isso é registado no inventário
 */
data class Campaign(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val goalType: CampaignGoalType = CampaignGoalType.PRODUCT,
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val status: CampaignStatus = CampaignStatus.DRAFT,
    val imageUrl: String? = null,
    val qrCodeUrl: String? = null,
    val publicUrl: String = "",
    val isPublic: Boolean = false,
    val createdBy: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * ✅ Verificar se a campanha está ativa (dentro do período)
     */
    fun isActive(): Boolean {
        val now = Date()
        return status == CampaignStatus.ACTIVE &&
                now.after(startDate) &&
                now.before(endDate)
    }

    /**
     * ✅ Verificar se a campanha já começou
     */
    fun hasStarted(): Boolean {
        return Date().after(startDate)
    }

    /**
     * ✅ Verificar se a campanha já terminou
     */
    fun hasEnded(): Boolean {
        return Date().after(endDate)
    }

    /**
     * ✅ Calcular dias restantes até o fim
     */
    fun daysRemaining(): Int {
        if (hasEnded()) return 0

        val now = Calendar.getInstance()
        val end = Calendar.getInstance().apply { time = endDate }

        val diffInMillis = end.timeInMillis - now.timeInMillis
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * ✅ Verificar se as datas são válidas
     */
    fun hasValidDates(): Boolean {
        val now = Date()
        return startDate.after(now) && endDate.after(startDate)
    }

    /**
     * ✅ Obter mensagem de erro de validação de datas
     */
    fun getDateValidationError(): String? {
        val now = Date()

        return when {
            startDate.before(now) -> "A data de início deve ser no futuro"
            endDate.before(startDate) || endDate == startDate ->
                "A data de fim deve ser posterior à data de início"
            else -> null
        }
    }
}

/**
 * Campaign Goal Type
 */
enum class CampaignGoalType {
    MONETARY,  // Angariação monetária (legado)
    PRODUCT    // Angariação de produtos (PADRÃO)
}

/**
 * Campaign Status
 */
enum class CampaignStatus {
    DRAFT,      // Rascunho
    ACTIVE,     // Ativa
    COMPLETED,  // Concluída
    CANCELLED   // Cancelada
}

/**
 * Donation (legado - manter para compatibilidade monetária)
 */
data class Donation(
    val id: String = "",
    val campaignId: String = "",
    val amount: Double = 0.0,
    val donorName: String? = null,
    val donorEmail: String? = null,
    val donatedAt: Date = Date()
)
