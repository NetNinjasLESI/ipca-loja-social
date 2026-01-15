package com.ipca.lojasocial.domain.repository

import com.ipca.lojasocial.domain.model.ApplicationStatus
import com.ipca.lojasocial.domain.model.BeneficiaryApplication
import com.ipca.lojasocial.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for beneficiary application operations
 */
interface ApplicationRepository {
    
    /**
     * Get all applications
     */
    fun getAllApplications(): Flow<Result<List<BeneficiaryApplication>>>
    
    /**
     * Get application by ID
     */
    suspend fun getApplicationById(id: String): Result<BeneficiaryApplication>
    
    /**
     * Get application by user ID
     */
    suspend fun getApplicationByUserId(userId: String): Result<BeneficiaryApplication>
    
    /**
     * Get applications by status
     */
    fun getApplicationsByStatus(status: ApplicationStatus): Flow<Result<List<BeneficiaryApplication>>>
    
    /**
     * Create a new application
     */
    suspend fun createApplication(application: BeneficiaryApplication): Result<BeneficiaryApplication>
    
    /**
     * Approve application (creates beneficiary and updates user role)
     */
    suspend fun approveApplication(
        applicationId: String,
        reviewedBy: String,
        reviewedByName: String
    ): Result<Unit>
    
    /**
     * Reject application
     */
    suspend fun rejectApplication(
        applicationId: String,
        reviewedBy: String,
        reviewedByName: String,
        reason: String
    ): Result<Unit>
    
    /**
     * Check if user already has an application
     */
    suspend fun hasExistingApplication(userId: String): Result<Boolean>
}
