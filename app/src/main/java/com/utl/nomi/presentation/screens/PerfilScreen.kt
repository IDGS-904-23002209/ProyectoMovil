package com.utl.nomi.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utl.nomi.data.PerfilUsuario
import com.utl.nomi.data.SesionUsuario
import com.utl.nomi.presentation.components.BottomBarComponent
import com.utl.nomi.presentation.components.TarjetaNomi
import com.utl.nomi.presentation.components.TopBarComponent
import com.utl.nomi.presentation.components.fechaCorta
import com.utl.nomi.presentation.components.iniciales

// Nombre del rol como se muestra al usuario
private fun etiquetaRol(rol: String): String = when (rol) {
    "ADMIN" -> "Administrador"
    "RRHH" -> "Recursos Humanos"
    "EMPLEADO" -> "Empleado"
    else -> rol
}

private fun valorOGuion(valor: String?): String = if (valor.isNullOrBlank()) "—" else valor

// Perfil del Figma: avatar con iniciales, nombre, número de empleado y rol,
// tarjetas con los datos (etiqueta a la izquierda, valor a la derecha)
// y las acciones de cambiar contraseña y cerrar sesión.
// Los datos vienen de GET api/auth/perfil (AuthViewModel), igual que antes.
@Composable
fun PerfilScreen(
    sesion: SesionUsuario,
    perfil: PerfilUsuario? = null,
    cargando: Boolean = false,
    error: String? = null,
    onCargarPerfil: () -> Unit = {},
    cambiandoContrasena: Boolean = false,
    errorContrasena: String? = null,
    onCambiarContrasena: (String, String, String) -> Unit = { _, _, _ -> },
    onLimpiarErrorContrasena: () -> Unit = {},
    onCerrarSesionClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onTabInicioClick: () -> Unit = {},
    onTabNominaClick: () -> Unit = {},
    onTabPerfilClick: () -> Unit = {}
) {
    // Al abrir la pestaña Perfil se piden los datos al servidor
    LaunchedEffect(Unit) { onCargarPerfil() }

    val nombre = perfil?.nombreCompleto ?: sesion.nombre
    val subtitulo = listOfNotNull(
        perfil?.empleadoId?.let { "#$it" },
        etiquetaRol(sesion.rol)
    ).joinToString(" · ")

    var mostrarDialogo by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBarComponent(onNotificacionesClick = onNotificacionesClick)
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Avatar con las iniciales del nombre
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    iniciales(nombre),
                    fontSize = 26.sp,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(nombre, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            // Carga y error
            if (cargando && perfil == null) {
                CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(20.dp))
            }
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                TextButton(onClick = onCargarPerfil) { Text("Reintentar") }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (perfil != null) {
                if (perfil.empleadoId == null) {
                    Text(
                        "Esta cuenta no tiene un empleado asociado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                TarjetaDatos(
                    titulo = "Información laboral",
                    filas = listOf(
                        "Departamento" to valorOGuion(perfil.departamento),
                        "Turno" to valorOGuion(perfil.turno),
                        "Planta" to valorOGuion(perfil.sede),
                        "Empresa" to valorOGuion(perfil.empresa),
                        "Fecha de ingreso" to fechaCorta(perfil.fechaIngreso)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                TarjetaDatos(
                    titulo = "Datos personales",
                    filas = listOf(
                        "Usuario" to perfil.nombreUsuario,
                        "RFC" to valorOGuion(perfil.rfc),
                        "CURP" to valorOGuion(perfil.curp),
                        "NSS" to valorOGuion(perfil.nss)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                TarjetaDatos(
                    titulo = "Contacto",
                    filas = listOf(
                        "Teléfono" to valorOGuion(perfil.telefono),
                        "Correo" to valorOGuion(perfil.correo)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Acciones
            TarjetaNomi(modifier = Modifier.fillMaxWidth(), relleno = 0.dp) {
                FilaAccion(Icons.Outlined.Lock, "Cambiar contraseña", MaterialTheme.colorScheme.onSurface) {
                    mostrarDialogo = true
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                FilaAccion(Icons.AutoMirrored.Outlined.Logout, "Cerrar sesión", MaterialTheme.colorScheme.error, onCerrarSesionClick)
            }
        }

        if (mostrarDialogo) {
            DialogoContrasena(
                cambiando = cambiandoContrasena,
                error = errorContrasena,
                onConfirmar = onCambiarContrasena,
                onCerrar = {
                    onLimpiarErrorContrasena()
                    mostrarDialogo = false
                }
            )
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomBarComponent(
                currentTab = "perfil",
                onTabInicioClick = onTabInicioClick,
                onTabNominaClick = onTabNominaClick,
                onTabPerfilClick = onTabPerfilClick
            )
        }
    }
}

// Tarjeta con título y filas "etiqueta ........ valor", separadas por líneas
@Composable
private fun TarjetaDatos(titulo: String, filas: List<Pair<String, String>>) {
    TarjetaNomi(modifier = Modifier.fillMaxWidth()) {
        Text(titulo, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(6.dp))
        filas.forEach { (etiqueta, valor) ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    valor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Fila tocable con icono, texto y flecha
@Composable
private fun FilaAccion(icono: ImageVector, texto: String, color: Color, alPulsar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alPulsar)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodyLarge, color = color, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Ventana para cambiar la contraseña. Al lograrlo, el servidor cierra las sesiones
// y la app regresa al login.
@Composable
private fun DialogoContrasena(
    cambiando: Boolean,
    error: String?,
    onConfirmar: (String, String, String) -> Unit,
    onCerrar: () -> Unit
) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!cambiando) onCerrar() },
        title = { Text("Cambiar contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = actual,
                    onValueChange = { actual = it },
                    label = { Text("Contraseña actual") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !cambiando,
                    shape = MaterialTheme.shapes.small
                )
                OutlinedTextField(
                    value = nueva,
                    onValueChange = { nueva = it },
                    label = { Text("Contraseña nueva") },
                    supportingText = { Text("Mínimo 6 caracteres") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !cambiando,
                    shape = MaterialTheme.shapes.small
                )
                OutlinedTextField(
                    value = confirmar,
                    onValueChange = { confirmar = it },
                    label = { Text("Confirmar contraseña nueva") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !cambiando,
                    shape = MaterialTheme.shapes.small
                )
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Al guardar se cerrará tu sesión y tendrás que iniciarla de nuevo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(actual, nueva, confirmar) }, enabled = !cambiando) {
                Text(if (cambiando) "Guardando…" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !cambiando) { Text("Cancelar") }
        }
    )
}
