package com.ipca.lojasocial.domain.usecase.delivery

import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject

/**
 * Use case para obter estatísticas de entregas
 */
class GetDeliveryStatisticsUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    suspend operator fun invoke(): Result<DeliveryStatistics> {
        return try {
            when (val result = repository.getAllDeliveries().first()) {
                is Result.Success -> {
                    val all = result.data
                    val scheduled = all.count { it.status == DeliveryStatus.SCHEDULED }
                    val confirmed = all.count { it.status == DeliveryStatus.CONFIRMED }
                    val cancelled = all.count { it.status == DeliveryStatus.CANCELLED }

                    // Entregas por beneficiário
                    val byBeneficiary = all.groupBy { it.beneficiaryId }
                        .mapValues { entry -> entry.value.size }

                    // Entregas por kit
                    val byKit = all.groupBy { it.kitId }
                        .mapValues { entry -> entry.value.size }

                    // Entregas hoje
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time

                    val tomorrow = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time

                    val scheduledToday = all.count {
                        it.status == DeliveryStatus.SCHEDULED &&
                                it.scheduledDate.after(today) &&
                                it.scheduledDate.before(tomorrow)
                    }

                    Result.Success(
                        DeliveryStatistics(
                            total = all.size,
                            scheduled = scheduled,
                            confirmed = confirmed,
                            cancelled = cancelled,
                            scheduledToday = scheduledToday,
                            topBeneficiary = byBeneficiary.maxByOrNull { entry -> entry.value },
                            topKit = byKit.maxByOrNull { entry -> entry.value }
                        )
                    )
                }
                is Result.Error -> Result.Error(result.exception, result.message)
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao calcular estatísticas")
        }
    }
}

/**
 * Classe de dados para estatísticas de entregas
 */
data class DeliveryStatistics(
    val total: Int,
    val scheduled: Int,
    val confirmed: Int,
    val cancelled: Int,
    val scheduledToday: Int,
    val topBeneficiary: Map.Entry<String, Int>?,
    val topKit: Map.Entry<String, Int>?
)

/**
 * Use case para validar se entrega pode ser confirmada
 */
class ValidateDeliveryConfirmationUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    suspend operator fun invoke(deliveryId: String): Result<Boolean> {
        if (deliveryId.isBlank()) {
            return Result.Error(Exception("ID da entrega inválido"))
        }

        return when (val result = repository.getDeliveryById(deliveryId)) {
            is Result.Success -> {
                val delivery = result.data
                val canConfirm = delivery.status == DeliveryStatus.SCHEDULED
                Result.Success(canConfirm)
            }
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        }
    }
}

/**
 * Use case para obter próximas entregas (próximos 7 dias)
 */
class GetUpcomingDeliveriesUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    suspend operator fun invoke(): Result<List<com.ipca.lojasocial.domain.model.Delivery>> {
        return try {
            when (val result = repository.getAllDeliveries().first()) {
                is Result.Success -> {
                    val now = Date()
                    val sevenDaysLater = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 7)
                    }.time

                    val upcoming = result.data.filter {
                        it.status == DeliveryStatus.SCHEDULED &&
                                it.scheduledDate.after(now) &&
                                it.scheduledDate.before(sevenDaysLater)
                    }.sortedBy { it.scheduledDate }

                    Result.Success(upcoming)
                }
                is Result.Error -> Result.Error(result.exception, result.message)
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter próximas entregas")
        }
    }
}