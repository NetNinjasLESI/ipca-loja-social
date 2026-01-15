package com.ipca.lojasocial.domain.repository

import com.ipca.lojasocial.domain.model.Delivery
import com.ipca.lojasocial.domain.model.DeliveryStatus
import com.ipca.lojasocial.domain.model.Result
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for Delivery operations
 */
interface DeliveryRepository {

    // ========== CRUD Básico ==========

    fun getAllDeliveries(): Flow<Result<List<Delivery>>>

    suspend fun getDeliveryById(id: String): Result<Delivery>

    suspend fun createDelivery(delivery: Delivery): Result<Delivery>

    // ========== Workflow de Aprovação ==========

    /**
     * Aprovar solicitação de entrega
     */
    suspend fun approveDeliveryRequest(
        deliveryId: String,
        userId: String
    ): Result<Unit>

    /**
     * Rejeitar solicitação de entrega
     */
    suspend fun rejectDeliveryRequest(
        deliveryId: String,
        userId: String,
        reason: String
    ): Result<Unit>

    /**
     * Agendar entrega aprovada
     */
    suspend fun scheduleDelivery(
        deliveryId: String,
        scheduledDate: Date,
        notes: String,
        userId: String
    ): Result<Unit>

    // ========== Ações de Entrega ==========

    /**
     * Confirmar entrega (deduz stock)
     */
    suspend fun confirmDelivery(
        deliveryId: String,
        userId: String
    ): Result<Unit>

    /**
     * Cancelar entrega
     */
    suspend fun cancelDelivery(
        deliveryId: String,
        userId: String,
        reason: String
    ): Result<Unit>

    // ========== Queries ==========

    fun getDeliveriesByBeneficiary(beneficiaryId: String): Flow<Result<List<Delivery>>>

    fun getDeliveriesByStatus(status: DeliveryStatus): Flow<Result<List<Delivery>>>

    fun searchDeliveries(query: String): Flow<Result<List<Delivery>>>
}