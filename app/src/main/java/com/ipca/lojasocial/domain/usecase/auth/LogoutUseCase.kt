package com.ipca.lojasocial.domain.usecase.auth

import com.google.firebase.auth.FirebaseAuth
import com.ipca.lojasocial.domain.model.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Use case para fazer logout
 */
class LogoutUseCase @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            auth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao fazer logout")
        }
    }
}