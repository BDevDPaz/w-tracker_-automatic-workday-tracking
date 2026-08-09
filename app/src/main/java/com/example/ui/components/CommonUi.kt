package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IntegrityType
import com.example.data.model.SessionState
import com.example.ui.theme.CarbonMistElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.MutedGold
import com.example.ui.theme.StateActiveGreen
import com.example.ui.theme.StateConflictRed
import com.example.ui.theme.StateExitAmber
import com.example.ui.theme.StateLunchOrange
import com.example.ui.theme.StatePassiveSlate
import com.example.ui.theme.StateTransferBlue
import com.example.ui.theme.StateWaitingYellow
import com.example.ui.theme.SteelLineBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StatusPill(state: SessionState, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (state) {
        SessionState.NO_INICIADA -> Triple(StatePassiveSlate.copy(alpha = 0.2f), StatePassiveSlate, "NO INICIADA")
        SessionState.ESPERANDO -> Triple(StateWaitingYellow.copy(alpha = 0.2f), StateWaitingYellow, "ESPERANDO LLEGADA")
        SessionState.ACTIVA -> Triple(StateActiveGreen.copy(alpha = 0.2f), StateActiveGreen, "JORNADA ACTIVA")
        SessionState.PAUSADA -> Triple(StateLunchOrange.copy(alpha = 0.2f), StateLunchOrange, "EN ALMUERZO")
        SessionState.TRASLADO -> Triple(StateTransferBlue.copy(alpha = 0.2f), StateTransferBlue, "EN TRASLADO")
        SessionState.SALIDA -> Triple(StateExitAmber.copy(alpha = 0.2f), StateExitAmber, "SALIDA DETECTADA")
        SessionState.CERRADA -> Triple(StatePassiveSlate.copy(alpha = 0.2f), StatePassiveSlate, "COMPLETADA")
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(textColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun IntegrityBadge(type: String, modifier: Modifier = Modifier) {
    val (label, color) = when (type) {
        IntegrityType.VALID.name -> Pair("VALIDADA", StateActiveGreen)
        IntegrityType.MANUAL.name -> Pair("AJUSTE MANUAL", TextSecondary)
        IntegrityType.AUTO.name -> Pair("AUTO CIERRE", StatePassiveSlate)
        IntegrityType.CONFLICT.name -> Pair("REVISAR", StateExitAmber)
        else -> Pair(type, TextMuted)
    }

    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    isGold: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = GraphiteSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SteelLineBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = if (isGold) MutedGold else TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ToastNotificationBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = CircleShape,
                color = CarbonMistElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, MutedGold)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MutedGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = message,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
