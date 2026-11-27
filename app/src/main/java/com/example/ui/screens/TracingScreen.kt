package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.LettersBundle
import com.example.data.StrokePoint
import com.example.ui.components.PathshalaTopBar
import com.example.ui.components.SpeakButton
import com.example.ui.components.StarRatingRow
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@Composable
fun TracingScreen(
    bundle: LettersBundle,
    strokesMap: Map<String, List<List<StrokePoint>>>,
    onSpeakBangla: (String) -> Unit,
    onSpeakEnglish: (String) -> Unit,
    onRecordResult: (char: String, category: String, accuracyPercent: Int) -> Unit,
    onBack: () -> Unit
) {
    val categories = listOf("ব্যঞ্জনবর্ণ", "স্বরবর্ণ", "সংখ্যা", "A-Z", "a-z")
    var selectedCategory by remember { mutableIntStateOf(0) }
    var indexInCat by remember { mutableIntStateOf(0) }
    var blindMode by remember { mutableStateOf(false) } // false = দেখে লেখো, true = না দেখে লেখো

    val charList = remember(selectedCategory, bundle) {
        when (selectedCategory) {
            0 -> bundle.consonants.map { it.char }
            1 -> bundle.vowels.map { it.char }
            2 -> bundle.digits.map { it.char }
            3 -> ('A'..'Z').map { it.toString() }
            else -> ('a'..'z').map { it.toString() }
        }.ifEmpty { listOf("ক") }
    }
    val safeIndex = indexInCat.coerceIn(0, charList.lastIndex)
    val currentChar = charList[safeIndex]
    val isEnglishChar = selectedCategory >= 3

    val drawnStrokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var earnedStars by remember { mutableIntStateOf(0) }
    var accuracyValue by remember { mutableIntStateOf(-1) }

    val animProgress = remember { Animatable(0f) }
    var showStrokeAnimation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val guideStrokes = remember(currentChar, strokesMap) {
        strokesMap[currentChar]
            ?: strokesMap[currentChar.uppercase()]
            ?: listOf(
                listOf(StrokePoint(0.25f, 0.25f), StrokePoint(0.75f, 0.25f)),
                listOf(StrokePoint(0.50f, 0.25f), StrokePoint(0.50f, 0.78f))
            )
    }

    LaunchedEffect(currentChar) {
        drawnStrokes.clear()
        currentStroke = emptyList()
        resultMessage = null
        accuracyValue = -1
        earnedStars = 0
        showStrokeAnimation = false
    }

    Scaffold(
        topBar = {
            PathshalaTopBar(title = "হাতে খড়ি — লেখা শেখা", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedCategory == idx,
                        onClick = {
                            selectedCategory = idx
                            indexInCat = 0
                        },
                        label = { Text(label) }
                    )
                }
            }

            // Letter header + Mode switch ("দেখে লেখো" vs "না দেখে লেখো")
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "বর্ণ: $currentChar (${safeIndex + 1}/${charList.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (blindMode) "ধাপ ২: না দেখে লেখো" else "ধাপ ১: দেখে লেখো",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("না দেখে", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(6.dp))
                        Switch(
                            checked = blindMode,
                            onCheckedChange = { blindMode = it },
                            modifier = Modifier.testTag("tracing_blind_switch")
                        )
                    }
                }
            }

            // Tracing Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(currentChar) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                showStrokeAnimation = false
                                currentStroke = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentStroke = currentStroke + change.position
                            },
                            onDragEnd = {
                                if (currentStroke.isNotEmpty()) {
                                    drawnStrokes.add(currentStroke)
                                    currentStroke = emptyList()
                                }
                            },
                            onDragCancel = {
                                currentStroke = emptyList()
                            }
                        )
                    }
                    .testTag("tracing_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Subtle slate grid lines
                    drawLine(Color(0xFFEAE3D7), Offset(0f, h * 0.26f), Offset(w, h * 0.26f), strokeWidth = 2f)
                    drawLine(Color(0xFFEAE3D7), Offset(0f, h * 0.80f), Offset(w, h * 0.80f), strokeWidth = 2f)
                    drawLine(Color(0xFFF2ECE1), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 2f)

                    // Build Android Path from Paint.getTextPath
                    val textPath = buildLetterOutlinePath(currentChar, w, h)
                    if (!blindMode) {
                        drawPath(
                            path = textPath.asComposePath(),
                            color = Color(0xFFBDB6A8),
                            style = Stroke(
                                width = 5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f)
                            )
                        )
                    }

                    // Show animated stroke order when "দেখাও" is pressed
                    if (showStrokeAnimation) {
                        val totalStrokes = guideStrokes.size
                        val totalProgress = animProgress.value * totalStrokes
                        guideStrokes.forEachIndexed { sIdx, pts ->
                            val startPt = pts.first()
                            val startOffset = Offset(startPt.x * w, startPt.y * h)

                            // Number badge 1, 2, 3 at start point
                            drawCircle(
                                color = Color(0xFFD98B14),
                                radius = 18f,
                                center = startOffset
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "${sIdx + 1}",
                                startOffset.x,
                                startOffset.y + 7f,
                                AndroidPaint().apply {
                                    color = AndroidColor.WHITE
                                    textSize = 24f
                                    textAlign = AndroidPaint.Align.CENTER
                                    isFakeBoldText = true
                                }
                            )

                            val strokeFraction = (totalProgress - sIdx).coerceIn(0f, 1f)
                            if (strokeFraction > 0f && pts.size >= 2) {
                                val segCount = ((pts.size - 1) * strokeFraction).toInt().coerceAtLeast(1)
                                val animPath = Path().apply {
                                    moveTo(pts[0].x * w, pts[0].y * h)
                                    for (i in 1..segCount.coerceAtMost(pts.lastIndex)) {
                                        lineTo(pts[i].x * w, pts[i].y * h)
                                    }
                                }
                                drawPath(
                                    path = animPath,
                                    color = Color(0xFFD98B14),
                                    style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }

                    // Draw user strokes
                    (drawnStrokes + listOf(currentStroke)).forEach { stroke ->
                        if (stroke.size >= 2) {
                            val p = Path().apply {
                                moveTo(stroke.first().x, stroke.first().y)
                                for (i in 1 until stroke.size) {
                                    lineTo(stroke[i].x, stroke[i].y)
                                }
                            }
                            drawPath(
                                path = p,
                                color = Color(0xFF106349),
                                style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }

                if (blindMode) {
                    Text(
                        text = "ইঙ্গিত: $currentChar",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    )
                }
            }

            // Action row: দেখাও, মুছুন, যাচাই করুন
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showStrokeAnimation = true
                        scope.launch {
                            animProgress.snapTo(0f)
                            animProgress.animateTo(1f, animationSpec = tween(2200))
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tracing_show_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "দেখাও")
                    Spacer(Modifier.width(4.dp))
                    Text("দেখাও")
                }
                OutlinedButton(
                    onClick = {
                        drawnStrokes.clear()
                        currentStroke = emptyList()
                        resultMessage = null
                        accuracyValue = -1
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tracing_clear_button")
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = "মুছুন")
                    Spacer(Modifier.width(4.dp))
                    Text("মুছুন")
                }
                Button(
                    onClick = {
                        if (drawnStrokes.isEmpty()) {
                            resultMessage = "প্রথমে বোর্ডে আঙুল দিয়ে বর্ণটি লিখুন।"
                            return@Button
                        }
                        val score = evaluateTraceAccuracy(
                            char = currentChar,
                            strokes = drawnStrokes,
                            width = canvasSize.width,
                            height = canvasSize.height
                        )
                        accuracyValue = score
                        earnedStars = when {
                            score >= 85 -> 3
                            score >= 70 -> 2
                            score >= 60 -> 1
                            else -> 0
                        }
                        onRecordResult(currentChar, categories[selectedCategory], score)
                        if (score < 60) {
                            resultMessage = "সঠিকতা $score% — আবার চেষ্টা করুন!"
                            onSpeakBangla("আবার চেষ্টা করুন")
                        } else {
                            resultMessage = "বাহ! খুব সুন্দর! ($score% মিল, $earnedStars তারকা)"
                            if (isEnglishChar) {
                                onSpeakEnglish("Great job! $currentChar")
                            } else {
                                onSpeakBangla("বাহ! খুব সুন্দর! এটি $currentChar")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("tracing_check_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "যাচাই")
                    Spacer(Modifier.width(4.dp))
                    Text("যাচাই")
                }
            }

            // Accuracy & Stars result card
            if (resultMessage != null) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (earnedStars > 0) SuccessGreen.copy(alpha = 0.14f) else ErrorRed.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = resultMessage!!,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (accuracyValue >= 0) {
                            StarRatingRow(stars = earnedStars)
                        }
                    }
                }
            }

            // Prev / Voice / Next navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (safeIndex > 0) indexInCat = safeIndex - 1 },
                    enabled = safeIndex > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tracing_prev_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "আগের বর্ণ")
                    Spacer(Modifier.width(4.dp))
                    Text("আগের বর্ণ")
                }
                SpeakButton(
                    label = "শুনুন",
                    onClick = {
                        if (isEnglishChar) onSpeakEnglish(currentChar) else onSpeakBangla(currentChar)
                    }
                )
                Button(
                    onClick = { if (safeIndex < charList.lastIndex) indexInCat = safeIndex + 1 },
                    enabled = safeIndex < charList.lastIndex,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tracing_next_button")
                ) {
                    Text("পরের বর্ণ")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "পরের বর্ণ")
                }
            }
        }
    }
}

