package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.AlarmManager
import android.app.PendingIntent
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.example.data.DatabaseProvider
import com.example.data.NorraRepository
import com.example.data.MedicationLog

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val medId = intent.getLongExtra("MED_ID", 0L)
        val medName = intent.getStringExtra("MED_NAME") ?: "Médicament"
        val medDosage = intent.getIntExtra("MED_DOSAGE", 0)
        val medCount = intent.getIntExtra("MED_COUNT", 0)
        val medTime = intent.getStringExtra("MED_TIME") ?: "08:00"
        val notifId = intent.getIntExtra("NOTIF_ID", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notifId)

        if (action == "ACTION_MARK_TAKEN") {
            CoroutineScope(Dispatchers.IO).launch {
                val db = DatabaseProvider.getDatabase(context)
                val repository = NorraRepository(db.norraDao())
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                
                if (medId == 99999L) {
                    val allMeds = repository.allMedications.first()
                    allMeds.forEach { med ->
                        val existingLog = repository.getMedicationLogSync(med.id, todayStr, 0)
                        if (existingLog != null) {
                            repository.insertMedicationLog(existingLog.copy(isTaken = true))
                        } else {
                            repository.insertMedicationLog(MedicationLog(medicationId = med.id, dateString = todayStr, isTaken = true, intakeIndex = 0))
                        }
                    }
                } else {
                    val existingLog = repository.getMedicationLogSync(medId, todayStr, 0)
                    if (existingLog != null) {
                        repository.insertMedicationLog(existingLog.copy(isTaken = true))
                    } else {
                        repository.insertMedicationLog(MedicationLog(medicationId = medId, dateString = todayStr, isTaken = true, intakeIndex = 0))
                    }
                }
            }
        } else if (action == "ACTION_SNOOZE") {
            // Schedule an alarm for 30 minutes from now
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val newIntent = Intent(context, MedicationReceiver::class.java).apply {
                putExtra("MED_ID", medId)
                putExtra("MED_NAME", medName)
                putExtra("MED_DOSAGE", medDosage)
                putExtra("MED_COUNT", medCount)
                putExtra("MED_TIME", medTime)
                putExtra("IS_SNOOZE", true) // So we don't re-schedule for tomorrow in the Snooze receiver 
            }
            val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, medId.toInt() + 10000, newIntent, flags)
            
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.MINUTE, 30)
            
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val info = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
                    alarmManager.setAlarmClock(info, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }
    }
}
