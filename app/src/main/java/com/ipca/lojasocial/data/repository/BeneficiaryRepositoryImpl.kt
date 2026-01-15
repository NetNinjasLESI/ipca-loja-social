package com.ipca.lojasocial.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipca.lojasocial.domain.model.*
import com.ipca.lojasocial.domain.repository.BeneficiaryRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

/**
 * Implementação do BeneficiaryRepository usando Firestore
 */
class BeneficiaryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BeneficiaryRepository {

    private val beneficiariesCollection = firestore.collection("beneficiaries")

    override fun getAllBeneficiaries(): Flow<Result<List<Beneficiary>>> =
        callbackFlow {
            val listener = beneficiariesCollection
                .orderBy("studentNumber", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao obter beneficiários"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val beneficiaries = snapshot.documents.mapNotNull { doc ->
                            try {
                                Beneficiary(
                                    id = doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    studentNumber = doc.getString("studentNumber") ?: "",
                                    name = doc.getString("name") ?: "",
                                    email = doc.getString("email") ?: "",
                                    phone = doc.getString("phone") ?: "",
                                    academicDegree = try {
                                        AcademicDegree.valueOf(doc.getString("academicDegree") ?: "BACHELOR")
                                    } catch (e: Exception) {
                                        AcademicDegree.BACHELOR
                                    },
                                    course = doc.getString("course") ?: "",
                                    academicYear = doc.getLong("academicYear")?.toInt() ?: 1,
                                    address = doc.getString("address") ?: "",
                                    zipCode = doc.getString("zipCode") ?: "",
                                    city = doc.getString("city") ?: "",
                                    nif = doc.getString("nif"),
                                    familySize = doc.getLong("familySize")?.toInt() ?: 1,
                                    monthlyIncome = doc.getDouble("monthlyIncome") ?: 0.0,
                                    hasSpecialNeeds = doc.getBoolean("hasSpecialNeeds") ?: false,
                                    specialNeedsDescription = doc.getString("specialNeedsDescription"),
                                    observations = doc.getString("observations") ?: "",
                                    isActive = doc.getBoolean("isActive") ?: true,
                                    registeredAt = doc.getDate("registeredAt") ?: Date(),
                                    registeredBy = doc.getString("registeredBy") ?: "",
                                    updatedAt = doc.getDate("updatedAt") ?: Date()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(Result.Success(beneficiaries))
                    }
                }

            awaitClose { listener.remove() }
        }

    override suspend fun getBeneficiaryById(id: String): Result<Beneficiary> {
        return try {
            val doc = beneficiariesCollection.document(id).get().await()

            if (doc.exists()) {
                val beneficiary = Beneficiary(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    studentNumber = doc.getString("studentNumber") ?: "",
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    academicDegree = try {
                        AcademicDegree.valueOf(doc.getString("academicDegree") ?: "BACHELOR")
                    } catch (e: Exception) {
                        AcademicDegree.BACHELOR
                    },
                    course = doc.getString("course") ?: "",
                    academicYear = doc.getLong("academicYear")?.toInt() ?: 1,
                    address = doc.getString("address") ?: "",
                    zipCode = doc.getString("zipCode") ?: "",
                    city = doc.getString("city") ?: "",
                    nif = doc.getString("nif"),
                    familySize = doc.getLong("familySize")?.toInt() ?: 1,
                    monthlyIncome = doc.getDouble("monthlyIncome") ?: 0.0,
                    hasSpecialNeeds = doc.getBoolean("hasSpecialNeeds") ?: false,
                    specialNeedsDescription = doc.getString("specialNeedsDescription"),
                    observations = doc.getString("observations") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    registeredAt = doc.getDate("registeredAt") ?: Date(),
                    registeredBy = doc.getString("registeredBy") ?: "",
                    updatedAt = doc.getDate("updatedAt") ?: Date()
                )
                Result.Success(beneficiary)
            } else {
                Result.Error(Exception("Beneficiário não encontrado"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao obter beneficiário: ${e.message}")
        }
    }

    override suspend fun getBeneficiaryByUserId(userId: String): Result<Beneficiary> {
        return try {
            val snapshot = beneficiariesCollection
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                val beneficiary = Beneficiary(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    studentNumber = doc.getString("studentNumber") ?: "",
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    academicDegree = try {
                        AcademicDegree.valueOf(doc.getString("academicDegree") ?: "BACHELOR")
                    } catch (e: Exception) {
                        AcademicDegree.BACHELOR
                    },
                    course = doc.getString("course") ?: "",
                    academicYear = doc.getLong("academicYear")?.toInt() ?: 1,
                    address = doc.getString("address") ?: "",
                    zipCode = doc.getString("zipCode") ?: "",
                    city = doc.getString("city") ?: "",
                    nif = doc.getString("nif"),
                    familySize = doc.getLong("familySize")?.toInt() ?: 1,
                    monthlyIncome = doc.getDouble("monthlyIncome") ?: 0.0,
                    hasSpecialNeeds = doc.getBoolean("hasSpecialNeeds") ?: false,
                    specialNeedsDescription = doc.getString("specialNeedsDescription"),
                    observations = doc.getString("observations") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    registeredAt = doc.getDate("registeredAt") ?: Date(),
                    registeredBy = doc.getString("registeredBy") ?: "",
                    updatedAt = doc.getDate("updatedAt") ?: Date()
                )
                Result.Success(beneficiary)
            } else {
                Result.Error(Exception("Beneficiário não encontrado"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao buscar beneficiário: ${e.message}")
        }
    }

    override suspend fun getBeneficiaryByStudentNumber(studentNumber: String): Result<Beneficiary> {
        return try {
            val snapshot = beneficiariesCollection
                .whereEqualTo("studentNumber", studentNumber)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                val beneficiary = Beneficiary(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    studentNumber = doc.getString("studentNumber") ?: "",
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    academicDegree = try {
                        AcademicDegree.valueOf(doc.getString("academicDegree") ?: "BACHELOR")
                    } catch (e: Exception) {
                        AcademicDegree.BACHELOR
                    },
                    course = doc.getString("course") ?: "",
                    academicYear = doc.getLong("academicYear")?.toInt() ?: 1,
                    address = doc.getString("address") ?: "",
                    zipCode = doc.getString("zipCode") ?: "",
                    city = doc.getString("city") ?: "",
                    nif = doc.getString("nif"),
                    familySize = doc.getLong("familySize")?.toInt() ?: 1,
                    monthlyIncome = doc.getDouble("monthlyIncome") ?: 0.0,
                    hasSpecialNeeds = doc.getBoolean("hasSpecialNeeds") ?: false,
                    specialNeedsDescription = doc.getString("specialNeedsDescription"),
                    observations = doc.getString("observations") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    registeredAt = doc.getDate("registeredAt") ?: Date(),
                    registeredBy = doc.getString("registeredBy") ?: "",
                    updatedAt = doc.getDate("updatedAt") ?: Date()
                )
                Result.Success(beneficiary)
            } else {
                Result.Error(Exception("Beneficiário não encontrado"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Erro ao buscar beneficiário: ${e.message}")
        }
    }

    override suspend fun createBeneficiary(beneficiary: Beneficiary): Result<Beneficiary> {
        return try {
            val exists = beneficiariesCollection
                .whereEqualTo("studentNumber", beneficiary.studentNumber)
                .get()
                .await()

            if (!exists.isEmpty) {
                return Result.Error(Exception("Número de estudante já registado"))
            }

            val beneficiaryData = hashMapOf(
                "userId" to beneficiary.userId,
                "studentNumber" to beneficiary.studentNumber,
                "name" to beneficiary.name,
                "email" to beneficiary.email,
                "phone" to beneficiary.phone,
                "academicDegree" to beneficiary.academicDegree.name,
                "course" to beneficiary.course,
                "academicYear" to beneficiary.academicYear,
                "address" to beneficiary.address,
                "zipCode" to beneficiary.zipCode,
                "city" to beneficiary.city,
                "nif" to beneficiary.nif,
                "familySize" to beneficiary.familySize,
                "monthlyIncome" to beneficiary.monthlyIncome,
                "hasSpecialNeeds" to beneficiary.hasSpecialNeeds,
                "specialNeedsDescription" to beneficiary.specialNeedsDescription,
                "observations" to beneficiary.observations,
                "isActive" to beneficiary.isActive,
                "registeredAt" to Date(),
                "registeredBy" to beneficiary.registeredBy,
                "updatedAt" to Date()
            )

            val docRef = beneficiariesCollection.add(beneficiaryData).await()
            Result.Success(beneficiary.copy(id = docRef.id, registeredAt = Date()))
        } catch (e: Exception) {
            Result.Error(e, "Erro ao criar beneficiário: ${e.message}")
        }
    }

    override suspend fun updateBeneficiary(beneficiary: Beneficiary): Result<Beneficiary> {
        return try {
            val exists = beneficiariesCollection
                .whereEqualTo("studentNumber", beneficiary.studentNumber)
                .get()
                .await()

            if (!exists.isEmpty && exists.documents[0].id != beneficiary.id) {
                return Result.Error(Exception("Número de estudante já registado"))
            }

            val beneficiaryData = hashMapOf(
                "name" to beneficiary.name,
                "email" to beneficiary.email,
                "phone" to beneficiary.phone,
                "academicDegree" to beneficiary.academicDegree.name,
                "course" to beneficiary.course,
                "academicYear" to beneficiary.academicYear,
                "address" to beneficiary.address,
                "zipCode" to beneficiary.zipCode,
                "city" to beneficiary.city,
                "nif" to beneficiary.nif,
                "familySize" to beneficiary.familySize,
                "monthlyIncome" to beneficiary.monthlyIncome,
                "hasSpecialNeeds" to beneficiary.hasSpecialNeeds,
                "specialNeedsDescription" to beneficiary.specialNeedsDescription,
                "observations" to beneficiary.observations,
                "isActive" to beneficiary.isActive,
                "updatedAt" to Date()
            )

            beneficiariesCollection.document(beneficiary.id)
                .update(beneficiaryData as Map<String, Any>)
                .await()

            Result.Success(beneficiary.copy(updatedAt = Date()))
        } catch (e: Exception) {
            Result.Error(e, "Erro ao atualizar beneficiário: ${e.message}")
        }
    }

    override suspend fun deleteBeneficiary(id: String): Result<Unit> {
        return try {
            beneficiariesCollection.document(id)
                .update("isActive", false, "updatedAt", Date())
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao deletar beneficiário: ${e.message}")
        }
    }

    override suspend fun deactivateBeneficiary(id: String): Result<Unit> {
        return try {
            beneficiariesCollection.document(id)
                .update("isActive", false, "updatedAt", Date())
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao desativar beneficiário: ${e.message}")
        }
    }

    override fun getActiveBeneficiaries(): Flow<Result<List<Beneficiary>>> =
        callbackFlow {
            val listener = beneficiariesCollection
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao obter beneficiários"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val beneficiaries = snapshot.documents.mapNotNull { doc ->
                            try {
                                Beneficiary(
                                    id = doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    studentNumber = doc.getString("studentNumber") ?: "",
                                    name = doc.getString("name") ?: "",
                                    email = doc.getString("email") ?: "",
                                    phone = doc.getString("phone") ?: "",
                                    academicDegree = try {
                                        AcademicDegree.valueOf(doc.getString("academicDegree") ?: "BACHELOR")
                                    } catch (e: Exception) {
                                        AcademicDegree.BACHELOR
                                    },
                                    course = doc.getString("course") ?: "",
                                    academicYear = doc.getLong("academicYear")?.toInt() ?: 1,
                                    address = doc.getString("address") ?: "",
                                    zipCode = doc.getString("zipCode") ?: "",
                                    city = doc.getString("city") ?: "",
                                    nif = doc.getString("nif"),
                                    familySize = doc.getLong("familySize")?.toInt() ?: 1,
                                    monthlyIncome = doc.getDouble("monthlyIncome") ?: 0.0,
                                    hasSpecialNeeds = doc.getBoolean("hasSpecialNeeds") ?: false,
                                    specialNeedsDescription = doc.getString("specialNeedsDescription"),
                                    observations = doc.getString("observations") ?: "",
                                    isActive = doc.getBoolean("isActive") ?: true,
                                    registeredAt = doc.getDate("registeredAt") ?: Date(),
                                    registeredBy = doc.getString("registeredBy") ?: "",
                                    updatedAt = doc.getDate("updatedAt") ?: Date()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        val sortedBeneficiaries = beneficiaries.sortedBy { it.name }
                        trySend(Result.Success(sortedBeneficiaries))
                    }
                }
            awaitClose { listener.remove() }
        }

    override fun searchBeneficiaries(query: String): Flow<Result<List<Beneficiary>>> =
        callbackFlow {
            val searchQuery = query.lowercase()
            val listener = beneficiariesCollection
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao pesquisar beneficiários"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val beneficiaries = snapshot.documents.mapNotNull { doc ->
                            try {
                                val name = doc.getString("name")?.lowercase() ?: ""
                                val studentNumber = doc.getString("studentNumber")?.lowercase() ?: ""
                                val email = doc.getString("email")?.lowercase() ?: ""
                                val course = doc.getString("course")?.lowercase() ?: ""

                                if (name.contains(searchQuery) ||
                                    studentNumber.contains(searchQuery) ||
                                    email.contains(searchQuery) ||
                                    course.contains(searchQuery)
                                ) {
                                    Beneficiary(
                                        id = doc.id,
                                        userId = doc.getString("userId") ?: "",
                                        studentNumber = doc.getString("studentNumber") ?: "",
                                        name = doc.getString("name") ?: "",
                                        email = doc.getString("email") ?: "",
                                        phone = doc.getString("phone") ?: "",
                                        academicDegree = try {
                                            AcademicDegree.valueOf(doc.getString("academicDegree") ?: "BACHELOR")
                                        } catch (e: Exception) {
                                            AcademicDegree.BACHELOR
                                        },
                                        course = doc.getString("course") ?: "",
                                        academicYear = doc.getLong("academicYear")?.toInt() ?: 1,
                                        address = doc.getString("address") ?: "",
                                        zipCode = doc.getString("zipCode") ?: "",
                                        city = doc.getString("city") ?: "",
                                        nif = doc.getString("nif"),
                                        familySize = doc.getLong("familySize")?.toInt() ?: 1,
                                        monthlyIncome = doc.getDouble("monthlyIncome") ?: 0.0,
                                        hasSpecialNeeds = doc.getBoolean("hasSpecialNeeds") ?: false,
                                        specialNeedsDescription = doc.getString("specialNeedsDescription"),
                                        observations = doc.getString("observations") ?: "",
                                        isActive = doc.getBoolean("isActive") ?: true,
                                        registeredAt = doc.getDate("registeredAt") ?: Date(),
                                        registeredBy = doc.getString("registeredBy") ?: "",
                                        updatedAt = doc.getDate("updatedAt") ?: Date()
                                    )
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                        val sortedBeneficiaries = beneficiaries.sortedBy { it.name }
                        trySend(Result.Success(sortedBeneficiaries))
                    }
                }
            awaitClose { listener.remove() }
        }

    override fun getBeneficiariesByCourse(course: String): Flow<Result<List<Beneficiary>>> =
        callbackFlow {
            val listener = beneficiariesCollection
                .whereEqualTo("course", course)
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error, "Erro ao obter beneficiários"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val beneficiaries = snapshot.documents.mapNotNull { doc ->
                            try {
                                Beneficiary(
                                    id = doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    studentNumber = doc.getString("studentNumber") ?: "",
                                    name = doc.getString("name") ?: "",
                                    email = doc.getString("email") ?: "",
                                    phone = doc.getString("phone") ?: "",
                                    academicDegree = try {
                                        AcademicDegree.valueOf(doc.getString("academicDegree") ?: "BACHELOR")
                                    } catch (e: Exception) {
                                        AcademicDegree.BACHELOR
                                    },
                                    course = doc.getString("course") ?: "",
                                    academicYear = doc.getLong("academicYear")?.toInt() ?: 1,
                                    address = doc.getString("address") ?: "",
                                    zipCode = doc.getString("zipCode") ?: "",
                                    city = doc.getString("city") ?: "",
                                    nif = doc.getString("nif"),
                                    familySize = doc.getLong("familySize")?.toInt() ?: 1,
                                    monthlyIncome = doc.getDouble("monthlyIncome") ?: 0.0,
                                    hasSpecialNeeds = doc.getBoolean("hasSpecialNeeds") ?: false,
                                    specialNeedsDescription = doc.getString("specialNeedsDescription"),
                                    observations = doc.getString("observations") ?: "",
                                    isActive = doc.getBoolean("isActive") ?: true,
                                    registeredAt = doc.getDate("registeredAt") ?: Date(),
                                    registeredBy = doc.getString("registeredBy") ?: "",
                                    updatedAt = doc.getDate("updatedAt") ?: Date()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        val sortedBeneficiaries = beneficiaries.sortedBy { it.name }
                        trySend(Result.Success(sortedBeneficiaries))
                    }
                }
            awaitClose { listener.remove() }
        }
}