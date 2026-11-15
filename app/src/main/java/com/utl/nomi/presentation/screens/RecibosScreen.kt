package com.utl.nomi.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.utl.nomi.data.model.ConceptoRecibo
import com.utl.nomi.data.model.ReciboDetalleMovil
import com.utl.nomi.data.model.ReciboMovil
import com.utl.nomi.presentation.components.BottomBarComponent
import com.utl.nomi.presentation.components.ChipEstado
import com.utl.nomi.presentation.components.IconoRedondo
import com.utl.nomi.presentation.components.TarjetaNomi
import com.utl.nomi.presentation.components.Tono
import com.utl.nomi.presentation.components.TopBarComponent
import com.utl.nomi.presentation.components.estadoPago
import com.utl.nomi.presentation.components.fechaCorta
import com.utl.nomi.presentation.components.pesos
import com.utl.nomi.presentation.movil.EstadoDetalle
import com.utl.nomi.presentation.movil.EstadoRecibos
import com.utl.nomi.presentation.movil.RecibosViewModel

// "Mis recibos" del Figma: lista de recibos de nómina del empleado con sesión
// (GET api/movil/recibos) y su detalle (GET api/movil/recibos/{id}).
// Reemplaza a NominaScreen, que mostraba los recibos de todos los empleados.
@Composable
fun RecibosScreen(
    onNotificacionesClick: () -> Unit = {},
    onTabInicioClick: () -> Unit = {},
    onTabNominaClick: () -> Unit = {},
    onTabPerfilClick: () -> Unit = {},
    viewModel: RecibosViewModel = viewModel()
) {
    val lista by viewModel.lista.collectAsState()
    val detalle by viewModel.detalle.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarLista() }

    // Si hay un recibo abierto se muestra el detalle; "atrás" regresa a la lista
    detalle?.let { d ->
        BackHandler { viewModel.cerrarDetalle() }
        DetalleRecibo(estado = d, onVolver = viewModel::cerrarDetalle)
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

            Text("NÓMINA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Recibos de nómina", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(20.dp))

            when (val e = lista) {
                is EstadoRecibos.Cargando -> Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is EstadoRecibos.Error -> Aviso(
                    icono = { IconoRedondo(Icons.Outlined.ErrorOutline, Tono.PELIGRO, 56.dp) },
                    texto = e.mensaje,
                    boton = "Reintentar",
                    alPulsar = viewModel::cargarLista
                )

                is EstadoRecibos.Listo ->
                    if (e.recibos.isEmpty()) {
                        Aviso(
                            icono = { IconoRedondo(Icons.Outlined.ReceiptLong, Tono.NEUTRO, 56.dp) },
                            texto = "No hay recibos de nómina disponibles. Aparecerán aquí cuando RH calcule tu primer periodo."
                        )
                    } else {
                        e.recibos.forEach { recibo ->
                            FilaRecibo(recibo = recibo, alPulsar = { viewModel.abrirDetalle(recibo.nominaId) })
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomBarComponent(
                currentTab = "nomina",
                onTabInicioClick = onTabInicioClick,
                onTabNominaClick = onTabNominaClick,
                onTabPerfilClick = onTabPerfilClick
            )
        }
    }
}

// Una tarjeta de la lista: icono, estado, periodo, fecha de pago y neto
@Composable
private fun FilaRecibo(recibo: ReciboMovil, alPulsar: () -> Unit) {
    val (textoEstado, tono) = estadoPago(recibo.estadoPago)
    TarjetaNomi(modifier = Modifier.fillMaxWidth().clickable(onClick = alPulsar), relleno = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoRedondo(Icons.Outlined.AccountBalanceWallet, Tono.PRINCIPAL, 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                ChipEstado(textoEstado, tono)
                Spacer(modifier = Modifier.height(6.dp))
                Text(recibo.periodo, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Pago: ${fechaCorta(recibo.fechaPago)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(pesos(recibo.sueldoNeto), style = MaterialTheme.typography.titleLarge)
            }
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Ver detalle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Detalle del recibo, en pantalla completa
@Composable
private fun DetalleRecibo(estado: EstadoDetalle, onVolver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Regresar")
                }
                Text("Recibo de nómina", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        when (estado) {
            is EstadoDetalle.Cargando -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is EstadoDetalle.Error -> Aviso(
                icono = { IconoRedondo(Icons.Outlined.ErrorOutline, Tono.PELIGRO, 56.dp) },
                texto = estado.mensaje,
                boton = "Volver",
                alPulsar = onVolver
            )
            is EstadoDetalle.Listo -> ContenidoDetalle(estado.detalle)
        }
    }
}

@Composable
private fun ContenidoDetalle(detalle: ReciboDetalleMovil) {
    val r = detalle.recibo
    val (textoEstado, tono) = estadoPago(r.estadoPago)
    val percepciones = detalle.conceptos.filter { it.tipo.equals("PERCEPCION", ignoreCase = true) }
    val deducciones = detalle.conceptos.filter { it.tipo.equals("DEDUCCION", ignoreCase = true) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        ChipEstado(textoEstado, tono)
        Spacer(modifier = Modifier.height(8.dp))
        Text(r.periodo, style = MaterialTheme.typography.headlineSmall)
        Text(
            "Del ${fechaCorta(r.fechaInicio)} al ${fechaCorta(r.fechaFin)} · Pago: ${fechaCorta(r.fechaPago)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (r.diasPagados != null) {
            Text(
                "${r.diasPagados} días pagados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Neto a pagar, destacado como en el Figma
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Neto a pagar", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Text(pesos(r.sueldoNeto), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SeccionConceptos("Percepciones", percepciones, total = r.totalPercepciones, restar = false)
        Spacer(modifier = Modifier.height(12.dp))
        SeccionConceptos("Deducciones", deducciones, total = r.totalDeducciones, restar = true)
    }
}

// Tarjeta con una lista de conceptos y su total
@Composable
private fun SeccionConceptos(titulo: String, conceptos: List<ConceptoRecibo>, total: Double, restar: Boolean) {
    TarjetaNomi(modifier = Modifier.fillMaxWidth()) {
        Text(titulo, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        if (conceptos.isEmpty()) {
            Text("Sin conceptos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        conceptos.forEach { c ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(c.concepto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text((if (restar) "−" else "") + pesos(c.montoTotal), style = MaterialTheme.typography.bodyMedium)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", style = MaterialTheme.typography.titleSmall)
            Text((if (restar) "−" else "") + pesos(total), style = MaterialTheme.typography.titleSmall)
        }
    }
}

// Mensaje centrado con icono y, opcionalmente, un botón
@Composable
private fun Aviso(
    icono: @Composable () -> Unit,
    texto: String,
    boton: String? = null,
    alPulsar: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 12.dp, end = 12.dp),
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
