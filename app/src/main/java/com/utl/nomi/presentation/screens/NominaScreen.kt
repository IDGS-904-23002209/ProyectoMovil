package com.utl.nomi.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utl.nomi.data.model.NominaDto
import com.utl.nomi.presentation.auth.nomina.NominaViewModel
import com.utl.nomi.presentation.components.BottomBarComponent
import com.utl.nomi.presentation.components.TopBarComponent

@Composable
fun NominaScreen(
    nominaViewModel: NominaViewModel = NominaViewModel(),
    onNotificacionesClick: () -> Unit = {},
    onTabInicioClick: () -> Unit = {},
    onTabNominaClick: () -> Unit = {},
    onTabPerfilClick: () -> Unit = {},
    onDescargarPdfClick: (NominaDto) -> Unit = {}
) {
    val colorGrisBlanco = Color(0xFFF8FAFC)
    val colorGrisOscuro = Color(0xFF787878)

    // Cargar los recibos reales del periodo al iniciar la pantalla
    LaunchedEffect(Unit) {
        nominaViewModel.cargarNominaPeriodo(periodoId = 1) // Ajusta el ID del periodo según tu lógica
    }

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

            TopBarComponent(onNotificacionesClick = onNotificacionesClick)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "RECIBOS DE NOMINA",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de filtro idéntica a tu maqueta
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_today),
                            contentDescription = "Calendario",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Filtrar por fecha",
                            fontSize = 14.sp,
                            color = colorGrisOscuro
                        )
                    }
                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                        contentDescription = "Desplegar",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Manejo de estados de carga y renderizado idéntico
            when {
                nominaViewModel.cargando -> {
                    CircularProgressIndicator(
                        color = Color(0xFF0F766E),
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
                nominaViewModel.error != null -> {
                    Text(
                        text = nominaViewModel.error!!,
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
                nominaViewModel.recibos.isEmpty() -> {
                    Text(
                        text = "No hay recibos de nómina disponibles.",
                        color = colorGrisOscuro,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
                else -> {
                    nominaViewModel.recibos.forEach { recibo ->
                        ItemReciboNominaReal(
                            recibo = recibo,
                            onDescargarClick = { onDescargarPdfClick(recibo) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomBarComponent(
                currentTab = "nomina",
                onTabInicioClick = onTabInicioClick,
                onTabNominaClick = onTabNominaClick,
                onTabPerfilClick = onTabPerfilClick
            )
        }
    }
}

@Composable
fun ItemReciboNominaReal(
    recibo: NominaDto,
    onDescargarClick: () -> Unit
) {
    // Mantiene el diseño exacto de tu Card original pero mostrando el sueldo neto y datos del DTO
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Periodo / Empleado #${recibo.empleadoId}",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%,.2f", recibo.sueldoNeto)} MXN",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            IconButton(onClick = onDescargarClick) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_save),
                    contentDescription = "Descargar PDF",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}