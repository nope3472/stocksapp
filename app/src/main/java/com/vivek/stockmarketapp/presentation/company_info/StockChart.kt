package com.vivek.stockmarketapp.presentation.company_info

import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vivek.stockmarketapp.domain.model.IntradayInfo
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StockChart(
    modifier: Modifier = Modifier,
    infos: List<IntradayInfo> = emptyList(),
    graphColor: Color = Color.Green
) {
    val spacing = 100f
    val upperValue = remember(infos) {
        (infos.maxOfOrNull { it.high }?.plus(1))?.roundToInt() ?: 0
    }
    val lowerValue = remember(infos) {
        (infos.minOfOrNull { it.low }?.toInt() ?: 0)
    }
    val density = LocalDensity.current

    var lastDrawPosition by remember {
        mutableStateOf<Offset?>(null)
    }

    var touchPosition by remember {
        mutableStateOf<Offset?>(null)
    }
    
    var selectedInfo by remember {
        mutableStateOf<IntradayInfo?>(null)
    }

    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        lastDrawPosition = offset
                        touchPosition = offset
                        val x = offset.x
                        val spacing = size.width / (infos.size - 1)
                        val index = (x / spacing).toInt().coerceIn(0, infos.size - 1)
                        selectedInfo = infos[index]
                    }
                }
        ) {
            if (infos.isEmpty()) {
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "No data available",
                        center.x - 50f,
                        center.y,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 40f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
                return@Canvas
            }

            val spacePerHour = (size.width - spacing) / infos.size
            (infos.indices step 6).forEach { i ->
                val info = infos[i]
                val hour = info.date.hour
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        hour.toString(),
                        spacing + i * spacePerHour,
                        size.height - 5,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            val priceRatio = (upperValue - lowerValue).toFloat() / size.height
            val path = Path()
            val highPath = Path()
            val lowPath = Path()

            for (i in infos.indices) {
                val info = infos[i]
                val nextInfo = infos.getOrNull(i + 1) ?: infos.last()
                val currentPrice = info.high.toFloat()
                val nextPrice = nextInfo.high.toFloat()
                val currentLow = info.low.toFloat()
                val nextLow = nextInfo.low.toFloat()
                
                val x1 = spacing + i * spacePerHour
                val y1 = size.height - ((currentPrice - lowerValue) / priceRatio)
                val x2 = spacing + (i + 1) * spacePerHour
                val y2 = size.height - ((nextPrice - lowerValue) / priceRatio)
                
                if (i == 0) {
                    path.moveTo(x1, y1)
                    highPath.moveTo(x1, y1)
                    lowPath.moveTo(x1, size.height - ((currentLow - lowerValue) / priceRatio))
                }

                path.lineTo(x2, y2)
                highPath.lineTo(x2, y2)
                lowPath.lineTo(x2, size.height - ((nextLow - lowerValue) / priceRatio))
            }

            drawPath(
                path = path,
                color = graphColor,
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round
                )
            )

            drawPath(
                path = highPath,
                color = Color.Green,
                style = Stroke(
                    width = 2f,
                    cap = StrokeCap.Round
                )
            )

            drawPath(
                path = lowPath,
                color = Color.Red,
                style = Stroke(
                    width = 2f,
                    cap = StrokeCap.Round
                )
            )
            
            touchPosition?.let { pos ->
                val x = pos.x
                val spacing = size.width / (infos.size - 1)
                val index = (x / spacing).toInt().coerceIn(0, infos.size - 1)
                val info = infos[index]
                val y = size.height - ((info.high.toFloat() - lowerValue) / priceRatio)

                drawCircle(
                    color = Color.Gray,
                    radius = 8f,
                    center = Offset(x, y)
                )

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "$${info.high}",
                        x + 20f,
                        y - 20f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.LEFT
                        }
            )
        }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        selectedInfo?.let { info ->
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Text(
                    text = info.date.format(
                        DateTimeFormatter.ofPattern("MMM dd, HH:mm", Locale.getDefault())
                    ),
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "High: $${info.high}",
                    color = Color.Green,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Low: $${info.low}",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}