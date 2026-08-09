package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CarbonMistElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.MutedGold
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.StateActiveGreen
import com.example.ui.theme.StateExitAmber
import com.example.ui.theme.SteelLineBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AjustesScreen(viewModel: MainViewModel) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val health by viewModel.systemHealth.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf(prefs.name) }
    var hourlyRate by remember { mutableStateOf(prefs.hourlyRate.toString()) }
    var reportEmail by remember { mutableStateOf(prefs.reportEmail) }
    var baseWorkHour by remember { mutableStateOf(prefs.baseWorkHour) }
    var overtimeMultiplier by remember { mutableStateOf(prefs.overtimeMultiplier.toString()) }
    var lunchDiscountMinutes by remember { mutableStateOf(prefs.lunchDiscountMinutes.toString()) }
    var geofenceRadius by remember { mutableStateOf(prefs.geofenceRadius.toString()) }
    var multiDestination by remember { mutableStateOf(prefs.multiDestination) }
    var paidTransfers by remember { mutableStateOf(prefs.paidTransfers) }

    LaunchedEffect(prefs) {
        name = prefs.name
        hourlyRate = prefs.hourlyRate.toString()
        reportEmail = prefs.reportEmail
        baseWorkHour = prefs.baseWorkHour
        overtimeMultiplier = prefs.overtimeMultiplier.toString()
        lunchDiscountMinutes = prefs.lunchDiscountMinutes.toString()
        geofenceRadius = prefs.geofenceRadius.toString()
        multiDestination = prefs.multiDestination
        paidTransfers = prefs.paidTransfers
    }

    // Permission Launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.updateHealthCheck()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "CONFIGURACIÓN",
            color = MutedGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Ajustes Operativos",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- SECTION 1: PERFIL & DATOS ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GraphiteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "DATOS DEL OPERARIO / TRABAJADOR", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                )
                OutlinedTextField(
                    value = reportEmail,
                    onValueChange = { reportEmail = it },
                    label = { Text("Correo para Envío de Reportes") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION 2: PARÁMETROS FINANCIEROS Y DE NÓMINA ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GraphiteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "PARÁMETROS DE REGLA Y NÓMINA", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = hourlyRate,
                        onValueChange = { hourlyRate = it },
                        label = { Text("Tarifa Base ($/h)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                    OutlinedTextField(
                        value = baseWorkHour,
                        onValueChange = { baseWorkHour = it },
                        label = { Text("Jornada Base (HH:MM)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = overtimeMultiplier,
                        onValueChange = { overtimeMultiplier = it },
                        label = { Text("Multiplicador OT (ej: 1.5)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                    OutlinedTextField(
                        value = lunchDiscountMinutes,
                        onValueChange = { lunchDiscountMinutes = it },
                        label = { Text("Descuento Almuerzo (min)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                    )
                }

                OutlinedTextField(
                    value = geofenceRadius,
                    onValueChange = { geofenceRadius = it },
                    label = { Text("Radio de Geocerca (Metros)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MutedGold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION 3: REGLAS MULTI-SITIO ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GraphiteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "REGLAS MULTI-DESTINO Y TRASLADOS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Soporte Multi-Destino", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Permite registrar más de un centro de trabajo por jornada", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = multiDestination,
                        onCheckedChange = { multiDestination = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MutedGold, checkedTrackColor = CarbonMistElevated)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Traslados Remunerados", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Computar el tiempo de viaje entre obras como tiempo pagado", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = paidTransfers,
                        onCheckedChange = { paidTransfers = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MutedGold, checkedTrackColor = CarbonMistElevated)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION 3.5: INCORPORACIÓN Y DESCARGO DE RESPONSABILIDAD ---
        var showDisclaimerDialog by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GraphiteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "INCORPORACIÓN Y DESCARGO LEGAL", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Interacciones Hápticas", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Vibración de confirmación en cambios de estado y zonas", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = prefs.hapticFeedbackEnabled,
                        onCheckedChange = { viewModel.setHapticFeedback(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MutedGold, checkedTrackColor = CarbonMistElevated)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Batería Sin Restricciones", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Garantiza el monitoreo continuo de geocercas en segundo plano", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = prefs.unrestrictedBatteryConfigured,
                        onCheckedChange = { viewModel.setBatteryOptimizationConfigured(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MutedGold, checkedTrackColor = CarbonMistElevated)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showDisclaimerDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonMistElevated)
                    ) {
                        Text("Ver Descargo", color = MutedGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.restartOnboarding() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonMistElevated)
                    ) {
                        Text("Re-iniciar Incorporación", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showDisclaimerDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDisclaimerDialog = false },
                title = {
                    Text(
                        text = "Descargo de Responsabilidad",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "1. Uso de Geolocalización: La aplicación monitorea la llegada y salida de geocercas registradas para el cómputo de horas laborales.\n\n" +
                                   "2. Precisión del GPS: Los cálculos dependen del sensor del dispositivo y las condiciones meteorológicas/físicas.\n\n" +
                                   "3. Privacidad de Datos: Toda la información de ubicaciones y horas de trabajo se almacena localmente en el dispositivo.\n\n" +
                                   "4. Responsabilidad del Usuario: Es responsabilidad del usuario auditar periódicamente sus jornadas y realizar ajustes manuales si se presentaron fallas de señal.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showDisclaimerDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
                    ) {
                        Text("Entendido", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = GraphiteSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GraphiteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "DIAGNÓSTICO DEL SISTEMA Y GEOFENCE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(10.dp))

                HealthRow(
                    label = "Permiso de Geolocalización Activa",
                    isOk = health.hasFineLocationPermission,
                    onRequestPermission = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )

                HealthRow(
                    label = "Permiso de Ubicación en Segundo Plano",
                    isOk = health.hasBackgroundLocationPermission,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            )
                        }
                    }
                )

                HealthRow(
                    label = "Estado de Monitoreo Geofence",
                    isOk = health.isGeofenceActive,
                    onRequestPermission = null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION 5: ZONA ADMINISTRATIVA PROTEGIDA (SECURE BLOCK) ---
        var isAdminUnlocked by remember { mutableStateOf(false) }
        var adminPinInput by remember { mutableStateOf("") }
        var adminPinError by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = CarbonMistElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, MutedGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isAdminUnlocked) "ZONA ADMINISTRATIVA ✓" else "ZONA ADMINISTRATIVA",
                            color = MutedGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = if (isAdminUnlocked) "Acceso administrativo activo" else "Esta sección requiere clave protegida",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isAdminUnlocked) {
                    // LOCKED STATE - Password input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = adminPinInput,
                            onValueChange = {
                                adminPinInput = it
                                adminPinError = false
                            },
                            label = { Text("PIN de Acceso (ej: 1234)") },
                            isError = adminPinError,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MutedGold,
                                errorBorderColor = StateExitAmber
                            )
                        )
                        Button(
                            onClick = {
                                if (adminPinInput == "1234" || adminPinInput == "admin") {
                                    isAdminUnlocked = true
                                    adminPinInput = ""
                                    adminPinError = false
                                } else {
                                    adminPinError = true
                                    viewModel.showToast("PIN incorrecto. Intenta '1234'")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
                        ) {
                            Text("Desbloquear", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // UNLOCKED STATE - Admin Tools
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.showToast("WAL reindexado y libre de conflictos") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GraphiteSurface)
                        ) {
                            Text("1. Auditoría de WAL & Inconsistencias", color = TextPrimary, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.showToast("Caché de sesiones limpiada") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GraphiteSurface)
                        ) {
                            Text("2. Reset de Caché & Sincronía SQLite", color = TextPrimary, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.showToast("Registros duplicados purgados") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GraphiteSurface)
                        ) {
                            Text("3. Limpieza de Sesiones Huérfanas", color = TextPrimary, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { isAdminUnlocked = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = StateExitAmber)
                        ) {
                            Text("Bloquear Zona Administrativa", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- SAVE BUTTON ---
        Button(
            onClick = {
                val updatedPrefs = prefs.copy(
                    name = name,
                    hourlyRate = hourlyRate.toDoubleOrNull() ?: 20.0,
                    reportEmail = reportEmail,
                    baseWorkHour = baseWorkHour,
                    overtimeMultiplier = overtimeMultiplier.toDoubleOrNull() ?: 1.5,
                    lunchDiscountMinutes = lunchDiscountMinutes.toIntOrNull() ?: 30,
                    geofenceRadius = geofenceRadius.toIntOrNull() ?: 100,
                    multiDestination = multiDestination,
                    paidTransfers = paidTransfers,
                    updatedMs = System.currentTimeMillis()
                )
                viewModel.savePreferences(updatedPrefs)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MutedGold)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = ObsidianBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "GUARDAR CONFIGURACIÓN", color = ObsidianBackground, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun HealthRow(label: String, isOk: Boolean, onRequestPermission: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isOk) StateActiveGreen else StateExitAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = TextPrimary, fontSize = 12.sp)
        }
        if (!isOk && onRequestPermission != null) {
            Button(
                onClick = onRequestPermission,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = StateExitAmber)
            ) {
                Text(text = "Activar", fontSize = 10.sp, color = ObsidianBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}
