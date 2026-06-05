package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat

class MedicationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isPsyReminder = intent.getBooleanExtra("IS_PSY_REMINDER", false)
        if (isPsyReminder) {
            val psyMsg = intent.getStringExtra("PSY_MSG") ?: "C'est l'heure de ton rendez-vous psy !"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channelId = "psy_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Rappels Suivi Psy",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Rappels pour vos séances de psychologie et vos notes"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val launchIntent = Intent(context, com.example.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("GOTO_SCREEN", "psy_notes")
            }
            
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, 10001, launchIntent, flags)
            
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(com.example.R.drawable.ic_launcher_foreground)
                .setContentTitle("Suivi Thérapeutique 💪")
                .setContentText(psyMsg)
                .setStyle(NotificationCompat.BigTextStyle().bigText(psyMsg))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            try {
                notificationManager.notify(20002, notification)
            } catch (e: Exception) {
                android.util.Log.e("MedicationReceiver", "Failed to show psy notification", e)
            }
            return
        }

        val medId = intent.getLongExtra("MED_ID", 0L)
        val medName = intent.getStringExtra("MED_NAME") ?: "Médicament"
        val medDosage = intent.getIntExtra("MED_DOSAGE", 0)
        val medCount = intent.getIntExtra("MED_COUNT", 0)
        val medTime = intent.getStringExtra("MED_TIME") ?: "08:00"
        
        val isSnooze = intent.getBooleanExtra("IS_SNOOZE", false)
        val isGlobal = medId == 99999L
        
        // Re-schedule for next day only if it's the normal alarm, not a snooze
        if (!isSnooze) {
            if (isGlobal) {
                com.example.util.NotificationHelper.scheduleGlobalAlarm(context, medTime, true)
            } else {
                com.example.util.NotificationHelper.scheduleMedicationAlarm(
                    context, medId, medName, medDosage, medCount, medTime
                )
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "medication_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels Médicaments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Rappels pour prendre vos médicaments"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val launchIntent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent, flags
        )

        val notifId = if (isGlobal) 99999 else System.currentTimeMillis().toInt()

        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_MARK_TAKEN"
            putExtra("MED_ID", medId)
            putExtra("NOTIF_ID", notifId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(context, notifId, takenIntent, flags)

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("MED_ID", medId)
            putExtra("MED_NAME", medName)
            putExtra("MED_DOSAGE", medDosage)
            putExtra("MED_COUNT", medCount)
            putExtra("MED_TIME", medTime)
            putExtra("NOTIF_ID", notifId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(context, notifId + 1, snoozeIntent, flags)

        val title = if (isGlobal) {
            "C'est l'heure de tes médicaments \uD83D\uDC8A"
        } else {
            "Coucou ! Il est l'heure de $medName \uD83E\uDE77"
        }
        
        val contentTxt = if (isGlobal) {
            "Prends un petit moment pour toi et n'oublie pas ton traitement. Tu en es capable ! \uD83D\uDC96"
        } else {
            "Prends un petit moment pour toi et n'oublie pas ton traitement ($medCount x ${medDosage}mg). Tu en es capable ! \uD83D\uDC96"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentTxt)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentTxt))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(com.example.R.drawable.ic_launcher_foreground, "C'est fait !", takenPendingIntent)
            .addAction(com.example.R.drawable.ic_launcher_foreground, "Plus tard (30m)", snoozePendingIntent)
            .build()

        try {
            notificationManager.notify(notifId, notification)
        } catch (e: Exception) {
            android.util.Log.e("MedicationReceiver", "Failed to show notification", e)
        }
    }
}
