package com.example.pdf

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.example.data.model.WorkSession
import com.example.repository.WeeklySummaryModel
import com.example.util.Formatters
import java.io.File

class PdfReportGenerator(private val context: Context) {

    fun generateHtmlReport(
        summary: WeeklySummaryModel,
        sessions: List<WorkSession>
    ): String {
        val sessionsHtml = StringBuilder()
        for (session in sessions) {
            sessionsHtml.append(buildSessionCardHtml(session))
        }

        return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Reporte de Jornada - W Tracker</title>
  <style>
    body {
      font-family: 'Helvetica Neue', Arial, sans-serif;
      color: #111111;
      background-color: #ffffff;
      margin: 30px;
      line-height: 1.4;
      font-size: 13px;
    }
    .header-container {
      border-bottom: 2px solid #C5A059;
      padding-bottom: 15px;
      margin-bottom: 25px;
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
    }
    .brand {
      font-size: 26px;
      font-weight: bold;
      color: #C5A059;
      letter-spacing: -1px;
    }
    .report-title {
      font-size: 13px;
      font-weight: 600;
      color: #666666;
      text-transform: uppercase;
      margin-top: 4px;
    }
    .period-label {
      font-size: 16px;
      font-weight: 700;
      color: #111111;
      text-align: right;
    }
    .section-title {
      font-size: 13px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: #111111;
      border-bottom: 1px solid #e5e5e5;
      padding-bottom: 6px;
      margin-top: 25px;
      margin-bottom: 15px;
    }
    .summary-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
      margin-bottom: 25px;
    }
    .summary-card {
      background-color: #fafafa;
      border: 1px solid #eeeeee;
      border-radius: 8px;
      padding: 12px;
      text-align: center;
    }
    .summary-card .label {
      font-size: 9px;
      font-weight: bold;
      color: #777777;
      text-transform: uppercase;
      letter-spacing: 1px;
      margin-bottom: 4px;
    }
    .summary-card .value {
      font-size: 18px;
      font-weight: bold;
      color: #111111;
    }
    .summary-card .value-gold {
      color: #b2853c;
    }
    .summary-table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 15px;
      margin-bottom: 25px;
    }
    .summary-table th, .summary-table td {
      padding: 8px 10px;
      text-align: left;
      border-bottom: 1px solid #eeeeee;
    }
    .summary-table th {
      background-color: #fafafa;
      font-size: 10px;
      font-weight: bold;
      text-transform: uppercase;
      color: #555555;
    }
    .workday-block {
      page-break-inside: avoid;
      border: 1px solid #e5e5e5;
      border-radius: 8px;
      padding: 15px;
      margin-bottom: 15px;
      background-color: #ffffff;
    }
    .workday-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px dashed #e5e5e5;
      padding-bottom: 8px;
      margin-bottom: 10px;
    }
    .workday-date {
      font-size: 14px;
      font-weight: 700;
      color: #111111;
    }
    .badge {
      font-size: 9px;
      font-weight: 700;
      text-transform: uppercase;
      padding: 3px 8px;
      border-radius: 12px;
      background-color: #e6f4ea;
      color: #1e7e34;
    }
    .footer-note {
      text-align: center;
      margin-top: 40px;
      font-size: 10px;
      color: #aaaaaa;
      border-top: 1px solid #eeeeee;
      padding-top: 12px;
    }
  </style>
