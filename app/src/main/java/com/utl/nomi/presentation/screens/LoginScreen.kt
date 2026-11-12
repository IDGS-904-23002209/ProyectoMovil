package com.utl.nomi.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utl.nomi.R

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    cargando: Boolean = false,
    error: String? = null,
    onForgotPasswordClick: () -> Unit = {}
) {
    var numeroEmpleado by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var mostrarAyudaContrasena by remember { mutableStateOf(false) }

    // Colores basados en tu paleta
    val colorAzul = Color(0xFF1E3A8A)
    val colorVerde = Color(0xFF10B981)
    val colorGrisBlanco = Color(0xFFF8FAFC)
    val colorGrisOscuro = Color(0xFF787878)
    val colorRojo = Color(0xFFEF4444)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorGrisBlanco)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Contenedor superior (Logo y Títulos)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Nomi",
                modifier = Modifier
                    .height(110.dp)
                    .fillMaxWidth(0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "BIENVENIDO A NOMI",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "TU SISTEMA DE ASISTENCIA Y NÓMINA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colorGrisOscuro,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Campos de entrada (Inputs)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            // Campo Número de empleado
            OutlinedTextField(
                value = numeroEmpleado,
                onValueChange = { numeroEmpleado = it },
                placeholder = { Text("Número de empleado o correo", color = colorGrisOscuro, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_agenda),
                        contentDescription = "Usuario",
                        tint = colorAzul,
                        modifier = Modifier.size(22.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorAzul,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                enabled = !cargando,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña", color = colorGrisOscuro, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_lock_idle_lock),
                        contentDescription = "Contraseña",
                        tint = colorAzul,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) android.R.drawable.ic_menu_view
                                else android.R.drawable.ic_menu_compass
                            ),
                            contentDescription = "Ver contraseña",
                            tint = colorGrisOscuro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorAzul,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                enabled = !cargando,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (!cargando) onLoginClick(numeroEmpleado, password) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Mensaje de error del servidor
            if (error != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = error,
                    color = colorRojo,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila con los botones de enlace secundario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // El alta de empleados se hace en la web (RRHH o ADMIN).
                // Antes había aquí un botón "Registrar empleado" sin sesión,
                // y cualquiera podía crearse una cuenta con cualquier rol.
                Spacer(modifier = Modifier.width(1.dp))

                // Botón ¿Olvidaste tu contraseña?
                TextButton(onClick = {
                    mostrarAyudaContrasena = true
                    onForgotPasswordClick()
                }) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = colorVerde,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Botón principal de Iniciar Sesión
        Button(
            onClick = { onLoginClick(numeroEmpleado, password) },
            enabled = !cargando,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorVerde,
                disabledContainerColor = colorVerde.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "INICIAR SESIÓN",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_input_add),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Pie de página (Versión)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Text(
                text = "Versión 1.0",
                fontSize = 11.sp,
                color = colorGrisOscuro
            )
            Text(
                text = "NOMI 2026",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colorGrisOscuro
            )
        }
    }

    if (mostrarAyudaContrasena) {
        AlertDialog(
            onDismissRequest = { mostrarAyudaContrasena = false },
            title = { Text("Recuperar contraseña") },
            text = { Text("Para restablecer tu contraseña, contacta a Recursos Humanos.") },
            confirmButton = {
                TextButton(onClick = { mostrarAyudaContrasena = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}