package com.ipca.lojasocial.domain.model

import java.util.Date

/**
 * Domain model representing a User in the system
 */
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val profileImageUrl: String? = null
)

enum class UserRole {
    USER,
    BENEFICIARY,
    COLLABORATOR,
    ADMINISTRATOR
}
