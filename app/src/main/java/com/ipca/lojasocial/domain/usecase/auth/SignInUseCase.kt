package com.ipca.lojasocial.domain.usecase.auth

import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.model.User
import com.ipca.lojasocial.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user sign in
 */
class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Validate email
        if (email.isBlank()) {
            return Result.Error(Exception("Email não pode estar vazio"))
        }
        
        // Validate password
        if (password.isBlank()) {
            return Result.Error(Exception("Palavra-passe não pode estar vazia"))
        }
        
        // Validate IPCA email format
        if (!email.endsWith("@ipca.pt") && !email.endsWith("@alunos.ipca.pt")) {
            return Result.Error(Exception("Email deve ser institucional do IPCA"))
        }
        
        return authRepository.signIn(email, password)
    }
}
