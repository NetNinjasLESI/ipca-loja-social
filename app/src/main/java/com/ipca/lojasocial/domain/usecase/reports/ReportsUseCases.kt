package com.ipca.lojasocial.domain.usecase.reports

import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.ReportsRepository
import java.util.Date
import javax.inject.Inject

/**
 * Use case para obter estatísticas do dashboard
 */
class GetDashboardStatisticsUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(): Result<DashboardStatistics> {
        return repository.getDashboardStatistics()
    }
}

/**
 * Use case para gerar relatório de inventário
 */
class GetInventoryReportUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(
        startDate: Date? = null,
        endDate: Date? = null
    ): Result<InventoryReport> {
        return repository.getInventoryReport(startDate, endDate)
    }
}

/**
 * Use case para gerar relatório de entregas
 */
class GetDeliveriesReportUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(
        startDate: Date? = null,
        endDate: Date? = null
    ): Result<DeliveriesReport> {
        return repository.getDeliveriesReport(startDate, endDate)
    }
}

/**
 * Use case para gerar relatório de campanhas
 */
class GetCampaignsReportUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(): Result<CampaignsReport> {
        return repository.getCampaignsReport()
    }
}

/**
 * Use case para gerar relatório de beneficiários
 */
class GetBeneficiariesReportUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(): Result<BeneficiariesReport> {
        return repository.getBeneficiariesReport()
    }
}
