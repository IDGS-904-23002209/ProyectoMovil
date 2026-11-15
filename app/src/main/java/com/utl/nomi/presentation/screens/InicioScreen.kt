package com.utl.nomi.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.utl.nomi.data.model.InicioMovil
import com.utl.nomi.presentation.components.BottomBarComponent
import com.utl.nomi.presentation.components.ChipEstado
import com.utl.nomi.presentation.components.IconoRedondo
import com.utl.nomi.presentation.components.TarjetaNomi
import com.utl.nomi.presentation.components.Tono
import com.utl.nomi.presentation.components.TopBarComponent
import com.utl.nomi.presentation.movil.EstadoInicio
import com.utl.nomi.presentation.movil.InicioViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Inicio del empleado, como en el Figma: saludo, turno de hoy,
// botón grande para registrar entrada o salida y resumen de la semana.
// Todo viene de GET api/movil/inicio.
@Composable
fun InicioScreen(
    // esSalida = true si ya registró entrada hoy (el siguiente registro será la salida)
    onRegistrarClick: (esSalida: Boolean, sede: String?) -> Unit = { _, _ -> },
    onSesionExpirada: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onTabInicioClick: () -> Unit = {},
    onTabNominaClick: () -> Unit = {},
    onTabPerfilClick: () -> Unit = {},
    viewModel: InicioViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    // Cada vez que se entra a Inicio se vuelve a consultar el registro del día
    LaunchedEffect(Unit) { viewModel.cargar() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 100.dp)
        ) {
            TopBarComponent(onNotificacionesClick = onNotificacionesClick)
            Spacer(modifier = Modifier.height(20.dp))

            when (val e = estado) {
                is EstadoInicio.Cargando -> Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is EstadoInicio.Error -> MensajeError(
                    mensaje = e.mensaje,
                    textoBoton = if (e.sesionExpirada) "Iniciar sesión" else "Reintentar",
                    alPulsar = if (e.sesionExpirada) onSesionExpirada else viewModel::cargar
                )

                is EstadoInicio.Listo -> ContenidoInicio(
                    datos = e.datos,
                    onRegistrarClick = { onRegistrarClick(e.datos.hoyEntrada != null, e.datos.sede) }
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomBarComponent(
                currentTab = "inicio",
                onTabInicioClick = onTabInicioClick,
                onTabNominaClick = onTabNominaClick,
                onTabPerfilClick = onTabPerfilClick
            )
        }
    }
}

@Composable
private fun ContenidoInicio(datos: InicioMovil, onRegistrarClick: () -> Unit) {
    val ahora = remember { LocalDateTime.now() }
    val fecha = remember {
        ahora.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "MX"))).uppercase()
    }
    val saludo = when (ahora.hour) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    val primerNombre = datos.nombre.trim().substringBefore(" ")

    // Saludo
    Text(fecha, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(6.dp))
    Text("$saludo, $primerNombre", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)

    Spacer(modifier = Modifier.height(20.dp))

    // Turno de hoy
    val completo = datos.hoyEntrada != null && datos.hoySalida != null
    val enTurno = datos.hoyEntrada != null && datos.hoySalida == null
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconoRedondo(icono = Icons.Outlined.Schedule, tono = Tono.PRINCIPAL)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("TURNO ACTUAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (datos.horaEntradaTurno != null) "${datos.horaEntradaTurno} – ${datos.horaSalidaTurno ?: "--:--"}" else "Sin turno asignado",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                if (datos.sede != null) "Ubicación: ${datos.sede}" + (datos.turno?.let { " · $it" } ?: "")
                else "Sin planta asignada",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        when {
            completo -> ChipEstado("Completo", Tono.NEUTRO)
            enTurno -> ChipEstado("En turno", Tono.EXITO)
            else -> ChipEstado("No iniciado", Tono.INFO)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Tarjeta principal con el botón circular
    TarjetaNomi(modifier = Modifier.fillMaxWidth(), relleno = 24.dp) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            BotonRegistro(completo = completo, alPulsar = onRegistrarClick)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                when {
                    completo -> "Jornada completa"
                    enTurno -> "Registrar salida"
                    else -> "Registrar entrada"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                when {
                    completo -> "Entrada ${datos.hoyEntrada} · Salida ${datos.hoySalida}"
                    enTurno -> "Entrada a las ${datos.hoyEntrada}"
                    else -> "Aún sin registro hoy"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (datos.hoyEntrada != null) {
                Spacer(modifier = Modifier.height(10.dp))
                if (datos.hoyRetardo) ChipEstado("Retardo de ${datos.hoyMinutosRetardo} min", Tono.AVISO)
                else ChipEstado("Puntual", Tono.EXITO)
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Escanea el QR de la planta para registrar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Resumen de la semana
    Text("Resumen de asistencia semanal", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
    Spacer(modifier = Modifier.height(10.dp))
    TarjetaNomi(modifier = Modifier.fillMaxWidth(), relleno = 0.dp) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 16.dp)) {
            DatoSemana(formatearHoras(datos.semanaMinutos), "Horas trabajadas", Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outline)
            DatoSemana(datos.semanaRetardos.toString(), if (datos.semanaRetardos == 1) "Retardo" else "Retardos", Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outline)
            DatoSemana(datos.semanaDias.toString(), if (datos.semanaDias == 1) "Día asistido" else "Días asistidos", Modifier.weight(1f))
        }
    }
}

// Botón circular grande del Figma: verde con aro claro y sombra.
// Si la jornada ya está completa se muestra deshabilitado con una palomita.
@Composable
private fun BotonRegistro(completo: Boolean, alPulsar: () -> Unit) {
    val color = if (completo) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
    Surface(
        onClick = alPulsar,
        enabled = !completo,
        shape = CircleShape,
        color = color,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        border = BorderStroke(8.dp, MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .size(112.dp)
            .shadow(if (completo) 0.dp else 10.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (completo) Icons.Outlined.CheckCircle else Icons.Outlined.QrCodeScanner,
                contentDescription = if (completo) "Jornada completa" else "Registrar asistencia",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun DatoSemana(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Mensaje de error con botón, centrado
@Composable
private fun MensajeError(mensaje: String, textoBoton: String, alPulsar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconoRedondo(icono = Icons.Outlined.ErrorOutline, tono = Tono.PELIGRO, tamano = 56.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = alPulsar, shape = MaterialTheme.shapes.small) {
            Text(textoBoton, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// 1250 minutos -> "20.8 h"; 0 -> "0 h"
private fun formatearHoras(minutos: Int): String {
    if (minutos <= 0) return "0 h"
    val horas = minutos / 60.0
    return if (horas < 10) String.format(Locale.US, "%.1f h", horas) else "${horas.toInt()} h"
}
