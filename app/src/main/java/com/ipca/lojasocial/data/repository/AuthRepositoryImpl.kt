package com.ipca.lojasocial.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.model.User
import com.ipca.lojasocial.domain.model.UserRole
import com.ipca.lojasocial.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

/**
 * Implementation of AuthRepository using Firebase
 */
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            android.util.Log.d("AuthRepo", "=== Starting sign in for: $email ===")

            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            android.util.Log.d("AuthRepo", "Firebase auth SUCCESS - UID: ${firebaseUser?.uid}")

            if (firebaseUser != null) {
                android.util.Log.d("AuthRepo", "Fetching user document from Firestore...")

                val userDoc = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                android.util.Log.d("AuthRepo", "Document exists: ${userDoc.exists()}")
                android.util.Log.d("AuthRepo", "Document data: ${userDoc.data}")

                if (userDoc.exists()) {
                    val roleString = userDoc.getString("role")
                    android.util.Log.d("AuthRepo", "Role from Firestore: '$roleString'")

                    val user = User(
                        id = userDoc.id,
                        email = userDoc.getString("email") ?: "",
                        name = userDoc.getString("name") ?: "",
                        role = UserRole.valueOf(
                            userDoc.getString("role") ?: UserRole.BENEFICIARY.name
                        ),
                        isActive = userDoc.getBoolean("isActive") ?: true,
                        createdAt = userDoc.getDate("createdAt") ?: Date(),
                        updatedAt = userDoc.getDate("updatedAt") ?: Date(),
                        profileImageUrl = userDoc.getString("profileImageUrl")
                    )

                    android.util.Log.d("AuthRepo", "User object created - Role: ${user.role}")
                    Result.Success(user)
                } else {
                    android.util.Log.e("AuthRepo", "ERROR: User document NOT found")
                    Result.Error(Exception("Dados do utilizador não encontrados"))
                }
            } else {
                android.util.Log.e("AuthRepo", "ERROR: Firebase user is null")
                Result.Error(Exception("Falha na autenticação"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "EXCEPTION during sign in: ${e.message}", e)
            Result.Error(e, "Erro ao fazer login: ${e.message}")
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao fazer logout")
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser != null) {
                val userDoc = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                if (userDoc.exists()) {
                    val user = User(
                        id = userDoc.id,
                        email = userDoc.getString("email") ?: "",
                        name = userDoc.getString("name") ?: "",
                        role = UserRole.valueOf(
                            userDoc.getString("role") ?: UserRole.BENEFICIARY.name
                        ),
                        isActive = userDoc.getBoolean("isActive") ?: true,
                        createdAt = userDoc.getDate("createdAt") ?: Date(),
                        updatedAt = userDoc.getDate("updatedAt") ?: Date(),
                        profileImageUrl = userDoc.getString("profileImageUrl")
                    )
                    Result.Success(user)
                } else {
                    Result.Success(null)
                }
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter utilizador atual")
        }
    }

    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Listen to user document changes
                val userListener = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val user = User(
                                id = snapshot.id,
                                email = snapshot.getString("email") ?: "",
                                name = snapshot.getString("name") ?: "",
                                role = UserRole.valueOf(
                                    snapshot.getString("role") ?: UserRole.BENEFICIARY.name
                                ),
                                isActive = snapshot.getBoolean("isActive") ?: true,
                                createdAt = snapshot.getDate("createdAt") ?: Date(),
                                updatedAt = snapshot.getDate("updatedAt") ?: Date(),
                                profileImageUrl = snapshot.getString("profileImageUrl")
                            )
                            trySend(user)
                        } else {
                            trySend(null)
                        }
                    }
            } else {
                trySend(null)
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao enviar email de recuperação")
        }
    }

    override suspend fun createBeneficiaryAccount(
        email: String,
        password: String,
        name: String
    ): Result<User> {
        return try {
            // Only collaborators/administrators can create beneficiary accounts
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.Error(Exception("Utilizador não autenticado"))
            }

            // Create authentication user
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val newUser = authResult.user

            if (newUser != null) {
                // Create user document in Firestore
                val userData = hashMapOf(
                    "email" to email,
                    "name" to name,
                    "role" to UserRole.BENEFICIARY.name,
                    "isActive" to true,
                    "createdAt" to Date(),
                    "updatedAt" to Date()
                )

                firestore.collection("users")
                    .document(newUser.uid)
                    .set(userData)
                    .await()

                val user = User(
                    id = newUser.uid,
                    email = email,
                    name = name,
                    role = UserRole.BENEFICIARY,
                    isActive = true,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                Result.Success(user)
            } else {
                Result.Error(Exception("Falha ao criar conta"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao criar conta de beneficiário: ${e.message}")
        }
    }

    override fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
