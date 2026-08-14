package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val ConfettiColors = listOf(
    Color(0xFFFFD700), // Gold
    Color(0xFFFF4081), // Pink
    Color(0xFF00E676), // Green
    Color(0xFF00E5FF), // Cyan
    Color(0xFFFF9100), // Orange
    Color(0xFF7C4DFF), // Purple
    Color(0xFFEEFF41), // Lime
    Color(0xFFFF1744)  // Red
)

private enum class ParticleShape {
    RECTANGLE, CIRCLE, STAR
}

private data class Confetti(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    val color: Color,
    val size: Float,
    val shape: ParticleShape,
    val maxAlpha: Float = 1f
)

@Composable
fun ConfettiCelebrationEffect(
    trigger: Any?,
    modifier: Modifier = Modifier.fillMaxSize(),
    particleCount: Int = 70
) {
    if (trigger == null || trigger == false || trigger == 0) return

    val progress = remember(trigger) { Animatable(0f) }

    val particles = remember(trigger) {
        List(particleCount) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 1200f + 300f
            val shape = when (Random.nextInt(10)) {
                in 0..4 -> ParticleShape.RECTANGLE
                in 5..8 -> ParticleShape.CIRCLE
                else -> ParticleShape.STAR
            }
            Confetti(
                x = 0.5f, // Center horizontally by default
                y = 0.3f, // Near top/center
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 400f, // Initial upward bias
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                color = ConfettiColors.random(),
                size = Random.nextFloat() * 12f + 8f,
                shape = shape,
                maxAlpha = Random.nextFloat() * 0.3f + 0.7f
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
        )
    }

    val currentProgress = progress.value
    if (currentProgress >= 1f) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val dt = 0.016f // approximate frame time step

        val gravity = 900f // px/s^2

        particles.forEach { p ->
            val timeElapsed = currentProgress * 2.8f
            // Update kinematics
            val currentX = width * p.x + p.vx * timeElapsed
            val currentY = height * p.y + p.vy * timeElapsed + 0.5f * gravity * timeElapsed * timeElapsed
            val currentRotation = p.rotation + p.rotationSpeed * timeElapsed
            val alpha = (1f - currentProgress).coerceIn(0f, 1f) * p.maxAlpha

            val drawColor = p.color.copy(alpha = alpha)

            rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                when (p.shape) {
                    ParticleShape.RECTANGLE -> {
                        drawRect(
                            color = drawColor,
                            topLeft = Offset(currentX - p.size / 2, currentY - p.size / 4),
                            size = Size(p.size, p.size / 2)
                        )
                    }
                    ParticleShape.CIRCLE -> {
                        drawCircle(
                            color = drawColor,
                            radius = p.size / 2,
                            center = Offset(currentX, currentY)
                        )
                    }
                    ParticleShape.STAR -> {
                        val path = Path().apply {
                            val outerRadius = p.size
                            val innerRadius = p.size / 2f
                            for (i in 0 until 5) {
                                val outerAngle = (i * 72 - 90) * (PI / 180f).toFloat()
                                val xOuter = currentX + outerRadius * cos(outerAngle)
                                val yOuter = currentY + outerRadius * sin(outerAngle)
                                if (i == 0) moveTo(xOuter, yOuter) else lineTo(xOuter, yOuter)

                                val innerAngle = ((i * 72 + 36) - 90) * (PI / 180f).toFloat()
                                val xInner = currentX + innerRadius * cos(innerAngle)
                                val yInner = currentY + innerRadius * sin(innerAngle)
                                lineTo(xInner, yInner)
                            }
                            close()
                        }
                        drawPath(path = path, color = drawColor)
                    }
                }
            }
        }
    }
}

@Composable
fun TrackFinishLineConfetti(
    isFinished: Boolean,
    isPredictionCorrect: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isFinished) return

    val particleCount = if (isPredictionCorrect) 50 else 25
    val progress = remember(isFinished) { Animatable(0f) }

    val particles = remember(isFinished) {
        List(particleCount) {
            val angle = (Random.nextFloat() - 0.5f) * PI.toFloat() // Burst leftwards/rightwards from finish line
            val speed = Random.nextFloat() * 400f + 150f
            Confetti(
                x = 1f, // At finish line (right edge)
                y = 0.5f,
                vx = -abs(cos(angle)) * speed, // Burst into track
                vy = sin(angle) * speed - 150f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 500f,
                color = if (isPredictionCorrect) Color(0xFFFFD700) else ConfettiColors.random(),
                size = Random.nextFloat() * 10f + 6f,
                shape = if (isPredictionCorrect) ParticleShape.STAR else ParticleShape.CIRCLE
            )
        }
    }

    LaunchedEffect(isFinished) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
    }

    val currentProgress = progress.value
    if (currentProgress >= 1f) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val timeElapsed = currentProgress * 2.0f
        val gravity = 400f

        particles.forEach { p ->
            val currentX = width + p.vx * timeElapsed
            val currentY = height * 0.5f + p.vy * timeElapsed + 0.5f * gravity * timeElapsed * timeElapsed
            val currentRotation = p.rotation + p.rotationSpeed * timeElapsed
            val alpha = (1f - currentProgress).coerceIn(0f, 1f)

            rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size / 2,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}

private fun abs(value: Float): Float = kotlin.math.abs(value)
