package com.utl.nomi.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utl.nomi.data.PerfilUsuario
import com.utl.nomi.data.SesionUsuario
import com.utl.nomi.presentation.components.BottomBarComponent

// Nombre del rol como se muestra al usuario
private fun etiquetaRol(rol: String): String = when (rol) {
    "ADMIN" -> "Administrador"
    "RRHH" -> "Recursos Humanos"
    "EMPLEADO" -> "Empleado"
    else -> rol
}

// "2024-03-15" -> "15/03/2024"
private fun formatoFecha(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "—"
    val partes = fecha.split("-")
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else fecha
}

// "Ana Martínez Ruiz" -> "AM"
private fun iniciales(nombre: String): String {
    val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
    if (partes.isEmpty()) return "?"
    val segunda = if (partes.size > 1) partes[1].first().toString() else ""
    return (partes[0].first().toString() + segunda).uppercase()
}

private fun valorOGuion(valor: String?): String = if (valor.isNullOrBlank()) "—" else valor

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
    onTabInicioClick: () -> Unit = {},
    onTabNominaClick: () -> Unit = {},
    onTabPerfilClick: () -> Unit = {}
) {
    val colorGrisBlanco = Color(0xFFF8FAFC)
    val colorGrisOscuro = Color(0xFF787878)
    val colorAzul = Color(0xFF1E3A8A)
    val colorRojo = Color(0xFFEF4444)

    // Al abrir la pestaña Perfil se piden los datos al servidor
    LaunchedEffect(Unit) {
        onCargarPerfil()
    }

    val nombre = perfil?.nombreCompleto ?: sesion.nombre
    // Rol como subtítulo; si hay perfil, también el departamento
    val departamento = perfil?.departamento
    val subtitulo = if (departamento != null)
        "${etiquetaRol(sesion.rol)} · $departamento"
    else
        etiquetaRol(sesion.rol)

    var mostrarDialogo by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorGrisBlanco)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PERFIL",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar con las iniciales del nombre
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(colorAzul),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales(nombre),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = nombre,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitulo,
                fontSize = 14.sp,
                color = colorGrisOscuro
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Estado de la carga: indicador, error con botón para reintentar
            if (cargando && perfil == null) {
                CircularProgressIndicator(
                    color = colorAzul,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (error != null) {
                Text(
                    text = error,
                    color = colorRojo,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = onCargarPerfil) {
                    Text("Reintentar", color = colorAzul, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (perfil != null) {
                if (perfil.empleadoId == null) {
                    Text(
                        text = "Esta cuenta no tiene un empleado asociado.",
                        fontSize = 12.sp,
                        color = colorGrisOscuro,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                TarjetaDatos(
                    titulo = "DATOS PERSONALES",
                    filas = listOf(
                        "Número de empleado" to (perfil.empleadoId?.toString() ?: "—"),
                        "Usuario" to perfil.nombreUsuario,
                        "RFC" to valorOGuion(perfil.rfc),
                        "CURP" to valorOGuion(perfil.curp),
                        "NSS" to valorOGuion(perfil.nss)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                TarjetaDatos(
                    titulo = "TRABAJO",
                    filas = listOf(
                        "Empresa" to valorOGuion(perfil.empresa),
                        "Departamento" to valorOGuion(perfil.departamento),
                        "Turno" to valorOGuion(perfil.turno),
                        "Sede" to valorOGuion(perfil.sede),
                        "Fecha de ingreso" to formatoFecha(perfil.fechaIngreso)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                TarjetaDatos(
                    titulo = "CONTACTO",
                    filas = listOf(
                        "Teléfono" to valorOGuion(perfil.telefono),
                        "Correo" to valorOGuion(perfil.correo)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { mostrarDialogo = true },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorAzul),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "CAMBIAR CONTRASEÑA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCerrarSesionClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorRojo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "CERRAR SESIÓN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
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

        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomBarComponent(
                currentTab = "perfil",
                onTabInicioClick = onTabInicioClick,
                onTabNominaClick = onTabNominaClick,
                onTabPerfilClick = onTabPerfilClick
            )
        }
    }
}

// Tarjeta con un título y una lista de "etiqueta: valor"
@Composable
private fun TarjetaDatos(titulo: String, filas: List<Pair<String, String>>) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))
            filas.forEachIndexed { indice, fila ->
                if (indice > 0) Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${fila.first}: ${fila.second}",
                    fontSize = 13.sp,
                    color = Color.Black
                )
            }
        }
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
        title = { Text("Cambiar contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = actual,
                    onValueChange = { actual = it },
                    label = { Text("Contraseña actual") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !cambiando
                )
                OutlinedTextField(
                    value = nueva,
                    onValueChange = { nueva = it },
                    label = { Text("Contraseña nueva") },
                    supportingText = { Text("Mínimo 6 caracteres") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !cambiando
                )
                OutlinedTextField(
                    value = confirmar,
                    onValueChange = { confirmar = it },
                    label = { Text("Confirmar contraseña nueva") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !cambiando
                )
                if (error != null) {
                    Text(text = error, color = Color(0xFFEF4444), fontSize = 13.sp)
                }
                Text(
                    text = "Al guardar se cerrará tu sesión y tendrás que iniciarla de nuevo.",
                    fontSize = 12.sp,
                    color = Color(0xFF787878)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmar(actual, nueva, confirmar) },
                enabled = !cambiando
            ) {
                Text(if (cambiando) "Guardando…" else "Guardar", color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !cambiando) {
                Text("Cancelar")
            }
        }
    )
}
