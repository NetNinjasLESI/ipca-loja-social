package com.ipca.lojasocial.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.ProductRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

/**
 * Implementação do ProductRepository usando Firestore
 */
class ProductRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProductRepository {

    private val productsCollection = firestore.collection("products")
    private val movementsCollection = firestore.collection("stock_movements")

    override fun getAllProducts(): Flow<Result<List<Product>>> = callbackFlow {
        val listener = productsCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter produtos"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        try {
                            Product(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                description = doc.getString("description") ?: "",
                                category = ProductCategory.valueOf(
                                    doc.getString("category") ?: "OTHER"
                                ),
                                barcode = doc.getString("barcode"),
                                unit = ProductUnit.valueOf(
                                    doc.getString("unit") ?: "UNIT"
                                ),
                                currentStock = doc.getDouble("currentStock") ?: 0.0,
                                minimumStock = doc.getDouble("minimumStock") ?: 0.0,
                                expiryDate = doc.getDate("expiryDate"),
                                imageUrl = doc.getString("imageUrl"),
                                isActive = doc.getBoolean("isActive") ?: true,
                                createdAt = doc.getDate("createdAt") ?: Date(),
                                createdBy = doc.getString("createdBy") ?: "",
                                updatedAt = doc.getDate("updatedAt") ?: Date()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(Result.Success(products))
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getProductById(id: String): Result<Product> {
        return try {
            val doc = productsCollection.document(id).get().await()

            if (doc.exists()) {
                val product = Product(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    category = ProductCategory.valueOf(
                        doc.getString("category") ?: "OTHER"
                    ),
                    barcode = doc.getString("barcode"),
                    unit = ProductUnit.valueOf(
                        doc.getString("unit") ?: "UNIT"
                    ),
                    currentStock = doc.getDouble("currentStock") ?: 0.0,
                    minimumStock = doc.getDouble("minimumStock") ?: 0.0,
                    expiryDate = doc.getDate("expiryDate"),
                    imageUrl = doc.getString("imageUrl"),
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdAt = doc.getDate("createdAt") ?: Date(),
                    createdBy = doc.getString("createdBy") ?: "",
                    updatedAt = doc.getDate("updatedAt") ?: Date()
                )
                Result.Success(product)
            } else {
                Result.Error(Exception("Produto não encontrado"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter produto: ${e.message}")
        }
    }

    override suspend fun getProductByBarcode(barcode: String): Result<Product> {
        return try {
            val snapshot = productsCollection
                .whereEqualTo("barcode", barcode)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                val product = Product(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    category = ProductCategory.valueOf(
                        doc.getString("category") ?: "OTHER"
                    ),
                    barcode = doc.getString("barcode"),
                    unit = ProductUnit.valueOf(
                        doc.getString("unit") ?: "UNIT"
                    ),
                    currentStock = doc.getDouble("currentStock") ?: 0.0,
                    minimumStock = doc.getDouble("minimumStock") ?: 0.0,
                    expiryDate = doc.getDate("expiryDate"),
                    imageUrl = doc.getString("imageUrl"),
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdAt = doc.getDate("createdAt") ?: Date(),
                    createdBy = doc.getString("createdBy") ?: "",
                    updatedAt = doc.getDate("updatedAt") ?: Date()
                )
                Result.Success(product)
            } else {
                Result.Error(Exception("Produto não encontrado"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao buscar produto por código: ${e.message}")
        }
    }

    override suspend fun createProduct(product: Product): Result<Product> {
        return try {
            val productData = hashMapOf(
                "name" to product.name,
                "description" to product.description,
                "category" to product.category.name,
                "barcode" to product.barcode,
                "unit" to product.unit.name,
                "currentStock" to product.currentStock,
                "minimumStock" to product.minimumStock,
                "expiryDate" to product.expiryDate,
                "imageUrl" to product.imageUrl,
                "isActive" to product.isActive,
                "createdAt" to Date(),
                "createdBy" to product.createdBy,
                "updatedAt" to Date()
            )

            val docRef = productsCollection.add(productData).await()
            val createdProduct = product.copy(id = docRef.id, createdAt = Date())

            Result.Success(createdProduct)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao criar produto: ${e.message}")
        }
    }

    override suspend fun updateProduct(product: Product): Result<Product> {
        return try {
            val productData = hashMapOf(
                "name" to product.name,
                "description" to product.description,
                "category" to product.category.name,
                "barcode" to product.barcode,
                "unit" to product.unit.name,
                "currentStock" to product.currentStock,
                "minimumStock" to product.minimumStock,
                "expiryDate" to product.expiryDate,
                "imageUrl" to product.imageUrl,
                "isActive" to product.isActive,
                "updatedAt" to Date()
            )

            productsCollection.document(product.id)
                .update(productData as Map<String, Any>)
                .await()

            Result.Success(product.copy(updatedAt = Date()))
        } catch (e: Exception) {
            Result.Error(e, "Erro ao atualizar produto: ${e.message}")
        }
    }

    override suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            // Soft delete - apenas marca como inativo
            productsCollection.document(id)
                .update("isActive", false, "updatedAt", Date())
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao deletar produto: ${e.message}")
        }
    }

    override fun getProductsByCategory(
        category: ProductCategory
    ): Flow<Result<List<Product>>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("category", category.name)
            .whereEqualTo("isActive", true)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter produtos"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        try {
                            Product(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                description = doc.getString("description") ?: "",
                                category = ProductCategory.valueOf(
                                    doc.getString("category") ?: "OTHER"
                                ),
                                barcode = doc.getString("barcode"),
                                unit = ProductUnit.valueOf(
                                    doc.getString("unit") ?: "UNIT"
                                ),
                                currentStock = doc.getDouble("currentStock") ?: 0.0,
                                minimumStock = doc.getDouble("minimumStock") ?: 0.0,
                                expiryDate = doc.getDate("expiryDate"),
                                imageUrl = doc.getString("imageUrl"),
                                isActive = doc.getBoolean("isActive") ?: true,
                                createdAt = doc.getDate("createdAt") ?: Date(),
                                createdBy = doc.getString("createdBy") ?: "",
                                updatedAt = doc.getDate("updatedAt") ?: Date()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(Result.Success(products))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getProductsWithLowStock(): Flow<Result<List<Product>>> =
        callbackFlow {
            val listener = productsCollection
                .whereEqualTo("isActive", true)
                .orderBy("currentStock", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao obter produtos"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val products = snapshot.documents.mapNotNull { doc ->
                            try {
                                val currentStock = doc.getDouble("currentStock") ?: 0.0
                                val minimumStock = doc.getDouble("minimumStock") ?: 0.0

                                // Filtrar apenas produtos com stock baixo
                                if (currentStock <= minimumStock) {
                                    Product(
                                        id = doc.id,
                                        name = doc.getString("name") ?: "",
                                        description = doc.getString("description") ?: "",
                                        category = ProductCategory.valueOf(
                                            doc.getString("category") ?: "OTHER"
                                        ),
                                        barcode = doc.getString("barcode"),
                                        unit = ProductUnit.valueOf(
                                            doc.getString("unit") ?: "UNIT"
                                        ),
                                        currentStock = currentStock,
                                        minimumStock = minimumStock,
                                        expiryDate = doc.getDate("expiryDate"),
                                        imageUrl = doc.getString("imageUrl"),
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
                        trySend(Result.Success(products))
                    }
                }

            awaitClose { listener.remove() }
        }

    override fun getProductsNearExpiry(
        daysThreshold: Int
    ): Flow<Result<List<Product>>> = callbackFlow {
        val thresholdDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysThreshold)
        }.time

        val listener = productsCollection
            .whereEqualTo("isActive", true)
            .whereLessThanOrEqualTo("expiryDate", thresholdDate)
            .whereGreaterThan("expiryDate", Date())
            .orderBy("expiryDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter produtos"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        try {
                            Product(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                description = doc.getString("description") ?: "",
                                category = ProductCategory.valueOf(
                                    doc.getString("category") ?: "OTHER"
                                ),
                                barcode = doc.getString("barcode"),
                                unit = ProductUnit.valueOf(
                                    doc.getString("unit") ?: "UNIT"
                                ),
                                currentStock = doc.getDouble("currentStock") ?: 0.0,
                                minimumStock = doc.getDouble("minimumStock") ?: 0.0,
                                expiryDate = doc.getDate("expiryDate"),
                                imageUrl = doc.getString("imageUrl"),
                                isActive = doc.getBoolean("isActive") ?: true,
                                createdAt = doc.getDate("createdAt") ?: Date(),
                                createdBy = doc.getString("createdBy") ?: "",
                                updatedAt = doc.getDate("updatedAt") ?: Date()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(Result.Success(products))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun searchProducts(query: String): Flow<Result<List<Product>>> =
        callbackFlow {
            val searchQuery = query.lowercase()

            val listener = productsCollection
                .whereEqualTo("isActive", true)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao pesquisar produtos"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val products = snapshot.documents.mapNotNull { doc ->
                            try {
                                val name = doc.getString("name")?.lowercase() ?: ""
                                val description =
                                    doc.getString("description")?.lowercase() ?: ""
                                val barcode = doc.getString("barcode")?.lowercase() ?: ""

                                // Filtrar no cliente (Firestore não suporta busca completa)
                                if (name.contains(searchQuery) ||
                                    description.contains(searchQuery) ||
                                    barcode.contains(searchQuery)
                                ) {
                                    Product(
                                        id = doc.id,
                                        name = doc.getString("name") ?: "",
                                        description = doc.getString("description") ?: "",
                                        category = ProductCategory.valueOf(
                                            doc.getString("category") ?: "OTHER"
                                        ),
                                        barcode = doc.getString("barcode"),
                                        unit = ProductUnit.valueOf(
                                            doc.getString("unit") ?: "UNIT"
                                        ),
                                        currentStock =
                                        doc.getDouble("currentStock") ?: 0.0,
                                        minimumStock =
                                        doc.getDouble("minimumStock") ?: 0.0,
                                        expiryDate = doc.getDate("expiryDate"),
                                        imageUrl = doc.getString("imageUrl"),
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
                        trySend(Result.Success(products))
                    }
                }

            awaitClose { listener.remove() }
        }

    override suspend fun recordStockMovement(
        movement: StockMovement
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()

            // 1. Registar movimentação
            val movementData = hashMapOf(
                "productId" to movement.productId,
                "productName" to movement.productName,
                "type" to movement.type.name,
                "quantity" to movement.quantity,
                "unit" to movement.unit.name,
                "reason" to movement.reason,
                "referenceDocument" to movement.referenceDocument,
                "performedBy" to movement.performedBy,
                "performedAt" to Date(),
                "notes" to movement.notes
            )

            val movementRef = movementsCollection.document()
            batch.set(movementRef, movementData)

            // 2. Atualizar stock do produto
            val productRef = productsCollection.document(movement.productId)

            when (movement.type) {
                MovementType.ENTRY -> {
                    batch.update(
                        productRef,
                        "currentStock",
                        com.google.firebase.firestore.FieldValue.increment(
                            movement.quantity
                        )
                    )
                }

                MovementType.EXIT -> {
                    batch.update(
                        productRef,
                        "currentStock",
                        com.google.firebase.firestore.FieldValue.increment(
                            -movement.quantity
                        )
                    )
                }

                MovementType.ADJUSTMENT -> {
                    // Para ajuste, definir valor absoluto (não usar increment)
                    batch.update(productRef, "currentStock", movement.quantity)
                }

                MovementType.TRANSFER -> {
                    // Transfer pode ser implementado com duas movimentações
                    batch.update(
                        productRef,
                        "currentStock",
                        com.google.firebase.firestore.FieldValue.increment(
                            -movement.quantity
                        )
                    )
                }
            }

            batch.update(productRef, "updatedAt", Date())

            batch.commit().await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao registar movimentação: ${e.message}")
        }
    }

    override fun getStockMovementsByProduct(
        productId: String
    ): Flow<Result<List<StockMovement>>> = callbackFlow {
        val listener = movementsCollection
            .whereEqualTo("productId", productId)
            .orderBy("performedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, "Erro ao obter movimentações"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val movements = snapshot.documents.mapNotNull { doc ->
                        try {
                            StockMovement(
                                id = doc.id,
                                productId = doc.getString("productId") ?: "",
                                productName = doc.getString("productName") ?: "",
                                type = MovementType.valueOf(
                                    doc.getString("type") ?: "ENTRY"
                                ),
                                quantity = doc.getDouble("quantity") ?: 0.0,
                                unit = ProductUnit.valueOf(
                                    doc.getString("unit") ?: "UNIT"
                                ),
                                reason = doc.getString("reason") ?: "",
                                referenceDocument = doc.getString("referenceDocument"),
                                performedBy = doc.getString("performedBy") ?: "",
                                performedAt = doc.getDate("performedAt") ?: Date(),
                                notes = doc.getString("notes") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(Result.Success(movements))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getAllStockMovements(): Flow<Result<List<StockMovement>>> =
        callbackFlow {
            val listener = movementsCollection
                .orderBy("performedAt", Query.Direction.DESCENDING)
                .limit(100) // Limitar para evitar sobrecarga
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao obter movimentações"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val movements = snapshot.documents.mapNotNull { doc ->
                            try {
                                StockMovement(
                                    id = doc.id,
                                    productId = doc.getString("productId") ?: "",
                                    productName = doc.getString("productName") ?: "",
                                    type = MovementType.valueOf(
                                        doc.getString("type") ?: "ENTRY"
                                    ),
                                    quantity = doc.getDouble("quantity") ?: 0.0,
                                    unit = ProductUnit.valueOf(
                                        doc.getString("unit") ?: "UNIT"
                                    ),
                                    reason = doc.getString("reason") ?: "",
                                    referenceDocument =
                                    doc.getString("referenceDocument"),
                                    performedBy = doc.getString("performedBy") ?: "",
                                    performedAt = doc.getDate("performedAt") ?: Date(),
                                    notes = doc.getString("notes") ?: ""
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(Result.Success(movements))
                    }
                }

            awaitClose { listener.remove() }
        }
}
