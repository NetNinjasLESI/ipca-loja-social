package com.ipca.lojasocial.presentation.ui.screens.inventory

import android.Manifest
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "BarcodeScanner"

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ler Código de Barras") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                cameraPermissionState.status.isGranted -> {
                    CameraPreviewWithScanner(
                        onBarcodeScanned = onBarcodeScanned,
                        onNavigateBack = onNavigateBack
                    )
                }
                cameraPermissionState.status.shouldShowRationale -> {
                    PermissionRationale(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }
                else -> {
                    PermissionRequest(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewWithScanner(
    onBarcodeScanned: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var hasFlash by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val isProcessing = remember { AtomicBoolean(false) }

    LaunchedEffect(scannedCode) {
        scannedCode?.let { code ->
            if (isProcessing.compareAndSet(false, true)) {
                showSuccess = true
                onBarcodeScanned(code) // envia o código para Add/Edit
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(executor, BarcodeAnalyzer { barcode ->
                                    if (scannedCode == null && !isProcessing.get()) {
                                        scannedCode = barcode
                                    }
                                })
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )

                        hasFlash = camera?.cameraInfo?.hasFlashUnit() ?: false
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao iniciar câmera", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        ScannerOverlay(isScanning = scannedCode == null)

        // Feedback visual quando detecta
        AnimatedVisibility(
            visible = showSuccess,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Green
                    )
                    Text(
                        text = "Código Lido!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = scannedCode ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = onNavigateBack) {
                        Text("Continuar")
                    }
                }
            }
        }

        // Controles de flash
        AnimatedVisibility(
            visible = !showSuccess,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Posicione o código de barras dentro do retângulo",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.small)
                        .padding(16.dp)
                )

                if (hasFlash) {
                    FloatingActionButton(
                        onClick = {
                            camera?.let {
                                isFlashOn = !isFlashOn
                                it.cameraControl.enableTorch(isFlashOn)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "Flash",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerOverlay(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val scanLinePosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scanRectWidth = size.width * 0.8f
            val scanRectHeight = 200.dp.toPx()
            val left = (size.width - scanRectWidth) / 2
            val top = (size.height - scanRectHeight) / 2

            drawRect(color = Color.Black.copy(alpha = 0.5f), size = size)
            drawRect(
                color = Color.Transparent,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(scanRectWidth, scanRectHeight),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )

            if (isScanning) {
                val lineY = top + (scanRectHeight * scanLinePosition)
                drawLine(
                    color = Color.Green,
                    start = androidx.compose.ui.geometry.Offset(left, lineY),
                    end = androidx.compose.ui.geometry.Offset(left + scanRectWidth, lineY),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun PermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(80.dp).padding(bottom = 16.dp))
        Text("Permissão de Câmara Necessária", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Para ler códigos de barras, precisamos de acesso à câmara.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) { Text("Permitir Acesso à Câmara") }
    }
}

@Composable
private fun PermissionRationale(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(80.dp).padding(bottom = 16.dp), tint = MaterialTheme.colorScheme.error)
        Text("Permissão Negada", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("A permissão foi negada. Ative-a nas configurações.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) { Text("Tentar Novamente") }
    }
}

private class BarcodeAnalyzer(private val onBarcodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(
                    BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A, BarcodeFormat.UPC_E,
                    BarcodeFormat.CODE_128, BarcodeFormat.CODE_39,
                    BarcodeFormat.CODE_93, BarcodeFormat.CODABAR,
                    BarcodeFormat.ITF, BarcodeFormat.QR_CODE
                ),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.PURE_BARCODE to false
            )
        )
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            try {
                val buffer = mediaImage.planes[0].buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)

                val result = tryDecode(data, imageProxy.width, imageProxy.height)
                result?.text?.let { onBarcodeDetected(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao analisar imagem", e)
            } finally {
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }

    private fun tryDecode(data: ByteArray, width: Int, height: Int): Result? {
        return try {
            val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            reader.decodeWithState(bitmap)
        } catch (e: NotFoundException) {
            null
        } finally {
            reader.reset()
        }
    }
}
