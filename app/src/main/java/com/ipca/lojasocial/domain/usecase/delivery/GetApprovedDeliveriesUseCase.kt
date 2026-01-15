package com.ipca.lojasocial.domain.usecase.delivery

import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para obter entregas aprovadas aguardando agendamento
 */
class GetApprovedDeliveriesUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    operator fun invoke(): Flow<Result<List<Delivery>>> =
        repository.getDeliveriesByStatus(DeliveryStatus.APPROVED)
}