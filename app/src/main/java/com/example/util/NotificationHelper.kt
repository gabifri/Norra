package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.receiver.MedicationReceiver
import java.util.Calendar

object NotificationHelper {

    fun scheduleMedicationAlarm(context: Context, medId: Long, name: String, dosageMg: Int, pillCount: Int, timeToTake: String, reminderEnabled: Boolean = true, intakes: String = "") {
        cancelMedicationAlarm(context, medId) // Always cancel previous alarms first
        if (!reminderEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intakeList = mutableListOf<Pair<String, Int>>()
        if (intakes.isNotBlank()) {
            intakes.split(";").forEach { intakeStr ->
                val parts = intakeStr.split(",")
                if (parts.size == 2) {
                    val count = parts[1].toIntOrNull() ?: 1
                    intakeList.add(Pair(parts[0], count))
                }
            }
        } else {
            intakeList.add(Pair(timeToTake, pillCount))
        }

        intakeList.forEachIndexed { index, intake ->
            val time = intake.first
            val count = intake.second
            
            val intent = Intent(context, MedicationReceiver::class.java).apply {
                putExtra("MED_ID", medId)
                putExtra("MED_NAME", name)
                putExtra("MED_DOSAGE", dosageMg)
                putExtra("MED_COUNT", count)
                putExtra("MED_TIME", time)
            }

            val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            // Generate a unique requestCode for each intake by combining medId and index
            val requestCode = (medId * 100 + index).toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flags
            )

            val normalizedTime = time.replace("h", ":").replace(" ", "").replace("H", ":")
            val timeParts = normalizedTime.split(":")
            
            var hour = 8
            var minute = 0
            var parsedSuccessfully = false

            if (timeParts.isNotEmpty()) {
                val parsedHour = timeParts[0].toIntOrNull()
                if (parsedHour != null) {
                    hour = parsedHour
                    if (timeParts.size > 1 && timeParts[1].isNotEmpty()) {
                        minute = timeParts[1].toIntOrNull() ?: 0
                    }
                    parsedSuccessfully = true
                }
            }

            if (!parsedSuccessfully) return@forEachIndexed

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1) // Schedule for next day if time has passed
            }

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val info = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
                    alarmManager.setAlarmClock(info, pendingIntent)
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                // Fallback to inexact if exact alarm permission is missing
                android.util.Log.e("NotificationHelper", "Failed exact alarm, fallback to inexact", e)
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelMedicationAlarm(context: Context, medId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Since we can have up to 10 intakes max (arbitrary safe limit), try to cancel them all
        for (index in 0..10) {
            val intent = Intent(context, MedicationReceiver::class.java)
            val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            // Cancel specific intake intents if they were created with new indexing
            val requestCode = (medId * 100 + index).toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flags
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        
        // Also cancel old format pending intents (using just medId.toInt())
        val intentOld = Intent(context, MedicationReceiver::class.java)
        val flagsOld = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntentOld = PendingIntent.getBroadcast(context, medId.toInt(), intentOld, flagsOld)
        alarmManager.cancel(pendingIntentOld)
        pendingIntentOld.cancel()
    }

    fun scheduleGlobalAlarm(context: Context, timeToTake: String, enabled: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationReceiver::class.java).apply {
            putExtra("MED_ID", 99999L)
            putExtra("MED_NAME", "GLOBAL_ALARM")
            putExtra("MED_TIME", timeToTake)
        }
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 99999, intent, flags)

