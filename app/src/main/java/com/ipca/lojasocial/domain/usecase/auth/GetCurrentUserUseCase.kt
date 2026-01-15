package com.ipca.lojasocial.domain.usecase.auth

import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.model.User
import com.ipca.lojasocial.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case to get the current authenticated user
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> {
        return authRepository.getCurrentUser()
    }
}
