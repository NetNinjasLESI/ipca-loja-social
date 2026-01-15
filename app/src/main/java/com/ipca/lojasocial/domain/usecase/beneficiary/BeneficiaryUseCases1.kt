package com.ipca.lojasocial.domain.usecase.beneficiary

import com.ipca.lojasocial.domain.model.Beneficiary
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.BeneficiaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para obter todos os beneficiários
 */
class GetAllBeneficiariesUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    operator fun invoke(): Flow<Result<List<Beneficiary>>> {
        return repository.getAllBeneficiaries()
    }
}

/**
 * Use case para obter beneficiários ativos
 */
class GetActiveBeneficiariesUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    operator fun invoke(): Flow<Result<List<Beneficiary>>> {
        return repository.getActiveBeneficiaries()
    }
}

/**
 * Use case para obter beneficiário por ID
 */
class GetBeneficiaryByIdUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    suspend operator fun invoke(beneficiaryId: String): Result<Beneficiary> {
        if (beneficiaryId.isBlank()) {
            return Result.Error(Exception("ID do beneficiário inválido"))
        }
        return repository.getBeneficiaryById(beneficiaryId)
    }
}

/**
 * Use case para obter beneficiário por user ID
 */
class GetBeneficiaryByUserIdUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    suspend operator fun invoke(userId: String): Result<Beneficiary> {
        if (userId.isBlank()) {
            return Result.Error(Exception("ID do utilizador inválido"))
        }
        return repository.getBeneficiaryByUserId(userId)
    }
}

/**
 * Use case para obter beneficiário por número de estudante
 */
class GetBeneficiaryByStudentNumberUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    suspend operator fun invoke(studentNumber: String): Result<Beneficiary> {
        if (studentNumber.isBlank()) {
            return Result.Error(Exception("Número de estudante inválido"))
        }
        return repository.getBeneficiaryByStudentNumber(studentNumber)
    }
}

/**
 * Use case para criar novo beneficiário
 */
class CreateBeneficiaryUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    suspend operator fun invoke(beneficiary: Beneficiary): Result<Beneficiary> {
        // Validações
        if (beneficiary.studentNumber.isBlank()) {
            return Result.Error(Exception("Número de estudante é obrigatório"))
        }

        if (beneficiary.name.isBlank()) {
            return Result.Error(Exception("Nome é obrigatório"))
        }

        if (beneficiary.email.isBlank()) {
            return Result.Error(Exception("Email é obrigatório"))
        }

        if (!isValidEmail(beneficiary.email)) {
            return Result.Error(Exception("Email inválido"))
        }

        if (beneficiary.phone.isBlank()) {
            return Result.Error(Exception("Telefone é obrigatório"))
        }

        if (beneficiary.course.isBlank()) {
            return Result.Error(Exception("Curso é obrigatório"))
        }

        if (beneficiary.academicYear < 1 || beneficiary.academicYear > 5) {
            return Result.Error(Exception("Ano académico inválido (1-5)"))
        }

        if (beneficiary.familySize < 1) {
            return Result.Error(Exception("Agregado familiar deve ser >= 1"))
        }

        if (beneficiary.monthlyIncome < 0) {
            return Result.Error(Exception("Rendimento mensal não pode ser negativo"))
        }

        return repository.createBeneficiary(beneficiary)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

/**
 * Use case para atualizar beneficiário
 */
class UpdateBeneficiaryUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    suspend operator fun invoke(beneficiary: Beneficiary): Result<Beneficiary> {
        // Validações
        if (beneficiary.id.isBlank()) {
            return Result.Error(Exception("ID do beneficiário é obrigatório"))
        }

        if (beneficiary.name.isBlank()) {
            return Result.Error(Exception("Nome é obrigatório"))
        }

        if (beneficiary.email.isBlank()) {
            return Result.Error(Exception("Email é obrigatório"))
        }

        if (!isValidEmail(beneficiary.email)) {
            return Result.Error(Exception("Email inválido"))
        }

        if (beneficiary.phone.isBlank()) {
            return Result.Error(Exception("Telefone é obrigatório"))
        }

        if (beneficiary.course.isBlank()) {
            return Result.Error(Exception("Curso é obrigatório"))
        }

        if (beneficiary.academicYear < 1 || beneficiary.academicYear > 5) {
            return Result.Error(Exception("Ano académico inválido (1-5)"))
        }

        if (beneficiary.familySize < 1) {
            return Result.Error(Exception("Agregado familiar deve ser >= 1"))
        }

        if (beneficiary.monthlyIncome < 0) {
            return Result.Error(Exception("Rendimento mensal não pode ser negativo"))
        }

        return repository.updateBeneficiary(beneficiary)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

/**
 * Use case para deletar beneficiário
 */
class DeleteBeneficiaryUseCase @Inject constructor(
    private val repository: BeneficiaryRepository
) {
    suspend operator fun invoke(beneficiaryId: String): Result<Unit> {
        if (beneficiaryId.isBlank()) {
            return Result.Error(Exception("ID do beneficiário inválido"))
        }

        return repository.deleteBeneficiary(beneficiaryId)
    }
}
