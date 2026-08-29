package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

enum class NorraEmotion(val id: String, val label: String) {
    DEFAULT("default", "Default"),
    JOIE("joie", "Joie"),
    SEREIN("serein", "Serein"),
    MOTIVE("motive", "Motivé"),
    FIER("fier", "Fier"),
    COLERE("colere", "Colère"),
    TRISTE("triste", "Triste"),
    VIDE("vide", "Vide"),
    SEUL("seul", "Seul"),
    PEUR("peur", "Peur"),
    HONTE("honte", "Honte"),
    IDK("idk", "IDK"),
    FATIGUE("fatigue", "Fatigué"),
    AUTRE("autre", "Autre");

    companion object {
        fun fromId(id: String): NorraEmotion {
            return values().find { it.id.lowercase() == id.lowercase() } ?: DEFAULT
        }
    }
}

@Composable
fun NorraCharacter(
    emotion: NorraEmotion,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    // Elegant hovering and breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "norra_breath")
    val breatheY by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 12f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breathe_y"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val breatheScaleY by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breathe_scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    // Little eye blinking trigger
    val blinkProgress by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.05f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 3500
                    1.0f at 0
                    1.0f at 3200
                    0.05f at 3300
                    1.0f at 3400
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "eye_blink"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Calculate animated center and scale
            val extraY = breatheY
            val scaleCompY = breatheScaleY

            // 1. Defining color schemes
            val isDarkThemeBg = false // Default to light/neutral cards, we will draw custom vibrant gradients

            // Body brush
            val bodyBrush = when (emotion) {
                NorraEmotion.DEFAULT -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF4F8FF), Color(0xFFBACAFC), Color(0xFF95A4FA)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.JOIE -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFF0D4), Color(0xFFFFD185), Color(0xFFFFAB40)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.SEREIN -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFE8F7F0), Color(0xFFAEE4CF), Color(0xFF75CBAD)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.MOTIVE -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFE6E6), Color(0xFFFF9292), Color(0xFFFF4949)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.FIER -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF2EAFE), Color(0xFFC7A2FC), Color(0xFF8B51F5)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.COLERE -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF7A68), Color(0xFFFF3E55), Color(0xFF8D3E93)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.TRISTE -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFB5B7CE), Color(0xFF8E91B0), Color(0xFF5E607D)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.VIDE -> SolidColor(Color.Transparent)
                NorraEmotion.SEUL -> Brush.verticalGradient(
                    colors = listOf(Color(0xFF7C809E), Color(0xFF555977), Color(0xFF2F3249)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.PEUR -> Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F7C2D), Color(0xFF044B15), Color(0xFF012005)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.HONTE -> Brush.verticalGradient(
                    colors = listOf(Color(0xFF9E9BAC), Color(0xFF605F70), Color(0xFF2B2B38)),
                    startY = extraY,
                    endY = height + extraY
                )
                NorraEmotion.IDK -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFDB3), Color(0xFFFFF59D), Color(0xFFCFCA88)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.FATIGUE -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFE8EBFC), Color(0xFFC3CAF5), Color(0xFF9EA9EB)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
                NorraEmotion.AUTRE -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF4F8FF), Color(0xFFBACAFC), Color(0xFF95A4FA)),
                    startY = extraY,
                    endY = height * scaleCompY + extraY
                )
            }

            // Ear Brush
            val earBrush = when (emotion) {
                NorraEmotion.DEFAULT -> Brush.verticalGradient(listOf(Color(0xFF7795FA), Color(0xFF495EE4)))
                NorraEmotion.JOIE -> Brush.verticalGradient(listOf(Color(0xFFFF9A00), Color(0xFFFF5200)))
                NorraEmotion.SEREIN -> Brush.verticalGradient(listOf(Color(0xFF4AC29A), Color(0xFF0F9B77)))
                NorraEmotion.MOTIVE -> Brush.verticalGradient(listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)))
                NorraEmotion.FIER -> Brush.verticalGradient(listOf(Color(0xFF9D50BB), Color(0xFF6E48AA)))
                NorraEmotion.COLERE -> Brush.verticalGradient(listOf(Color(0xFF9F439D), Color(0xFF611C74)))
                NorraEmotion.TRISTE -> Brush.verticalGradient(listOf(Color(0xFF5A6293), Color(0xFF384074)))
                NorraEmotion.VIDE -> SolidColor(Color.Transparent)
                NorraEmotion.SEUL -> Brush.verticalGradient(listOf(Color(0xFF53566E), Color(0xFF303244)))
                NorraEmotion.PEUR -> Brush.verticalGradient(listOf(Color(0xFF54A870), Color(0xFF2E6B43)))
                NorraEmotion.HONTE -> Brush.verticalGradient(listOf(Color(0xFF5E5C6E), Color(0xFF35343D)))
                NorraEmotion.IDK -> Brush.verticalGradient(listOf(Color(0xFF9E9885), Color(0xFF7A7462)))
                NorraEmotion.FATIGUE -> Brush.verticalGradient(listOf(Color(0xFF8B97D8), Color(0xFF6773B8)))
                NorraEmotion.AUTRE -> Brush.verticalGradient(listOf(Color(0xFF7795FA), Color(0xFF495EE4)))
            }

            // Eyes / Details color
            val detailsColor = when (emotion) {
                NorraEmotion.DEFAULT, NorraEmotion.JOIE, NorraEmotion.SEREIN, NorraEmotion.MOTIVE, NorraEmotion.FIER -> Color(0xFF13234E)
                NorraEmotion.COLERE -> Color(0xFF13234E)
                NorraEmotion.TRISTE -> Color(0xFF13234E)
                NorraEmotion.VIDE -> Color(0xFF13234E)
                NorraEmotion.SEUL -> Color(0xFF13234E)
                NorraEmotion.PEUR -> Color(0xFF3BFF75)
                NorraEmotion.HONTE -> Color(0xFF13234E)
                NorraEmotion.IDK -> Color(0xFF13234E)
                NorraEmotion.FATIGUE -> Color(0xFF13234E)
                NorraEmotion.AUTRE -> Color(0xFF13234E)
            }

            // Body Path
            val bodyPath = Path().apply {
                val startY = height * 0.22f + extraY
                val endY = height * 0.88f * scaleCompY + extraY
                val bodyWidthRight = width * 0.88f
                val bodyWidthLeft = width * 0.12f

                moveTo(width / 2, startY)
                // Top-right shoulder curve
                cubicTo(
                    width * 0.78f, startY,
                    width * 0.88f, height * 0.45f + extraY,
                    bodyWidthRight, endY - (height * 0.1f)
                )
                // Bottom-right chubby curve
                quadraticBezierTo(
                    width * 0.88f, endY,
                    width * 0.5f, endY
                )
                // Bottom-left chubby curve
                quadraticBezierTo(
                    width * 0.12f, endY,
                    bodyWidthLeft, endY - (height * 0.1f)
                )
                // Top-left shoulder curve
                cubicTo(
                    width * 0.12f, height * 0.45f + extraY,
                    width * 0.22f, startY,
                    width / 2, startY
                )
                close()
            }

            // Draw shadow/glow behind Norra for magical atmosphere
            if (emotion != NorraEmotion.VIDE) {
                val shadowColor = when (emotion) {
                    NorraEmotion.DEFAULT -> Color(0x55B1CFFF)
                    NorraEmotion.JOIE -> Color(0x66FFC107)
                    NorraEmotion.SEREIN -> Color(0x5500C9A7)
                    NorraEmotion.MOTIVE -> Color(0x66FF5252)
                    NorraEmotion.FIER -> Color(0x668E24AA)
                    NorraEmotion.COLERE -> Color(0x77FF2E63)
                    NorraEmotion.TRISTE -> Color(0x5500A2E8)
                    NorraEmotion.SEUL -> Color(0x33444455)
                    NorraEmotion.PEUR -> Color(0x664AF2A1)
                    NorraEmotion.HONTE -> Color(0x339E9BAC)
                    NorraEmotion.IDK -> Color(0x44FFF59D)
                    NorraEmotion.FATIGUE -> Color(0x558B97D8)
                    NorraEmotion.AUTRE -> Color(0x55E040FB)
                    else -> Color.Transparent
                }
                drawCircle(
                    color = shadowColor,
                    radius = width * 0.38f,
                    center = Offset(width / 2, height * 0.55f + extraY),
                    style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 2. Draw Ears
            val leftEarOrigin = Offset(width * 0.33f, height * 0.23f + extraY)
            val rightEarOrigin = Offset(width * 0.67f, height * 0.23f + extraY)

            // Draw Left Ear
            withTransform({
                rotate(-18f, leftEarOrigin)
            }) {
                val earSize = Size(width * 0.10f, height * 0.21f)
                val earTopLeft = Offset(width * 0.26f, height * 0.08f + extraY)
                if (emotion == NorraEmotion.VIDE) {
                    drawOval(
                        color = detailsColor,
                        topLeft = earTopLeft,
                        size = earSize,
                        style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    )
                } else {
                    drawOval(
                        brush = earBrush,
                        topLeft = earTopLeft,
                        size = earSize
                    )
                }
            }

            // Draw Right Ear
            withTransform({
                rotate(18f, rightEarOrigin)
            }) {
                val earSize = Size(width * 0.10f, height * 0.21f)
                val earTopLeft = Offset(width * 0.64f, height * 0.08f + extraY)
                if (emotion == NorraEmotion.VIDE) {
                    drawOval(
                        color = detailsColor,
                        topLeft = earTopLeft,
                        size = earSize,
                        style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    )
                } else {
                    drawOval(
                        brush = earBrush,
                        topLeft = earTopLeft,
                        size = earSize
                    )
                }
            }

            // 3. Draw Main Body
            if (emotion == NorraEmotion.VIDE) {
                // Dashed stroke body outline
                drawPath(
                    path = bodyPath,
                    color = detailsColor,
                    style = Stroke(
                        width = 4.7.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                )
            } else {
                // Solid colored gradient body
                drawPath(
                    path = bodyPath,
                    brush = bodyBrush
                )
                
                // Add an elegant top highlight for glowing 3D depth
                val highlightPath = Path().apply {
                    val startY = height * 0.24f + extraY
                    moveTo(width * 0.32f, startY + height * 0.1f)
                    cubicTo(
                        width * 0.38f, startY,
                        width * 0.62f, startY,
                        width * 0.68f, startY + height * 0.1f
                    )
                }
                drawPath(
                    path = highlightPath,
                    color = Color.White.copy(alpha = 0.25f),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Draw Face Features relative to body
            val faceCenterY = height * 0.52f + extraY
            val leftEyeX = width * 0.40f
            val rightEyeX = width * 0.60f

            val eyeWidth = width * 0.045f
            val eyeHeight = width * 0.085f

            // Left Eye drawing
            if (emotion == NorraEmotion.VIDE) {
                drawOval(
                    color = detailsColor,
                    topLeft = Offset(leftEyeX - eyeWidth / 2, faceCenterY - eyeHeight / 2),
                    size = Size(eyeWidth, eyeHeight)
                )
            } else if (emotion == NorraEmotion.PEUR) {
                // Glowing circular surprised green eyes
                drawCircle(
                    color = detailsColor,
                    radius = eyeWidth * 1.1f,
                    center = Offset(leftEyeX, faceCenterY)
                )
            } else if (emotion == NorraEmotion.FATIGUE) {
                // Sleepy relaxed closed eyelids (arcs)
                val sleepyEyePath = Path().apply {
                    moveTo(leftEyeX - eyeWidth * 0.8f, faceCenterY)
                    quadraticBezierTo(
                        leftEyeX, faceCenterY + eyeHeight * 0.4f,
                        leftEyeX + eyeWidth * 0.8f, faceCenterY
                    )
                }
                drawPath(
                    path = sleepyEyePath,
                    color = detailsColor,
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
            } else {
                // Regular cozy ovals with custom blinking simulation
                val activeEyeHeight = eyeHeight * blinkProgress
                val activeEyeTop = faceCenterY - activeEyeHeight / 2
                drawOval(
                    color = detailsColor,
                    topLeft = Offset(leftEyeX - eyeWidth / 2, activeEyeTop),
                    size = Size(eyeWidth, activeEyeHeight)
                )

                // Add nice little white reflection dots for default/autre
                if (emotion == NorraEmotion.DEFAULT || emotion == NorraEmotion.AUTRE) {
                    drawCircle(
                        color = Color.White,
                        radius = eyeWidth * 0.2f,
                        center = Offset(leftEyeX - eyeWidth * 0.15f, activeEyeTop + activeEyeHeight * 0.32f)
                    )
                }
            }

            // Right Eye drawing
            if (emotion == NorraEmotion.VIDE) {
                drawOval(
                    color = detailsColor,
                    topLeft = Offset(rightEyeX - eyeWidth / 2, faceCenterY - eyeHeight / 2),
                    size = Size(eyeWidth, eyeHeight)
                )
            } else if (emotion == NorraEmotion.PEUR) {
                drawCircle(
                    color = detailsColor,
                    radius = eyeWidth * 1.1f,
                    center = Offset(rightEyeX, faceCenterY)
                )
            } else if (emotion == NorraEmotion.FATIGUE) {
                val sleepyEyePath = Path().apply {
                    moveTo(rightEyeX - eyeWidth * 0.8f, faceCenterY)
                    quadraticBezierTo(
                        rightEyeX, faceCenterY + eyeHeight * 0.4f,
                        rightEyeX + eyeWidth * 0.8f, faceCenterY
                    )
                }
                drawPath(
                    path = sleepyEyePath,
                    color = detailsColor,
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
            } else {
                val activeEyeHeight = eyeHeight * blinkProgress
                val activeEyeTop = faceCenterY - activeEyeHeight / 2
                drawOval(
                    color = detailsColor,
                    topLeft = Offset(rightEyeX - eyeWidth / 2, activeEyeTop),
                    size = Size(eyeWidth, activeEyeHeight)
                )

                if (emotion == NorraEmotion.DEFAULT || emotion == NorraEmotion.AUTRE) {
                    drawCircle(
                        color = Color.White,
                        radius = eyeWidth * 0.2f,
                        center = Offset(rightEyeX - eyeWidth * 0.15f, activeEyeTop + activeEyeHeight * 0.32f)
                    )
                }
            }

            // 5. Draw Eyebrows (based on emotional traits in DA images)
            val browY = faceCenterY - eyeHeight * 0.8f
            val browWidth = width * 0.07f
            val browThickness = 3.dp.toPx()

            when (emotion) {
                NorraEmotion.JOIE -> {
                    val leftBrow = Path().apply {
                        moveTo(leftEyeX - browWidth * 0.7f, browY)
                        quadraticBezierTo(leftEyeX, browY - browWidth * 0.4f, leftEyeX + browWidth * 0.7f, browY)
                    }
                    val rightBrow = Path().apply {
                        moveTo(rightEyeX - browWidth * 0.7f, browY)
                        quadraticBezierTo(rightEyeX, browY - browWidth * 0.4f, rightEyeX + browWidth * 0.7f, browY)
                    }
                    drawPath(leftBrow, detailsColor, style = Stroke(browThickness, cap = StrokeCap.Round))
                    drawPath(rightBrow, detailsColor, style = Stroke(browThickness, cap = StrokeCap.Round))
                }
                NorraEmotion.SEREIN -> {
                    drawLine(
                        color = detailsColor,
                        start = Offset(leftEyeX - browWidth * 0.7f, browY),
                        end = Offset(leftEyeX + browWidth * 0.7f, browY),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = detailsColor,
                        start = Offset(rightEyeX + browWidth * 0.7f, browY),
                        end = Offset(rightEyeX - browWidth * 0.7f, browY),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                }
                NorraEmotion.MOTIVE -> {
                    drawLine(
                        color = detailsColor,
                        start = Offset(leftEyeX - browWidth * 0.7f, browY - browWidth * 0.1f),
                        end = Offset(leftEyeX + browWidth * 0.7f, browY + browWidth * 0.15f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = detailsColor,
                        start = Offset(rightEyeX + browWidth * 0.7f, browY - browWidth * 0.1f),
                        end = Offset(rightEyeX - browWidth * 0.7f, browY + browWidth * 0.15f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                }
                NorraEmotion.FIER -> {
                    drawLine(
                        color = detailsColor,
                        start = Offset(leftEyeX - browWidth * 0.7f, browY - browWidth * 0.1f),
                        end = Offset(leftEyeX + browWidth * 0.7f, browY - browWidth * 0.3f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = detailsColor,
                        start = Offset(rightEyeX + browWidth * 0.7f, browY - browWidth * 0.1f),
                        end = Offset(rightEyeX - browWidth * 0.7f, browY - browWidth * 0.3f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                }
                NorraEmotion.COLERE -> {
                    // Angry frowning eyebrows pointing diagonally down-inwards: \   /
                    drawLine(
                        color = detailsColor,
                        start = Offset(leftEyeX - browWidth * 0.7f, browY - browWidth * 0.3f),
                        end = Offset(leftEyeX + browWidth * 0.7f, browY + browWidth * 0.3f),
                        strokeWidth = browThickness + 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = detailsColor,
                        start = Offset(rightEyeX + browWidth * 0.7f, browY - browWidth * 0.3f),
                        end = Offset(rightEyeX - browWidth * 0.7f, browY + browWidth * 0.3f),
                        strokeWidth = browThickness + 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                NorraEmotion.TRISTE -> {
                    // Sad comforting eyebrows raised inwards: /   \
                    drawLine(
                        color = detailsColor,
                        start = Offset(leftEyeX - browWidth * 0.7f, browY + browWidth * 0.25f),
                        end = Offset(leftEyeX + browWidth * 0.7f, browY - browWidth * 0.25f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = detailsColor,
                        start = Offset(rightEyeX + browWidth * 0.7f, browY + browWidth * 0.25f),
                        end = Offset(rightEyeX - browWidth * 0.7f, browY - browWidth * 0.25f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )

                    // Draw little crying teardrops details! (glowing neon blue curves)
                    val leftTearPath = Path().apply {
                        moveTo(leftEyeX, faceCenterY + eyeHeight * 0.3f)
                        quadraticBezierTo(
                            leftEyeX - width * 0.03f, faceCenterY + eyeHeight * 1.3f,
                            leftEyeX, faceCenterY + eyeHeight * 1.8f
                        )
                    }
                    drawPath(
                        path = leftTearPath,
                        color = detailsColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                NorraEmotion.PEUR -> {
                    // Scared wavy tilted eyebrows
                    drawLine(
                        color = detailsColor,
                        start = Offset(leftEyeX - browWidth * 0.7f, browY + browWidth * 0.2f),
                        end = Offset(leftEyeX + browWidth * 0.7f, browY - browWidth * 0.4f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = detailsColor,
                        start = Offset(rightEyeX + browWidth * 0.7f, browY + browWidth * 0.2f),
                        end = Offset(rightEyeX - browWidth * 0.7f, browY - browWidth * 0.4f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                }
                NorraEmotion.HONTE, NorraEmotion.SEUL -> {
                    // Slumped worried/droopy eyebrows
                    drawLine(
                        color = detailsColor,
                        start = Offset(leftEyeX - browWidth * 0.5f, browY + browWidth * 0.2f),
                        end = Offset(leftEyeX + browWidth * 0.7f, browY + browWidth * 0.5f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = detailsColor,
                        start = Offset(rightEyeX + browWidth * 0.5f, browY + browWidth * 0.2f),
                        end = Offset(rightEyeX - browWidth * 0.7f, browY + browWidth * 0.5f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                }
                NorraEmotion.IDK -> {
                    // Expressive asymmetrical puzzled eyebrows
                    drawLine(
                        color = detailsColor,
                        start = Offset(leftEyeX - browWidth * 0.6f, browY + browWidth * 0.1f),
                        end = Offset(leftEyeX + browWidth * 0.6f, browY - browWidth * 0.1f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = detailsColor,
                        start = Offset(rightEyeX - browWidth * 0.6f, browY - browWidth * 0.4f),
                        end = Offset(rightEyeX + browWidth * 0.6f, browY - browWidth * 0.5f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                }
                NorraEmotion.FATIGUE -> {
                    // Relaxed slightly downward-drooping sleepy eyebrows
                    drawLine(
                        color = detailsColor,
                        start = Offset(leftEyeX - browWidth * 0.7f, browY + browWidth * 0.1f),
                        end = Offset(leftEyeX + browWidth * 0.7f, browY + browWidth * 0.3f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = detailsColor,
                        start = Offset(rightEyeX - browWidth * 0.7f, browY + browWidth * 0.3f),
                        end = Offset(rightEyeX + browWidth * 0.7f, browY + browWidth * 0.1f),
                        strokeWidth = browThickness,
                        cap = StrokeCap.Round
                    )
                }
                else -> { /* No specific eyebrows for neutral smiley states */ }
            }

            // 6. Draw Mouth
            val mouthY = faceCenterY + width * 0.08f
            val mouthWidth = width * 0.07f

            when (emotion) {
                NorraEmotion.JOIE -> {
                    val openSmilePath = Path().apply {
                        moveTo(width / 2 - mouthWidth * 1.2f, mouthY)
                        quadraticBezierTo(
                            width / 2, mouthY + mouthWidth * 1.5f,
                            width / 2 + mouthWidth * 1.2f, mouthY
                        )
                        close()
                    }
                    drawPath(
                        path = openSmilePath,
                        color = detailsColor,
                        style = androidx.compose.ui.graphics.drawscope.Fill
                    )
                }
                NorraEmotion.SEREIN, NorraEmotion.FIER -> {
                    val gentleSmilePath = Path().apply {
                        moveTo(width / 2 - mouthWidth * 0.9f, mouthY)
                        quadraticBezierTo(
                            width / 2, mouthY + mouthWidth * 0.4f,
                            width / 2 + mouthWidth * 0.9f, mouthY
                        )
                    }
                    drawPath(
                        path = gentleSmilePath,
                        color = detailsColor,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                NorraEmotion.DEFAULT, NorraEmotion.AUTRE, NorraEmotion.MOTIVE -> {
                    // Gorgeous arc smiley mouth
                    val smilePath = Path().apply {
                        moveTo(width / 2 - mouthWidth, mouthY)
                        quadraticBezierTo(
                            width / 2, mouthY + mouthWidth * 0.7f,
                            width / 2 + mouthWidth, mouthY
                        )
                    }
                    drawPath(
                        path = smilePath,
                        color = detailsColor,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                NorraEmotion.FATIGUE -> {
                    // Soft gentle relaxed sleepy mouth curve
                    val tiredMouthPath = Path().apply {
                        moveTo(width / 2 - mouthWidth * 0.7f, mouthY)
                        quadraticBezierTo(
                            width / 2, mouthY + mouthWidth * 0.25f,
                            width / 2 + mouthWidth * 0.7f, mouthY
                        )
                    }
                    drawPath(
                        path = tiredMouthPath,
                        color = detailsColor,
                        style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                NorraEmotion.COLERE, NorraEmotion.TRISTE, NorraEmotion.HONTE -> {
                    // Decisively sad downwards frown mouth
                    val frownPath = Path().apply {
                        moveTo(width / 2 - mouthWidth * 1.1f, mouthY + mouthWidth * 0.3f)
                        quadraticBezierTo(
                            width / 2, mouthY - mouthWidth * 0.3f,
                            width / 2 + mouthWidth * 1.1f, mouthY + mouthWidth * 0.3f
                        )
                    }
                    drawPath(
                        path = frownPath,
                        color = detailsColor,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                NorraEmotion.VIDE, NorraEmotion.SEUL -> {
                    // Absolute neutral straight horizontal line
                    drawLine(
                        color = detailsColor,
                        start = Offset(width / 2 - mouthWidth * 0.8f, mouthY),
                        end = Offset(width / 2 + mouthWidth * 0.8f, mouthY),
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                NorraEmotion.PEUR -> {
                    // Terrified open surprised mouth (cute green oval)
                    drawOval(
                        color = detailsColor,
                        topLeft = Offset(width / 2 - mouthWidth * 0.8f, mouthY - mouthWidth * 0.4f),
                        size = Size(mouthWidth * 1.6f, mouthWidth * 0.9f)
                    )
                }
                NorraEmotion.IDK -> {
                    // Squiggly undecided wavy mouth (hilarious and cute)
                    val squigglyPath = Path().apply {
                        moveTo(width / 2 - mouthWidth, mouthY)
                        cubicTo(
                            width / 2 - mouthWidth * 0.5f, mouthY - mouthWidth * 0.4f,
                            width / 2, mouthY + mouthWidth * 0.4f,
                            width / 2 + mouthWidth, mouthY - mouthWidth * 0.1f
                        )
                    }
                    drawPath(
                        path = squigglyPath,
                        color = detailsColor,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
