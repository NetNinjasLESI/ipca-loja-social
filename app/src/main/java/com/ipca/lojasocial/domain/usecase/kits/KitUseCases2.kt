package com.ipca.lojasocial.domain.usecase.kit

import com.ipca.lojasocial.data.repository.KitItemAvailability
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.KitRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case para verificar disponibilidade de stock do kit
 */
class CheckKitAvailabilityUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kitId: String): Result<Boolean> {
        if (kitId.isBlank()) {
            return Result.Error(Exception("ID do kit inválido"))
        }

        return repository.checkKitAvailability(kitId)
    }
}

/**
 * Use case para obter detalhes de disponibilidade do kit
 */
class GetKitAvailabilityDetailsUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(
        kitId: String
    ): Result<Map<String, KitItemAvailability>> {
        if (kitId.isBlank()) {
            return Result.Error(Exception("ID do kit inválido"))
        }

        return repository.getKitAvailabilityDetails(kitId)
    }
}

/**
 * Use case para validar se kit está ativo
 */
class ValidateKitActiveUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kitId: String): Result<Boolean> {
        if (kitId.isBlank()) {
            return Result.Error(Exception("ID do kit inválido"))
        }

        return when (val result = repository.getKitById(kitId)) {
            is Result.Success -> Result.Success(result.data.isActive)
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        }
    }
}

/**
 * Use case para toggle status do kit
 */
class ToggleKitStatusUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(
        kitId: String,
        isActive: Boolean
    ): Result<Unit> {
        if (kitId.isBlank()) {
            return Result.Error(Exception("ID do kit inválido"))
        }

        return when (val result = repository.getKitById(kitId)) {
            is Result.Success -> {
                val updated = result.data.copy(isActive = isActive)
                when (val updateResult = repository.updateKit(updated)) {
                    is Result.Success -> Result.Success(Unit)
                    is Result.Error -> Result.Error(updateResult.exception, updateResult.message)
                    is Result.Loading -> Result.Loading
                }
            }
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        }
    }
}

/**
 * Use case para obter estatísticas de kits
 */
class GetKitStatisticsUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(): Result<KitStatistics> {
        return try {
            when (val result = repository.getAllKits().first()) {
                is Result.Success -> {
                    val all = result.data
                    val active = all.filter { it.isActive }
                    val inactive = all.filter { !it.isActive }

                    // Contar total de itens em todos os kits
                    val totalItems = all.sumOf { it.items.size }

                    // Kit com mais produtos
                    val maxProducts = all.maxOfOrNull { it.items.size } ?: 0

                    Result.Success(
                        KitStatistics(
                            total = all.size,
                            active = active.size,
                            inactive = inactive.size,
                            totalItems = totalItems,
                            averageItemsPerKit = if (all.isNotEmpty()) {
                                totalItems.toDouble() / all.size
                            } else 0.0,
                            maxProductsInKit = maxProducts
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
 * Classe de dados para estatísticas de kits
 */
data class KitStatistics(
    val total: Int,
    val active: Int,
    val inactive: Int,
    val totalItems: Int,
    val averageItemsPerKit: Double,
    val maxProductsInKit: Int
)