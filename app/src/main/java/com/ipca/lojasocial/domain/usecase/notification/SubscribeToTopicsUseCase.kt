package com.ipca.lojasocial.domain.usecase.notification

import com.ipca.lojasocial.data.datasource.firebase.messaging.NotificationHelper
import com.ipca.lojasocial.domain.model.UserRole
import javax.inject.Inject

/**
 * Use case to subscribe user to appropriate notification topics
 */
class SubscribeToTopicsUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    suspend operator fun invoke(userRole: UserRole) {
        notificationHelper.subscribeToUserTopics(userRole.name)
    }
}
