package com.ipca.lojasocial.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.KitRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

/**
 * Implementação do KitRepository usando Firestore
 */
class KitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : KitRepository {

    private val kitsCollection = firestore.collection("kits")
    private val productsCollection = firestore.collection("products")

    override fun getAllKits(): Flow<Result<List<Kit>>> = callbackFlow {
        val listener = kitsCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter kits"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val kits = snapshot.documents.mapNotNull { doc ->
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val itemsData = doc.get("items") as? List<Map<String, Any>>
                            val items = itemsData?.mapNotNull { itemMap ->
                                try {
                                    KitItem(
                                        productId = itemMap["productId"] as? String ?: "",
                                        productName = itemMap["productName"] as? String ?: "",
                                        quantity = (itemMap["quantity"] as? Number)?.toDouble() ?: 0.0,
                                        unit = ProductUnit.valueOf(
                                            itemMap["unit"] as? String ?: "UNIT"
                                        )
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            } ?: emptyList()

                            Kit(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                description = doc.getString("description") ?: "",
                                items = items,
                                isActive = doc.getBoolean("isActive") ?: true,
                                createdAt = doc.getDate("createdAt") ?: Date(),
                                createdBy = doc.getString("createdBy") ?: "",
                                updatedAt = doc.getDate("updatedAt") ?: Date()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(Result.Success(kits))
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getKitById(id: String): Result<Kit> {
        return try {
            val doc = kitsCollection.document(id).get().await()

            if (doc.exists()) {
                @Suppress("UNCHECKED_CAST")
                val itemsData = doc.get("items") as? List<Map<String, Any>>
                val items = itemsData?.mapNotNull { itemMap ->
                    try {
                        KitItem(
                            productId = itemMap["productId"] as? String ?: "",
                            productName = itemMap["productName"] as? String ?: "",
                            quantity = (itemMap["quantity"] as? Number)?.toDouble() ?: 0.0,
                            unit = ProductUnit.valueOf(
                                itemMap["unit"] as? String ?: "UNIT"
                            )
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                val kit = Kit(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    items = items,
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdAt = doc.getDate("createdAt") ?: Date(),
                    createdBy = doc.getString("createdBy") ?: "",
                    updatedAt = doc.getDate("updatedAt") ?: Date()
                )
                Result.Success(kit)
            } else {
                Result.Error(Exception("Kit não encontrado"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter kit: ${e.message}")
        }
    }

    override suspend fun createKit(kit: Kit): Result<Kit> {
        return try {
            val itemsData = kit.items.map { item ->
                hashMapOf(
                    "productId" to item.productId,
                    "productName" to item.productName,
                    "quantity" to item.quantity,
                    "unit" to item.unit.name
                )
            }

            val kitData = hashMapOf(
                "name" to kit.name,
                "description" to kit.description,
                "items" to itemsData,
                "isActive" to kit.isActive,
                "createdAt" to Date(),
                "createdBy" to kit.createdBy,
                "updatedAt" to Date()
            )

            val docRef = kitsCollection.add(kitData).await()
            val createdKit = kit.copy(id = docRef.id, createdAt = Date())

            Result.Success(createdKit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao criar kit: ${e.message}")
        }
    }

    override suspend fun updateKit(kit: Kit): Result<Kit> {
        return try {
            val itemsData = kit.items.map { item ->
                hashMapOf(
                    "productId" to item.productId,
                    "productName" to item.productName,
                    "quantity" to item.quantity,
                    "unit" to item.unit.name
                )
            }

            val kitData = hashMapOf(
                "name" to kit.name,
                "description" to kit.description,
                "items" to itemsData,
                "isActive" to kit.isActive,
                "updatedAt" to Date()
            )

            kitsCollection.document(kit.id)
                .update(kitData as Map<String, Any>)
                .await()

            Result.Success(kit.copy(updatedAt = Date()))
        } catch (e: Exception) {
            Result.Error(e, "Erro ao atualizar kit: ${e.message}")
        }
    }

    override suspend fun deleteKit(id: String): Result<Unit> {
        return try {
            // Soft delete - apenas marca como inativo
            kitsCollection.document(id)
                .update("isActive", false, "updatedAt", Date())
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao deletar kit: ${e.message}")
        }
    }

    override suspend fun deactivateKit(id: String): Result<Unit> {
        return try {
            kitsCollection.document(id)
                .update("isActive", false, "updatedAt", Date())
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao desativar kit: ${e.message}")
        }
    }

    override fun getActiveKits(): Flow<Result<List<Kit>>> = callbackFlow {
        val listener = kitsCollection
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter kits"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val kits = snapshot.documents.mapNotNull { doc ->
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val itemsData = doc.get("items") as? List<Map<String, Any>>
                            val items = itemsData?.mapNotNull { itemMap ->
                                try {
                                    KitItem(
                                        productId = itemMap["productId"] as? String ?: "",
                                        productName = itemMap["productName"] as? String ?: "",
                                        quantity = (itemMap["quantity"] as? Number)?.toDouble() ?: 0.0,
                                        unit = ProductUnit.valueOf(
                                            itemMap["unit"] as? String ?: "UNIT"
                                        )
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            } ?: emptyList()

                            Kit(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                description = doc.getString("description") ?: "",
                                items = items,
                                isActive = doc.getBoolean("isActive") ?: true,
                                createdAt = doc.getDate("createdAt") ?: Date(),
                                createdBy = doc.getString("createdBy") ?: "",
                                updatedAt = doc.getDate("updatedAt") ?: Date()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    // Ordenar em memória
                    val sortedKits = kits.sortedBy { it.name }
                    trySend(Result.Success(sortedKits))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun searchKits(query: String): Flow<Result<List<Kit>>> =
        callbackFlow {
            val searchQuery = query.lowercase()

            val listener = kitsCollection
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao pesquisar kits"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val kits = snapshot.documents.mapNotNull { doc ->
                            try {
                                val name = doc.getString("name")?.lowercase() ?: ""
                                val description = doc.getString("description")?.lowercase() ?: ""

                                // Filtrar no cliente
                                if (name.contains(searchQuery) || description.contains(searchQuery)) {
                                    @Suppress("UNCHECKED_CAST")
                                    val itemsData = doc.get("items") as? List<Map<String, Any>>
                                    val items = itemsData?.mapNotNull { itemMap ->
                                        try {
                                            KitItem(
                                                productId = itemMap["productId"] as? String ?: "",
                                                productName = itemMap["productName"] as? String ?: "",
                                                quantity = (itemMap["quantity"] as? Number)?.toDouble() ?: 0.0,
                                                unit = ProductUnit.valueOf(
                                                    itemMap["unit"] as? String ?: "UNIT"
                                                )
                                            )
                                        } catch (e: Exception) {
                                            null
                                        }
                                    } ?: emptyList()

                                    Kit(
                                        id = doc.id,
                                        name = doc.getString("name") ?: "",
                                        description = doc.getString("description") ?: "",
                                        items = items,
                                        isActive = doc.getBoolean("isActive") ?: true,
                                        createdAt = doc.getDate("createdAt") ?: Date(),
                                        createdBy = doc.getString("createdBy") ?: "",
                                        updatedAt = doc.getDate("updatedAt") ?: Date()
                                    )
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                        // Ordenar em memória
                        val sortedKits = kits.sortedBy { it.name }
                        trySend(Result.Success(sortedKits))
                    }
                }

            awaitClose { listener.remove() }
        }

    override suspend fun checkKitAvailability(kitId: String): Result<Boolean> {
        return try {
            val kitResult = getKitById(kitId)

            if (kitResult is Result.Success) {
                val kit = kitResult.data

                // Verificar stock de cada item do kit
                for (item in kit.items) {
                    val productDoc = productsCollection.document(item.productId).get().await()

                    if (!productDoc.exists()) {
                        return Result.Error(
                            Exception("Produto ${item.productName} não encontrado")
                        )
                    }

                    val currentStock = productDoc.getDouble("currentStock") ?: 0.0
                    val isActive = productDoc.getBoolean("isActive") ?: false

                    if (!isActive) {
                        return Result.Success(false)
                    }

                    if (currentStock < item.quantity) {
                        return Result.Success(false)
                    }
                }

                Result.Success(true)
            } else {
                Result.Error(Exception("Kit não encontrado"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao verificar disponibilidade: ${e.message}")
        }
    }

    override suspend fun getKitAvailabilityDetails(
        kitId: String
    ): Result<Map<String, KitItemAvailability>> {
        return try {
            val kitResult = getKitById(kitId)

            if (kitResult is Result.Success) {
                val kit = kitResult.data
                val availabilityMap = mutableMapOf<String, KitItemAvailability>()

                for (item in kit.items) {
                    val productDoc = productsCollection.document(item.productId).get().await()

                    if (productDoc.exists()) {
                        val currentStock = productDoc.getDouble("currentStock") ?: 0.0
                        val isActive = productDoc.getBoolean("isActive") ?: false

                        availabilityMap[item.productId] = KitItemAvailability(
                            productId = item.productId,
                            productName = item.productName,
                            requiredQuantity = item.quantity,
                            availableStock = currentStock,
                            isAvailable = isActive && currentStock >= item.quantity,
                            isActive = isActive
                        )
                    } else {
                        availabilityMap[item.productId] = KitItemAvailability(
                            productId = item.productId,
                            productName = item.productName,
                            requiredQuantity = item.quantity,
                            availableStock = 0.0,
                            isAvailable = false,
                            isActive = false
                        )
                    }
                }

                Result.Success(availabilityMap)
            } else {
                Result.Error(Exception("Kit não encontrado"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter detalhes de disponibilidade: ${e.message}")
        }
    }
}

/**
 * Classe de dados para disponibilidade de item do kit
 */
data class KitItemAvailability(
    val productId: String,
    val productName: String,
    val requiredQuantity: Double,
    val availableStock: Double,
    val isAvailable: Boolean,
    val isActive: Boolean
)