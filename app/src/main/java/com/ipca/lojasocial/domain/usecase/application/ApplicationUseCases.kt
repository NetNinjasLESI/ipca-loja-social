package com.ipca.lojasocial.domain.usecase.application

import com.ipca.lojasocial.domain.model.ApplicationStatus
import com.ipca.lojasocial.domain.model.BeneficiaryApplication
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.ApplicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para obter todas as candidaturas
 */
class GetAllApplicationsUseCase @Inject constructor(
    private val repository: ApplicationRepository
) {
    operator fun invoke(): Flow<Result<List<BeneficiaryApplication>>> {
        return repository.getAllApplications()
    }
}

/**
 * Use case para obter candidatura por ID
 */
class GetApplicationByIdUseCase @Inject constructor(
    private val repository: ApplicationRepository
) {
    suspend operator fun invoke(id: String): Result<BeneficiaryApplication> {
        if (id.isBlank()) {
            return Result.Error(Exception("ID da candidatura inválido"))
        }
        return repository.getApplicationById(id)
    }
}

/**
 * Use case para obter candidatura por user ID
 */
class GetApplicationByUserIdUseCase @Inject constructor(
    private val repository: ApplicationRepository
) {
    suspend operator fun invoke(userId: String): Result<BeneficiaryApplication> {
        if (userId.isBlank()) {
            return Result.Error(Exception("ID do utilizador inválido"))
        }
        return repository.getApplicationByUserId(userId)
    }
}

/**
 * Use case para obter candidaturas por status
 */
class GetApplicationsByStatusUseCase @Inject constructor(
    private val repository: ApplicationRepository
) {
    operator fun invoke(status: ApplicationStatus): Flow<Result<List<BeneficiaryApplication>>> {
        return repository.getApplicationsByStatus(status)
    }
}

/**
 * Use case para criar nova candidatura
 */
class CreateApplicationUseCase @Inject constructor(
    private val repository: ApplicationRepository
) {
    suspend operator fun invoke(application: BeneficiaryApplication): Result<BeneficiaryApplication> {
        // Validations
        if (application.userId.isBlank()) {
            return Result.Error(Exception("Utilizador não autenticado"))
        }

        if (application.studentNumber.isBlank()) {
            return Result.Error(Exception("Número de estudante é obrigatório"))
        }

        if (application.phone.isBlank()) {
            return Result.Error(Exception("Telefone é obrigatório"))
        }

        if (application.nif.isBlank()) {
            return Result.Error(Exception("NIF é obrigatório"))
        }

        if (application.course.isBlank()) {
            return Result.Error(Exception("Curso é obrigatório"))
        }

        if (application.academicYear < 1 || application.academicYear > 5) {
            return Result.Error(Exception("Ano académico inválido (1-5)"))
        }

        if (application.address.isBlank()) {
            return Result.Error(Exception("Morada é obrigatória"))
        }

        if (application.city.isBlank()) {
            return Result.Error(Exception("Cidade é obrigatória"))
        }

        if (application.familySize < 1) {
            return Result.Error(Exception("Agregado familiar deve ser >= 1"))
        }

        if (application.monthlyIncome < 0) {
            return Result.Error(Exception("Rendimento mensal não pode ser negativo"))
        }

        // Check if user already has an application
        when (val existingResult = repository.hasExistingApplication(application.userId)) {
            is Result.Success -> {
                if (existingResult.data) {
                    return Result.Error(Exception("Já tens uma candidatura submetida"))
                }
            }
            is Result.Error -> {
                return Result.Error(Exception("Erro ao verificar candidatura existente"))
            }
            else -> {}
        }

        return repository.createApplication(application)
    }
}

/**
 * Use case para aprovar candidatura
 */
class ApproveApplicationUseCase @Inject constructor(
    private val repository: ApplicationRepository
) {
    suspend operator fun invoke(
        applicationId: String,
        reviewedBy: String,
        reviewedByName: String
    ): Result<Unit> {
        if (applicationId.isBlank()) {
            return Result.Error(Exception("ID da candidatura inválido"))
        }

        if (reviewedBy.isBlank()) {
            return Result.Error(Exception("Revisor não identificado"))
        }

        // Verify application exists and is pending
        when (val appResult = repository.getApplicationById(applicationId)) {
            is Result.Success -> {
                if (appResult.data.status != ApplicationStatus.PENDING) {
                    return Result.Error(Exception("Apenas candidaturas pendentes podem ser aprovadas"))
                }
            }
            is Result.Error -> {
                return Result.Error(Exception("Candidatura não encontrada"))
            }
            else -> {}
        }

        return repository.approveApplication(applicationId, reviewedBy, reviewedByName)
    }
}

/**
 * Use case para rejeitar candidatura
 */
class RejectApplicationUseCase @Inject constructor(
    private val repository: ApplicationRepository
) {
    suspend operator fun invoke(
        applicationId: String,
        reviewedBy: String,
        reviewedByName: String,
        reason: String
    ): Result<Unit> {
        if (applicationId.isBlank()) {
            return Result.Error(Exception("ID da candidatura inválido"))
        }

        if (reviewedBy.isBlank()) {
            return Result.Error(Exception("Revisor não identificado"))
        }

        if (reason.isBlank()) {
            return Result.Error(Exception("Motivo de rejeição é obrigatório"))
        }

        // Verify application exists and is pending
        when (val appResult = repository.getApplicationById(applicationId)) {
            is Result.Success -> {
                if (appResult.data.status != ApplicationStatus.PENDING) {
                    return Result.Error(Exception("Apenas candidaturas pendentes podem ser rejeitadas"))
                }
            }
            is Result.Error -> {
                return Result.Error(Exception("Candidatura não encontrada"))
            }
            else -> {}
        }

        return repository.rejectApplication(applicationId, reviewedBy, reviewedByName, reason)
    }
}

/**
 * Use case para verificar se utilizador já tem candidatura
 */
class HasExistingApplicationUseCase @Inject constructor(
    private val repository: ApplicationRepository
) {
    suspend operator fun invoke(userId: String): Result<Boolean> {
        if (userId.isBlank()) {
            return Result.Error(Exception("ID do utilizador inválido"))
        }
        return repository.hasExistingApplication(userId)
    }
}
