package com.utl.nomi.presentation.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.utl.nomi.data.model.RegistroAsistenciaMovil
import com.utl.nomi.presentation.components.ChipEstado
import com.utl.nomi.presentation.components.EscanerQr
import com.utl.nomi.presentation.components.IconoRedondo
import com.utl.nomi.presentation.components.TarjetaNomi
import com.utl.nomi.presentation.components.Tono
import com.utl.nomi.presentation.movil.EstadoRegistro
import com.utl.nomi.presentation.movil.RegistroAsistenciaViewModel
import com.utl.nomi.ui.theme.Exito
import com.utl.nomi.ui.theme.ExitoFondo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Flujo de registro de asistencia, como en el Figma:
// 1) escanear el QR de la planta, 2) la app toma la ubicación y el servidor
// valida todo, 3) pantalla de éxito con la hora, o el motivo del rechazo.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RegistroAsistenciaScreen(
    esSalida: Boolean,
    sede: String?,
    onVolver: () -> Unit,
    viewModel: RegistroAsistenciaViewModel = viewModel()
) {
    val contexto = LocalContext.current
    val estado by viewModel.estado.collectAsState()
    val permisos = rememberMultiplePermissionsState(
        listOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
    )

    // Al entrar: pantalla limpia y pedir permisos si faltan
    LaunchedEffect(Unit) {
        viewModel.reiniciar()
        if (!permisos.allPermissionsGranted) permisos.launchMultiplePermissionRequest()
    }

    // El botón "atrás" del teléfono regresa a Inicio (antes cerraba la app)
    BackHandler(onBack = onVolver)

    val titulo = if (esSalida) "Registrar salida" else "Registrar entrada"

    when (val e = estado) {
        is EstadoRegistro.Exito -> PantallaExito(registro = e.registro, sede = sede, onListo = onVolver)

        is EstadoRegistro.Rechazado -> PantallaRechazo(
            mensaje = e.mensaje,
            onReintentar = { viewModel.reiniciar() },
            onVolver = onVolver
        )

        else -> Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Encabezado(titulo = titulo, onVolver = onVolver)

            // Cámara a pantalla casi completa con el marco del Figma
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds().background(Color(0xFF142329)),
                contentAlignment = Alignment.Center
            ) {
                if (!permisos.allPermissionsGranted) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text(
                            "Para registrar tu asistencia necesitamos la cámara (para leer el QR) y tu ubicación (para confirmar que estás en la planta).",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { permisos.launchMultiplePermissionRequest() }, shape = MaterialTheme.shapes.small) {
                            Text("Dar permisos", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    if (e is EstadoRegistro.Escaneando) {
                        EscanerQr(modifier = Modifier.fillMaxSize()) { codigo ->
                            viewModel.registrar(contexto, codigo)
                        }
                    }
                    MarcoEscaneo()
                }

                // Capa de "verificando" encima de la cámara
                if (e is EstadoRegistro.Verificando) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(e.paso, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // Instrucciones abajo
            Column(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Apunta al código QR de la planta", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Coloca el código dentro del marco",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (sede != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sede, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// Barra blanca con flecha para regresar y el título al centro
@Composable
private fun Encabezado(titulo: String, onVolver: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Regresar")
            }
            Text(
                titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // equilibra el espacio de la flecha
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}

// Cuatro esquinas blancas que marcan dónde poner el QR
@Composable
private fun MarcoEscaneo() {
    val grosor = 4.dp
    val largo = 44.dp
    Box(modifier = Modifier.size(240.dp)) {
        listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd).forEach { esquina ->
            Box(modifier = Modifier.align(esquina).size(largo)) {
                val arriba = esquina == Alignment.TopStart || esquina == Alignment.TopEnd
                val izquierda = esquina == Alignment.TopStart || esquina == Alignment.BottomStart
                // Línea horizontal
                Box(
                    modifier = Modifier
                        .align(if (arriba) Alignment.TopCenter else Alignment.BottomCenter)
                        .fillMaxWidth().height(grosor)
                        .clip(RoundedCornerShape(2.dp)).background(Color.White)
                )
                // Línea vertical
                Box(
                    modifier = Modifier
                        .align(if (izquierda) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight().width(grosor)
                        .clip(RoundedCornerShape(2.dp)).background(Color.White)
                )
            }
        }
    }
}

// Pantalla de éxito del Figma: palomita verde, hora grande y estado
@Composable
private fun PantallaExito(registro: RegistroAsistenciaMovil, sede: String?, onListo: () -> Unit) {
    val esEntrada = registro.tipo == "ENTRADA"
    val fecha = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "MX")))
            .replaceFirstChar { it.uppercase() }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .border(8.dp, ExitoFondo.copy(alpha = 0.5f), CircleShape)
                .padding(8.dp)
                .clip(CircleShape)
                .background(ExitoFondo),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Check, contentDescription = null, tint = Exito, modifier = Modifier.size(42.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("REGISTRO EXITOSO", style = MaterialTheme.typography.labelSmall, color = Exito)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            if (esEntrada) "Entrada registrada" else "Salida registrada",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            registro.hora,
            fontSize = 44.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 6.dp)
        )
        when {
            !esEntrada -> ChipEstado("Jornada terminada", Tono.NEUTRO)
            registro.retardo -> ChipEstado("Retardo de ${registro.minutosRetardo} min", Tono.AVISO)
            else -> ChipEstado("Puntual", Tono.EXITO)
        }

        Spacer(modifier = Modifier.height(28.dp))
        TarjetaNomi(modifier = Modifier.fillMaxWidth(), relleno = 14.dp) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(fecha, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    listOfNotNull(sede, "a ${registro.distanciaMetros} m").joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onListo,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Listo", style = MaterialTheme.typography.labelLarge) }
    }
}

// Pantalla cuando el servidor rechaza: muestra su mensaje tal cual
// (QR de otra planta, fuera de rango, ya registrado, sin GPS...)
@Composable
private fun PantallaRechazo(mensaje: String, onReintentar: () -> Unit, onVolver: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconoRedondo(icono = Icons.Outlined.Close, tono = Tono.PELIGRO, tamano = 80.dp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("No se pudo registrar", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onReintentar,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Intentar de nuevo", style = MaterialTheme.typography.labelLarge) }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onVolver,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Volver al inicio", style = MaterialTheme.typography.labelLarge) }
    }
}
