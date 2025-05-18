package com.aditya.stockmarketapp.presentation.company_info

import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aditya.stockmarketapp.domain.model.IntradayInfo
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.*
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StockChart(
    infos: List<IntradayInfo> = emptyList(),
    modifier: Modifier = Modifier,
    graphColor: Color = Color.Green
) {
    if (infos.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        return
    }
    
    // State for animation
    val graphAnimation = remember { Animatable(0f) }
    
    // State for touch interaction
    var touchX by remember { mutableStateOf(-1f) }
    var touchY by remember { mutableStateOf(-1f) }
    var selectedIndex by remember { mutableStateOf(-1) }
    
    // Format for date display
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    // Calculate values for graph
    val spacing = 40f
    val transparentGraphColor = remember { graphColor.copy(alpha = 0.2f) }
    val strongGraphColor = remember { graphColor.copy(alpha = 0.8f) }
    
    val upperValue = remember(infos) {
        (infos.maxOfOrNull { it.close }?.plus(1))?.roundToInt() ?: 0
    }
    val lowerValue = remember(infos) {
        (infos.minOfOrNull { it.close }?.minus(1))?.roundToInt() ?: 0
    }
    
    val density = LocalDensity.current
    val textPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.LTGRAY
            textAlign = Paint.Align.CENTER
            textSize = density.run { 11.sp.toPx() }
        }
    }
    
    val axisTextPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.LTGRAY
            textAlign = Paint.Align.RIGHT
            textSize = density.run { 11.sp.toPx() }
        }
    }
    
    // Launch animation on composition
    LaunchedEffect(infos) {
        graphAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000)
        )
    }
    
    // Selected point info
    val selectedInfo = if (selectedIndex >= 0 && selectedIndex < infos.size) {
        infos[selectedIndex]
    } else null
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .pointerInput(infos) {
                    detectTapGestures { offset ->
                        try {
                            touchX = offset.x
                            touchY = offset.y
                            
                            // Find closest point
                            val spacePerHour = (size.width - spacing) / infos.size
                            selectedIndex = ((touchX - spacing) / spacePerHour).roundToInt()
                                .coerceIn(0, infos.size - 1)
                        } catch (e: Exception) {
                            selectedIndex = -1
                        }
                    }
                }
        ) {
            try {
                val width = size.width
                val height = size.height
                val priceRange = (upperValue - lowerValue).coerceAtLeast(1)
                val priceStep = priceRange / (height - 2 * spacing)
                val spacePerHour = (width - 2 * spacing) / (infos.size - 1).coerceAtLeast(1)
            
                // Draw grid lines and price labels
                val numGridLines = 5
                for (i in 0..numGridLines) {
                    val y = spacing + (height - 2 * spacing) * i / numGridLines
                    val price = lowerValue + (priceRange * i / numGridLines)
                
                // Grid line
                drawLine(
                        color = Color.Gray.copy(alpha = 0.1f),
                    start = Offset(spacing, y),
                        end = Offset(width - spacing, y),
                    strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                )
                
                // Price label
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        String.format("%.2f", price),
                        spacing - 8.dp.toPx(),
                        y + 4.dp.toPx(),
                        axisTextPaint
                    )
                }
            }
            
            // Draw time labels (x-axis)
                val timeStep = (infos.size / 6).coerceAtLeast(1)
                for (i in 0 until infos.size step timeStep) {
                val x = spacing + i * spacePerHour
                    val time = dateFormat.format(Date.from(infos[i].date.toInstant(ZoneOffset.UTC)))
                
                // Time label
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        time,
                        x,
                            height - spacing / 3,
                        textPaint
                    )
                }
                }
                
                // Draw gradient background
                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        graphColor.copy(alpha = 0.1f),
                        graphColor.copy(alpha = 0.0f)
                    ),
                    startY = 0f,
                    endY = height
                )
                drawRect(brush = gradient)

                // Draw the line
                val path = Path()
                val pathEffect = androidx.compose.ui.graphics.PathEffect.cornerPathEffect(100f)

                infos.forEachIndexed { index, info ->
                    val x = spacing + index * spacePerHour
                    val y = height - spacing - ((info.close - lowerValue) / priceStep).toFloat()
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                // Draw the filled area under the line
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width - spacing, height - spacing)
                    lineTo(spacing, height - spacing)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            graphColor.copy(alpha = 0.3f),
                            graphColor.copy(alpha = 0.0f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw the main line
                drawPath(
                    path = path,
                    color = strongGraphColor,
                    style = Stroke(
                        width = 3f,
                        pathEffect = pathEffect
                    )
                )

                // Draw selected point indicator
                if (selectedIndex >= 0) {
                    val x = spacing + selectedIndex * spacePerHour
                    val y = height - spacing - ((infos[selectedIndex].close - lowerValue) / priceStep).toFloat()
                    
                    // Draw vertical indicator line
                    drawLine(
                        color = graphColor.copy(alpha = 0.3f),
                        start = Offset(x, spacing),
                        end = Offset(x, height - spacing),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                    )
                    
                    // Draw point indicator
                    drawCircle(
                        color = graphColor,
                        radius = 6.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            } catch (e: Exception) {
                // Log the error for debugging
                e.printStackTrace()
            
                // Draw error message
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "Error: ${e.message}",
                        size.width / 2,
                        size.height / 2,
                        textPaint
                    )
                }
            }
        }
        
        // Tooltip display for selected point
        if (selectedInfo != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${dateFormat.format(Date.from(selectedInfo.date.toInstant(ZoneOffset.UTC)))} - $${String.format("%.2f", selectedInfo.close)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}