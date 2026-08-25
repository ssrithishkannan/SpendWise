package com.example.ui.components

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
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

data class ConfettiParticle(
    val xRatio: Float,
    val initialY: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
    val horizontalWobble: Float
)

@Composable
fun ConfettiCelebration(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    if (!isActive) return

    val progress = remember { Animatable(0f) }
    val colors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFF4081), // Pink
        Color(0xFF00E5FF), // Cyan
        Color(0xFF76FF03), // Lime
        Color(0xFFFF9100), // Orange
        Color(0xFF7C4DFF)  // Purple
    )

    val particles = remember {
        List(60) {
            ConfettiParticle(
                xRatio = Random.nextFloat(),
                initialY = -Random.nextFloat() * 200f,
                speed = 450f + Random.nextFloat() * 600f,
                size = 12f + Random.nextFloat() * 16f,
                color = colors[Random.nextInt(colors.size)],
                rotationSpeed = Random.nextFloat() * 360f,
                horizontalWobble = (Random.nextFloat() - 0.5f) * 60f
            )
        }
    }

    LaunchedEffect(isActive) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2600, easing = LinearEasing)
        )
        onFinished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { particle ->
            val currentY = particle.initialY + (particle.speed * progress.value * (height / 600f))
            val currentX = (particle.xRatio * width) + (particle.horizontalWobble * kotlin.math.sin(progress.value * 6.28f * 2f))
            val currentRotation = particle.rotationSpeed * progress.value * 3f

            if (currentY in 0f..height + 50f) {
                rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                    drawRect(
                        color = particle.color.copy(alpha = (1f - (progress.value * 0.7f)).coerceIn(0f, 1f)),
                        topLeft = Offset(currentX, currentY),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}
