package com.tareaandroid.plantcare.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.model.MonthlyCare
import java.time.format.DateTimeFormatter

/**
 * Gráfico de barras apiladas con los cuidados de los últimos meses: riegos abajo y el
 * resto de cuidados encima. Se dibuja con Canvas, sin librerías externas.
 */
@Composable
fun CareChart(stats: List<MonthlyCare>, modifier: Modifier = Modifier) {
    val waterColor = MaterialTheme.colorScheme.primary
    val otherColor = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.surface
    val maxTotal = (stats.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMM") }

    val summary = stringResource(
        R.string.chart_description,
        stats.size,
        stats.sumOf { it.waterings },
        stats.sumOf { it.otherCares },
    )

    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.chart_title), style = MaterialTheme.typography.titleMedium)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    // Los lectores de pantalla leen un resumen en lugar del dibujo
                    .semantics { contentDescription = summary },
            ) {
                val slot = size.width / stats.size
                val barWidth = slot * 0.55f
                val radius = CornerRadius(6.dp.toPx())
                stats.forEachIndexed { index, month ->
                    val left = index * slot + (slot - barWidth) / 2
                    // Fondo de la barra para que se vean también los meses vacíos
                    drawRoundRect(trackColor, Offset(left, 0f), Size(barWidth, size.height), radius)
                    val waterHeight = size.height * month.waterings / maxTotal
                    val otherHeight = size.height * month.otherCares / maxTotal
                    drawBar(waterColor, left, size.height - waterHeight, barWidth, waterHeight, radius)
                    drawBar(otherColor, left, size.height - waterHeight - otherHeight, barWidth, otherHeight, radius)
                }
            }

            Row(Modifier.fillMaxWidth()) {
                stats.forEach { month ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(month.total.toString(), style = MaterialTheme.typography.labelMedium)
                        Text(
                            month.month.format(monthFormatter),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(waterColor, stringResource(R.string.care_water))
                LegendItem(otherColor, stringResource(R.string.chart_other_cares))
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBar(
    color: Color,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    radius: CornerRadius,
) {
    if (height > 0f) drawRoundRect(color, Offset(left, top), Size(width, height), radius)
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, CircleShape),
        )
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
