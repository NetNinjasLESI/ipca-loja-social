package com.ipca.lojasocial.domain.repository

import com.ipca.lojasocial.domain.model.Beneficiary
import com.ipca.lojasocial.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for beneficiary operations
 */
interface BeneficiaryRepository {

    /**
     * Get all beneficiaries
     */
    fun getAllBeneficiaries(): Flow<Result<List<Beneficiary>>>

    /**
     * Get beneficiary by ID
     */
    suspend fun getBeneficiaryById(id: String): Result<Beneficiary>

    /**
     * Get beneficiary by user ID
     */
    suspend fun getBeneficiaryByUserId(userId: String): Result<Beneficiary>

    /**
     * Get beneficiary by student number
     */
    suspend fun getBeneficiaryByStudentNumber(studentNumber: String): Result<Beneficiary>

    /**
     * Create a new beneficiary
     */
    suspend fun createBeneficiary(beneficiary: Beneficiary): Result<Beneficiary>

    /**
     * Update beneficiary information
     */
    suspend fun updateBeneficiary(beneficiary: Beneficiary): Result<Beneficiary>

    /**
     * Delete beneficiary (soft delete - marks as inactive)
     */
    suspend fun deleteBeneficiary(id: String): Result<Unit>

    /**
     * Deactivate beneficiary
     */
    suspend fun deactivateBeneficiary(id: String): Result<Unit>

    /**
     * Search beneficiaries by name or student number
     */
    fun searchBeneficiaries(query: String): Flow<Result<List<Beneficiary>>>

    /**
     * Get active beneficiaries
     */
    fun getActiveBeneficiaries(): Flow<Result<List<Beneficiary>>>

    /**
     * Get beneficiaries by course
     */
    fun getBeneficiariesByCourse(course: String): Flow<Result<List<Beneficiary>>>
}