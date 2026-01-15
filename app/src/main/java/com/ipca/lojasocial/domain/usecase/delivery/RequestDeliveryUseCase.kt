package com.ipca.lojasocial.domain.usecase.delivery

import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.DeliveryRepository
import com.ipca.lojasocial.domain.repository.KitRepository
import com.ipca.lojasocial.domain.repository.BeneficiaryRepository
import java.util.*
import javax.inject.Inject

/**
 * Use case para beneficiário solicitar entrega
 */
class RequestDeliveryUseCase @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val kitRepository: KitRepository,
    private val beneficiaryRepository: BeneficiaryRepository
) {
    suspend operator fun invoke(
        beneficiaryId: String,
        kitId: String,
        requestNotes: String
    ): Result<Delivery> {

        if (beneficiaryId.isBlank()) {
            return Result.Error(Exception("Beneficiário inválido"))
        }

        if (kitId.isBlank()) {
            return Result.Error(Exception("Kit é obrigatório"))
        }

        // 1. Validar beneficiário
        val beneficiary = when (
            val result = beneficiaryRepository.getBeneficiaryById(beneficiaryId)
        ) {
            is Result.Success -> {
                if (!result.data.isActive) {
                    return Result.Error(Exception("Sua conta está inativa"))
                }
                result.data
            }
            else -> return Result.Error(Exception("Beneficiário não encontrado"))
        }

        // 2. Validar kit
        val kit = when (val result = kitRepository.getKitById(kitId)) {
            is Result.Success -> {
                if (!result.data.isActive) {
                    return Result.Error(Exception("Este kit não está disponível"))
                }
                result.data
            }
            else -> return Result.Error(Exception("Kit não encontrado"))
        }

        // 3. Criar solicitação
        val delivery = Delivery(
            beneficiaryId = beneficiaryId,
            beneficiaryName = beneficiary.name,
            kitId = kitId,
            kitName = kit.name,
            status = DeliveryStatus.PENDING_APPROVAL,
            requestedDate = Date(),
            requestNotes = requestNotes,
            createdAt = Date(),
            createdBy = beneficiaryId,
            updatedAt = Date()
        )

        return deliveryRepository.createDelivery(delivery)
    }
}