package com.utl.nomi.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.utl.nomi.data.model.SolicitudMovil
import com.utl.nomi.presentation.components.BottomBarComponent
import com.utl.nomi.presentation.components.ChipEstado
import com.utl.nomi.presentation.components.IconoRedondo
import com.utl.nomi.presentation.components.TarjetaNomi
import com.utl.nomi.presentation.components.Tono
import com.utl.nomi.presentation.components.TopBarComponent
import com.utl.nomi.presentation.components.fechaCorta
import com.utl.nomi.presentation.movil.EstadoSolicitudes
import com.utl.nomi.presentation.movil.FormularioSolicitud
import com.utl.nomi.presentation.movil.SolicitudesViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// "Mis solicitudes": vacaciones y permisos del empleado con sesión
// (GET api/movil/solicitudes) y formulario para pedir uno nuevo
// (POST api/movil/solicitudes). RH las aprueba o rechaza en la web.
@Composable
fun SolicitudesScreen(
    onNotificacionesClick: () -> Unit = {},
    onTabInicioClick: () -> Unit = {},
    onTabNominaClick: () -> Unit = {},
    onTabSolicitudesClick: () -> Unit = {},
    onTabPerfilClick: () -> Unit = {},
    viewModel: SolicitudesViewModel = viewModel()
) {
    val lista by viewModel.lista.collectAsState()
    val formulario by viewModel.formulario.collectAsState()
    val aviso by viewModel.aviso.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargarLista() }

    // Mensaje de confirmación después de enviar
    LaunchedEffect(aviso) {
        aviso?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarAviso()
        }
    }

    // Formulario abierto: pantalla completa; "atrás" lo cierra
    formulario?.let { f ->
        BackHandler { viewModel.cerrarFormulario() }
        FormularioNuevaSolicitud(f, viewModel)
        return
    }

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

            Text("SOLICITUDES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Mis solicitudes", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::abrirFormulario,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva solicitud", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(20.dp))

            when (val e = lista) {
                is EstadoSolicitudes.Cargando -> Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is EstadoSolicitudes.Error -> AvisoSolicitudes(
                    icono = { IconoRedondo(Icons.Outlined.ErrorOutline, Tono.PELIGRO, 56.dp) },
                    texto = e.mensaje,
                    boton = "Reintentar",
                    alPulsar = viewModel::cargarLista
                )

                is EstadoSolicitudes.Listo ->
                    if (e.solicitudes.isEmpty()) {
                        AvisoSolicitudes(
                            icono = { IconoRedondo(Icons.Outlined.EventBusy, Tono.NEUTRO, 56.dp) },
                            texto = "Aún no tienes solicitudes. Pide vacaciones o un permiso con el botón de arriba."
                        )
                    } else {
                        e.solicitudes.forEach { s ->
                            FilaSolicitud(s)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp)
        )

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomBarComponent(
                currentTab = "solicitudes",
                onTabInicioClick = onTabInicioClick,
                onTabNominaClick = onTabNominaClick,
                onTabSolicitudesClick = onTabSolicitudesClick,
                onTabPerfilClick = onTabPerfilClick
            )
        }
    }
}

// Estado de la solicitud como texto y tono del chip
private fun estadoSolicitud(estado: String): Pair<String, Tono> = when (estado.uppercase()) {
    "APROBADA" -> "Aprobada" to Tono.EXITO
    "RECHAZADA" -> "Rechazada" to Tono.PELIGRO
    "CANCELADA" -> "Cancelada" to Tono.NEUTRO
    else -> "Pendiente" to Tono.AVISO
}

