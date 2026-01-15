package com.ipca.lojasocial.domain.usecase.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.model.User
import com.ipca.lojasocial.domain.model.UserRole
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

/**
 * Use case para registar novo utilizador
 */
class RegisterUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String
    ): Result<User> {
        // Validations
        if (email.isBlank()) {
            return Result.Error(Exception("Email é obrigatório"))
        }

        if (!isValidIPCAEmail(email)) {
            return Result.Error(
                Exception("Email deve ser @ipca.pt ou @alunos.ipca.pt")
            )
        }

        if (password.isBlank()) {
            return Result.Error(Exception("Password é obrigatória"))
        }

        if (password.length < 6) {
            return Result.Error(Exception("Password deve ter pelo menos 6 caracteres"))
        }

        if (name.isBlank()) {
            return Result.Error(Exception("Nome é obrigatório"))
        }

        return try {
            // 1. Create Firebase Auth account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.Error(Exception("Erro ao criar conta"))

            // 2. Create User document in Firestore
            val user = User(
                id = firebaseUser.uid,
                email = email,
                name = name,
                role = UserRole.USER, // Start as normal user
                isActive = true,
                createdAt = Date(),
                updatedAt = Date()
            )

            val userMap = hashMapOf(
                "id" to user.id,
                "email" to user.email,
                "name" to user.name,
                "role" to user.role.name,
                "isActive" to user.isActive,
                "createdAt" to user.createdAt,
                "updatedAt" to user.updatedAt
            )

            firestore.collection("users")
                .document(user.id)
                .set(userMap)
                .await()

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao registar: ${e.message}")
        }
    }

    private fun isValidIPCAEmail(email: String): Boolean {
        return email.endsWith("@ipca.pt") || email.endsWith("@alunos.ipca.pt")
    }
}
