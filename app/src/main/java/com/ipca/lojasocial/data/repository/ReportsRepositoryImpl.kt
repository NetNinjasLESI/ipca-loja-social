package com.ipca.lojasocial.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.ReportsRepository
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

/**
 * Implementação do ReportsRepository usando Firestore
 */
class ReportsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReportsRepository {

    override suspend fun getDashboardStatistics(): Result<DashboardStatistics> {
        return try {
            // Buscar dados de todos os módulos
            val products = firestore.collection("products").get().await()
            val beneficiaries = firestore.collection("beneficiaries").get().await()
            val kits = firestore.collection("kits").get().await()
            val deliveries = firestore.collection("deliveries").get().await()
            val campaigns = firestore.collection("campaigns").get().await()
            val stockMovements = firestore.collection("stock_movements").get().await()

            // Calcular total de stock
            val totalStock = products.documents.sumOf { 
                it.getDouble("currentStock") ?: 0.0 
            }

            // Calcular valor total de stock
            val totalStockValue = products.documents.sumOf { doc ->
                val stock = doc.getDouble("currentStock") ?: 0.0
                val price = doc.getDouble("unitPrice") ?: 0.0
                stock * price
            }

            // Produtos em falta (stock < minStock)
            val lowStockCount = products.documents.count { doc ->
                val current = doc.getDouble("currentStock") ?: 0.0
                val min = doc.getDouble("minStock") ?: 0.0
                current < min
            }

            // Beneficiários ativos
            val activeBeneficiaries = beneficiaries.documents.count {
                it.getBoolean("isActive") == true
            }

            // Entregas este mês
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            val startOfMonth = calendar.time

            val deliveriesThisMonth = deliveries.documents.count { doc ->
                val date = doc.getDate("scheduledDate")
                date != null && date.after(startOfMonth)
            }

            // Entregas confirmadas este mês
            val confirmedDeliveriesThisMonth = deliveries.documents.count { doc ->
                val date = doc.getDate("confirmedDate")
                val status = doc.getString("status")
                status == "CONFIRMED" && date != null && date.after(startOfMonth)
            }

            // Campanhas ativas
            val activeCampaigns = campaigns.documents.count {
                it.getString("status") == "ACTIVE"
            }

            // Total angariado em campanhas
            val totalDonations = campaigns.documents.sumOf {
                it.getDouble("currentAmount") ?: 0.0
            }

            // Movimentações este mês
            val movementsThisMonth = stockMovements.documents.count { doc ->
                val date = doc.getDate("date")
                date != null && date.after(startOfMonth)
            }

            // Entradas vs Saídas este mês
            val entriesThisMonth = stockMovements.documents.count { doc ->
                val date = doc.getDate("date")
                val type = doc.getString("type")
                type == "ENTRY" && date != null && date.after(startOfMonth)
            }

            val exitsThisMonth = stockMovements.documents.count { doc ->
                val date = doc.getDate("date")
                val type = doc.getString("type")
                type == "EXIT" && date != null && date.after(startOfMonth)
            }

            Result.Success(
                DashboardStatistics(
                    totalProducts = products.size(),
                    totalStock = totalStock,
                    totalStockValue = totalStockValue,
                    lowStockProducts = lowStockCount,
                    totalBeneficiaries = beneficiaries.size(),
                    activeBeneficiaries = activeBeneficiaries,
                    totalKits = kits.size(),
                    activeKits = kits.documents.count { it.getBoolean("isActive") == true },
                    totalDeliveries = deliveries.size(),
                    deliveriesThisMonth = deliveriesThisMonth,
                    confirmedDeliveriesThisMonth = confirmedDeliveriesThisMonth,
                    totalCampaigns = campaigns.size(),
                    activeCampaigns = activeCampaigns,
                    totalDonations = totalDonations,
                    movementsThisMonth = movementsThisMonth,
                    entriesThisMonth = entriesThisMonth,
                    exitsThisMonth = exitsThisMonth
                )
            )
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter estatísticas: ${e.message}")
        }
    }

