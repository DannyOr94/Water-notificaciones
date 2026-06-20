package com.watersf.app.presentation.notification.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.watersf.app.R
import com.watersf.app.presentation.theme.AppIcons
import com.watersf.app.presentation.theme.BackgroundBase
import com.watersf.app.presentation.theme.Dimens
import com.watersf.app.presentation.theme.SeverityHigh
import com.watersf.app.presentation.theme.SeverityHighContainer
import com.watersf.app.presentation.theme.TextPrimary

/**
 * [REQ-5.2] Aviso crítico de acumulación: aparece cuando el conteo de no leídas supera
 * el umbral. Usa el token semántico de severidad ALTA (rojo) e incluye CTA "Revisar".
 */
@Composable
fun AccumulationAlertBanner(
    visible: Boolean,
    unreadCount: Int,
    onReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Card(
            shape = RoundedCornerShape(Dimens.radiusMd),
            colors = CardDefaults.cardColors(containerColor = SeverityHighContainer),
            border = BorderStroke(Dimens.borderThin, SeverityHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevCard),
            modifier = modifier
                .padding(horizontal = Dimens.space4, vertical = Dimens.space2)
        ) {
            Row(
                modifier = Modifier.padding(Dimens.space4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.space3)
            ) {
                Icon(
                    imageVector = AppIcons.alertHigh,
                    contentDescription = null,
                    tint = SeverityHigh,
                    modifier = Modifier.size(Dimens.iconLg)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.accumulation_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = SeverityHigh,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.accumulation_desc, unreadCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
                Button(
                    onClick = onReview,
                    shape = RoundedCornerShape(Dimens.radiusSm),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SeverityHigh,
                        contentColor = BackgroundBase
                    )
                ) {
                    Text(
                        text = stringResource(R.string.accumulation_cta),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
