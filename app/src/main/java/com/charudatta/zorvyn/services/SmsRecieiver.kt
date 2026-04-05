package com.charudatta.zorvyn.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.charudatta.zorvyn.R
import com.charudatta.zorvyn.utils.SmsTransactionParser
import com.charudatta.zorvyn.utils.DetectedTransaction

class SmsReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the action is SMS_RECEIVED
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // Get the SMS messages from the intent
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        for (sms in messages) {
            val body = sms.messageBody ?: continue
            val sender = sms.originatingAddress ?: ""

            // Only process SMS from likely bank senders
            if (!looksLikeBank(sender) && !looksLikeBank(body)) continue

            val detected = SmsTransactionParser.parse(body) ?: continue

            showNotification(context, detected)
        }
    }

    // Heuristic: bank sender IDs are usually uppercase like "HDFCBK", "ICICIB", "PAYTMB"
    private fun looksLikeBank(sender: String): Boolean {
        val upper = sender.uppercase()
        return upper.contains("BANK") ||
                upper.contains("HDFC") ||
                upper.contains("ICICI") ||
                upper.contains("SBI") ||
                upper.contains("AXIS") ||
                upper.contains("KOTAK") ||
                upper.contains("PAYTM") ||
                upper.contains("GPAY") ||
                upper.contains("PHONEPE") ||
                upper.contains("UPI") ||
                upper.contains("NEFT") ||
                upper.contains("IMPS")
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, tx: DetectedTransaction) {
        createNotificationChannel(context)

        val typeLabel = if (tx.type == "expense") "spent" else "received"
        val title = "💰 Transaction Detected"
        val body = tx.merchant
            ?.let { "₹${"%.0f".format(tx.amount)} $typeLabel at $it — tap to log it" }
            ?: "₹${"%.0f".format(tx.amount)} $typeLabel — tap to log it"

        // Deep link into AddTransaction screen with pre-filled args
        val deepLinkUri = Uri.parse("zorvyn://add").buildUpon()
            .appendQueryParameter("amount", tx.amount.toString())
            .appendQueryParameter("type", tx.type)
            .apply { tx.merchant?.let { appendQueryParameter("note", it) } }
            .build()

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            Intent(Intent.ACTION_VIEW, deepLinkUri),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Transaction Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a bank transaction SMS is detected"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "transaction_alerts"
    }
}