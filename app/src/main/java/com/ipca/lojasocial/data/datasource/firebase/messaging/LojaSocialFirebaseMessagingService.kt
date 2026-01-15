package com.ipca.lojasocial.data.datasource.firebase.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ipca.lojasocial.R
import com.ipca.lojasocial.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service to handle Firebase Cloud Messaging
 */
@AndroidEntryPoint
class LojaSocialFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: NotificationHelper

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Loja Social IPCA"
            val body = notification.body ?: ""

            // Get notification type from data
            val type = remoteMessage.data["type"] ?: "INFO"

            showNotification(title, body, type, remoteMessage.data)
        }

        // Handle data payload (when app is in foreground)
        if (remoteMessage.data.isNotEmpty()) {
            handleDataPayload(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Send token to server or save locally
        // This will be called when the token is generated or refreshed
        saveTokenToPreferences(token)

        // TODO: Send token to your backend server if needed
        // sendTokenToServer(token)
    }

    private fun showNotification(
        title: String,
        body: String,
        type: String,
        data: Map<String, String>
    ) {
        val channelId = when (type) {
            "DELIVERY" -> CHANNEL_DELIVERIES
            "ALERT" -> CHANNEL_ALERTS
            "CAMPAIGN" -> CHANNEL_CAMPAIGNS
            else -> CHANNEL_GENERAL
        }

        // Create notification channel
        createNotificationChannel(channelId)

        // Intent to open app when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add data to intent
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to create this icon
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (name, description, importance) = when (channelId) {
                CHANNEL_DELIVERIES -> Triple(
                    "Entregas",
                    "Notificações sobre entregas agendadas e atualizações",
                    NotificationManager.IMPORTANCE_HIGH
                )
                CHANNEL_ALERTS -> Triple(
                    "Alertas",
                    "Alertas importantes sobre stock e produtos",
                    NotificationManager.IMPORTANCE_HIGH
                )
                CHANNEL_CAMPAIGNS -> Triple(
                    "Campanhas",
                    "Notificações sobre campanhas de doação",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                else -> Triple(
                    "Geral",
                    "Notificações gerais",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            }

            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Handle different types of data payloads
        when (data["type"]) {
            "DELIVERY" -> {
                // Handle delivery notification
                val deliveryId = data["deliveryId"]
                // You can broadcast this to update UI if app is open
            }
            "STOCK_ALERT" -> {
                // Handle stock alert
                val productId = data["productId"]
            }
            "CAMPAIGN" -> {
                // Handle campaign notification
                val campaignId = data["campaignId"]
            }
        }
    }

    private fun saveTokenToPreferences(token: String) {
        // Save to SharedPreferences or DataStore
        val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }

    companion object {
        private const val CHANNEL_GENERAL = "general"
        private const val CHANNEL_DELIVERIES = "deliveries"
        private const val CHANNEL_ALERTS = "alerts"
        private const val CHANNEL_CAMPAIGNS = "campaigns"
    }
}
