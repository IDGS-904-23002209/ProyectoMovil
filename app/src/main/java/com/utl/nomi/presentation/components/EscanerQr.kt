package com.utl.nomi.presentation.components

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

// Vista de la cámara trasera que lee códigos QR con ML Kit.
// Llama a alLeerQr con el texto del código cada vez que detecta uno.
@Composable
fun EscanerQr(
    modifier: Modifier = Modifier,
    alLeerQr: (String) -> Unit
) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val ejecutor = remember { Executors.newSingleThreadExecutor() }
    val lector = remember { BarcodeScanning.getClient() }
    val proveedorFuturo = remember { ProcessCameraProvider.getInstance(contexto) }
    // Siempre llama a la versión más reciente de alLeerQr
    val alLeerActual = rememberUpdatedState(alLeerQr)

    DisposableEffect(Unit) {
        onDispose {
            // Apaga la cámara al salir de la pantalla. Antes se quedaba encendida
            // porque estaba ligada a la Activity, no a la pantalla.
            if (proveedorFuturo.isDone) proveedorFuturo.get().unbindAll()
            ejecutor.shutdown()
            lector.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // COMPATIBLE usa TextureView: respeta los bordes de la caja
            val vista = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            proveedorFuturo.addListener({
                val proveedor = proveedorFuturo.get()
                val vistaPrevia = Preview.Builder().build().also {
                    it.setSurfaceProvider(vista.surfaceProvider)
                }
                val analisis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analisis.setAnalyzer(ejecutor) { imagen ->
                    procesarImagen(lector, imagen) { texto -> alLeerActual.value(texto) }
                }
                try {
                    proveedor.unbindAll()
                    proveedor.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, vistaPrevia, analisis)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            vista
        }
    )
}

@SuppressLint("UnsafeOptInUsageError")
private fun procesarImagen(lector: BarcodeScanner, imagen: ImageProxy, alLeer: (String) -> Unit) {
    val media = imagen.image
    if (media == null) {
        imagen.close()
        return
    }
    val entrada = InputImage.fromMediaImage(media, imagen.imageInfo.rotationDegrees)
    lector.process(entrada)
        .addOnSuccessListener { codigos -> codigos.firstOrNull()?.rawValue?.let(alLeer) }
        .addOnCompleteListener { imagen.close() }
}
