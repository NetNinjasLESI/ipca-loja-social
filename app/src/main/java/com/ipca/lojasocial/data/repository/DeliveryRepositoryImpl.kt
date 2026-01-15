package com.ipca.lojasocial.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.DeliveryRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class DeliveryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DeliveryRepository {

    private val deliveriesCollection = firestore.collection("deliveries")
    private val kitsCollection = firestore.collection("kits")
    private val productsCollection = firestore.collection("products")
    private val stockMovementsCollection = firestore.collection("stock_movements")

    override fun getAllDeliveries(): Flow<Result<List<Delivery>>> = callbackFlow {
        val listener = deliveriesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter entregas"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val deliveries = snapshot.documents.mapNotNull {
                        parseDeliveryDocument(it)
                    }
                    trySend(Result.Success(deliveries))
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getDeliveryById(id: String): Result<Delivery> {
        return try {
            val doc = deliveriesCollection.document(id).get().await()

            if (doc.exists()) {
                val delivery = parseDeliveryDocument(doc)
                if (delivery != null) {
                    Result.Success(delivery)
                } else {
                    Result.Error(Exception("Erro ao processar entrega"))
                }
            } else {
                Result.Error(Exception("Entrega não encontrada"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter entrega: ${e.message}")
        }
    }

    override suspend fun createDelivery(delivery: Delivery): Result<Delivery> {
        return try {
            val deliveryData = hashMapOf(
                "beneficiaryId" to delivery.beneficiaryId,
                "beneficiaryName" to delivery.beneficiaryName,
                "kitId" to delivery.kitId,
                "kitName" to delivery.kitName,
                "scheduledDate" to delivery.scheduledDate,
                "status" to delivery.status.name,
                "notes" to delivery.notes,
                "requestedDate" to delivery.requestedDate,
                "requestNotes" to delivery.requestNotes,
                "approvedDate" to delivery.approvedDate,
                "approvedBy" to delivery.approvedBy,
                "createdAt" to Date(),
                "createdBy" to delivery.createdBy,
                "updatedAt" to Date()
            )

            val docRef = deliveriesCollection.add(deliveryData).await()
            val createdDelivery = delivery.copy(
                id = docRef.id,
                createdAt = Date(),
                updatedAt = Date()
            )

            Result.Success(createdDelivery)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao criar entrega: ${e.message}")
        }
    }

    override suspend fun approveDeliveryRequest(
        deliveryId: String,
        userId: String
    ): Result<Unit> {
        return try {
            deliveriesCollection.document(deliveryId)
                .update(
                    mapOf(
                        "status" to DeliveryStatus.APPROVED.name,
                        "approvedDate" to Date(),
                        "approvedBy" to userId,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao aprovar solicitação: ${e.message}")
        }
    }

    override suspend fun rejectDeliveryRequest(
        deliveryId: String,
        userId: String,
        reason: String
    ): Result<Unit> {
        return try {
            deliveriesCollection.document(deliveryId)
                .update(
                    mapOf(
                        "status" to DeliveryStatus.REJECTED.name,
                        "rejectedDate" to Date(),
                        "rejectedBy" to userId,
                        "rejectionReason" to reason,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao rejeitar solicitação: ${e.message}")
        }
    }

    override suspend fun scheduleDelivery(
        deliveryId: String,
        scheduledDate: Date,
        notes: String,
        userId: String
    ): Result<Unit> {
        return try {
            deliveriesCollection.document(deliveryId)
                .update(
                    mapOf(
                        "status" to DeliveryStatus.SCHEDULED.name,
                        "scheduledDate" to scheduledDate,
                        "notes" to notes,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao agendar entrega: ${e.message}")
        }
    }

    override suspend fun confirmDelivery(
        deliveryId: String,
        userId: String
    ): Result<Unit> {
        return try {
            // 1. Obter entrega
            val deliveryDoc = deliveriesCollection.document(deliveryId).get().await()
            if (!deliveryDoc.exists()) {
                return Result.Error(Exception("Entrega não encontrada"))
            }

            val kitId = deliveryDoc.getString("kitId") ?: ""

            // 2. Obter kit
            val kitDoc = kitsCollection.document(kitId).get().await()
            if (!kitDoc.exists()) {
                return Result.Error(Exception("Kit não encontrado"))
            }

            @Suppress("UNCHECKED_CAST")
            val itemsData = kitDoc.get("items") as? List<Map<String, Any>>
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

            // 3. Atualizar stock de cada produto (batch operation)
            val batch = firestore.batch()

            for (item in items) {
                val productRef = productsCollection.document(item.productId)
                val productDoc = productRef.get().await()

                if (productDoc.exists()) {
                    val currentStock = productDoc.getDouble("currentStock") ?: 0.0
                    val newStock = currentStock - item.quantity

                    batch.update(productRef, "currentStock", newStock)
                    batch.update(productRef, "updatedAt", Date())

                    // Criar movimento de stock
                    val movementData = hashMapOf(
                        "productId" to item.productId,
                        "productName" to item.productName,
                        "type" to "EXIT",
                        "quantity" to item.quantity,
                        "unit" to item.unit.name,
                        "reason" to "Entrega confirmada",
                        "referenceDocument" to "Entrega #$deliveryId",
                        "performedBy" to userId,
                        "performedAt" to Date(),
                        "notes" to "Entrega de kit ${kitDoc.getString("name")}"
                    )
                    val movementRef = stockMovementsCollection.document()
                    batch.set(movementRef, movementData)
                }
            }

            // 4. Atualizar status da entrega
            val deliveryRef = deliveriesCollection.document(deliveryId)
            batch.update(
                deliveryRef,
                mapOf(
                    "status" to DeliveryStatus.CONFIRMED.name,
                    "confirmedDate" to Date(),
                    "confirmedBy" to userId,
                    "updatedAt" to Date()
                )
            )

            // 5. Executar batch
            batch.commit().await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao confirmar entrega: ${e.message}")
        }
    }

    override suspend fun cancelDelivery(
        deliveryId: String,
        userId: String,
        reason: String
    ): Result<Unit> {
        return try {
            deliveriesCollection.document(deliveryId)
                .update(
                    mapOf(
                        "status" to DeliveryStatus.CANCELLED.name,
                        "cancelledDate" to Date(),
                        "cancelledBy" to userId,
                        "cancellationReason" to reason,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao cancelar entrega: ${e.message}")
        }
    }

    override fun getDeliveriesByBeneficiary(
        beneficiaryId: String
    ): Flow<Result<List<Delivery>>> = callbackFlow {
        val listener = deliveriesCollection
            .whereEqualTo("beneficiaryId", beneficiaryId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter entregas"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val deliveries = snapshot.documents.mapNotNull {
                        parseDeliveryDocument(it)
                    }
                    trySend(Result.Success(deliveries))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getDeliveriesByStatus(
        status: DeliveryStatus
    ): Flow<Result<List<Delivery>>> = callbackFlow {
        val listener = deliveriesCollection
            .whereEqualTo("status", status.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter entregas"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val deliveries = snapshot.documents.mapNotNull {
                        parseDeliveryDocument(it)
                    }
                    trySend(Result.Success(deliveries))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun searchDeliveries(query: String): Flow<Result<List<Delivery>>> =
        callbackFlow {
            val searchQuery = query.lowercase()

            val listener = deliveriesCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao pesquisar entregas"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val deliveries = snapshot.documents.mapNotNull { doc ->
                            val beneficiaryName =
                                doc.getString("beneficiaryName")?.lowercase() ?: ""
                            val kitName =
                                doc.getString("kitName")?.lowercase() ?: ""

                            if (beneficiaryName.contains(searchQuery) ||
                                kitName.contains(searchQuery)
                            ) {
                                parseDeliveryDocument(doc)
                            } else null
                        }
                        trySend(Result.Success(deliveries))
                    }
                }

            awaitClose { listener.remove() }
        }

    // ========== Helper Methods ==========

    private fun parseDeliveryDocument(
        doc: com.google.firebase.firestore.DocumentSnapshot
    ): Delivery? {
        return try {
            Delivery(
                id = doc.id,
                beneficiaryId = doc.getString("beneficiaryId") ?: "",
                beneficiaryName = doc.getString("beneficiaryName") ?: "",
                kitId = doc.getString("kitId") ?: "",
                kitName = doc.getString("kitName") ?: "",
                scheduledDate = doc.getDate("scheduledDate") ?: Date(),
                status = DeliveryStatus.valueOf(
                    doc.getString("status") ?: "PENDING_APPROVAL"
                ),
                notes = doc.getString("notes") ?: "",
                requestedDate = doc.getDate("requestedDate"),
                requestNotes = doc.getString("requestNotes") ?: "",
                approvedDate = doc.getDate("approvedDate"),
                approvedBy = doc.getString("approvedBy"),
                rejectedDate = doc.getDate("rejectedDate"),
                rejectedBy = doc.getString("rejectedBy"),
                rejectionReason = doc.getString("rejectionReason"),
                confirmedDate = doc.getDate("confirmedDate"),
                confirmedBy = doc.getString("confirmedBy"),
                cancelledDate = doc.getDate("cancelledDate"),
                cancelledBy = doc.getString("cancelledBy"),
                cancellationReason = doc.getString("cancellationReason"),
                createdAt = doc.getDate("createdAt") ?: Date(),
                createdBy = doc.getString("createdBy") ?: "",
                updatedAt = doc.getDate("updatedAt") ?: Date()
            )
        } catch (e: Exception) {
            null
        }
    }
}