    override suspend fun getInventoryReport(
        startDate: Date?,
        endDate: Date?
    ): Result<InventoryReport> {
        return try {
            val products = firestore.collection("products").get().await()
            val movements = if (startDate != null && endDate != null) {
                firestore.collection("stock_movements")
                    .whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThanOrEqualTo("date", endDate)
                    .get()
                    .await()
            } else {
                firestore.collection("stock_movements").get().await()
            }

            // Produtos por categoria
            val productsByCategory = products.documents
                .groupBy { it.getString("category") ?: "Sem Categoria" }
                .mapValues { it.value.size }

            // Stock por categoria
            val stockByCategory = products.documents
                .groupBy { it.getString("category") ?: "Sem Categoria" }
                .mapValues { entries ->
                    entries.value.sumOf { it.getDouble("currentStock") ?: 0.0 }
                }

            // Valor por categoria
            val valueByCategory = products.documents
                .groupBy { it.getString("category") ?: "Sem Categoria" }
                .mapValues { entries ->
                    entries.value.sumOf { doc ->
                        val stock = doc.getDouble("currentStock") ?: 0.0
                        val price = doc.getDouble("unitPrice") ?: 0.0
                        stock * price
                    }
                }

            // Top produtos por stock
            val topProducts = products.documents
                .sortedByDescending { it.getDouble("currentStock") ?: 0.0 }
                .take(10)
                .map { doc ->
                    ProductStockInfo(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        currentStock = doc.getDouble("currentStock") ?: 0.0,
                        unit = doc.getString("unit") ?: ""
                    )
                }

            // Produtos em falta
            val lowStockProducts = products.documents
                .filter { doc ->
                    val current = doc.getDouble("currentStock") ?: 0.0
                    val min = doc.getDouble("minStock") ?: 0.0
                    current < min
                }
                .map { doc ->
                    ProductStockInfo(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        currentStock = doc.getDouble("currentStock") ?: 0.0,
                        unit = doc.getString("unit") ?: ""
                    )
                }

            // Movimentações por tipo
            val entriesCount = movements.documents.count { 
                it.getString("type") == "ENTRY" 
            }
            val exitsCount = movements.documents.count { 
                it.getString("type") == "EXIT" 
            }
            val adjustmentsCount = movements.documents.count { 
                it.getString("type") == "ADJUSTMENT" 
            }

            Result.Success(
                InventoryReport(
                    totalProducts = products.size(),
                    totalStock = products.documents.sumOf { 
                        it.getDouble("currentStock") ?: 0.0 
                    },
                    totalValue = products.documents.sumOf { doc ->
                        val stock = doc.getDouble("currentStock") ?: 0.0
                        val price = doc.getDouble("unitPrice") ?: 0.0
                        stock * price
                    },
                    productsByCategory = productsByCategory,
                    stockByCategory = stockByCategory,
                    valueByCategory = valueByCategory,
                    topProducts = topProducts,
                    lowStockProducts = lowStockProducts,
                    totalMovements = movements.size(),
                    entries = entriesCount,
                    exits = exitsCount,
                    adjustments = adjustmentsCount,
                    generatedAt = Date()
                )
            )
        } catch (e: Exception) {
            Result.Error(e, "Erro ao gerar relatório de inventário: ${e.message}")
        }
    }

