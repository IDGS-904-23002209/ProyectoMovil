package com.utl.nomi.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.utl.nomi.data.model.NotificacionMovil
import com.utl.nomi.presentation.components.IconoRedondo
import com.utl.nomi.presentation.components.TarjetaNomi
import com.utl.nomi.presentation.components.Tono
import com.utl.nomi.presentation.components.fechaCorta
import com.utl.nomi.presentation.movil.EstadoNotificaciones
import com.utl.nomi.presentation.movil.NotificacionesViewModel
import java.time.LocalDate

// Notificaciones del usuario con sesión (GET api/movil/notificaciones):
// respuestas de RH a sus solicitudes y avisos de recibos disponibles.
// Se agrupan en "Hoy", "Ayer" y "Anteriores". Al tocar una, se marca
// como leída y se abre la pantalla relacionada (Solicitudes o Recibos).
@Composable
fun NotificacionesScreen(
    onVolver: () -> Unit = {},
    onAbrirSolicitudes: () -> Unit = {},
    onAbrirRecibos: () -> Unit = {},
    viewModel: NotificacionesViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargar() }
    BackHandler(onBack = onVolver)

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Regresar")
                }
                Text("Notificaciones", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        when (val e = estado) {
            is EstadoNotificaciones.Cargando -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            is EstadoNotificaciones.Error -> AvisoNotificaciones(
                icono = { IconoRedondo(Icons.Outlined.ErrorOutline, Tono.PELIGRO, 56.dp) },
                texto = e.mensaje,
                boton = "Reintentar",
                alPulsar = viewModel::cargar
            )

            is EstadoNotificaciones.Listo ->
                if (e.notificaciones.isEmpty()) {
                    AvisoNotificaciones(
                        icono = { IconoRedondo(Icons.Outlined.NotificationsNone, Tono.NEUTRO, 56.dp) },
                        texto = "No tienes notificaciones. Aquí te avisaremos cuando RH responda tus solicitudes o tengas un recibo nuevo."
                    )
                } else {
                    ListaNotificaciones(
                        notificaciones = e.notificaciones,
                        onMarcarTodas = viewModel::marcarTodas,
                        onTocar = { n ->
                            viewModel.marcarLeida(n)
                            when (n.tipo?.uppercase()) {
                                "SOLICITUD" -> onAbrirSolicitudes()
                                "NOMINA" -> onAbrirRecibos()
                            }
                        }
                    )
                }
        }
    }
}

@Composable
private fun ListaNotificaciones(
    notificaciones: List<NotificacionMovil>,
    onMarcarTodas: () -> Unit,
    onTocar: (NotificacionMovil) -> Unit
) {
    val hoy = LocalDate.now()
    val grupos = notificaciones.groupBy { n ->
        val fecha = n.fechaCreacion?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        when (fecha) {
            hoy -> "Hoy"
            hoy.minusDays(1) -> "Ayer"
            else -> "Anteriores"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        if (notificaciones.any { !it.leida }) {
            TextButton(onClick = onMarcarTodas, modifier = Modifier.align(Alignment.End)) {
                Text("Marcar todas como leídas")
            }
        }

        listOf("Hoy", "Ayer", "Anteriores").forEach { titulo ->
            val lista = grupos[titulo].orEmpty()
            if (lista.isNotEmpty()) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp, bottom = 10.dp))
                lista.forEach { n ->
                    ItemNotificacion(n, alPulsar = { onTocar(n) })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

// Icono y tono según el tipo y el título del aviso
private fun iconoDe(n: NotificacionMovil) = when {
    n.tipo.equals("NOMINA", true) -> Icons.Outlined.ReceiptLong to Tono.PRINCIPAL
    n.titulo.contains("aprobada", true) -> Icons.Outlined.CheckCircle to Tono.EXITO
    n.titulo.contains("rechazada", true) -> Icons.Outlined.HighlightOff to Tono.PELIGRO
    else -> Icons.Outlined.Notifications to Tono.INFO
}

@Composable
private fun ItemNotificacion(n: NotificacionMovil, alPulsar: () -> Unit) {
    val (icono, tono) = iconoDe(n)
    TarjetaNomi(modifier = Modifier.fillMaxWidth().clickable(onClick = alPulsar), relleno = 14.dp) {
        Row(verticalAlignment = Alignment.Top) {
            IconoRedondo(icono, tono, 40.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(n.titulo, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text(n.mensaje, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    cuando(n.fechaCreacion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Punto verde = sin leer
            if (!n.leida) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// "2026-11-15T18:23:00" -> "15/11/2026 · 18:23"
private fun cuando(fecha: String?): String {
    if (fecha.isNullOrBlank()) return ""
    val hora = if (fecha.length >= 16) fecha.substring(11, 16) else ""
    return if (hora.isBlank()) fechaCorta(fecha) else "${fechaCorta(fecha)} · $hora"
}

@Composable
private fun AvisoNotificaciones(
    icono: @Composable () -> Unit,
    texto: String,
    boton: String? = null,
    alPulsar: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icono()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (boton != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = alPulsar, shape = MaterialTheme.shapes.small) {
                Text(boton, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
