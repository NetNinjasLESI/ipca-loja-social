package com.ipca.lojasocial.domain.repository

import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    
    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<User>
    
    /**
     * Sign out the current user
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Get the current authenticated user
     */
    suspend fun getCurrentUser(): Result<User?>
    
    /**
     * Observe the current user state
     */
    fun observeCurrentUser(): Flow<User?>
    
    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    
    /**
     * Create a new beneficiary account (only for collaborators)
     */
    suspend fun createBeneficiaryAccount(
        email: String,
        password: String,
        name: String
    ): Result<User>
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean
}
