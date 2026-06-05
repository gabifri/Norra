package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BubbleBurstPanel(vibrationEnabled: Boolean = true, onFinished: () -> Unit) {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var isSoundToggleEnabled by remember { mutableStateOf(true) }
    var isHapticToggleEnabled by remember { mutableStateOf(vibrationEnabled) }

    var thoughtInput by remember { mutableStateOf("") }
    var bubbles by remember { mutableStateOf(listOf<Pair<Int, String>>()) }
    var bubbleIdCounter by remember { mutableStateOf(0) }
    var poppedCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Crisp Sound & Haptic Quick Toggles Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { isSoundToggleEnabled = !isSoundToggleEnabled }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Toggle Son",
                    tint = if (isSoundToggleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = if (isSoundToggleEnabled) "Son Actif" else "Muet",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { isHapticToggleEnabled = !isHapticToggleEnabled }) {
                Icon(
                    imageVector = if (isHapticToggleEnabled) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = "Toggle Vibration",
                    tint = if (isHapticToggleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = if (isHapticToggleEnabled) "Vibration" else "Désactivé",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "Écrivez une pensée intrusive ou anxieuse :",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = thoughtInput,
                onValueChange = { thoughtInput = it },
                label = { Text("Votre pensée...") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (thoughtInput.isNotBlank()) {
                        bubbles = bubbles + (bubbleIdCounter++ to thoughtInput)
                        thoughtInput = ""
                    }
                },
                enabled = thoughtInput.isNotBlank()
            ) {
                Text("Ajouter")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (bubbles.isEmpty() && poppedCount > 0) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Toutes vos pensées ont été éclatées !", color = Color(0xFF4CAF50), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { poppedCount = 0; onFinished() }) {
                Text("Terminer")
            }
        } else if (bubbles.isNotEmpty()) {
            Text(
                text = "Touchez les bulles pour les détruire !",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ), label = "bubble_anim"
            )

            androidx.compose.foundation.lazy.LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                val chunkedBubbles = bubbles.chunked(2)
                items(chunkedBubbles) { rowBubbles ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowBubbles.forEach { bubble ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .scale(scale)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFCDD2))
                                    .clickable { 
                                        bubbles = bubbles.filter { it.first != bubble.first }
                                        poppedCount++

                                        // 1. Core Android hardware vibration and Compose backup haptics
                                        if (isHapticToggleEnabled) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            try {
                                                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                                                    vibratorManager?.defaultVibrator
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                                }
                                                if (vibrator != null && vibrator.hasVibrator()) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        vibrator.vibrate(80)
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("NorraVib", "Hardware vibration failure", e)
                                            }
                                        }

                                        // 2. Play beautiful native "poc/pop" sound
                                        if (isSoundToggleEnabled) {
                                            try {
                                                view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                                            } catch (e: Exception) {}
                                            try {
                                                val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 90)
                                                // TONE_CDMA_PIP is a super swift, sharp "pop/poc/click" sound!
                                                toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 45)
                                            } catch (e: Exception) {}
                                        }
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = bubble.second,
                                    fontSize = 14.sp,
                                    color = Color(0xFFC62828),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 4,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (rowBubbles.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TactileBreathePanel(globalVibrationEnabled: Boolean = true, onFinished: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var isHolding by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }
    var vibrationEnabled by remember { mutableStateOf(globalVibrationEnabled) }
    
    val context = LocalContext.current
    LaunchedEffect(isHolding, vibrationEnabled, globalVibrationEnabled) {
        if (!vibrationEnabled || !globalVibrationEnabled) return@LaunchedEffect
        if (isHolding) {
            var elapsed = 0
            while (elapsed < 4000) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                        vibratorManager?.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    }
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(80, 200))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(80)
                        }
                    }
                } catch (e: Exception) {}
                delay(300)
                elapsed += 300
            }
        } else if (hasStarted && !isHolding) {
            var elapsed = 0
            while (elapsed < 6000) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                        vibratorManager?.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    }
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(40, 100))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(40)
                        }
                    }
                } catch (e: Exception) {}
                delay(600)
                elapsed += 600
            }
        }
    }

    val targetScale = if (isHolding) 1.5f else 1.0f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 4000, easing = LinearOutSlowInEasing),
        label = "breathe_scale"
    )

    val targetColor = if (isHolding) Color(0xFF4CAF50) else Color(0xFF90CAF9)
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 2000),
        label = "breathe_color"
    )

    val targetText = if (isHolding) "Inspirez..." else if (hasStarted) "Relâchez et expirez lentement..." else "Maintenez la bulle pour inspirer"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Respiration Tactile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Connectez votre toucher à votre souffle.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            if (globalVibrationEnabled) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Vibration", fontSize = 10.sp)
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it },
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(64.dp))

        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(animatedColor.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            hasStarted = true
                            try {
                                awaitRelease()
                            } finally {
                                isHolding = false
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(animatedColor)
            )
        }

        Spacer(modifier = Modifier.height(64.dp))
        
        AnimatedContent(targetState = targetText, label = "text_anim") { txt ->
            Text(
                text = txt,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(onClick = onFinished) {
            Text("J'ai terminé")
        }
    }
}
