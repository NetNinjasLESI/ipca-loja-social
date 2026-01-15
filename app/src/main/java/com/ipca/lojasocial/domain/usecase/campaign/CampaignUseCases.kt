package com.ipca.lojasocial.domain.usecase.campaign

import com.ipca.lojasocial.domain.model.Campaign
import com.ipca.lojasocial.domain.model.CampaignStatus
import com.ipca.lojasocial.domain.model.ProductDonation
import com.ipca.lojasocial.domain.model.Donation
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject

/**
 * Use case para obter todas as campanhas
 */
class GetAllCampaignsUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    operator fun invoke(): Flow<Result<List<Campaign>>> {
        return repository.getAllCampaigns()
    }
}

/**
 * Use case para obter campanha por ID
 */
class GetCampaignByIdUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(campaignId: String): Result<Campaign> {
        if (campaignId.isBlank()) {
            return Result.Error(Exception("ID da campanha inválido"))
        }
        return repository.getCampaignById(campaignId)
    }
}

/**
 * Use case para criar nova campanha
 */
class CreateCampaignUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(campaign: Campaign): Result<Campaign> {
        if (campaign.title.isBlank()) {
            return Result.Error(Exception("Título é obrigatório"))
        }

        if (campaign.description.isBlank()) {
            return Result.Error(Exception("Descrição é obrigatória"))
        }

        if (campaign.endDate.before(campaign.startDate)) {
            return Result.Error(
                Exception("Data de fim deve ser posterior à data de início")
            )
        }

        if (campaign.endDate.before(Date())) {
            return Result.Error(
                Exception("Data de fim não pode ser no passado")
            )
        }

        return repository.createCampaign(campaign)
    }
}

/**
 * Use case para atualizar campanha
 */
class UpdateCampaignUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(campaign: Campaign): Result<Unit> {
        if (campaign.id.isBlank()) {
            return Result.Error(Exception("ID da campanha inválido"))
        }

        if (campaign.title.isBlank()) {
            return Result.Error(Exception("Título é obrigatório"))
        }

        if (campaign.description.isBlank()) {
            return Result.Error(Exception("Descrição é obrigatória"))
        }

        return repository.updateCampaign(campaign)
    }
}

/**
 * Use case para eliminar campanha
 */
class DeleteCampaignUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(campaignId: String): Result<Unit> {
        if (campaignId.isBlank()) {
            return Result.Error(Exception("ID da campanha inválido"))
        }

        when (val result = repository.getCampaignById(campaignId)) {
            is Result.Success -> {
                if (result.data.status == CampaignStatus.ACTIVE) {
                    return Result.Error(
                        Exception(
                            "Não é possível eliminar campanha ativa. " +
                                    "Desative primeiro."
                        )
                    )
                }
            }

            is Result.Error -> {
                return Result.Error(Exception("Campanha não encontrada"))
            }

            else -> {}
        }

        return repository.deleteCampaign(campaignId)
    }
}

/**
 * Use case para ativar campanha
 */
class ActivateCampaignUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(campaignId: String): Result<Unit> {
        if (campaignId.isBlank()) {
            return Result.Error(Exception("ID da campanha inválido"))
        }

        when (val result = repository.getCampaignById(campaignId)) {
            is Result.Success -> {
                val campaign = result.data

                if (campaign.status == CampaignStatus.ACTIVE) {
                    return Result.Error(Exception("Campanha já está ativa"))
                }

                if (campaign.status == CampaignStatus.COMPLETED) {
                    return Result.Error(
                        Exception("Campanha já foi concluída")
                    )
                }

                if (campaign.endDate.before(Date())) {
                    return Result.Error(
                        Exception("Campanha expirou. Atualize a data de fim.")
                    )
                }
            }

            is Result.Error -> {
                return Result.Error(Exception("Campanha não encontrada"))
            }

            else -> {}
        }

