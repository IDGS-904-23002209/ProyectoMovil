package com.utl.nomi.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.utl.nomi.presentation.components.LogoNomi

// Login del móvil: logo de Nomi, bienvenida y lema del equipo, dos campos con
// etiqueta arriba, botón principal, ayuda para recuperar la contraseña y versión.
// Sin sesión no hay ningún otro botón (el alta de empleados se hace en la web).
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    cargando: Boolean = false,
    error: String? = null,
    onForgotPasswordClick: () -> Unit = {}
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }
    var mostrarAyuda by remember { mutableStateOf(false) }

    val puedeEntrar = !cargando && usuario.isNotBlank() && contrasena.isNotBlank()
    val entrar = { if (puedeEntrar) onLoginClick(usuario.trim(), contrasena) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        LogoNomi(alto = 110.dp)

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Bienvenido a Nomi",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "TU SISTEMA DE ASISTENCIA Y NÓMINA",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        CampoLogin(
            etiqueta = "Número de empleado o correo",
            valor = usuario,
            alCambiar = { usuario = it },
            icono = { Icon(Icons.Outlined.Person, contentDescription = null) },
            habilitado = !cargando,
            opciones = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        CampoLogin(
            etiqueta = "Contraseña",
            valor = contrasena,
            alCambiar = { contrasena = it },
            icono = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            habilitado = !cargando,
            opciones = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            acciones = KeyboardActions(onDone = { entrar() }),
            ocultar = !verContrasena,
            final = {
                IconButton(onClick = { verContrasena = !verContrasena }) {
                    Icon(
                        if (verContrasena) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (verContrasena) "Ocultar contraseña" else "Ver contraseña"
                    )
                }
            }
        )

        // Error del servidor (usuario o contraseña incorrectos, cuenta inactiva...)
        if (error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = entrar,
            enabled = puedeEntrar,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text("Iniciar sesión", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = {
            mostrarAyuda = true
            onForgotPasswordClick()
        }) {
            Text("¿Olvidaste tu contraseña? Contacta a RH", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Pie de página (versión), como estaba antes
        Text("Versión 1.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("NOMI 2026", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (mostrarAyuda) {
        AlertDialog(
            onDismissRequest = { mostrarAyuda = false },
            title = { Text("Recuperar contraseña") },
            text = { Text("Para restablecer tu contraseña, contacta a Recursos Humanos.") },
            confirmButton = {
                TextButton(onClick = { mostrarAyuda = false }) { Text("Entendido") }
            }
        )
    }
}

// Campo con la etiqueta arriba, como en el Figma
@Composable
private fun CampoLogin(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    icono: @Composable () -> Unit,
    habilitado: Boolean,
    opciones: KeyboardOptions,
    acciones: KeyboardActions = KeyboardActions.Default,
    ocultar: Boolean = false,
    final: (@Composable () -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            leadingIcon = icono,
            trailingIcon = final,
            singleLine = true,
            enabled = habilitado,
            keyboardOptions = opciones,
            keyboardActions = acciones,
            visualTransformation = if (ocultar) PasswordVisualTransformation() else VisualTransformation.None,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
