package com.watersf.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.watersf.app.domain.model.User
import com.watersf.app.presentation.theme.AppIcons
import com.watersf.app.presentation.theme.BackgroundBase
import com.watersf.app.presentation.theme.Dimens
import com.watersf.app.presentation.theme.OnPrimary
import com.watersf.app.presentation.theme.Outline
import com.watersf.app.presentation.theme.Primary
import com.watersf.app.presentation.theme.PrimaryBright
import com.watersf.app.presentation.theme.Surface
import com.watersf.app.presentation.theme.SurfaceVariant
import com.watersf.app.presentation.theme.TextMuted
import com.watersf.app.presentation.theme.TextPrimary
import com.watersf.app.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.loginState.collectAsState()
    val focusManager = LocalFocusManager.current

    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Redirección en caso de éxito
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess((state as LoginState.Success).user)
            viewModel.resetState()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBase),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.space6)
        ) {
            // Header del App (logo con gradiente de marca)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Dimens.logoSize)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PrimaryBright, Primary)
                        )
                    )
                    .border(Dimens.borderThick, SurfaceVariant, CircleShape)
                    .padding(Dimens.space5)
            ) {
                Icon(
                    imageVector = AppIcons.brand,
                    contentDescription = "Water-SF Icon",
                    tint = OnPrimary,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(Dimens.space4))

            Text(
                text = "Water-SF",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            Text(
                text = "Sistema de Gestión y Alertas",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = Dimens.space1, bottom = Dimens.space7)
            )

            // Contenedor de formulario
            Card(
                shape = RoundedCornerShape(Dimens.radiusLg),
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(Dimens.borderThin, Outline, RoundedCornerShape(Dimens.radiusLg))
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.space6),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(Dimens.space5))

                    // Campo de Usuario/Email
                    OutlinedTextField(
                        value = usernameOrEmail,
                        onValueChange = { usernameOrEmail = it },
                        label = { Text("Usuario o Correo") },
                        leadingIcon = {
                            Icon(AppIcons.email, contentDescription = null, tint = TextMuted)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = Surface,
                            focusedBorderColor = PrimaryBright,
                            unfocusedBorderColor = Outline,
                            focusedLabelColor = PrimaryBright,
                            unfocusedLabelColor = TextMuted
                        ),
                        shape = RoundedCornerShape(Dimens.radiusSm),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Dimens.space4))

                    // Campo de Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(AppIcons.password, contentDescription = null, tint = TextMuted)
                        },
                        trailingIcon = {
                            val image = if (passwordVisible) AppIcons.passwordShow else AppIcons.passwordHide
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, contentDescription = null, tint = TextMuted)
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login(usernameOrEmail, password)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = Surface,
                            focusedBorderColor = PrimaryBright,
                            unfocusedBorderColor = Outline,
                            focusedLabelColor = PrimaryBright,
                            unfocusedLabelColor = TextMuted
                        ),
                        shape = RoundedCornerShape(Dimens.radiusSm),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Dimens.space6))

                    // Botón de ingresar / Carga
                    if (state is LoginState.Loading) {
                        CircularProgressIndicator(
                            color = PrimaryBright,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.login(usernameOrEmail, password)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = OnPrimary
                            ),
                            shape = RoundedCornerShape(Dimens.radiusSm),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "INGRESAR",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    // Mensaje de Error
                    if (state is LoginState.Error) {
                        Spacer(modifier = Modifier.height(Dimens.space4))
                        Text(
                            text = (state as LoginState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
