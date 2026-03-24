package com.embien.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

class SpinWheelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpinWheelApp()
        }
    }
}

@Composable
fun SpinWheelApp() {
    val items = listOf("100", "200", "300", "400", "500", "LOSE")
    var rotation by remember { mutableStateOf(0f) }
    var result by remember { mutableStateOf("") }
    var isSpinning by remember { mutableStateOf(false) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
        finishedListener = {
            val normalized = (it % 360 + 360) % 360
            val anglePerItem = 360f / items.size

            val index = ((270f - normalized) / anglePerItem).toInt()
                .let { (it + items.size) % items.size }

            result = items[index]
            isSpinning = false
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1E2C),
                        Color(0xFF3A3A5A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎡 Spin & Win",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(30.dp))
            Box(
                contentAlignment = Alignment.TopCenter
            ) {
                Wheel(
                    items = items,
                    rotation = animatedRotation
                )
                Text(
                    text = "🔻",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.offset(y = (-10).dp)
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    if (!isSpinning) {
                        isSpinning = true
                        val randomExtra = Random.nextInt(0, 360)
                        rotation += (360f * 5) + randomExtra
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(55.dp)
            ) {
                Text(
                    text = if (isSpinning) "Spinning..." else "SPIN",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Result: $result",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun Wheel(items: List<String>, rotation: Float) {
    val colors = listOf(
        Color(0xFFFF5252),
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFFFFEB3B),
        Color(0xFF9C27B0),
        Color(0xFF00BCD4)
    )
    Canvas(
        modifier = Modifier
            .size(260.dp)
            .padding(10.dp)
    ) {
        val sweepAngle = 360f / items.size
        rotate(rotation) {
            items.forEachIndexed { index, item ->
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = index * sweepAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                val angle = (index * sweepAngle + sweepAngle / 2) * (PI / 180)
                val x = (size.width / 2 + cos(angle) * size.width / 3).toFloat()
                val y = (size.height / 2 + sin(angle) * size.height / 3).toFloat()
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        textSize = 40f
                        color = android.graphics.Color.BLACK
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    canvas.nativeCanvas.drawText(
                        item,
                        x,
                        y,
                        paint
                    )
                }
            }
        }
    }
}