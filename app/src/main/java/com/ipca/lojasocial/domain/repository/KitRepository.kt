package com.ipca.lojasocial.domain.repository

import com.ipca.lojasocial.data.repository.KitItemAvailability
import com.ipca.lojasocial.domain.model.Kit
import com.ipca.lojasocial.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for kit operations
 */
interface KitRepository {

    /**
     * Get all kits
     */
    fun getAllKits(): Flow<Result<List<Kit>>>

    /**
     * Get kit by ID
     */
    suspend fun getKitById(id: String): Result<Kit>

    /**
     * Create a new kit
     */
    suspend fun createKit(kit: Kit): Result<Kit>

    /**
     * Update kit
     */
    suspend fun updateKit(kit: Kit): Result<Kit>

    /**
     * Delete kit (soft delete)
     */
    suspend fun deleteKit(id: String): Result<Unit>

    /**
     * Deactivate kit
     */
    suspend fun deactivateKit(id: String): Result<Unit>

    /**
     * Get active kits
     */
    fun getActiveKits(): Flow<Result<List<Kit>>>

    /**
     * Search kits
     */
    fun searchKits(query: String): Flow<Result<List<Kit>>>

    /**
     * Check if kit has sufficient stock
     */
    suspend fun checkKitAvailability(kitId: String): Result<Boolean>

    /**
     * Get kit availability details
     */
    suspend fun getKitAvailabilityDetails(kitId: String): Result<Map<String, KitItemAvailability>>
}