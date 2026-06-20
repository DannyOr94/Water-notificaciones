package com.watersf.app.presentation.notification.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.watersf.app.presentation.theme.AppIcons
import com.watersf.app.presentation.theme.Dimens
import com.watersf.app.presentation.theme.SurfaceOffline
import com.watersf.app.presentation.theme.TextPrimary

@Composable
fun OfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(SurfaceOffline)
                .padding(vertical = 10.dp, horizontal = Dimens.space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = AppIcons.offline,
                contentDescription = "Sin conexión a internet",
                tint = TextPrimary,
                modifier = Modifier.size(Dimens.iconSm)
            )
            Spacer(modifier = Modifier.width(Dimens.space2))
            Text(
                text = "Modo sin conexión — Mostrando caché local",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
    }
}