// Una tarjeta de la lista: tipo, estado, fechas, días y motivo
@Composable
private fun FilaSolicitud(s: SolicitudMovil) {
    val (textoEstado, tono) = estadoSolicitud(s.estado)
    TarjetaNomi(modifier = Modifier.fillMaxWidth(), relleno = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoRedondo(Icons.Outlined.EditCalendar, Tono.PRINCIPAL, 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(s.tipo, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Solicitada el ${fechaCorta(s.fechaSolicitud)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ChipEstado(textoEstado, tono)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            if (s.dias == 1) "${fechaCorta(s.fechaInicio)} · 1 día"
            else "Del ${fechaCorta(s.fechaInicio)} al ${fechaCorta(s.fechaFin)} · ${s.dias} días",
            style = MaterialTheme.typography.bodyMedium
        )
        if (!s.motivo.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(s.motivo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Formulario de nueva solicitud, en pantalla completa
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioNuevaSolicitud(f: FormularioSolicitud, viewModel: SolicitudesViewModel) {
    // Qué calendario está abierto: "inicio", "fin" o ninguno
    var calendario by remember { mutableStateOf<String?>(null) }
    var menuTipos by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::cerrarFormulario) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Regresar")
                }
                Text("Nueva solicitud", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        if (f.cargandoTipos) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            // Tipo de solicitud
            Text("Tipo de solicitud", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = menuTipos, onExpandedChange = { menuTipos = it }) {
                OutlinedTextField(
                    value = f.tipo?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Elige una opción") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuTipos) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = menuTipos, onDismissRequest = { menuTipos = false }) {
                    f.tipos.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.nombre) },
                            onClick = {
                                viewModel.elegirTipo(tipo)
                                menuTipos = false
                            }
                        )
                    }
                }
            }
            f.tipo?.let { t ->
                Spacer(modifier = Modifier.height(6.dp))
                val partes = listOfNotNull(
                    if (t.conGoceSueldo) "Con goce de sueldo" else "Sin goce de sueldo",
                    t.diasMaximos?.let { "máximo $it días" }
                )
                Text(
                    partes.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Fechas
            Spacer(modifier = Modifier.height(20.dp))
            Text("Fechas", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoFecha("Desde", f.fechaInicio, Modifier.weight(1f)) { calendario = "inicio" }
                CampoFecha("Hasta", f.fechaFin, Modifier.weight(1f)) { calendario = "fin" }
            }
            f.dias?.let { d ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    if (d == 1) "1 día" else "$d días",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Motivo
            Spacer(modifier = Modifier.height(20.dp))
            Text("Motivo", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = f.motivo,
                onValueChange = viewModel::escribirMotivo,
                placeholder = { Text("Ej. Viaje familiar") },
                minLines = 3,
                supportingText = { Text("${f.motivo.length}/255") },
                modifier = Modifier.fillMaxWidth()
            )

            f.error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = viewModel::enviar,
                enabled = !f.enviando,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.small
            ) {
                if (f.enviando) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Enviar solicitud", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Recursos Humanos la revisará y te llegará una notificación con la respuesta.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Calendario (no deja elegir días que ya pasaron)
    calendario?.let { cual ->
        val hoy = LocalDate.now()
        val minimo = if (cual == "fin") f.fechaInicio ?: hoy else hoy
        val inicial = (if (cual == "inicio") f.fechaInicio else f.fechaFin) ?: minimo
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = inicial.aMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = !utcTimeMillis.aFecha().isBefore(minimo)
                override fun isSelectableYear(year: Int) = year >= minimo.year
            }
        )
        DatePickerDialog(
            onDismissRequest = { calendario = null },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { ms ->
                        if (cual == "inicio") viewModel.elegirInicio(ms.aFecha()) else viewModel.elegirFin(ms.aFecha())
                    }
                    calendario = null
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { calendario = null }) { Text("Cancelar") } }
        ) {
            DatePicker(state = estado)
        }
    }
}

// Campo que muestra una fecha y abre el calendario al tocarlo
@Composable
private fun CampoFecha(etiqueta: String, fecha: LocalDate?, modifier: Modifier, alPulsar: () -> Unit) {
    TarjetaNomi(modifier = modifier.clickable(onClick = alPulsar), relleno = 12.dp) {
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                fecha?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Elegir",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// El calendario de Material trabaja en milisegundos UTC
private fun LocalDate.aMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.aFecha(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

// Mensaje centrado con icono y, opcionalmente, un botón
@Composable
private fun AvisoSolicitudes(
    icono: @Composable () -> Unit,
    texto: String,
    boton: String? = null,
    alPulsar: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 12.dp, end = 12.dp),
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