</head>
<body>

  <div class="header-container">
    <div>
      <div class="brand">W TRACKER</div>
      <div class="report-title">Reporte de Auditoría Laboral V2.0</div>
    </div>
    <div class="period-label">
      ${summary.periodLabel}
    </div>
  </div>

  <div class="section-title">Resumen Financiero y de Rendimiento</div>
  <div class="summary-grid">
    <div class="summary-card">
      <div class="label">Horas Totales</div>
      <div class="value">${Formatters.formatHours(summary.totalHours)}</div>
    </div>
    <div class="summary-card">
      <div class="label">Tiempo Extra</div>
      <div class="value">${Formatters.formatHours(summary.overtimeHours)}</div>
    </div>
    <div class="summary-card">
      <div class="label">Total Estimado</div>
      <div class="value value-gold">${Formatters.formatCurrency(summary.totalEarnings)}</div>
    </div>
    <div class="summary-card">
      <div class="label">Jornadas</div>
      <div class="value">${summary.sessionCount}</div>
    </div>
  </div>

  <table class="summary-table">
    <thead>
      <tr>
        <th>Concepto</th>
        <th>Horas</th>
        <th>Tarifa Base</th>
        <th>Subtotal</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>Horas Ordinarias (Base)</td>
        <td>${Formatters.formatHours(Math.max(0.0, summary.totalHours - summary.overtimeHours))}</td>
        <td>${Formatters.formatCurrency(summary.baseRate)}/h</td>
        <td>${Formatters.formatCurrency(summary.baseEarnings)}</td>
      </tr>
      <tr>
        <td>Horas de Tiempo Extra (Overtime)</td>
        <td>${Formatters.formatHours(summary.overtimeHours)}</td>
        <td>${Formatters.formatCurrency(summary.baseRate)}/h (${summary.overtimeMultiplier}x)</td>
        <td>${Formatters.formatCurrency(summary.overtimeEarnings)}</td>
      </tr>
      <tr style="font-weight: bold; border-top: 1px solid #111111;">
        <td>Total Consolidado</td>
        <td>${Formatters.formatHours(summary.totalHours)}</td>
        <td>—</td>
        <td style="color: #b2853c; font-size: 15px;">${Formatters.formatCurrency(summary.totalEarnings)}</td>
      </tr>
    </tbody>
  </table>

  <div class="section-title">Detalle por Jornada</div>
  $sessionsHtml

  <div class="footer-note">
    Documento oficial generado por W Tracker - Plataforma de Control Laboral e Industrial.
  </div>

</body>
</html>
        """.trimIndent()
    }

    private fun buildSessionCardHtml(session: WorkSession): String {
        return """
      <div class="workday-block">
        <div class="workday-header">
          <span class="workday-date">${Formatters.formatDate(session.date)}</span>
          <span class="badge">${session.integrityType}</span>
        </div>
        <div style="margin-bottom: 6px;">
          <strong>Destino:</strong> ${session.destination} ${session.alias?.let { "($it)" } ?: ""}
        </div>
        <div style="display: flex; justify-content: space-between; font-size: 12px; color: #444;">
          <span><strong>Horas:</strong> ${Formatters.formatTimeRange(session.startTime, session.endTime)} (${Formatters.formatHours(session.totalHours)})</span>
          <span><strong>Descuento Almuerzo:</strong> ${session.lunchDeductedMinutes} min</span>
          <span style="font-weight: bold; color: #b2853c;">${Formatters.formatCurrency(session.totalEarnings)}</span>
        </div>
        ${session.notes?.let { "<div style='margin-top: 6px; font-style: italic; font-size: 11px; color: #666;'>Nota: $it</div>" } ?: ""}
      </div>
        """.trimIndent()
    }

    fun printOrShareHtml(htmlContent: String, jobName: String = "W_Tracker_Report") {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager?.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    fun shareReportAsText(summary: WeeklySummaryModel, emailTo: String? = null) {
        val text = """
            W TRACKER - REPORTE DE AUDITORÍA LABORAL
            Período: ${summary.periodLabel}
            ------------------------------------
            Horas Totales: ${Formatters.formatHours(summary.totalHours)}
            Horas Extra: ${Formatters.formatHours(summary.overtimeHours)}
            Ganancias Ordinarias: ${Formatters.formatCurrency(summary.baseEarnings)}
            Ganancias Overtime: ${Formatters.formatCurrency(summary.overtimeEarnings)}
            TOTAL NETO ESTIMADO: ${Formatters.formatCurrency(summary.totalEarnings)}
            Jornadas Registradas: ${summary.sessionCount}
            ------------------------------------
            Generado con W Tracker v2.0
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Jornada W Tracker - ${summary.periodLabel}")
            putExtra(Intent.EXTRA_TEXT, text)
            if (!emailTo.isNull_or_blank_custom()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailTo))
            }
        }
        val chooser = Intent.createChooser(intent, "Compartir Reporte W Tracker")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun String?.isNull_or_blank_custom(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}