        return repository.activateCampaign(campaignId)
    }
}

/**
 * Use case para completar campanha
 */
class CompleteCampaignUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(campaignId: String): Result<Unit> {
        if (campaignId.isBlank()) {
            return Result.Error(Exception("ID da campanha inválido"))
        }

        return repository.completeCampaign(campaignId)
    }
}

/**
 * Use case para obter campanhas ativas
 */
class GetActiveCampaignsUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    operator fun invoke(): Flow<Result<List<Campaign>>> {
        return repository.getActiveCampaigns()
    }
}

/**
 * Use case para obter doações de uma campanha
 */
class GetProductDonationsByCampaignUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    operator fun invoke(campaignId: String): Flow<Result<List<ProductDonation>>> {
        if (campaignId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Result.Error(Exception("ID da campanha inválido")))
            }
        }
        return repository.getProductDonationsByCampaign(campaignId)
    }
}

/**
 * Use case para pesquisar campanhas
 */
class SearchCampaignsUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    operator fun invoke(query: String): Flow<Result<List<Campaign>>> {
        if (query.isBlank()) {
            return repository.getAllCampaigns()
        }
        return repository.searchCampaigns(query)
    }
}

/**
 * ✅ Use case para obter estatísticas SIMPLIFICADAS (SEM valores monetários)
 */
class GetCampaignStatisticsUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(): Result<CampaignStatistics> {
        return try {
            when (val result = repository.getAllCampaigns().first()) {
                is Result.Success -> {
                    val campaigns = result.data

                    val active = campaigns.count { it.status == CampaignStatus.ACTIVE }
                    val draft = campaigns.count { it.status == CampaignStatus.DRAFT }
                    val completed = campaigns.count { it.status == CampaignStatus.COMPLETED }

                    Result.Success(
                        CampaignStatistics(
                            total = campaigns.size,
                            active = active,
                            draft = draft,
                            completed = completed
                        )
                    )
                }

                is Result.Error -> result
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao calcular estatísticas")
        }
    }
}

/**
 * ✅ Classe de dados para estatísticas SIMPLIFICADAS
 */
data class CampaignStatistics(
    val total: Int,
    val active: Int,
    val draft: Int,
    val completed: Int
)

/**
 * Use case para adicionar doação MONETÁRIA (COMPATIBILIDADE - legacy)
 */
class AddDonationUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(
        campaignId: String,
        amount: Double,
        donorName: String?,
        donorEmail: String?
    ): Result<Unit> {
        if (campaignId.isBlank()) {
            return Result.Error(Exception("ID da campanha inválido"))
        }

        if (amount <= 0) {
            return Result.Error(Exception("Valor deve ser maior que zero"))
        }

        when (val result = repository.getCampaignById(campaignId)) {
            is Result.Success -> {
                if (result.data.status != CampaignStatus.ACTIVE) {
                    return Result.Error(
                        Exception("Campanha não está ativa")
                    )
                }
            }

            is Result.Error -> {
                return Result.Error(Exception("Campanha não encontrada"))
            }

            else -> {}
        }

        return (repository as? com.ipca.lojasocial.data.repository.CampaignRepositoryImpl)
            ?.addDonation(campaignId, amount, donorName, donorEmail)
            ?: Result.Error(Exception("Método não suportado"))
    }
}

/**
 * Use case para obter doações MONETÁRIAS (COMPATIBILIDADE - legacy)
 */
class GetDonationsByCampaignUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    operator fun invoke(campaignId: String): Flow<Result<List<Donation>>> {
        if (campaignId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Result.Error(Exception("ID da campanha inválido")))
            }
        }
        return (repository as? com.ipca.lojasocial.data.repository.CampaignRepositoryImpl)
            ?.getDonationsByCampaign(campaignId)
            ?: kotlinx.coroutines.flow.flow {
                emit(Result.Error(Exception("Método não suportado")))
            }
    }
}