private fun buildLetterOutlinePath(char: String, width: Float, height: Float): AndroidPath {
    val paint = AndroidPaint().apply {
        isAntiAlias = true
        textSize = minOf(width, height) * 0.62f
        textAlign = AndroidPaint.Align.CENTER
    }
    val rawPath = AndroidPath()
    paint.getTextPath(char, 0, char.length, 0f, 0f, rawPath)
    val bounds = android.graphics.RectF()
    rawPath.computeBounds(bounds, true)
    val dx = (width / 2f) - bounds.centerX()
    val dy = (height / 2f) - bounds.centerY()
    val matrix = android.graphics.Matrix().apply { postTranslate(dx, dy) }
    rawPath.transform(matrix)
    return rawPath
}

private fun evaluateTraceAccuracy(
    char: String,
    strokes: List<List<Offset>>,
    width: Int,
    height: Int
): Int {
    if (width <= 0 || height <= 0) return 0
    val grid = 96
    val refBmp = Bitmap.createBitmap(grid, grid, Bitmap.Config.ALPHA_8)
    val usrBmp = Bitmap.createBitmap(grid, grid, Bitmap.Config.ALPHA_8)

    val refCanvas = AndroidCanvas(refBmp)
    val refPath = buildLetterOutlinePath(char, grid.toFloat(), grid.toFloat())
    val refPaint = AndroidPaint().apply {
        color = AndroidColor.BLACK
        style = AndroidPaint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = false
    }
    refCanvas.drawPath(refPath, refPaint)

    val usrCanvas = AndroidCanvas(usrBmp)
    val usrPaint = AndroidPaint().apply {
        color = AndroidColor.BLACK
        style = AndroidPaint.Style.STROKE
        strokeWidth = 10f
        strokeCap = AndroidPaint.Cap.ROUND
        strokeJoin = AndroidPaint.Join.ROUND
        isAntiAlias = false
    }
    val sx = grid.toFloat() / width.toFloat()
    val sy = grid.toFloat() / height.toFloat()
    strokes.forEach { pts ->
        if (pts.size >= 2) {
            val p = AndroidPath().apply {
                moveTo(pts.first().x * sx, pts.first().y * sy)
                for (i in 1 until pts.size) {
                    lineTo(pts[i].x * sx, pts[i].y * sy)
                }
            }
            usrCanvas.drawPath(p, usrPaint)
        }
    }

    var refPixels = 0
    var coveredPixels = 0
    var userPixels = 0
    var outsidePixels = 0
    for (y in 0 until grid) {
        for (x in 0 until grid) {
            val rOn = (refBmp.getPixel(x, y) ushr 24) > 40
            val uOn = (usrBmp.getPixel(x, y) ushr 24) > 40
            if (rOn) {
                refPixels++
                if (uOn) coveredPixels++
            }
            if (uOn) {
                userPixels++
                if (!rOn) outsidePixels++
            }
        }
    }
    refBmp.recycle()
    usrBmp.recycle()

    if (refPixels == 0 || userPixels == 0) return 0
    val coverage = coveredPixels.toFloat() / refPixels.toFloat()
    val outsideRatio = outsidePixels.toFloat() / userPixels.toFloat()
    val rawScore = ((coverage * 1.15f - outsideRatio * 0.45f) * 100f).toInt()
    return rawScore.coerceIn(0, 100)
}
