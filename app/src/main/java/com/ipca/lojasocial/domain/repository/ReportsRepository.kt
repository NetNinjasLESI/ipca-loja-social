package com.ipca.lojasocial.domain.repository

import com.ipca.lojasocial.domain.model.BeneficiariesReport
import com.ipca.lojasocial.domain.model.CampaignsReport
import com.ipca.lojasocial.domain.model.DashboardStatistics
import com.ipca.lojasocial.domain.model.DeliveriesReport
import com.ipca.lojasocial.domain.model.InventoryReport
import com.ipca.lojasocial.domain.model.Result
import java.util.Date

/**
 * Interface do repositório de relatórios
 */
interface ReportsRepository {

    /**
     * Obter estatísticas do dashboard
     */
    suspend fun getDashboardStatistics(): Result<DashboardStatistics>

    /**
     * Gerar relatório de inventário
     */
    suspend fun getInventoryReport(
        startDate: Date? = null,
        endDate: Date? = null
    ): Result<InventoryReport>

    /**
     * Gerar relatório de entregas
     */
    suspend fun getDeliveriesReport(
        startDate: Date? = null,
        endDate: Date? = null
    ): Result<DeliveriesReport>

    /**
     * Gerar relatório de campanhas
     */
    suspend fun getCampaignsReport(): Result<CampaignsReport>

    /**
     * Gerar relatório de beneficiários
     */
    suspend fun getBeneficiariesReport(): Result<BeneficiariesReport>
}