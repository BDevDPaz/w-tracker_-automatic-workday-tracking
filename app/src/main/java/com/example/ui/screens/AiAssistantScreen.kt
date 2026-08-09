package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.GeminiMessage
import com.example.ai.GeminiService
import com.example.ui.theme.CarbonMistElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.MutedGold
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.StateActiveGreen
import com.example.ui.theme.SteelLineBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

enum class AiTab(val title: String) {
    AUDIT("Auditoría de Nómina"),
    CHAT("Consulta IA"),
    SUMMARY("Notas de Cierre")
}

data class ChatMsg(val isUser: Boolean, val text: String)

@Composable
fun AiAssistantScreen(viewModel: MainViewModel) {
    val geminiService = remember { GeminiService() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(AiTab.AUDIT) }

    // Audit State
    var auditAnalysis by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Chat State
    val chatMessages = remember {
        mutableStateListOf(
            ChatMsg(false, "¡Hola! Soy tu Asistente de W Tracker. ¿En qué te puedo ayudar hoy con tus jornadas, cálculo de horas extra o registros?")
        )
    }
    var inputQuery by remember { mutableStateOf("") }
    var isChatLoading by remember { mutableStateOf(false) }

    // Smart Note State
    var generatedNote by remember { mutableStateOf<String?>(null) }

    fun runAudit() {
        scope.launch {
            isAnalyzing = true
            auditAnalysis = geminiService.analyzeWorkSessions(
                sessions = sessions,
                baseRate = prefs.hourlyRate,
                overtimeMultiplier = prefs.overtimeMultiplier
            )
            isAnalyzing = false
        }
    }

    fun sendChat(query: String) {
        if (query.isBlank()) return
        chatMessages.add(ChatMsg(true, query))
        inputQuery = ""
        isChatLoading = true

        scope.launch {
            val history = chatMessages.map {
                GeminiMessage(
                    role = if (it.isUser) "user" else "model",
                    text = it.text
                )
            }
            val reply = geminiService.askAssistant(history, query)
            chatMessages.add(ChatMsg(false, reply))
            isChatLoading = false
        }
    }

    LaunchedEffect(Unit) {
        runAudit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ASISTENTE W TRACKER",
                    color = MutedGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Inteligencia & Auditoría",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                shape = CircleShape,
                color = CarbonMistElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, MutedGold)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MutedGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "IA Activa", color = MutedGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SEGMENTED TABS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiTab.values().forEach { tab ->
                FilterChip(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    label = { Text(tab.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MutedGold,
                        selectedLabelColor = ObsidianBackground,
                        containerColor = CarbonMistElevated,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- TAB CONTENT ---
        when (selectedTab) {
            AiTab.AUDIT -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = GraphiteSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ANÁLISIS AUTOMÁTICO DE REGISTROS",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                TextButton(onClick = { runAudit() }) {
                                    Text(text = "Reanalizar", color = MutedGold, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isAnalyzing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = MutedGold)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Auditando jornadas y patrones de horas extra...", color = TextSecondary, fontSize = 13.sp)
                                    }
                                }
                            } else {
                                Text(
                                    text = auditAnalysis ?: "No hay suficientes datos para auditar.",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            AiTab.CHAT -> {
                Column(modifier = Modifier.weight(1f)) {
                    // Chat Messages List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages) { msg ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (msg.isUser) MutedGold else GraphiteSurface,
                                    border = if (msg.isUser) null else androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder),
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = if (msg.isUser) ObsidianBackground else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (msg.isUser) FontWeight.SemiBold else FontWeight.Normal,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }
                        if (isChatLoading) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MutedGold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pensando...", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick prompt chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TextButton(
                            onClick = { sendChat("¿Cómo se calculan mis horas extra?") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("¿Cómo es el OT?", fontSize = 10.sp, color = MutedGold)
                        }
                        TextButton(
                            onClick = { sendChat("¿Tengo solapamientos en mis registros?") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("¿Ver solapamientos?", fontSize = 10.sp, color = MutedGold)
                        }
                    }

                    // Input Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = { inputQuery = it },
                            placeholder = { Text("Escribe una consulta a la IA...", color = TextMuted) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MutedGold,
                                unfocusedBorderColor = SteelLineBorder,
                                focusedContainerColor = GraphiteSurface,
                                unfocusedContainerColor = GraphiteSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { sendChat(inputQuery) },
                            enabled = inputQuery.isNotBlank() && !isChatLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = if (inputQuery.isNotBlank()) MutedGold else TextMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }

            AiTab.SUMMARY -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = GraphiteSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "GENERADOR DE NOTA DE TURNO / CLIENTE",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Genera un texto listo para copiar y enviar a tu supervisor o cliente por WhatsApp/Correo con los datos exactos del día.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val activeSession = viewModel.activeSession.collectAsStateWithLifecycle().value
                            val elapsedTime = viewModel.elapsedTimeStr.collectAsStateWithLifecycle().value

                            Button(
                                onClick = {
                                    val activeDest = activeSession?.destination ?: "Obra / Centro de Trabajo"
                                    val totalH = activeSession?.totalHours ?: 0.0
                                    generatedNote = """
                                        📌 *REPORTE DE TURNO - W TRACKER*
                                        • Operario: ${prefs.name.ifBlank { "Técnico" }}
                                        • Obra/Destino: $activeDest
                                        • Horario: ${activeSession?.startTime ?: "08:00"} — ${activeSession?.endTime ?: "En curso"}
                                        • Tiempo Acumulado: $elapsedTime
                                        • Estado: ${activeSession?.state ?: "JORNADA CERRADA"}
                                        
                                        _Generado con W Tracker v1.0_
                                    """.trimIndent()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
                            ) {
                                Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = ObsidianBackground)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generar Nota de Turno", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                            }

                            generatedNote?.let { noteText ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = CarbonMistElevated
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(text = noteText, color = TextPrimary, fontSize = 13.sp, lineHeight = 20.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(noteText))
                                                viewModel.showToast("Nota copiada al portapapeles")
                                            },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copiar", tint = MutedGold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
