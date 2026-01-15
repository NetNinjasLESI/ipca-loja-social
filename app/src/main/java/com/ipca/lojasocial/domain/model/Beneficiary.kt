package com.ipca.lojasocial.domain.model

import java.util.Date

/**
 * Graus académicos disponíveis no IPCA
 */
enum class AcademicDegree(val displayName: String, val years: Int) {
    CTESP("CTeSP - Curso Técnico Superior Profissional", 2),
    BACHELOR("Licenciatura", 3),
    MASTER("Mestrado", 2);

    companion object {
        fun getYears(degree: AcademicDegree): List<Int> {
            return (1..degree.years).toList()
        }
    }
}

/**
 * Cursos organizados por grau académico
 */
object AcademicCourses {

    val CTESP_COURSES = listOf(
        "Tecnologias e Programação em Sistemas de Informação",
        "Design e Desenvolvimento de Websites",
        "Apoio à Gestão",
        "Contabilidade e Fiscalidade",
        "Gestão Comercial e Vendas"
    )

    val BACHELOR_COURSES = listOf(
        "Contabilidade",
        "Design Audiovisual",
        "Design Gráfico",
        "Design Industrial",
        "Desporto",
        "Engenharia Eletrotécnica e de Computadores",
        "Engenharia Informática Médica",
        "Engenharia de Dados e Inteligência Artificial",
        "Engenharia de Sistemas Informáticos",
        "Engenharia e Gestão Industrial",
        "Engenharia em Desenvolvimento de Jogos Digitais",
        "Finanças",
        "Fiscalidade",
        "Gastronomia e Sustentabilidade Alimentar",
        "Gestão Hoteleira",
        "Gestão Pública",
        "Gestão de Atividades Turísticas",
        "Gestão de Empresas",
        "Solicitadoria"
    )

    var MASTER_COURSES = listOf(
        "Auditoria",
        "Design Digital",
        "Design e Desenvolvimento do Produto",
        "Engenharia Informática"
    )

    fun getCoursesByDegree(degree: AcademicDegree): List<String> {
        return when (degree) {
            AcademicDegree.CTESP -> CTESP_COURSES
            AcademicDegree.BACHELOR -> BACHELOR_COURSES
            AcademicDegree.MASTER -> MASTER_COURSES
        }
    }
}

/**
 * Domain model representing a Beneficiary (student)
 */
data class Beneficiary(
    val id: String = "",
    val userId: String = "",

    // Identificação
    val studentNumber: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",

    // Dados Académicos
    val academicDegree: AcademicDegree = AcademicDegree.BACHELOR,
    val course: String = "",
    val academicYear: Int = 1,

    // Morada
    val address: String = "",
    val zipCode: String = "",
    val city: String = "",

    // Dados Sociais
    val nif: String? = null,
    val familySize: Int = 1,
    val monthlyIncome: Double = 0.0,

    // Necessidades Especiais
    val hasSpecialNeeds: Boolean = false,
    val specialNeedsDescription: String? = null,

    // Outros
    val observations: String = "",
    val isActive: Boolean = true,

    // Timestamps
    val registeredAt: Date = Date(),
    val registeredBy: String = "",
    val updatedAt: Date = Date()
)

/**
 * Domain model representing a Beneficiary Application
 */
data class BeneficiaryApplication(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",

    // Personal Information
    val studentNumber: String = "",
    val phone: String = "",
    val nif: String = "",

    // Academic Information
    val academicDegree: AcademicDegree = AcademicDegree.BACHELOR,
    val course: String = "",
    val academicYear: Int = 1,

    // Address Information
    val address: String = "",
    val zipCode: String = "",
    val city: String = "",

    // Family Information
    val familySize: Int = 1,
    val monthlyIncome: Double = 0.0,

    // Special Needs
    val hasSpecialNeeds: Boolean = false,
    val specialNeedsDescription: String = "",

    // Additional Information
    val observations: String = "",

    // Application Status
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val appliedAt: Date = Date(),

    // Review Information
    val reviewedBy: String? = null,
    val reviewedByName: String? = null,
    val reviewedAt: Date? = null,
    val rejectionReason: String? = null,

    // Timestamps
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class ApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED
}