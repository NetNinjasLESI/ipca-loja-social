package com.ipca.lojasocial.domain.usecase.delivery

import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.DeliveryRepository
import com.ipca.lojasocial.domain.repository.KitRepository
import com.ipca.lojasocial.domain.repository.BeneficiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject

/**
 * Use case para obter todas as entregas
 */
class GetAllDeliveriesUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    operator fun invoke(): Flow<Result<List<Delivery>>> =
        repository.getAllDeliveries()
}

/**
 * Use case para obter entrega por ID
 */
class GetDeliveryByIdUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    suspend operator fun invoke(deliveryId: String): Result<Delivery> {
        if (deliveryId.isBlank()) {
            return Result.Error(Exception("ID da entrega inválido"))
        }
        return repository.getDeliveryById(deliveryId)
    }
}

/**
 * Use case para criar nova entrega
 */
class CreateDeliveryUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val kitRepository: KitRepository,
    private val beneficiaryRepository: BeneficiaryRepository
) {
    suspend operator fun invoke(delivery: Delivery): Result<Delivery> {

        // 🔴 CORREÇÃO CRÍTICA
        if (delivery.createdBy.isBlank()) {
            return Result.Error(Exception("Utilizador não autenticado"))
        }

        if (delivery.beneficiaryId.isBlank()) {
            return Result.Error(Exception("Beneficiário é obrigatório"))
        }

        if (delivery.kitId.isBlank()) {
            return Result.Error(Exception("Kit é obrigatório"))
        }

        // 1. Validar beneficiário
        val beneficiary = when (
            val result = beneficiaryRepository.getBeneficiaryById(delivery.beneficiaryId)
        ) {
            is Result.Success -> {
                if (!result.data.isActive) {
                    return Result.Error(Exception("Beneficiário está inativo"))
                }
                result.data
            }
            else -> return Result.Error(Exception("Beneficiário não encontrado"))
        }

        // 2. Validar kit
        val kit = when (val result = kitRepository.getKitById(delivery.kitId)) {
            is Result.Success -> {
                if (!result.data.isActive) {
                    return Result.Error(Exception("Kit está inativo"))
                }
                result.data
            }
            else -> return Result.Error(Exception("Kit não encontrado"))
        }

        // 3. Verificar stock
        when (val availability = kitRepository.checkKitAvailability(kit.id)) {
            is Result.Success -> {
                if (!availability.data) {
                    return Result.Error(Exception("Kit não disponível. Verifique o stock."))
                }
            }
            else -> return Result.Error(Exception("Erro ao verificar stock"))
        }

        // 4. Validar data
        if (delivery.scheduledDate.before(Date())) {
            return Result.Error(Exception("Data de entrega não pode ser no passado"))
        }

        // 5. Criar entrega final
        val finalDelivery = delivery.copy(
            beneficiaryName = beneficiary.name,
            kitName = kit.name,
            status = DeliveryStatus.SCHEDULED,
            createdAt = Date(),
            updatedAt = Date()
        )

        return deliveryRepository.createDelivery(finalDelivery)
    }
}

/**
 * Use case para confirmar entrega
 */
class ConfirmDeliveryUseCase @Inject constructor(
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

        when (val deliveryResult = repository.getDeliveryById(deliveryId)) {
            is Result.Success -> {
                if (deliveryResult.data.status != DeliveryStatus.SCHEDULED) {
                    return Result.Error(
                        Exception("Apenas entregas agendadas podem ser confirmadas")
                    )
                }
            }
            else -> return Result.Error(Exception("Entrega não encontrada"))
        }

        return repository.confirmDelivery(deliveryId, userId)
    }
}

/**
 * Use case para cancelar entrega
 */
class CancelDeliveryUseCase @Inject constructor(
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
            return Result.Error(Exception("Motivo de cancelamento é obrigatório"))
        }

        when (val deliveryResult = repository.getDeliveryById(deliveryId)) {
            is Result.Success -> {
                val status = deliveryResult.data.status
                if (status == DeliveryStatus.CANCELLED) {
                    return Result.Error(Exception("Entrega já está cancelada"))
                }
                if (status == DeliveryStatus.CONFIRMED) {
                    return Result.Error(
                        Exception("Entregas confirmadas não podem ser canceladas")
                    )
                }
            }
            else -> return Result.Error(Exception("Entrega não encontrada"))
        }

        return repository.cancelDelivery(deliveryId, userId, reason)
    }
}

/**
 * Use case para obter entregas por beneficiário
 */
class GetDeliveriesByBeneficiaryUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    operator fun invoke(beneficiaryId: String): Flow<Result<List<Delivery>>> =
        if (beneficiaryId.isBlank()) {
            flow { emit(Result.Error(Exception("ID do beneficiário inválido"))) }
        } else {
            repository.getDeliveriesByBeneficiary(beneficiaryId)
        }
}

/**
 * Use case para obter entregas por status
 */
class GetDeliveriesByStatusUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    operator fun invoke(status: DeliveryStatus): Flow<Result<List<Delivery>>> =
        repository.getDeliveriesByStatus(status)
}

/**
 * Use case para pesquisar entregas
 */
class SearchDeliveriesUseCase @Inject constructor(
    private val repository: DeliveryRepository
) {
    operator fun invoke(query: String): Flow<Result<List<Delivery>>> =
        if (query.isBlank()) repository.getAllDeliveries()
        else repository.searchDeliveries(query)
}
