package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pdf.PdfReportGenerator
import com.example.repository.WeeklySummaryModel
import com.example.ui.components.MetricCard
import com.example.ui.theme.CarbonMistElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.MutedGold
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SteelLineBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Formatters
import kotlinx.coroutines.launch

@Composable
fun ReportesScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val pdfGenerator = remember { PdfReportGenerator(context) }
    val scope = rememberCoroutineScope()

    var weekOffset by remember { mutableIntStateOf(0) }
    var summaryModel by remember { mutableStateOf<WeeklySummaryModel?>(null) }

    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()

    val weekStart = remember(weekOffset) { Formatters.getWeekStartIso(weekOffset) }
    val weekEnd = remember(weekStart) { Formatters.getWeekEndIso(weekStart) }

    LaunchedEffect(weekOffset, allSessions, prefs) {
        summaryModel = viewModel.getWeeklySummary(weekStart, weekEnd)
    }

    val currentWeeklySessions = remember(allSessions, weekStart, weekEnd) {
        allSessions.filter { it.date >= weekStart && it.date <= weekEnd && it.state == "CERRADA" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "AUDITORÍA Y NÓMINA",
            color = MutedGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Reportes Financieros",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- WEEK PERIOD SELECTOR ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GraphiteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PERÍODO DE FACTURACIÓN",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { weekOffset -= 1 }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = MutedGold)
                    }
                    Text(
                        text = summaryModel?.periodLabel ?: Formatters.formatWeekRangeLabel(weekStart, weekEnd),
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { weekOffset += 1 }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Siguiente", tint = MutedGold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- METRICS GRID ---
        val summary = summaryModel
        if (summary != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    label = "Horas Totales",
                    value = Formatters.formatHours(summary.totalHours),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Ingreso Neto",
                    value = Formatters.formatCurrency(summary.totalEarnings),
                    isGold = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    label = "Horas Extra (OT)",
                    value = Formatters.formatHours(summary.overtimeHours),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Jornadas",
                    value = "${summary.sessionCount}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- DETAILED AUDIT BREAKDOWN ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = GraphiteSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DESGLOSE AUDITABLE DE NÓMINA",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AuditRow(label = "Tarifa Base ($/h)", value = Formatters.formatCurrency(summary.baseRate))
                    AuditRow(label = "Ganancia Ordinaria (Base)", value = Formatters.formatCurrency(summary.baseEarnings))
                    AuditRow(label = "Recargo Tiempo Extra", value = "${summary.overtimeMultiplier}x")
                    AuditRow(label = "Ganancia Tiempo Extra (OT)", value = Formatters.formatCurrency(summary.overtimeEarnings))
                    AuditRow(label = "Ajustes Manuales Aplicados", value = "${summary.manualAdjustments}")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- ACTIONS ---
            Button(
                onClick = {
                    val html = pdfGenerator.generateHtmlReport(summary, currentWeeklySessions)
                    pdfGenerator.printOrShareHtml(html)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
            ) {
                Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = ObsidianBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "GENERAR / IMPRIMIR REPORTE PDF", color = ObsidianBackground, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    pdfGenerator.shareReportAsText(summary, prefs.reportEmail)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "COMPARTIR RESUMEN POR CORREO / MENSAJE")
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun AuditRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
