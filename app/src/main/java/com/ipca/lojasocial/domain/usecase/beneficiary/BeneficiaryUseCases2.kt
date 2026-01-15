package com.ipca.lojasocial.domain.usecase.beneficiary

import com.ipca.lojasocial.domain.model.Beneficiary
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.BeneficiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case para pesquisar beneficiários
 */
class SearchBeneficiariesUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    operator fun invoke(query: String): Flow<Result<List<Beneficiary>>> {
        if (query.isBlank()) {
            return repository.getAllBeneficiaries()
        }
        return repository.searchBeneficiaries(query)
    }
}

/**
 * Use case para obter beneficiários por curso
 */
class GetBeneficiariesByCourseUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    operator fun invoke(course: String): Flow<Result<List<Beneficiary>>> {
        if (course.isBlank()) {
            return repository.getAllBeneficiaries()
        }
        return repository.getBeneficiariesByCourse(course)
    }
}

/**
 * Use case para validar se beneficiário está ativo
 */
class ValidateBeneficiaryActiveUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    suspend operator fun invoke(beneficiaryId: String): Result<Boolean> {
        if (beneficiaryId.isBlank()) {
            return Result.Error(Exception("ID do beneficiário inválido"))
        }

        return when (val result = repository.getBeneficiaryById(beneficiaryId)) {
            is Result.Success -> Result.Success(result.data.isActive)
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        }
    }
}

/**
 * Use case para ativar/desativar beneficiário
 */
class ToggleBeneficiaryStatusUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    suspend operator fun invoke(
        beneficiaryId: String,
        isActive: Boolean
    ): Result<Unit> {
        if (beneficiaryId.isBlank()) {
            return Result.Error(Exception("ID do beneficiário inválido"))
        }

        return when (val result = repository.getBeneficiaryById(beneficiaryId)) {
            is Result.Success -> {
                val updated = result.data.copy(isActive = isActive)
                when (val updateResult = repository.updateBeneficiary(updated)) {
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
 * Use case para obter estatísticas de beneficiários
 */
class GetBeneficiaryStatisticsUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    suspend operator fun invoke(): Result<BeneficiaryStatistics> {
        return try {
            when (val result = repository.getAllBeneficiaries().first()) {
                is Result.Success -> {
                    val all = result.data
                    val active = all.filter { it.isActive }
                    val inactive = all.filter { !it.isActive }
                    val withSpecialNeeds = all.filter { it.hasSpecialNeeds }

                    // Agrupar por curso
                    val byCourse = all.groupBy { it.course }
                        .mapValues { entry -> entry.value.size }

                    // Agrupar por ano académico
                    val byAcademicYear = all.groupBy { it.academicYear }
                        .mapValues { entry -> entry.value.size }

                    Result.Success(
                        BeneficiaryStatistics(
                            total = all.size,
                            active = active.size,
                            inactive = inactive.size,
                            withSpecialNeeds = withSpecialNeeds.size,
                            byCourse = byCourse,
                            byAcademicYear = byAcademicYear
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
 * Classe de dados para estatísticas
 */
data class BeneficiaryStatistics(
    val total: Int,
    val active: Int,
    val inactive: Int,
    val withSpecialNeeds: Int,
    val byCourse: Map<String, Int>,
    val byAcademicYear: Map<Int, Int>
)