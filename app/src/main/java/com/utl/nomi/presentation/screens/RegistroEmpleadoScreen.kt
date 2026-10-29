package com.utl.nomi.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utl.nomi.presentation.auth.EmpleadoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroEmpleadoScreen(
    empleadoViewModel: EmpleadoViewModel,
    onCancelarClick: () -> Unit,
    onGuardarExitoso: () -> Unit
) {
    // Campos de texto para información personal y credenciales
    var nombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Variables para almacenar los IDs seleccionados en los Selects
    var rolIdSeleccionado by remember { mutableStateOf("3") } // Por defecto Empleado (3)
    var departamentoIdSeleccionado by remember { mutableStateOf("") }
    var turnoIdSeleccionado by remember { mutableStateOf("") }

    // Campos fiscales y laborales adicionales
    var rfc by remember { mutableStateOf("") }
    var curp by remember { mutableStateOf("") }
    var nss by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Nomi",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Text(
                text = "Agregar un nuevo empleado",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Sección de Datos Personales
            Text(text = "Información Personal", fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5), modifier = Modifier.align(Alignment.Start).padding(vertical = 8.dp))
            CampoFormulario(label = "Nombre(s):", value = nombre, enabled = !empleadoViewModel.registroCargando) { nombre = it }
            CampoFormulario(label = "Apellido Paterno:", value = apellidoPaterno, enabled = !empleadoViewModel.registroCargando) { apellidoPaterno = it }
            CampoFormulario(label = "Apellido Materno:", value = apellidoMaterno, enabled = !empleadoViewModel.registroCargando) { apellidoMaterno = it }
            CampoFormulario(label = "Correo:", value = correo, enabled = !empleadoViewModel.registroCargando) { correo = it }
            CampoFormulario(label = "Teléfono:", value = telefono, enabled = !empleadoViewModel.registroCargando) { telefono = it }

            // Sección de Credenciales de Usuario y Rol
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Datos de Usuario", fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5), modifier = Modifier.align(Alignment.Start).padding(vertical = 8.dp))
            CampoFormulario(label = "Nombre de Usuario:", value = nombreUsuario, enabled = !empleadoViewModel.registroCargando) { nombreUsuario = it }
            CampoFormulario(label = "Contraseña:", value = contrasena, isPassword = true, enabled = !empleadoViewModel.registroCargando) { contrasena = it }

            // Select desplegable para Rol
            DropdownSelectField(
                label = "Rol:",
                items = empleadoViewModel.listaRoles.map { it.id.toString() to it.nombre },
                selectedId = rolIdSeleccionado,
                enabled = !empleadoViewModel.registroCargando
            ) { nuevoId ->
                rolIdSeleccionado = nuevoId
            }

            // Sección de Datos Laborales / Organización
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Datos Laborales y Fiscales", fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5), modifier = Modifier.align(Alignment.Start).padding(vertical = 8.dp))

            // Select desplegable para Departamento (Cargado desde la API)
            DropdownSelectField(
                label = "Departamento:",
                items = empleadoViewModel.listaDepartamentos.map { it.id.toString() to it.nombre },
                selectedId = departamentoIdSeleccionado,
                enabled = !empleadoViewModel.registroCargando && !empleadoViewModel.cargandoCatalogos
            ) { nuevoId ->
                departamentoIdSeleccionado = nuevoId
            }

            // Select desplegable para Turno (Cargado desde la API)
            DropdownSelectField(
                label = "Turno:",
                items = empleadoViewModel.listaTurnos.map { it.id.toString() to it.nombre },
                selectedId = turnoIdSeleccionado,
                enabled = !empleadoViewModel.registroCargando && !empleadoViewModel.cargandoCatalogos
            ) { nuevoId ->
                turnoIdSeleccionado = nuevoId
            }

            CampoFormulario(label = "RFC:", value = rfc, enabled = !empleadoViewModel.registroCargando) { rfc = it }
            CampoFormulario(label = "CURP:", value = curp, enabled = !empleadoViewModel.registroCargando) { curp = it }
            CampoFormulario(label = "NSS:", value = nss, enabled = !empleadoViewModel.registroCargando) { nss = it }

            // Mensaje de error si la API devuelve fallo
            if (empleadoViewModel.registroError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = empleadoViewModel.registroError!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCancelarClick,
                    enabled = !empleadoViewModel.registroCargando,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA8BEB)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = "Cancelar", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        val datosEmpleado = mapOf(
                            "nombre" to nombre,
                            "apellido_paterno" to apellidoPaterno,
                            "apellido_materno" to apellidoMaterno,
                            "correo" to correo,
                            "telefono" to telefono,
                            "nombre_usuario" to nombreUsuario,
                            "contrasena" to contrasena,
                            "rol_id" to rolIdSeleccionado,
                            "empresa_id" to "1", // Fijo por requerimiento
                            "departamento_id" to departamentoIdSeleccionado,
                            "turno_id" to turnoIdSeleccionado,
                            "sede_id" to "1",     // Fijo por requerimiento
                            "rfc" to rfc,
                            "curp" to curp,
                            "nss" to nss
                        )
                        empleadoViewModel.registrarEmpleado(datosEmpleado) {
                            onGuardarExitoso()
                        }
                    },
                    enabled = !empleadoViewModel.registroCargando,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (empleadoViewModel.registroCargando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Guardar", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CampoFormulario(
    label: String,
    value: String,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            textStyle = TextStyle(color = Color.Black),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF7F5F5),
                focusedContainerColor = Color(0xFFF7F5F5),
                focusedBorderColor = Color(0xFF3F51B5),
                unfocusedBorderColor = Color.LightGray,
                disabledContainerColor = Color(0xFFEFEFEF)
            )
        )
    }
}

/**
 * Componente reutilizable para los menús desplegables (Selects) que muestran el nombre
 * pero seleccionan y guardan el ID numérico por detrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelectField(
    label: String,
    items: List<Pair<String, String>>, // Lista de Pares: (ID, Nombre)
    selectedId: String,
    enabled: Boolean = true,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Encontramos el nombre correspondiente al ID seleccionado para mostrarlo en caja
    val selectedText = items.firstOrNull { it.first == selectedId }?.second ?: "Seleccione una opción"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                enabled = enabled,
                textStyle = TextStyle(color = Color.Black),
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF7F5F5),
                    focusedContainerColor = Color(0xFFF7F5F5),
                    focusedBorderColor = Color(0xFF3F51B5),
                    unfocusedBorderColor = Color.LightGray,
                    disabledContainerColor = Color(0xFFEFEFEF)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (items.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay elementos disponibles") },
                        onClick = { expanded = false }
                    )
                } else {
                    items.forEach { (id, nombre) ->
                        DropdownMenuItem(
                            text = { Text(text = nombre) },
                            onClick = {
                                onItemSelected(id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}