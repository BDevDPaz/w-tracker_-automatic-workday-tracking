package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun OnboardingScreen(viewModel: MainViewModel) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 6

    var isDisclaimerAccepted by remember { mutableStateOf(prefs.isDisclaimerAccepted) }
    var hasForegroundPermission by remember { mutableStateOf(viewModel.geofenceService.hasLocationPermission()) }
    var hasBackgroundPermission by remember { mutableStateOf(viewModel.geofenceService.hasBackgroundLocationPermission()) }
    var isBatteryUnrestricted by remember { mutableStateOf(prefs.unrestrictedBatteryConfigured) }
    var isBackgroundExecutionEnabled by remember { mutableStateOf(prefs.backgroundExecutionEnabled) }
    var isHapticEnabled by remember { mutableStateOf(prefs.hapticFeedbackEnabled) }
    var isAlertsEnabled by remember { mutableStateOf(prefs.dailyReminder) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ObsidianBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // --- HEADER WITH PROGRESS BAR ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) currentStep--
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Anterior",
                            tint = MutedGold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "INCORPORACIÓN Y AJUSTES",
                        color = MutedGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Paso $currentStep de $totalSteps",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = CarbonMistElevated,
                    border = BorderStroke(1.dp, SteelLineBorder)
                ) {
                    Text(
                        text = "${(currentStep * 100) / totalSteps}%",
                        color = MutedGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MutedGold,
                trackColor = CarbonMistElevated,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- STEP CONTENT ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentStep) {
                    1 -> Step1Disclaimer(
                        isAccepted = isDisclaimerAccepted,
                        onAcceptedChange = {
                            isDisclaimerAccepted = it
                            if (it) {
                                viewModel.acceptDisclaimer()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )
                    2 -> Step2GpsPermissions(
                        hasForeground = hasForegroundPermission,
                        hasBackground = hasBackgroundPermission,
                        onVerify = {
                            hasForegroundPermission = viewModel.geofenceService.hasLocationPermission()
                            hasBackgroundPermission = viewModel.geofenceService.hasBackgroundLocationPermission()
                            viewModel.showToast(
                                if (hasForegroundPermission) "Permiso GPS de primer plano verificado ✓"
                                else "Por favor aprueba el permiso de ubicación en Ajustes"
                            )
                        },
                        onOpenSettings = {
                            openAppSettings(context)
                        }
                    )
                    3 -> Step3BatteryOptimization(
                        isUnrestricted = isBatteryUnrestricted,
                        onToggleUnrestricted = {
                            isBatteryUnrestricted = it
                            viewModel.setBatteryOptimizationConfigured(it)
                            if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onOpenBatterySettings = {
                            openBatteryOptimizationSettings(context)
                        }
                    )
                    4 -> Step4BackgroundExecution(
                        isEnabled = isBackgroundExecutionEnabled,
                        onToggle = {
                            isBackgroundExecutionEnabled = it
                            viewModel.setBackgroundExecution(it)
                            if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    5 -> Step5HapticsAndAlerts(
                        isHaptic = isHapticEnabled,
                        isAlerts = isAlertsEnabled,
                        onHapticChange = {
                            isHapticEnabled = it
                            viewModel.setHapticFeedback(it)
                            if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onAlertsChange = {
                            isAlertsEnabled = it
                        },
                        onTestHaptic = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.showToast("¡Prueba de vibración háptica realizada!")
                        }
                    )
                    6 -> Step6Summary(
                        isDisclaimerAccepted = isDisclaimerAccepted,
                        hasForegroundGps = hasForegroundPermission,
                        hasBackgroundGps = hasBackgroundPermission,
                        isBatteryUnrestricted = isBatteryUnrestricted,
                        isBackgroundExecution = isBackgroundExecutionEnabled,
                        isHapticEnabled = isHapticEnabled
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTTOM NAVIGATION BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep < totalSteps) {
                    TextButton(
                        onClick = {
                            currentStep = totalSteps
                        }
                    ) {
                        Text("Omitir por ahora", color = TextMuted, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (currentStep == 1 && !isDisclaimerAccepted) {
                                viewModel.showToast("Debes aceptar el descargo de responsabilidad para continuar.")
                                return@Button
                            }
                            if (currentStep < totalSteps) {
                                currentStep++
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MutedGold),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Siguiente", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = ObsidianBackground
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.completeOnboarding()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StateActiveGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ObsidianBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Completar e Iniciar Aplicación",
                            color = ObsidianBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Step1Disclaimer(
    isAccepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MutedGold.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = MutedGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "DESCARGO DE RESPONSABILIDAD",
                    color = MutedGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Términos y Privacidad de Ubicación",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GraphiteSurface,
            border = BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Por favor lee atentamente los siguientes términos antes de hacer uso de la aplicación:",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                DisclaimerBullet(
                    title = "1. Uso Responsable de la Geolocalización",
                    body = "Esta aplicación utiliza el GPS del dispositivo para registrar la llegada, permanencia y salida de zonas de trabajo o proyectos configurados. No se realiza rastreo continuo en tiempo real fuera de las geocercas autorizadas."
                )

                Spacer(modifier = Modifier.height(10.dp))

                DisclaimerBullet(
                    title = "2. Precisión del GPS y Factores Ambientales",
                    body = "La exactitud de la detección depende del sensor del dispositivo, la visibilidad de satélites y la cobertura de red. El usuario es responsable de verificar que las horas registradas coincidan con su jornada real."
                )

                Spacer(modifier = Modifier.height(10.dp))

                DisclaimerBullet(
                    title = "3. Almacenamiento Local y Privacidad de Datos",
                    body = "Toda la información de ubicaciones, jornadas laborales y reportes financieros se almacena de forma local y encriptada en la base de datos del dispositivo. Ninguna información es transmitida a terceros sin consentimiento."
                )

                Spacer(modifier = Modifier.height(10.dp))

                DisclaimerBullet(
                    title = "4. Ajustes Manuales y Auditoría",
                    body = "En caso de inconsistencias causadas por pérdidas de señal de GPS, el sistema permite realizar ajustes manuales marcados transparente como 'Ajustes Manuales'."
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAcceptedChange(!isAccepted) },
            shape = RoundedCornerShape(16.dp),
            color = if (isAccepted) StateActiveGreen.copy(alpha = 0.15f) else CarbonMistElevated,
            border = BorderStroke(1.dp, if (isAccepted) StateActiveGreen else SteelLineBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAccepted,
                    onCheckedChange = onAcceptedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = StateActiveGreen,
                        uncheckedColor = TextMuted,
                        checkmarkColor = ObsidianBackground
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "He leído y acepto el descargo de responsabilidad y las condiciones de uso.",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun DisclaimerBullet(title: String, body: String) {
    Column {
        Text(text = title, color = MutedGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = body, color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
fun Step2GpsPermissions(
    hasForeground: Boolean,
    hasBackground: Boolean,
    onVerify: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MutedGold.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = MutedGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "CONFIGURACIÓN DE GPS",
                    color = MutedGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Permisos de Ubicación",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Para detectar de forma precisa cuando entras o sales de una obra o destino, la aplicación requiere permisos de ubicación.",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card 1: Foreground GPS
        PermissionCardItem(
            title = "1. Ubicación en Primer Plano",
            subtitle = "Permite identificar tu ubicación al abrir la app e iniciar jornadas.",
            isGranted = hasForeground,
            onActionClick = onOpenSettings,
            actionLabel = "Ajustes de GPS"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Card 2: Background GPS
        PermissionCardItem(
            title = "2. Ubicación en Segundo Plano",
            subtitle = "Permite que las geocercas registren entradas y salidas automáticamente sin necesidad de encender la pantalla.",
            isGranted = hasBackground,
            onActionClick = onOpenSettings,
            actionLabel = "Permitir Todo el Tiempo"
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MutedGold)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = MutedGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verificar Permisos del Sistema", color = MutedGold, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PermissionCardItem(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onActionClick: () -> Unit,
    actionLabel: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = GraphiteSurface,
        border = BorderStroke(1.dp, if (isGranted) StateActiveGreen.copy(alpha = 0.4f) else SteelLineBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = CircleShape,
                    color = if (isGranted) StateActiveGreen.copy(alpha = 0.2f) else StateExitAmber.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.GpsOff,
                            contentDescription = null,
                            tint = if (isGranted) StateActiveGreen else StateExitAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGranted) "Verificado ✓" else "Pendiente",
                            color = if (isGranted) StateActiveGreen else StateExitAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = subtitle, color = TextMuted, fontSize = 12.sp, lineHeight = 16.sp)

            Spacer(modifier = Modifier.height(12.dp))

            if (!isGranted) {
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonMistElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MutedGold)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MutedGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(actionLabel, color = MutedGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun Step3BatteryOptimization(
    isUnrestricted: Boolean,
    onToggleUnrestricted: (Boolean) -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MutedGold.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = MutedGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "OPTIMIZACIÓN DE ENERGÍA",
                    color = MutedGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Uso de Batería Sin Restricciones",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GraphiteSurface,
            border = BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "¿Por qué es crucial para las geocercas?",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "El sistema operativo Android suspende automáticamente las aplicaciones que están en segundo plano para ahorrar batería. Para garantizar que la detección de entrada y salida de geocercas funcione de forma ininterrumpida mientras te desplazas, es necesario configurar el uso de batería como 'Sin Restricciones'.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onOpenBatterySettings,
                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.BatteryFull, contentDescription = null, tint = ObsidianBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abrir Ajustes de Batería de Android", color = ObsidianBackground, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CarbonMistElevated,
            border = BorderStroke(1.dp, SteelLineBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Configuración Confirmada",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Marcar como 'Batería Sin Restricciones' configurado.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = isUnrestricted,
                    onCheckedChange = onToggleUnrestricted,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBackground,
                        checkedTrackColor = StateActiveGreen,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = GraphiteSurface
                    )
                )
            }
        }
    }
}

@Composable
fun Step4BackgroundExecution(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MutedGold.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = MutedGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "EJECUCIÓN CONTINUA",
                    color = MutedGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Servicio en Segundo Plano",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GraphiteSurface,
            border = BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Protección Anti-rebote y Auto-geocerca",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "El motor de geocercas incluye un filtro anti-rebote (debounce) de 10 segundos y verificación de precisión GPS (máximo 35 metros) para descartar eventos erróneos causados por fluctuaciones de señal. Mantener la ejecución en segundo plano garantiza la lectura inmediata de estas validaciones.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CarbonMistElevated,
            border = BorderStroke(1.dp, SteelLineBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Servicio de Geocercas Habilitado",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Permitir servicio de monitoreo en segundo plano.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBackground,
                        checkedTrackColor = StateActiveGreen,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = GraphiteSurface
                    )
                )
            }
        }
    }
}

@Composable
fun Step5HapticsAndAlerts(
    isHaptic: Boolean,
    isAlerts: Boolean,
    onHapticChange: (Boolean) -> Unit,
    onAlertsChange: (Boolean) -> Unit,
    onTestHaptic: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MutedGold.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = MutedGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "FEEDBACK DEL SISTEMA",
                    color = MutedGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Interacciones Hápticas y Alertas",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle 1: Haptics
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = GraphiteSurface,
            border = BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Retroalimentación Háptica",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Emite una pequeña vibración al registrar entradas o salidas de zonas y confirmar acciones.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Switch(
                        checked = isHaptic,
                        onCheckedChange = onHapticChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBackground,
                            checkedTrackColor = MutedGold,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CarbonMistElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onTestHaptic,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MutedGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = MutedGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Probar Vibración Háptica Ahora", color = MutedGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Toggle 2: System Alerts
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = GraphiteSurface,
            border = BorderStroke(1.dp, SteelLineBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Alertas y Notificaciones de Geocerca",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Notificar en barra de estado cuando se detecte un cambio de zona o salida de perímetro.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Switch(
                    checked = isAlerts,
                    onCheckedChange = onAlertsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBackground,
                        checkedTrackColor = MutedGold,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CarbonMistElevated
                    )
                )
            }
        }
    }
}

@Composable
fun Step6Summary(
    isDisclaimerAccepted: Boolean,
    hasForegroundGps: Boolean,
    hasBackgroundGps: Boolean,
    isBatteryUnrestricted: Boolean,
    isBackgroundExecution: Boolean,
    isHapticEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = StateActiveGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StateActiveGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "RESUMEN DE CONFIGURACIÓN",
                    color = StateActiveGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "¡Todo Listo para Empezar!",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GraphiteSurface,
            border = BorderStroke(1.dp, SteelLineBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Estado de incorporación:",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                SummaryCheckRow(label = "Descargo de Responsabilidad Aceptado", isChecked = isDisclaimerAccepted)
                SummaryCheckRow(label = "Permiso GPS Primer Plano", isChecked = hasForegroundGps)
                SummaryCheckRow(label = "Permiso GPS Segundo Plano", isChecked = hasBackgroundGps)
                SummaryCheckRow(label = "Uso de Batería Sin Restricciones", isChecked = isBatteryUnrestricted)
                SummaryCheckRow(label = "Servicio de Geocercas Activo", isChecked = isBackgroundExecution)
                SummaryCheckRow(label = "Interacciones Hápticas Activadas", isChecked = isHapticEnabled)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Puedes volver a revisar o ajustar estos términos y permisos en cualquier momento desde el menú de Ajustes.",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun SummaryCheckRow(label: String, isChecked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.GpsOff,
            contentDescription = null,
            tint = if (isChecked) StateActiveGreen else StateExitAmber,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback
    }
}

private fun openBatteryOptimizationSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        openAppSettings(context)
    }
}
