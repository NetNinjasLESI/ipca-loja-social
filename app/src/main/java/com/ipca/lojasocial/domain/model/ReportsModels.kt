package com.ipca.lojasocial.domain.model

import java.util.Date

/**
 * Estatísticas do Dashboard
 */
data class DashboardStatistics(
    val totalProducts: Int,
    val totalStock: Double,
    val totalStockValue: Double,
    val lowStockProducts: Int,
    val totalBeneficiaries: Int,
    val activeBeneficiaries: Int,
    val totalKits: Int,
    val activeKits: Int,
    val totalDeliveries: Int,
    val deliveriesThisMonth: Int,
    val confirmedDeliveriesThisMonth: Int,
    val totalCampaigns: Int,
    val activeCampaigns: Int,
    val totalDonations: Double,
    val movementsThisMonth: Int,
    val entriesThisMonth: Int,
    val exitsThisMonth: Int
)

/**
 * Relatório de Inventário
 */
data class InventoryReport(
    val totalProducts: Int,
    val totalStock: Double,
    val totalValue: Double,
    val productsByCategory: Map<String, Int>,
    val stockByCategory: Map<String, Double>,
    val valueByCategory: Map<String, Double>,
    val topProducts: List<ProductStockInfo>,
    val lowStockProducts: List<ProductStockInfo>,
    val totalMovements: Int,
    val entries: Int,
    val exits: Int,
    val adjustments: Int,
    val generatedAt: Date
)

data class ProductStockInfo(
    val id: String,
    val name: String,
    val category: String,
    val currentStock: Double,
    val unit: String
)

/**
 * Relatório de Entregas
 */
data class DeliveriesReport(
    val totalDeliveries: Int,
    val scheduled: Int,
    val confirmed: Int,
    val cancelled: Int,
    val deliveriesByBeneficiary: Map<String, Int>,
    val deliveriesByKit: Map<String, Int>,
    val deliveriesByMonth: Map<String, Int>,
    val generatedAt: Date
)

/**
 * Relatório de Campanhas
 */
data class CampaignsReport(
    val totalCampaigns: Int,
    val draft: Int,
    val active: Int,
    val completed: Int,
    val totalGoal: Double,
    val totalRaised: Double,
    val totalDonations: Int,
    val averageDonation: Double,
    val topCampaigns: List<CampaignPerformance>,
    val donationsByMonth: Map<String, Double>,
    val generatedAt: Date
)

data class CampaignPerformance(
    val id: String,
    val title: String,
    val goal: Double,
    val raised: Double,
    val progress: Int
)

/**
 * Relatório de Beneficiários
 */
data class BeneficiariesReport(
    val totalBeneficiaries: Int,
    val active: Int,
    val inactive: Int,
    val byCourse: Map<String, Int>,
    val byHouseholdSize: Map<String, Int>,
    val topBeneficiaries: List<BeneficiaryDeliveryCount>,
    val generatedAt: Date
)

data class BeneficiaryDeliveryCount(
    val id: String,
    val name: String,
    val deliveriesCount: Int
)
