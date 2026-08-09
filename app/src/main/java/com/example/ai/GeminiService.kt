package com.example.ai

import com.example.BuildConfig
import com.example.data.model.WorkSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiMessage(
    val role: String,
    val text: String
)

class GeminiService {

    private val apiKey: String = try {
        BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
        ""
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun isKeyConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "12345"
    }

    suspend fun analyzeWorkSessions(
        sessions: List<WorkSession>,
        baseRate: Double,
        overtimeMultiplier: Double
    ): String = withContext(Dispatchers.IO) {
        if (!isKeyConfigured()) {
            return@withContext generateLocalAuditAnalysis(sessions, baseRate, overtimeMultiplier)
        }

        val sessionsSummary = sessions.take(10).joinToString("\n") {
            "Fecha: ${it.date}, Destino: ${it.destination}, Horas: ${it.totalHours}h, OT: ${it.overtimeHours}h, Pago: $${it.totalEarnings}, Integridad: ${it.integrityType}, Estado: ${it.state}"
        }

        val systemPrompt = "Eres el Asistente W Tracker de Auditoría Financiera y Nómina. Analiza los registros de jornada laboral proporcionados. Identifica tendencias de horas extra, posibles inconsistencias (ajustes manuales o auto cierres), y sugiere recomendaciones para maximizar la eficiencia laboral y el cumplimiento contable."
        val userPrompt = "Analiza estos registros de jornadas recientes:\n$sessionsSummary\n\nTarifa base: $$baseRate/h, Multiplicador OT: ${overtimeMultiplier}x. Dame un resumen ejecutivo conciso, 3 hallazgos clave y 2 recomendaciones."

        try {
            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", userPrompt) })
                        })
                    })
                }
                put("contents", contentsArray)

                val sysInstruction = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                }
                put("systemInstruction", sysInstruction)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val respStr = response.body?.string() ?: ""
                val rootObj = JSONObject(respStr)
                val candidates = rootObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
            }
            generateLocalAuditAnalysis(sessions, baseRate, overtimeMultiplier)
        } catch (e: Exception) {
            generateLocalAuditAnalysis(sessions, baseRate, overtimeMultiplier)
        }
    }

    suspend fun askAssistant(
        conversationHistory: List<GeminiMessage>,
        userQuery: String
    ): String = withContext(Dispatchers.IO) {
        if (!isKeyConfigured()) {
            return@withContext generateLocalAssistantAnswer(userQuery)
        }

        val systemPrompt = "Eres el Asistente IA de W Tracker, un experto en gestión de jornadas laborales, control de tiempos multi-sitio, cálculo de nómina y horas extra. Responde con tono profesional, claro, conciso y amigable en español."

        try {
            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    conversationHistory.forEach { msg ->
                        put(JSONObject().apply {
                            put("role", msg.role)
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", msg.text) })
                            })
                        })
                    }
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", userQuery) })
                        })
                    })
                }
                put("contents", contentsArray)

                val sysInstruction = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                }
                put("systemInstruction", sysInstruction)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val respStr = response.body?.string() ?: ""
                val rootObj = JSONObject(respStr)
                val candidates = rootObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
            }
            generateLocalAssistantAnswer(userQuery)
        } catch (e: Exception) {
            generateLocalAssistantAnswer(userQuery)
        }
    }

    private fun generateLocalAuditAnalysis(
        sessions: List<WorkSession>,
        baseRate: Double,
        overtimeMultiplier: Double
    ): String {
        val totalHours = sessions.sumOf { it.totalHours }
        val totalOt = sessions.sumOf { it.overtimeHours }
        val totalEarnings = sessions.sumOf { it.totalEarnings }
        val manualCount = sessions.count { it.integrityType == "MANUAL" }

        return """
            📊 **Auditoría de Jornadas - W Tracker Engine**
            
            • **Horas Totales Computadas:** ${String.format("%.1f", totalHours)} h (${String.format("%.1f", totalOt)} h Overtime)
            • **Total Ganancia Estimada:** ${String.format("$%.2f", totalEarnings)} USD
            • **Transacciones Ajustadas Manualmente:** $manualCount
            
            🔍 **Hallazgos Clave:**
            1. Se aplicó una tarifa base de $$baseRate/h con recargo de ${overtimeMultiplier}x para overtime.
            2. ${if (manualCount > 0) "Existen $manualCount jornadas editadas manualmente que requieren firma de validación." else "Todas las jornadas registradas mantienen integridad nativa verificada."}
            3. ${if (totalOt > 5.0) "Atención: Elevada acumulación de tiempo extra esta semana." else "La carga horaria se mantiene dentro de los parámetros estándar de jornada."}
            
            💡 **Recomendaciones:**
            • Revisa la pestaña de Reportes para exportar tu PDF con el desglose formal para nómina.
            • Activa la geocerca multi-destino en Ajustes si trabajas en más de una obra al día.
        """.trimIndent()
    }

    private fun generateLocalAssistantAnswer(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("overtime") || q.contains("hora extra") || q.contains("ot") -> {
                "En W Tracker, las horas extras se computan automáticamente al superar las 8 horas diarias de jornada base. La tarifa se multiplica por el factor de overtime configurado en Ajustes (por defecto 1.5x)."
            }
            q.contains("almuerzo") || q.contains("lonche") || q.contains("pausa") -> {
                "Puedes registrar tu tiempo de almuerzo presionando 'Almuerzo' en la pantalla Hoy, o confirmar los minutos a descontar al finalizar tu jornada."
            }
            q.contains("traslado") || q.contains("viaje") || q.contains("obra") -> {
                "Si te trasladas entre múltiples centros de trabajo en un mismo día, usa la opción 'Traslado'. El tiempo de viaje se computará como tiempo remunerado conforme a las reglas multi-destino."
            }
            q.contains("pdf") || q.contains("reporte") || q.contains("imprimir") -> {
                "En la pestaña 'Reportes' puedes generar e imprimir un reporte en PDF formal con el desglose de horas normales, horas extra y total bruto para entregarlo a tu supervisor o cliente."
            }
            else -> {
                "Hola, soy tu Asistente W Tracker. Puedo ayudarte con el cálculo de tus horas de trabajo, tarifas de overtime, resolución de traslapes en el historial y exportación de reportes PDF de nómina. ¿En qué te puedo colaborar hoy?"
            }
        }
    }
}
