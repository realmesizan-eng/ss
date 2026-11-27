package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.CameraAiState
import com.example.ui.components.PathshalaTopBar
import com.example.ui.components.SpeakButton

private const val TAG = "LiveCameraScreen"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveCameraScreen(
    capturedBitmap: Bitmap?,
    cameraAiState: CameraAiState,
    onAnalyzeBitmap: (bitmap: Bitmap, prompt: String, speakAloud: Boolean, speakEnglishMode: Boolean) -> Unit,
    onSpeakBangla: (String) -> Unit,
    onSpeakEnglish: (String) -> Unit,
    onStopSpeak: () -> Unit,
    onSaveWord: (word: String, meaningBn: String, pronBn: String, example: String) -> Unit,
    onClearAnalysis: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var autoSpeak by remember { mutableStateOf(true) }
    var speakEnglishVoice by remember { mutableStateOf(false) }
    var customPrompt by remember { mutableStateOf("") }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            decodeUriToBitmap(context, uri)?.let { bmp ->
                onAnalyzeBitmap(bmp, customPrompt, autoSpeak, speakEnglishVoice)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onStopSpeak()
        }
    }

    Scaffold(
        topBar = {
            PathshalaTopBar(
                title = "লাইভ ক্যামেরা ও ভয়েস",
                actions = {
                    IconButton(
                        onClick = onStopSpeak,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "ভয়েস থামান"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live CameraX Preview OR Captured Frame
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (capturedBitmap != null) {
                        Image(
                            bitmap = capturedBitmap.asImageBitmap(),
                            contentDescription = "তোলা ছবি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        OutlinedButton(
                            onClick = onClearAnalysis,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.6f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .testTag("camera_retake_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("লাইভ ক্যামেরায় ফিরুন")
                        }
                    } else if (hasCameraPermission) {
                        CameraPreviewBox(
                            imageCapture = imageCapture,
                            lifecycleOwner = lifecycleOwner,
                            onCameraError = { cameraError = it }
                        )
                        if (cameraError != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cameraError ?: "ক্যামেরা চালু করা যায়নি। গ্যালারি থেকে ছবি বেছে নিন।",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "ক্যামেরা অনুমতি",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "বই, সাইনবোর্ড বা যেকোনো বস্তু ক্যামেরায় ধরে ছবি তুলে বাংলা ও ইংরেজি ভয়েসে শুনুন।",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { cameraPermLauncher.launch(Manifest.permission.CAMERA) },
                                modifier = Modifier.testTag("camera_permission_button")
                            ) {
                                Text("ক্যামেরা চালু করুন")
                            }
                        }
                    }
                }
            }

            // Voice Options
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ছবি দেখেই ভয়েসে বলে দিবে", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { autoSpeak = it },
                            modifier = Modifier.testTag("camera_auto_speak_switch")
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !speakEnglishVoice,
                            onClick = { speakEnglishVoice = false },
                            label = { Text("বাংলা ভয়েসে বলো") }
                        )
                        FilterChip(
                            selected = speakEnglishVoice,
                            onClick = { speakEnglishVoice = true },
                            label = { Text("English Voice") }
                        )
                    }
                    OutlinedTextField(
                        value = customPrompt,
                        onValueChange = { customPrompt = it },
                        label = { Text("বিশেষ প্রশ্ন (ঐচ্ছিক, যেমন: ছবির লেখা পড়ে দাও)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Primary Capture & Gallery Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (!hasCameraPermission) {
                            cameraPermLauncher.launch(Manifest.permission.CAMERA)
                            return@Button
                        }
                        captureBitmap(
                            context = context,
                            imageCapture = imageCapture,
                            onCaptured = { bmp ->
                                onAnalyzeBitmap(bmp, customPrompt, autoSpeak, speakEnglishVoice)
                            },
                            onError = { msg -> cameraError = msg }
                        )
                    },
                    enabled = cameraAiState !is CameraAiState.Analyzing,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("camera_capture_speak_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ছবি দেখে বলো")
                }

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = cameraAiState !is CameraAiState.Analyzing,
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("camera_gallery_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "গ্যালারি থেকে ছবি")
                    Spacer(Modifier.width(6.dp))
                    Text("ছবি নিন")
                }
            }

            // AI Result Card
            when (cameraAiState) {
                CameraAiState.Idle -> {
                    Text(
                        text = "টিপস: বইয়ের পাতা, ফল, পশু বা ইংরেজি লেখার দিকে ক্যামেরা ধরে 'ছবি দেখে বলো' চাপুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CameraAiState.Analyzing -> {
                    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Text(
                                text = "ছবি দেখে বিশ্লেষণ করছি ও ভয়েস তৈরি করছি...",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                is CameraAiState.Error -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = cameraAiState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is CameraAiState.Success -> {
                    val res = cameraAiState.result
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = res.titleBn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = res.explanationBn,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (res.explanationEn.isNotBlank()) {
                                Text(
                                    text = "English: ${res.explanationEn}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SpeakButton(
                                    label = "বাংলায় বলো",
                                    onClick = { onSpeakBangla("${res.titleBn}। ${res.explanationBn}") }
                                )
                                if (res.explanationEn.isNotBlank()) {
                                    SpeakButton(
                                        label = "Speak EN",
                                        onClick = { onSpeakEnglish(res.explanationEn) }
                                    )
                                }
                            }
                        }
                    }

                    if (res.detectedWords.isNotEmpty()) {
                        Text(
                            text = "ছবিতে পাওয়া শব্দসমূহ (ট্যাপ করে শিখুন ও সেভ করুন):",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            res.detectedWords.forEach { item ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${item.word} — ${item.meaningBn}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (item.pronunciationBn.isNotBlank()) {
                                                Text(
                                                    text = "উচ্চারণ: ${item.pronunciationBn}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            if (item.example.isNotBlank()) {
                                                Text(
                                                    text = item.example,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    val isAscii = item.word.any { it in 'A'..'Z' || it in 'a'..'z' }
                                                    if (isAscii) onSpeakEnglish(item.word) else onSpeakBangla(item.word)
                                                },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = "শব্দ শুনুন"
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    onSaveWord(
                                                        item.word,
                                                        item.meaningBn,
                                                        item.pronunciationBn,
                                                        item.example
                                                    )
                                                },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.BookmarkAdd,
                                                    contentDescription = "আমার শব্দে যোগ করুন",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewBox(
    imageCapture: ImageCapture,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onCameraError: (String) -> Unit
) {
    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        var boundProvider: ProcessCameraProvider? = null
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                boundProvider = provider
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val selector = when {
                    provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                    provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                    else -> {
                        onCameraError("এই ডিভাইসে ক্যামেরা পাওয়া যায়নি। নিচের 'ছবি নিন' দিয়ে গ্যালারি ব্যবহার করুন।")
                        return@addListener
                    }
                }
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e(TAG, "Camera bind failed", e)
                onCameraError("ক্যামেরা প্রিভিউ চালু করা যায়নি: ${e.localizedMessage ?: "অজানা ত্রুটি"}")
            }
        }, executor)

        onDispose {
            try {
                boundProvider?.unbindAll()
            } catch (_: Exception) {
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
    )
}

private fun captureBitmap(
    context: android.content.Context,
    imageCapture: ImageCapture,
    onCaptured: (Bitmap) -> Unit,
    onError: (String) -> Unit
) {
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val raw = image.toBitmap()
                    val rotation = image.imageInfo.rotationDegrees
                    val rotated = if (rotation != 0) {
                        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                        Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
                    } else {
                        raw
                    }
                    onCaptured(rotated)
                } catch (e: Exception) {
                    Log.e(TAG, "Bitmap conversion failed", e)
                    onError("ছবি প্রসেস করতে সমস্যা হয়েছে।")
                } finally {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Image capture failed", exception)
                onError("ছবি তোলা যায়নি। গ্যালারি থেকে ছবি নির্বাচন করতে পারেন।")
            }
        }
    )
}

private fun decodeUriToBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Decode URI failed", e)
        null
    }
}
