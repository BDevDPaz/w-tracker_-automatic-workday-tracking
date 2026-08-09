package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SessionState
import com.example.ui.components.StatusPill
import com.example.ui.theme.CarbonMistElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.MutedGold
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.StateActiveGreen
import com.example.ui.theme.StateExitAmber
import com.example.ui.theme.StateLunchOrange
import com.example.ui.theme.StatePassiveSlate
import com.example.ui.theme.StateTransferBlue
import com.example.ui.theme.SteelLineBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Formatters

@Composable
fun HoyScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val sessionState by viewModel.activeSessionState.collectAsStateWithLifecycle()
    val elapsedTime by viewModel.elapsedTimeStr.collectAsStateWithLifecycle()
    val visits by viewModel.visits.collectAsStateWithLifecycle()
    val transfers by viewModel.transfers.collectAsStateWithLifecycle()
    val timeline by viewModel.timeline.collectAsStateWithLifecycle()
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()

    var showStartDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    var inputDestination by remember { mutableStateOf("Sede Norte - Obra Central") }
    var inputAlias by remember { mutableStateOf("Fase 1") }
    var inputLunchMinutes by remember { mutableStateOf(prefs.lunchDiscountMinutes.toString()) }
    var inputNote by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "W TRACKER",
                    color = MutedGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Jornada de Hoy",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            StatusPill(state = sessionState)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- OPERATIONAL HERO CARD ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = GraphiteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Location Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MutedGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeSession?.destination ?: "Sin destino activo",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    activeSession?.alias?.let {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "($it)",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Timer Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CarbonMistElevated)
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TIEMPO ACUMULADO",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (sessionState == SessionState.NO_INICIADA) "00:00:00" else elapsedTime,
                            color = if (sessionState == SessionState.ACTIVA) StateActiveGreen else MutedGold,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        if (activeSession != null) {
                            Text(
                                text = "Tarifa: ${Formatters.formatCurrency(activeSession!!.snapshotRate)}/h • Inicio: ${Formatters.formatTime12(activeSession!!.startTime)}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dynamic Action Buttons Matrix
                when (sessionState) {
                    SessionState.NO_INICIADA -> {
                        Button(
                            onClick = { showStartDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = ObsidianBackground)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "INICIAR JORNADA", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                        }
                    }

                    SessionState.ESPERANDO -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.confirmArrival() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StateActiveGreen)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "CONFIRMAR LLEGADA FÍSICA", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { viewModel.cancelWorkDay() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StateExitAmber)
                            ) {
                                Text(text = "Cancelar Configuración")
                            }
                        }
                    }

                    SessionState.ACTIVA -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.startLunch() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StateLunchOrange)
                                ) {
                                    Icon(imageVector = Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Almuerzo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { showTransferDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StateTransferBlue)
                                ) {
                                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Traslado", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = { showFinishDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = ObsidianBackground)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "FINALIZAR JORNADA", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    SessionState.PAUSADA -> {
                        Button(
                            onClick = { viewModel.endLunch() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StateActiveGreen)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "FINALIZAR ALMUERZO Y RETORNAR", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    SessionState.TRASLADO -> {
                        Button(
                            onClick = { viewModel.completeTransfer() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StateActiveGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "LLEGADA AL NUEVO DESTINO", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    SessionState.SALIDA -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = StateExitAmber.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StateExitAmber)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StateExitAmber)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Fuera de geocerca. ¿Saliste de la obra o cambiaste de destino?",
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.resumeFromExit() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(23.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StateActiveGreen)
                                ) {
                                    Text(text = "Reingresé", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { showFinishDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(23.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
                                ) {
                                    Text(text = "Cerrar Jornada", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ObsidianBackground)
                                }
                            }
                        }
                    }

                    SessionState.CERRADA -> {
                        Text(
                            text = "Jornada completada. Puedes revisar tus totales en la pestaña de Reportes o Historial.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- DESTINATION VISITS & TRANSFERS MULTI-SITE LOG ---
        if (visits.isNotEmpty() || transfers.isNotEmpty()) {
            Text(
                text = "DESTINOS Y TRASLADOS EN JORNADA",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = GraphiteSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    visits.forEachIndexed { idx, v ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(StateActiveGreen.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "${idx + 1}", color = StateActiveGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = v.destination, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "Entrada: ${Formatters.formatTime12(v.enterTime)} • Salida: ${Formatters.formatTime12(v.exitTime)}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- TIMELINE OF EVENTS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CRONOLOGÍA DE EVENTOS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            if (activeSession != null) {
                TextButton(onClick = { showNoteDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MutedGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Agregar Nota", color = MutedGold, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (timeline.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = GraphiteSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Inicia una jornada para registrar eventos en tiempo real.", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                timeline.forEach { ev ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CarbonMistElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        when (ev.status) {
                                            "active" -> StateActiveGreen
                                            "warning" -> StateLunchOrange
                                            "closed" -> StatePassiveSlate
                                            else -> MutedGold
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = ev.description, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // --- DIALOGS ---

    // 1. Start Workday Dialog
    if (showStartDialog) {
        AlertDialog(
            onDismissRequest = { showStartDialog = false },
            title = { Text(text = "Iniciar Nueva Jornada", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Ingresa la dirección o nombre de la obra/centro de trabajo:", color = TextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = inputDestination,
                        onValueChange = { inputDestination = it },
                        label = { Text("Destino de Trabajo") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                    OutlinedTextField(
                        value = inputAlias,
                        onValueChange = { inputAlias = it },
                        label = { Text("Alias / Referencia (Opcional)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputDestination.isNotBlank()) {
                            viewModel.startWorkDay(inputDestination, inputAlias)
                            showStartDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
                ) {
                    Text("Iniciar", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = GraphiteSurface
        )
    }

    // 2. Transfer Destination Dialog
    if (showTransferDialog) {
        var transferDest by remember { mutableStateOf("") }
        var transferAlias by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Iniciar Traslado Multi-Destino", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("El tiempo de viaje será computado como traslado remunerado.", color = TextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = transferDest,
                        onValueChange = { transferDest = it },
                        label = { Text("Nuevo Destino") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                    OutlinedTextField(
                        value = transferAlias,
                        onValueChange = { transferAlias = it },
                        label = { Text("Alias / Referencia") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (transferDest.isNotBlank()) {
                            viewModel.startTransfer(transferDest, transferAlias)
                            showTransferDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StateTransferBlue)
                ) {
                    Text("Comenzar Traslado", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = GraphiteSurface
        )
    }

    // 3. Finish Workday Dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Finalizar Jornada Laboral", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Confirma los minutos a descontar por concepto de almuerzo:", color = TextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = inputLunchMinutes,
                        onValueChange = { inputLunchMinutes = it },
                        label = { Text("Minutos de Almuerzo (ej: 30)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lunch = inputLunchMinutes.toIntOrNull() ?: 30
                        viewModel.finishWorkDay(lunch)
                        showFinishDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
                ) {
                    Text("Cerrar Jornada", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Volver", color = TextSecondary)
                }
            },
            containerColor = GraphiteSurface
        )
    }

    // 4. Add Note Dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Agregar Nota a la Jornada", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = inputNote,
                    onValueChange = { inputNote = it },
                    label = { Text("Escribe una observación...") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputNote.isNotBlank()) {
                            viewModel.addNote(inputNote)
                            inputNote = ""
                            showNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
                ) {
                    Text("Guardar", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = GraphiteSurface
        )
    }
}
