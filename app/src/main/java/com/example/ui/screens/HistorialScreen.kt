package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.model.IntegrityType
import com.example.data.model.WorkSession
import com.example.ui.components.IntegrityBadge
import com.example.ui.theme.CarbonMistElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.MutedGold
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.StateConflictRed
import com.example.ui.theme.SteelLineBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Formatters

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.filled.HistoryToggleOff

enum class HistoryFilter(val label: String) {
    ALL("Todo"),
    WEEK("Esta semana"),
    MANUAL("Ajustadas"),
    CONFLICT("Por revisar")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(viewModel: MainViewModel) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    var sessionToEdit by remember { mutableStateOf<WorkSession?>(null) }
    var editDate by remember { mutableStateOf("") }
    var editStart by remember { mutableStateOf("") }
    var editEnd by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }

    val filteredSessions = remember(sessions, selectedFilter, searchQuery) {
        sessions.filter { s ->
            val matchesFilter = when (selectedFilter) {
                HistoryFilter.ALL -> true
                HistoryFilter.WEEK -> {
                    val weekStart = Formatters.getWeekStartIso(0)
                    val weekEnd = Formatters.getWeekEndIso(weekStart)
                    s.date >= weekStart && s.date <= weekEnd
                }
                HistoryFilter.MANUAL -> s.integrityType == IntegrityType.MANUAL.name
                HistoryFilter.CONFLICT -> s.integrityType == IntegrityType.CONFLICT.name
            }
            val matchesSearch = searchQuery.isBlank() ||
                    s.destination.contains(searchQuery, ignoreCase = true) ||
                    (s.alias?.contains(searchQuery, ignoreCase = true) == true) ||
                    s.date.contains(searchQuery)
            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        Text(
            text = "HISTORIAL Y AUDITORÍA",
            color = MutedGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Jornadas Pasadas",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar por destino, fecha o alias...", color = TextMuted) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MutedGold,
                unfocusedBorderColor = SteelLineBorder,
                focusedContainerColor = GraphiteSurface,
                unfocusedContainerColor = GraphiteSurface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HistoryFilter.values()) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MutedGold,
                        selectedLabelColor = ObsidianBackground,
                        containerColor = CarbonMistElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CarbonMistElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HistoryToggleOff,
                                contentDescription = null,
                                tint = MutedGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Sin registros en el historial",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No se encontraron jornadas registradas para este filtro o búsqueda.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredSessions, key = { it.id }) { session ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteSession(session.id)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> com.example.ui.theme.StateExitAmber
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Eliminar",
                                        color = ObsidianBackground,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = ObsidianBackground
                                    )
                                }
                            }
                        },
                        content = {
                            SessionCardItem(
                                session = session,
                                onEdit = {
                                    sessionToEdit = session
                                    editDate = session.date
                                    editStart = session.startTime
                                    editEnd = session.endTime ?: ""
                                    editNotes = session.notes ?: ""
                                },
                                onDelete = {
                                    viewModel.deleteSession(session.id)
                                }
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Edit Session Modal Dialog
    sessionToEdit?.let { target ->
        AlertDialog(
            onDismissRequest = { sessionToEdit = null },
            title = { Text("Ajustar Jornada Manualmente", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Nota: Los cambios marcarán esta jornada como 'AJUSTE MANUAL'.", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = editDate,
                        onValueChange = { editDate = it },
                        label = { Text("Fecha (YYYY-MM-DD)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                    OutlinedTextField(
                        value = editStart,
                        onValueChange = { editStart = it },
                        label = { Text("Hora Inicio (HH:MM)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                    OutlinedTextField(
                        value = editEnd,
                        onValueChange = { editEnd = it },
                        label = { Text("Hora Fin (HH:MM)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Observaciones / Justificación") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.editSession(target.id, editDate, editStart, editEnd, editNotes)
                        sessionToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
                ) {
                    Text("Guardar Ajuste", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToEdit = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = GraphiteSurface
        )
    }
}

@Composable
fun SessionCardItem(
    session: WorkSession,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { expanded = !expanded },
        color = GraphiteSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Formatters.formatDate(session.date),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IntegrityBadge(type = session.integrityType)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = session.destination + (session.alias?.let { " ($it)" } ?: ""),
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${Formatters.formatTimeRange(session.startTime, session.endTime)} (${Formatters.formatHours(session.totalHours)})",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = Formatters.formatCurrency(session.totalEarnings),
                    color = MutedGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(CarbonMistElevated, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "DESGLOSE DE AUDITORÍA",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Descuento Almuerzo: ${session.lunchDeductedMinutes} min\n" +
                                "• Horas Extra (OT): ${Formatters.formatHours(session.overtimeHours)}\n" +
                                "• Tarifa Snapshot: ${Formatters.formatCurrency(session.snapshotRate)}/h\n" +
                                "• Destinos Visitados: ${session.destinationCount}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    session.notes?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Notas: $it", color = MutedGold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = MutedGold)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = StateConflictRed)
                        }
                    }
                }
            }
        }
    }
}
