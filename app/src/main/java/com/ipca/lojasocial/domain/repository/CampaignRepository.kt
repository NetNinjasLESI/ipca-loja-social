package com.ipca.lojasocial.domain.repository

import com.ipca.lojasocial.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface para Campanhas SIMPLES
 * Versão SEM controlo de quantidades/progresso
 */
interface CampaignRepository {
    
    // ========== CAMPANHAS ==========
    
    fun getAllCampaigns(): Flow<Result<List<Campaign>>>
    
    suspend fun getCampaignById(id: String): Result<Campaign>
    
    suspend fun createCampaign(campaign: Campaign): Result<Campaign>
    
    suspend fun updateCampaign(campaign: Campaign): Result<Unit>
    
    suspend fun deleteCampaign(id: String): Result<Unit>
    
    suspend fun activateCampaign(id: String): Result<Unit>
    
    suspend fun completeCampaign(id: String): Result<Unit>
    
    suspend fun deactivateCampaign(id: String): Result<Unit>
    
    fun getActiveCampaigns(): Flow<Result<List<Campaign>>>
    
    fun getPublicCampaigns(): Flow<Result<List<Campaign>>>
    
    fun searchCampaigns(query: String): Flow<Result<List<Campaign>>>
    
    // ========== DOAÇÕES (OPCIONAL - VERSÃO SIMPLES) ==========
    
    /**
     * Registar doação simples de produto
     * Sem quantidade exata - apenas registo
     */
    suspend fun addProductDonation(
        campaignId: String,
        productName: String,
        donorName: String?,
        donorEmail: String?,
        donorPhone: String?,
        notes: String = "" // Ex: "2 pacotes de arroz"
    ): Result<String>
    
    /**
     * Obter doações de uma campanha
     */
    fun getProductDonationsByCampaign(
        campaignId: String
    ): Flow<Result<List<ProductDonation>>>
}
