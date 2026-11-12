package com.utl.nomi.presentation.screens

import android.Manifest
import android.annotation.SuppressLint
import android.media.Image
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.utl.nomi.data.remote.AsistenciaApi
import com.utl.nomi.presentation.auth.nomina.AsistenciaViewModel
import com.utl.nomi.presentation.auth.nomina.AsistenciaViewModelFactory
import com.utl.nomi.presentation.auth.nomina.EstadoRegistro
import com.utl.nomi.presentation.components.BottomBarComponent
import com.utl.nomi.presentation.components.TopBarComponent
import com.utl.nomi.ui.theme.Exito
import java.util.concurrent.Executors

// Pantalla para registrar asistencia escaneando el QR de la planta.
// empleadoId viene del perfil del usuario con sesión (antes estaba fijo en 1).
// Si todavía no se cargó el perfil, se pide con onCargarPerfil.
// El diseño completo del Figma (elegir GPS o QR y pantalla de éxito) llega en la Fase 4.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RegistroAsistenciaScreen(
    api: AsistenciaApi,
    empleadoId: Int?,
    cargandoPerfil: Boolean = false,
    onCargarPerfil: () -> Unit = {},
    onCancelarClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onTabInicioClick: () -> Unit = {},
    onTabNominaClick: () -> Unit = {},
    onTabPerfilClick: () -> Unit = {}
) {
    val viewModel: AsistenciaViewModel = viewModel(
        factory = AsistenciaViewModelFactory(api)
    )
    val context = LocalContext.current

    // Pide cámara y ubicación juntos (antes eran dos solicitudes seguidas y una tapaba a la otra)
    val permisos = rememberMultiplePermissionsState(
        listOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
    )

    val statusMessage by viewModel.statusMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(Unit) {
        if (!permisos.allPermissionsGranted) permisos.launchMultiplePermissionRequest()
        if (empleadoId == null) onCargarPerfil()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                TopBarComponent(onNotificacionesClick = onNotificacionesClick)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registrar asistencia",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
            Text(
                text = "Apunta la cámara al código QR de la planta.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cámara
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !permisos.allPermissionsGranted -> TextoSobreCamara(
                        "Se necesitan permisos de cámara y ubicación para registrar tu asistencia."
                    )
                    empleadoId == null -> TextoSobreCamara(
                        if (cargandoPerfil) "Cargando tus datos..."
                        else "Tu usuario no tiene un empleado asociado. Contacta a RH."
                    )
                    estado == EstadoRegistro.ESPERANDO -> CameraPreviewView(
                        modifier = Modifier.fillMaxSize(),
                        onQrCodeScanned = { qrCode ->
                            viewModel.validarYRegistrar(context, empleadoId, qrCode)
                        }
                    )
                    else -> TextoSobreCamara(
                        if (estado == EstadoRegistro.PROCESANDO) "QR detectado, procesando..." else "QR leído"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = statusMessage ?: "Escanea el QR para registrar tu asistencia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (estado) {
                        EstadoRegistro.ERROR -> MaterialTheme.colorScheme.error
                        EstadoRegistro.EXITO -> Exito
                        else -> MaterialTheme.colorScheme.onBackground
                    },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Si falló, deja volver a escanear sin salir de la pantalla
                if (estado == EstadoRegistro.ERROR) {
                    Button(
                        onClick = { viewModel.reiniciar() },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Intentar de nuevo", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onCancelarClick,
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        if (estado == EstadoRegistro.EXITO) "Volver al inicio" else "Cancelar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
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
private fun TextoSobreCamara(texto: String) {
    Text(
        text = texto,
        color = Color.White.copy(alpha = 0.85f),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(24.dp)
    )
}

// --- CÁMARA CON ML KIT ---
@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    AndroidView(
        factory = { ctx ->
            // COMPATIBLE usa TextureView: respeta los bordes de la caja.
            // Con el modo por defecto (SurfaceView) la cámara se dibujaba
            // encima del título y del mensaje de abajo.
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImageProxy(barcodeScanner, imageProxy, onQrCodeScanned)
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

@SuppressLint("UnsafeOptInUsageError")
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onQrCodeScanned: (String) -> Unit
) {
    val mediaImage: Image? = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { qrValue -> onQrCodeScanned(qrValue) }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}