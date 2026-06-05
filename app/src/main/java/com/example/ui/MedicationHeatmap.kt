package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Medication
import com.example.data.MedicationLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MedicationHeatmap(
    medications: List<Medication>,
    logs: List<MedicationLog>,
    modifier: Modifier = Modifier
) {
    if (medications.isEmpty()) return

    var monthOffset by remember { mutableIntStateOf(0) }

    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, monthOffset)
    cal.set(Calendar.DAY_OF_MONTH, 1) // Start of month

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val currentMonthName = monthFormat.format(cal.time).replaceFirstChar { it.uppercase() }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // total expected intakes per day
    val totalExpected = medications.sumOf { med ->
        if (med.intakes.isNotBlank()) med.intakes.split(";").filter { it.isNotBlank() }.size 
        else 1 
    }

    val dailyAdherence = mutableMapOf<String, Float>()
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = format.format(Calendar.getInstance().time)

    for (i in 1..daysInMonth) {
        cal.set(Calendar.DAY_OF_MONTH, i)
        val dateString = format.format(cal.time)
        
        val logsForDate = logs.filter { it.dateString == dateString }
        val takenCount = logsForDate.count { it.isTaken }
        
        val percentage = if (totalExpected > 0) {
            takenCount.toFloat() / totalExpected.toFloat()
        } else {
            0f
        }
        dailyAdherence[dateString] = percentage.coerceIn(0f, 1f)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { monthOffset-- }) {
                Icon(Icons.Default.KeyboardArrowLeft, "Mois précédent")
            }
            Text(currentMonthName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            IconButton(onClick = { monthOffset++ }, enabled = monthOffset < 0) {
                Icon(Icons.Default.KeyboardArrowRight, "Mois suivant")
            }
        }
        
        // Month calendar grid
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7 // Monday = 0
        
        val daysList = mutableListOf<String?>()
        for (i in 0 until startDayOfWeek) daysList.add(null)
        for (i in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            daysList.add(format.format(cal.time))
        }

        val weeks = daysList.chunked(7)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            weeks.forEach { weekDays ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    weekDays.forEach { dateString ->
                        if (dateString == null) {
                            Spacer(modifier = Modifier.size(24.dp))
                        } else {
                            val ratio = dailyAdherence[dateString] ?: 0f
                            val isPastOrToday = dateString <= todayStr
                            val isToday = dateString == todayStr
                            
                            val boxColor = when {
                                ratio == 0f && logs.any { it.dateString == dateString && it.isTaken } -> Color(0xFFFFB74D) // Some partial earlier?
                                ratio == 0f && isPastOrToday && logs.any { it.dateString == dateString } -> Color(0xFFE57373)
                                ratio == 0f -> MaterialTheme.colorScheme.surfaceVariant
                                ratio > 0f && ratio < 1f -> Color(0xFFFFB74D) // Orange
                                ratio == 1f -> Color(0xFF81C784) // Green
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            
                            val borderModifier = if (isToday) {
                                Modifier.border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                            } else Modifier
                            
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(boxColor)
                                    .then(borderModifier)
                            )
                        }
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem(Color(0xFF81C784), "100%")
            LegendItem(Color(0xFFFFB74D), "Partiel")
            LegendItem(Color(0xFFE57373), "Oublié")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