        if (!enabled) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            return
        }

        val timeParts = timeToTake.replace("h", ":").replace(" ", "").replace("H", ":").split(":")
        var hour = 8
        var minute = 0
        if (timeParts.isNotEmpty()) {
            val parsedHour = timeParts[0].toIntOrNull()
            if (parsedHour != null) {
                hour = parsedHour
                if (timeParts.size > 1 && timeParts[1].isNotEmpty()) {
                    minute = timeParts[1].toIntOrNull() ?: 0
                }
            }
        }

        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

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

    fun testNotification(context: Context) {
        val intent = Intent(context, MedicationReceiver::class.java).apply {
            putExtra("MED_ID", 999L)
            putExtra("MED_NAME", "Test Notification")
            putExtra("MED_DOSAGE", 10)
            putExtra("MED_COUNT", 1)
            putExtra("MED_TIME", "Maintenant")
        }
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            intent,
            flags
        )
        try {
            pendingIntent.send()
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Test notification failed", e)
        }
    }

    fun schedulePsychAppointmentAlarms(context: Context, dateTimeString: String) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        val appointmentTime: java.util.Date
        try {
            appointmentTime = sdf.parse(dateTimeString) ?: return
        } catch (e: Exception) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // Alarm 1: Day of the appointment (2 hours before)
        val dayOfTime = appointmentTime.time - (2L * 60 * 60 * 1000)
        if (dayOfTime > System.currentTimeMillis()) {
            val intent = Intent(context, MedicationReceiver::class.java).apply {
                putExtra("IS_PSY_REMINDER", true)
                putExtra("PSY_MSG", "C'est aujourd'hui ton rendez-vous ! Tu as des choses à lui dire ? Regarde tes notes ici. 🌸")
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 30001, intent, flags)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dayOfTime, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, dayOfTime, pendingIntent)
                }
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, dayOfTime, pendingIntent)
            }
        }

        // Alarm 2: 1 day before
        val oneDayBefore = appointmentTime.time - (24L * 60 * 60 * 1000)
        if (oneDayBefore > System.currentTimeMillis()) {
            val intent = Intent(context, MedicationReceiver::class.java).apply {
                putExtra("IS_PSY_REMINDER", true)
                putExtra("PSY_MSG", "Rappel : Ton rendez-vous psy est demain. Pense à noter tes observations ! 📝")
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 30002, intent, flags)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, oneDayBefore, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, oneDayBefore, pendingIntent)
                }
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, oneDayBefore, pendingIntent)
            }
        }

        // Alarm 3: 7 days before
        val sevenDaysBefore = appointmentTime.time - (7L * 24 * 60 * 60 * 1000)
        if (sevenDaysBefore > System.currentTimeMillis()) {
            val intent = Intent(context, MedicationReceiver::class.java).apply {
                putExtra("IS_PSY_REMINDER", true)
                putExtra("PSY_MSG", "Dans une semaine tu as ton rendez-vous psy ! Note tes humeurs et tes pensées pour ne rien oublier. 💜")
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 30003, intent, flags)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, sevenDaysBefore, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, sevenDaysBefore, pendingIntent)
                }
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, sevenDaysBefore, pendingIntent)
            }
        }

        // Alarm 4: 15 days before
        val fifteenDaysBefore = appointmentTime.time - (15L * 24 * 60 * 60 * 1000)
        if (fifteenDaysBefore > System.currentTimeMillis()) {
            val intent = Intent(context, MedicationReceiver::class.java).apply {
                putExtra("IS_PSY_REMINDER", true)
                putExtra("PSY_MSG", "Ton rendez-vous psy est dans 15 jours. Tu as des choses à noter pour ton rendez-vous ? 🌸")
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 30004, intent, flags)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fifteenDaysBefore, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, fifteenDaysBefore, pendingIntent)
                }
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, fifteenDaysBefore, pendingIntent)
            }
        }
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        // Cancel psych appointment alarms
        for (id in listOf(30001, 30002, 30003, 30004)) {
            val intent = Intent(context, MedicationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, id, intent, flags)
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        
        // Cancel global medication alarm
        val globalIntent = Intent(context, MedicationReceiver::class.java)
        val globalPendingIntent = PendingIntent.getBroadcast(context, 99999, globalIntent, flags)
        alarmManager.cancel(globalPendingIntent)
        globalPendingIntent.cancel()
    }
}
