package com.ipca.lojasocial.data.datasource.firebase.messaging

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to manage FCM tokens and topic subscriptions
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseMessaging: FirebaseMessaging
) {

    /**
     * Get current FCM token
     */
    suspend fun getCurrentToken(): String? {
        return try {
            firebaseMessaging.token.await()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Subscribe to a topic
     * Topics are created automatically when the first user subscribes
     */
    suspend fun subscribeToTopic(topic: String): Boolean {
        return try {
            firebaseMessaging.subscribeToTopic(topic).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Unsubscribe from a topic
     */
    suspend fun unsubscribeFromTopic(topic: String): Boolean {
        return try {
            firebaseMessaging.unsubscribeFromTopic(topic).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Subscribe user to topics based on their role
     */
    suspend fun subscribeToUserTopics(userRole: String) {
        // All users subscribe to general topic
        subscribeToTopic(TOPIC_ALL_USERS)

        when (userRole) {
            "BENEFICIARY" -> {
                subscribeToTopic(TOPIC_BENEFICIARIES)
                subscribeToTopic(TOPIC_DELIVERY_REMINDERS)
            }
            "COLLABORATOR", "ADMINISTRATOR" -> {
                subscribeToTopic(TOPIC_COLLABORATORS)
            }
        }
    }

    /**
     * Unsubscribe from all topics
     */
    suspend fun unsubscribeFromAllTopics() {
        unsubscribeFromTopic(TOPIC_ALL_USERS)
        unsubscribeFromTopic(TOPIC_BENEFICIARIES)
        unsubscribeFromTopic(TOPIC_COLLABORATORS)
        unsubscribeFromTopic(TOPIC_DELIVERY_REMINDERS)
    }

    /**
     * Save FCM token to Firestore (optional, for direct messaging)
     */
    suspend fun saveTokenToFirestore(userId: String, token: String) {
        // TODO: Implement saving token to Firestore user document
        // This is useful if you want to send direct notifications to specific users
        // val firestore = Firebase.firestore
        // firestore.collection("users").document(userId)
        //     .update("fcmToken", token).await()
    }

    companion object {
        // Topic names - these are created automatically when first user subscribes
        const val TOPIC_ALL_USERS = "all_users"
        const val TOPIC_BENEFICIARIES = "beneficiaries"
        const val TOPIC_COLLABORATORS = "collaborators"
        const val TOPIC_DELIVERY_REMINDERS = "delivery_reminders"
    }
}
