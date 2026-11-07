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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.utl.nomi.data.remote.AsistenciaApi
import com.util.nomi.presentation.nomina.AsistenciaViewModelFactory
import com.utl.nomi.presentation.auth.nomina.AsistenciaViewModel
import com.utl.nomi.presentation.components.BottomBarComponent
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RegistroAsistenciaScreen(
    api: AsistenciaApi,
    onCancelarClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onTabInicioClick: () -> Unit = {},
    onTabNominaClick: () -> Unit = {},
    onTabPerfilClick: () -> Unit = {}
) {
    val viewModel: AsistenciaViewModel = viewModel(
        factory = AsistenciaViewModelFactory(api)
    )

    val colorVerde = Color(0xFF10B981)
    val colorGrisBlanco = Color(0xFFF8FAFC)
    val colorRojo = Color(0xFFEF4444)
    val context = LocalContext.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val statusMessage by viewModel.statusMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var qrEscaneado by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) cameraPermissionState.launchPermissionRequest()
        if (!locationPermissionState.status.isGranted) locationPermissionState.launchPermissionRequest()
    }

    Box(modifier = Modifier.fillMaxSize().background(colorGrisBlanco)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 75.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nomi",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorVerde
                )

                Box(modifier = Modifier.size(40.dp)) {
                    IconButton(onClick = onNotificacionesClick, modifier = Modifier.align(Alignment.Center)) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_popup_reminder),
                            contentDescription = "Notificaciones",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Box(modifier = Modifier.size(10.dp).background(colorRojo, RoundedCornerShape(5.dp)).align(Alignment.TopEnd))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Cámara
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (cameraPermissionState.status.isGranted && locationPermissionState.status.isGranted) {
                    if (!qrEscaneado) {
                        CameraPreviewView(
                            modifier = Modifier.fillMaxSize(),
                            onQrCodeScanned = { qrCode ->
                                qrEscaneado = true
                                val empleadoId = 1 // TODO: ID real logueado
                                viewModel.validarYRegistrar(context, empleadoId, qrCode)
                            }
                        )
                    } else {
                        Text(
                            text = "QR detectado, procesando...",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = "Se requieren permisos de cámara y ubicación",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = colorVerde)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = statusMessage ?: "Escanea el QR para registrar tu asistencia",
                    fontSize = 13.sp,
                    color = when {
                        statusMessage?.startsWith("❌") == true -> colorRojo
                        statusMessage?.startsWith("✅") == true -> colorVerde
                        else -> Color.Black
                    },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onCancelarClick,
                    colors = ButtonDefaults.buttonColors(containerColor = colorVerde),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Cancelar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            val previewView = PreviewView(ctx)
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