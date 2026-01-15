package com.ipca.lojasocial.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.CampaignRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

/**
 * ✅ Repository SIMPLIFICADO - SEM produtos necessários
 */
class CampaignRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CampaignRepository {

    private val campaignsCollection = firestore.collection("campaigns")
    private val productDonationsCollection = firestore.collection("product_donations")

    /**
     * ✅ Helper: Converter documento Firestore para Campaign
     */
    private fun documentToCampaign(doc: com.google.firebase.firestore.DocumentSnapshot): Campaign? {
        return try {
            Campaign(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                goalType = CampaignGoalType.PRODUCT,
                startDate = doc.getDate("startDate") ?: Date(),
                endDate = doc.getDate("endDate") ?: Date(),
                status = CampaignStatus.valueOf(
                    doc.getString("status") ?: "DRAFT"
                ),
                imageUrl = doc.getString("imageUrl"),
                qrCodeUrl = doc.getString("qrCodeUrl"),
                publicUrl = doc.getString("publicUrl") ?: "",
                isPublic = doc.getBoolean("isPublic") ?: false,
                createdBy = doc.getString("createdBy") ?: "",
                createdAt = doc.getDate("createdAt") ?: Date(),
                updatedAt = doc.getDate("updatedAt") ?: Date()
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun getAllCampaigns(): Flow<Result<List<Campaign>>> = callbackFlow {
        val listener = campaignsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter campanhas"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val campaigns = snapshot.documents.mapNotNull { doc ->
                        documentToCampaign(doc)
                    }
                    trySend(Result.Success(campaigns))
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getCampaignById(id: String): Result<Campaign> {
        return try {
            val doc = campaignsCollection.document(id).get().await()

            if (doc.exists()) {
                val campaign = documentToCampaign(doc)
                    ?: return Result.Error(Exception("Erro ao converter campanha"))

                Result.Success(campaign)
            } else {
                Result.Error(Exception("Campanha não encontrada"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter campanha: ${e.message}")
        }
    }

    override suspend fun createCampaign(campaign: Campaign): Result<Campaign> {
        return try {
            val validationError = campaign.getDateValidationError()
            if (validationError != null) {
                return Result.Error(Exception(validationError))
            }

            val campaignData = hashMapOf(
                "title" to campaign.title,
                "description" to campaign.description,
                "goalType" to CampaignGoalType.PRODUCT.name,
                "startDate" to campaign.startDate,
                "endDate" to campaign.endDate,
                "status" to CampaignStatus.DRAFT.name,
                "imageUrl" to campaign.imageUrl,
                "qrCodeUrl" to null,
                "publicUrl" to "",
                "isPublic" to false,
                "createdBy" to campaign.createdBy,
                "createdAt" to Date(),
                "updatedAt" to Date()
            )

            val docRef = campaignsCollection.add(campaignData).await()
            val campaignId = docRef.id

            val publicUrl = "https://lojasocial.ipca.pt/campaigns/$campaignId"
            campaignsCollection.document(campaignId)
                .update("publicUrl", publicUrl)
                .await()

            val createdCampaign = campaign.copy(
                id = campaignId,
                publicUrl = publicUrl,
                status = CampaignStatus.DRAFT,
                createdAt = Date()
            )

            Result.Success(createdCampaign)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao criar campanha: ${e.message}")
        }
    }

    override suspend fun updateCampaign(campaign: Campaign): Result<Unit> {
        return try {
            if (campaign.id.isBlank()) {
                return Result.Error(Exception("ID da campanha inválido"))
            }

            val validationError = campaign.getDateValidationError()
            if (validationError != null) {
                return Result.Error(Exception(validationError))
            }

            val updates = hashMapOf(
                "title" to campaign.title,
                "description" to campaign.description,
                "startDate" to campaign.startDate,
                "endDate" to campaign.endDate,
                "imageUrl" to campaign.imageUrl,
                "updatedAt" to Date()
            )

            campaignsCollection.document(campaign.id)
                .update(updates as Map<String, Any>)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao atualizar campanha: ${e.message}")
        }
    }

    override suspend fun deleteCampaign(id: String): Result<Unit> {
        return try {
            campaignsCollection.document(id)
                .update(
                    mapOf(
                        "status" to CampaignStatus.CANCELLED.name,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao eliminar campanha: ${e.message}")
        }
    }

    override suspend fun activateCampaign(id: String): Result<Unit> {
        return try {
            val doc = campaignsCollection.document(id).get().await()
            if (doc.exists()) {
                val campaign = documentToCampaign(doc)
                if (campaign != null) {
                    val validationError = campaign.getDateValidationError()
                    if (validationError != null) {
                        return Result.Error(Exception("Não é possível ativar: $validationError"))
                    }
                }
            }

            campaignsCollection.document(id)
                .update(
                    mapOf(
                        "status" to CampaignStatus.ACTIVE.name,
                        "isPublic" to true,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao ativar campanha: ${e.message}")
        }
    }

    override suspend fun completeCampaign(id: String): Result<Unit> {
        return try {
            campaignsCollection.document(id)
                .update(
                    mapOf(
                        "status" to CampaignStatus.COMPLETED.name,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao completar campanha: ${e.message}")
        }
    }

    override suspend fun deactivateCampaign(id: String): Result<Unit> {
        return try {
            campaignsCollection.document(id)
                .update(
                    mapOf(
                        "status" to CampaignStatus.DRAFT.name,
                        "isPublic" to false,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao desativar campanha: ${e.message}")
        }
    }

    override fun getActiveCampaigns(): Flow<Result<List<Campaign>>> = callbackFlow {
        val listener = campaignsCollection
            .whereEqualTo("status", CampaignStatus.ACTIVE.name)
            .whereEqualTo("isPublic", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter campanhas ativas"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val campaigns = snapshot.documents.mapNotNull { doc ->
                        documentToCampaign(doc)
                    }
                    trySend(Result.Success(campaigns))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getPublicCampaigns(): Flow<Result<List<Campaign>>> = callbackFlow {
        val listener = campaignsCollection
            .whereEqualTo("isPublic", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter campanhas públicas"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val campaigns = snapshot.documents.mapNotNull { doc ->
                        documentToCampaign(doc)
                    }
                    trySend(Result.Success(campaigns))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun searchCampaigns(query: String): Flow<Result<List<Campaign>>> =
        callbackFlow {
            val searchQuery = query.lowercase()

            val listener = campaignsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao pesquisar campanhas"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val campaigns = snapshot.documents.mapNotNull { doc ->
                            try {
                                val title = doc.getString("title")?.lowercase() ?: ""
                                val description = doc.getString("description")?.lowercase() ?: ""

                                if (title.contains(searchQuery) || description.contains(searchQuery)) {
                                    documentToCampaign(doc)
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(Result.Success(campaigns))
                    }
                }

            awaitClose { listener.remove() }
        }

    override suspend fun addProductDonation(
        campaignId: String,
        productName: String,
        donorName: String?,
        donorEmail: String?,
        donorPhone: String?,
        notes: String
    ): Result<String> {
        return try {
            val donationData = hashMapOf(
                "campaignId" to campaignId,
                "productName" to productName,
                "donorName" to donorName,
                "donorEmail" to donorEmail,
                "donorPhone" to donorPhone,
                "donatedAt" to Date(),
                "notes" to notes
            )

            val donationRef = productDonationsCollection.add(donationData).await()

            Result.Success(donationRef.id)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao registar doação: ${e.message}")
        }
    }

    override fun getProductDonationsByCampaign(
        campaignId: String
    ): Flow<Result<List<ProductDonation>>> = callbackFlow {
        val listener = productDonationsCollection
            .whereEqualTo("campaignId", campaignId)
            .orderBy("donatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter doações"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val donations = snapshot.documents.mapNotNull { doc ->
                        try {
                            ProductDonation(
                                id = doc.id,
                                campaignId = doc.getString("campaignId") ?: "",
                                productName = doc.getString("productName") ?: "",
                                quantity = doc.getDouble("quantity") ?: 0.0,
                                unit = try {
                                    ProductUnit.valueOf(doc.getString("unit") ?: "UNIT")
                                } catch (e: Exception) {
                                    ProductUnit.UNIT
                                },
                                category = try {
                                    ProductCategory.valueOf(doc.getString("category") ?: "OTHER")
                                } catch (e: Exception) {
                                    ProductCategory.OTHER
                                },
                                donorName = doc.getString("donorName"),
                                donorEmail = doc.getString("donorEmail"),
                                donorPhone = doc.getString("donorPhone"),
                                donatedAt = doc.getDate("donatedAt") ?: Date(),
                                notes = doc.getString("notes") ?: "",
                                inventoryProductId = doc.getString("inventoryProductId") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(Result.Success(donations))
                }
            }

        awaitClose { listener.remove() }
    }

    // ========== COMPATIBILIDADE: Métodos monetários antigos ==========
    suspend fun addDonation(
        campaignId: String,
        amount: Double,
        donorName: String?,
        donorEmail: String?
    ): Result<Unit> {
        return try {
            val donationData = hashMapOf(
                "campaignId" to campaignId,
                "amount" to amount,
                "donorName" to donorName,
                "donorEmail" to donorEmail,
                "donatedAt" to Date()
            )

            firestore.collection("donations").add(donationData).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao registar doação: ${e.message}")
        }
    }

    fun getDonationsByCampaign(
        campaignId: String
    ): Flow<Result<List<Donation>>> = callbackFlow {
        val listener = firestore.collection("donations")
            .whereEqualTo("campaignId", campaignId)
            .orderBy("donatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter doações"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val donations = snapshot.documents.mapNotNull { doc ->
                        try {
                            Donation(
                                id = doc.id,
                                campaignId = doc.getString("campaignId") ?: "",
                                amount = doc.getDouble("amount") ?: 0.0,
                                donorName = doc.getString("donorName"),
                                donorEmail = doc.getString("donorEmail"),
                                donatedAt = doc.getDate("donatedAt") ?: Date()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(Result.Success(donations))
                }
            }

        awaitClose { listener.remove() }
    }
}
