package com.ipca.lojasocial.domain.usecase.delivery

import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

/**
 * Use case para obter solicitações de entrega pendentes de aprovação
 */
class GetPendingDeliveryRequestsUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    operator fun invoke(): Flow<Result<List<Delivery>>> =
        repository.getDeliveriesByStatus(DeliveryStatus.PENDING_APPROVAL)
}

/**
 * Use case para aprovar uma solicitação de entrega
 */
class ApproveDeliveryRequestUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    suspend operator fun invoke(
        deliveryId: String,
        userId: String
    ): Result<Unit> {

        if (deliveryId.isBlank()) {
            return Result.Error(Exception("ID da entrega inválido"))
        }

        if (userId.isBlank()) {
            return Result.Error(Exception("ID do utilizador inválido"))
        }

        // Validar que a entrega existe e está em estado correto
        when (val deliveryResult = repository.getDeliveryById(deliveryId)) {
            is Result.Success -> {
                if (deliveryResult.data.status != DeliveryStatus.PENDING_APPROVAL) {
                    return Result.Error(
                        Exception("Apenas solicitações pendentes podem ser aprovadas")
                    )
                }
            }
            else -> return Result.Error(Exception("Solicitação não encontrada"))
        }

        // Aprovar a solicitação (muda status para APPROVED)
        return repository.approveDeliveryRequest(
            deliveryId = deliveryId,
            userId = userId
        )
    }
}

/**
 * Use case para rejeitar uma solicitação de entrega
 */
class RejectDeliveryRequestUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    suspend operator fun invoke(
        deliveryId: String,
        userId: String,
        reason: String
    ): Result<Unit> {

        if (deliveryId.isBlank()) {
            return Result.Error(Exception("ID da entrega inválido"))
        }

        if (userId.isBlank()) {
            return Result.Error(Exception("ID do utilizador inválido"))
        }

        if (reason.isBlank()) {
            return Result.Error(Exception("Motivo da rejeição é obrigatório"))
        }

        // Validar que a entrega existe e está em estado correto
        when (val deliveryResult = repository.getDeliveryById(deliveryId)) {
            is Result.Success -> {
                if (deliveryResult.data.status != DeliveryStatus.PENDING_APPROVAL) {
                    return Result.Error(
                        Exception("Apenas solicitações pendentes podem ser rejeitadas")
                    )
                }
            }
            else -> return Result.Error(Exception("Solicitação não encontrada"))
        }

        // Rejeitar a solicitação
        return repository.rejectDeliveryRequest(
            deliveryId = deliveryId,
            userId = userId,
            reason = reason
        )
    }
}

/**
 * Use case para agendar uma entrega aprovada
 */
class ScheduleDeliveryUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    suspend operator fun invoke(
        deliveryId: String,
        scheduledDate: Date,
        notes: String,
        userId: String
    ): Result<Unit> {

        if (deliveryId.isBlank()) {
            return Result.Error(Exception("ID da entrega inválido"))
        }

        if (userId.isBlank()) {
            return Result.Error(Exception("ID do utilizador inválido"))
        }

        // Validar que a data não é no passado
        if (scheduledDate.before(Date())) {
            return Result.Error(Exception("Data de entrega não pode ser no passado"))
        }

        // Validar que a entrega existe e está aprovada
        when (val deliveryResult = repository.getDeliveryById(deliveryId)) {
            is Result.Success -> {
                if (deliveryResult.data.status != DeliveryStatus.APPROVED) {
                    return Result.Error(
                        Exception("Apenas entregas aprovadas podem ser agendadas")
                    )
                }
            }
            else -> return Result.Error(Exception("Entrega não encontrada"))
        }

        // Agendar a entrega
        return repository.scheduleDelivery(
            deliveryId = deliveryId,
            scheduledDate = scheduledDate,
            notes = notes,
            userId = userId
        )
    }
}
