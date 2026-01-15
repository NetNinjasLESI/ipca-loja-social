package com.ipca.lojasocial.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.ApplicationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

/**
 * ✅ REPOSITORY CORRIGIDO com campos corretos do modelo
 */
class ApplicationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ApplicationRepository {

    private val applicationsCollection = firestore.collection("beneficiary_applications")

    override fun getAllApplications(): Flow<Result<List<BeneficiaryApplication>>> =
        callbackFlow {
            val listener = applicationsCollection
                .orderBy("appliedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao obter candidaturas"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val applications = snapshot.documents.mapNotNull { doc ->
                            documentToApplication(doc)
                        }

                        android.util.Log.d("AppRepo", "getAllApplications: ${applications.size} total")

                        trySend(Result.Success(applications))
                    }
                }

            awaitClose { listener.remove() }
        }

    /**
     * ✅ CORRIGIDO: Filtra por status no Firestore
     */
    override fun getApplicationsByStatus(
        status: ApplicationStatus
    ): Flow<Result<List<BeneficiaryApplication>>> = callbackFlow {

        // ✅ LOG para debug
        android.util.Log.d("AppRepo", "Iniciando consulta para status: ${status.name}")

        val listener = applicationsCollection
            .whereEqualTo("status", status.name)  // ✅ CRÍTICO - Filtro no Firestore
            .orderBy("appliedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("AppRepo", "Erro na consulta: ${error.message}")
                    trySend(Result.Error(error, "Erro ao obter candidaturas"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    android.util.Log.d("AppRepo", "Firestore retornou: ${snapshot.documents.size} documentos")

                    val applications = snapshot.documents.mapNotNull { doc ->
                        val app = documentToApplication(doc)

                        // ✅ LOG detalhado
                        if (app != null) {
                            android.util.Log.d(
                                "AppRepo",
                                "  Doc ${doc.id}: userName=${app.userName}, status=${app.status}"
                            )

                            // ✅ VERIFICAÇÃO DE SEGURANÇA
                            if (app.status != status) {
                                android.util.Log.e(
                                    "AppRepo",
                                    "⚠️ ERRO: Doc tem status ${app.status} mas queríamos $status"
                                )
                            }
                        }

                        app
                    }

                    // ✅ Filtro extra de segurança
                    val filteredApps = applications.filter { it.status == status }

                    android.util.Log.d("AppRepo", "Após filtro: ${filteredApps.size} candidaturas")

                    trySend(Result.Success(filteredApps))
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getApplicationById(
        id: String
    ): Result<BeneficiaryApplication> {
        return try {
            val doc = applicationsCollection.document(id).get().await()

            if (doc.exists()) {
                val application = documentToApplication(doc)
                    ?: return Result.Error(Exception("Erro ao converter candidatura"))

                Result.Success(application)
            } else {
                Result.Error(Exception("Candidatura não encontrada"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter candidatura: ${e.message}")
        }
    }

    override suspend fun getApplicationByUserId(
        userId: String
    ): Result<BeneficiaryApplication> {
        return try {
            val snapshot = applicationsCollection
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                val application = documentToApplication(doc)
                    ?: return Result.Error(Exception("Erro ao converter candidatura"))

                Result.Success(application)
            } else {
                Result.Error(Exception("Candidatura não encontrada"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter candidatura: ${e.message}")
        }
    }

    override suspend fun createApplication(
        application: BeneficiaryApplication
    ): Result<BeneficiaryApplication> {
        return try {
            // Verificar se já existe candidatura para este utilizador
            val existingSnapshot = applicationsCollection
                .whereEqualTo("userId", application.userId)
                .get()
                .await()

            if (!existingSnapshot.isEmpty) {
                return Result.Error(
                    Exception("Já existe uma candidatura para este utilizador")
                )
            }

            val now = Date()

            val applicationData = hashMapOf(
                // Identificação
                "userId" to application.userId,
                "userName" to application.userName,
                "userEmail" to application.userEmail,

                // Informação Pessoal
                "studentNumber" to application.studentNumber,
                "phone" to application.phone,
                "nif" to application.nif,

                // Informação Académica
                "academicDegree" to application.academicDegree.name,
                "course" to application.course,
                "academicYear" to application.academicYear,

                // Morada
                "address" to application.address,
                "zipCode" to application.zipCode,
                "city" to application.city,

                // Informação Familiar
                "familySize" to application.familySize,
                "monthlyIncome" to application.monthlyIncome,

                // Necessidades Especiais
                "hasSpecialNeeds" to application.hasSpecialNeeds,
                "specialNeedsDescription" to application.specialNeedsDescription,

                // Informação Adicional
                "observations" to application.observations,

                // Status
                "status" to ApplicationStatus.PENDING.name,  // ✅ UPPERCASE
                "appliedAt" to now,

                // Review (null no início)
                "reviewedBy" to null,
                "reviewedByName" to null,
                "reviewedAt" to null,
                "rejectionReason" to null,

                // Timestamps
                "createdAt" to now,
                "updatedAt" to now
            )

            val docRef = applicationsCollection.add(applicationData).await()
            val createdApplication = application.copy(
                id = docRef.id,
                status = ApplicationStatus.PENDING,
                appliedAt = now,
                createdAt = now,
                updatedAt = now
            )

            android.util.Log.d("AppRepo", "Candidatura criada: ${docRef.id} com status PENDING")

            Result.Success(createdApplication)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao criar candidatura: ${e.message}")
        }
    }

    override suspend fun approveApplication(
        applicationId: String,
        reviewedBy: String,
        reviewedByName: String
    ): Result<Unit> {
        return try {
            val now = Date()

            val updates = hashMapOf<String, Any>(
                "status" to ApplicationStatus.APPROVED.name,  // ✅ UPPERCASE
                "reviewedAt" to now,
                "reviewedBy" to reviewedBy,
                "reviewedByName" to reviewedByName,
                "rejectionReason" to "",
                "updatedAt" to now
            )

            applicationsCollection.document(applicationId)
                .update(updates)
                .await()

            android.util.Log.d("AppRepo", "Candidatura $applicationId aprovada")

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao aprovar candidatura: ${e.message}")
        }
    }

    override suspend fun rejectApplication(
        applicationId: String,
        reviewedBy: String,
        reviewedByName: String,
        reason: String
    ): Result<Unit> {
        return try {
            val now = Date()

            val updates = hashMapOf<String, Any>(
                "status" to ApplicationStatus.REJECTED.name,  // ✅ UPPERCASE
                "reviewedAt" to now,
                "reviewedBy" to reviewedBy,
                "reviewedByName" to reviewedByName,
                "rejectionReason" to reason,
                "updatedAt" to now
            )

            applicationsCollection.document(applicationId)
                .update(updates)
                .await()

            android.util.Log.d("AppRepo", "Candidatura $applicationId rejeitada")

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao rejeitar candidatura: ${e.message}")
        }
    }

    override suspend fun hasExistingApplication(userId: String): Result<Boolean> {
        return try {
            val snapshot = applicationsCollection
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            Result.Success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao verificar candidatura: ${e.message}")
        }
    }

    /**
     * ✅ Helper: Converter documento Firestore para BeneficiaryApplication
     * Com todos os campos do modelo correto
     */
    private fun documentToApplication(
        doc: com.google.firebase.firestore.DocumentSnapshot
    ): BeneficiaryApplication? {
        return try {
            // Status
            val statusString = doc.getString("status") ?: "PENDING"
            val status = try {
                ApplicationStatus.valueOf(statusString.uppercase())
            } catch (e: Exception) {
                android.util.Log.e("AppRepo", "Status inválido: $statusString, usando PENDING")
                ApplicationStatus.PENDING
            }

            // Academic Degree
            val degreeString = doc.getString("academicDegree") ?: "BACHELOR"
            val academicDegree = try {
                AcademicDegree.valueOf(degreeString.uppercase())
            } catch (e: Exception) {
                android.util.Log.e("AppRepo", "Grau inválido: $degreeString, usando BACHELOR")
                AcademicDegree.BACHELOR
            }

            BeneficiaryApplication(
                id = doc.id,

                // Identificação
                userId = doc.getString("userId") ?: "",
                userName = doc.getString("userName") ?: "",
                userEmail = doc.getString("userEmail") ?: "",

                // Informação Pessoal
                studentNumber = doc.getString("studentNumber") ?: "",
                phone = doc.getString("phone") ?: "",
                nif = doc.getString("nif") ?: "",

                // Informação Académica
                academicDegree = academicDegree,
                course = doc.getString("course") ?: "",
                academicYear = doc.getLong("academicYear")?.toInt() ?: 1,

                // Morada
                address = doc.getString("address") ?: "",
                zipCode = doc.getString("zipCode") ?: "",
                city = doc.getString("city") ?: "",

                // Informação Familiar
                familySize = doc.getLong("familySize")?.toInt() ?: 1,
                monthlyIncome = doc.getDouble("monthlyIncome") ?: 0.0,

                // Necessidades Especiais
                hasSpecialNeeds = doc.getBoolean("hasSpecialNeeds") ?: false,
                specialNeedsDescription = doc.getString("specialNeedsDescription") ?: "",

                // Informação Adicional
                observations = doc.getString("observations") ?: "",

                // Status
                status = status,
                appliedAt = doc.getDate("appliedAt") ?: Date(),

                // Review
                reviewedBy = doc.getString("reviewedBy"),
                reviewedByName = doc.getString("reviewedByName"),
                reviewedAt = doc.getDate("reviewedAt"),
                rejectionReason = doc.getString("rejectionReason"),

                // Timestamps
                createdAt = doc.getDate("createdAt") ?: Date(),
                updatedAt = doc.getDate("updatedAt") ?: Date()
            )
        } catch (e: Exception) {
            android.util.Log.e("AppRepo", "Erro ao converter documento ${doc.id}: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