    override suspend fun getDeliveriesReport(
        startDate: Date?,
        endDate: Date?
    ): Result<DeliveriesReport> {
        return try {
            val deliveries = if (startDate != null && endDate != null) {
                firestore.collection("deliveries")
                    .whereGreaterThanOrEqualTo("scheduledDate", startDate)
                    .whereLessThanOrEqualTo("scheduledDate", endDate)
                    .get()
                    .await()
            } else {
                firestore.collection("deliveries").get().await()
            }

            // Por status
            val scheduled = deliveries.documents.count { 
                it.getString("status") == "SCHEDULED" 
            }
            val confirmed = deliveries.documents.count { 
                it.getString("status") == "CONFIRMED" 
            }
            val cancelled = deliveries.documents.count { 
                it.getString("status") == "CANCELLED" 
            }

            // Por beneficiário (top 10)
            val deliveriesByBeneficiary = deliveries.documents
                .groupBy { it.getString("beneficiaryName") ?: "Desconhecido" }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(10)
                .toMap()

            // Por kit (top 10)
            val deliveriesByKit = deliveries.documents
                .groupBy { it.getString("kitName") ?: "Desconhecido" }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(10)
                .toMap()

            // Por mês
            val deliveriesByMonth = deliveries.documents
                .groupBy { doc ->
                    val date = doc.getDate("scheduledDate")
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
                    } else "Desconhecido"
                }
                .mapValues { it.value.size }

            Result.Success(
                DeliveriesReport(
                    totalDeliveries = deliveries.size(),
                    scheduled = scheduled,
                    confirmed = confirmed,
                    cancelled = cancelled,
                    deliveriesByBeneficiary = deliveriesByBeneficiary,
                    deliveriesByKit = deliveriesByKit,
                    deliveriesByMonth = deliveriesByMonth,
                    generatedAt = Date()
                )
            )
        } catch (e: Exception) {
            Result.Error(e, "Erro ao gerar relatório de entregas: ${e.message}")
        }
    }

    override suspend fun getCampaignsReport(): Result<CampaignsReport> {
        return try {
            val campaigns = firestore.collection("campaigns").get().await()
            val donations = firestore.collection("donations").get().await()

            // Por status
            val draft = campaigns.documents.count { 
                it.getString("status") == "DRAFT" 
            }
            val active = campaigns.documents.count { 
                it.getString("status") == "ACTIVE" 
            }
            val completed = campaigns.documents.count { 
                it.getString("status") == "COMPLETED" 
            }

            // Totais
            val totalGoal = campaigns.documents.sumOf { 
                it.getDouble("goalAmount") ?: 0.0 
            }
            val totalRaised = campaigns.documents.sumOf { 
                it.getDouble("currentAmount") ?: 0.0 
            }

            // Campanhas com melhor performance
            val topCampaigns = campaigns.documents
                .sortedByDescending { it.getDouble("currentAmount") ?: 0.0 }
                .take(10)
                .map { doc ->
                    CampaignPerformance(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        goal = doc.getDouble("goalAmount") ?: 0.0,
                        raised = doc.getDouble("currentAmount") ?: 0.0,
                        progress = if ((doc.getDouble("goalAmount") ?: 0.0) > 0) {
                            ((doc.getDouble("currentAmount") ?: 0.0) / 
                             (doc.getDouble("goalAmount") ?: 1.0) * 100).toInt()
                        } else 0
                    )
                }

            // Doações por mês
            val donationsByMonth = donations.documents
                .groupBy { doc ->
                    val date = doc.getDate("donatedAt")
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
                    } else "Desconhecido"
                }
                .mapValues { entries ->
                    entries.value.sumOf { it.getDouble("amount") ?: 0.0 }
                }

            Result.Success(
                CampaignsReport(
                    totalCampaigns = campaigns.size(),
                    draft = draft,
                    active = active,
                    completed = completed,
                    totalGoal = totalGoal,
                    totalRaised = totalRaised,
                    totalDonations = donations.size(),
                    averageDonation = if (donations.size() > 0) {
                        totalRaised / donations.size()
                    } else 0.0,
                    topCampaigns = topCampaigns,
                    donationsByMonth = donationsByMonth,
                    generatedAt = Date()
                )
            )
        } catch (e: Exception) {
            Result.Error(e, "Erro ao gerar relatório de campanhas: ${e.message}")
        }
    }

    override suspend fun getBeneficiariesReport(): Result<BeneficiariesReport> {
        return try {
            val beneficiaries = firestore.collection("beneficiaries").get().await()
            val deliveries = firestore.collection("deliveries").get().await()

            // Por status
            val active = beneficiaries.documents.count { 
                it.getBoolean("isActive") == true 
            }
            val inactive = beneficiaries.documents.count { 
                it.getBoolean("isActive") == false 
            }

            // Por curso
            val byCourse = beneficiaries.documents
                .groupBy { it.getString("course") ?: "Desconhecido" }
                .mapValues { it.value.size }

            // Por agregado familiar
            val byHouseholdSize = beneficiaries.documents
                .groupBy { 
                    val size = it.getLong("householdSize")?.toInt() ?: 0
                    when {
                        size == 1 -> "1 pessoa"
                        size in 2..3 -> "2-3 pessoas"
                        size in 4..5 -> "4-5 pessoas"
                        size > 5 -> "6+ pessoas"
                        else -> "Desconhecido"
                    }
                }
                .mapValues { it.value.size }

            // Beneficiários com mais entregas
            val deliveriesByBeneficiary = deliveries.documents
                .filter { it.getString("status") == "CONFIRMED" }
                .groupBy { it.getString("beneficiaryId") ?: "" }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(10)

            val topBeneficiaries = deliveriesByBeneficiary.mapNotNull { (id, count) ->
                val beneficiary = beneficiaries.documents.find { it.id == id }
                if (beneficiary != null) {
                    BeneficiaryDeliveryCount(
                        id = id,
                        name = beneficiary.getString("name") ?: "",
                        deliveriesCount = count
                    )
                } else null
            }

            Result.Success(
                BeneficiariesReport(
                    totalBeneficiaries = beneficiaries.size(),
                    active = active,
                    inactive = inactive,
                    byCourse = byCourse,
                    byHouseholdSize = byHouseholdSize,
                    topBeneficiaries = topBeneficiaries,
                    generatedAt = Date()
                )
            )
        } catch (e: Exception) {
            Result.Error(e, "Erro ao gerar relatório de beneficiários: ${e.message}")
        }
    }
}
