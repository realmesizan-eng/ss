package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

private const val TAG = "SpeechListener"

data class WordRangeHighlight(
    val utteranceId: String = "",
    val activeWordIndex: Int = -1,
    val isSpeaking: Boolean = false
)

data class WordMatchResult(
    val expected: String,
    val isMatched: Boolean
)

data class SpeechEvalState(
    val isListening: Boolean = false,
    val recognizedText: String = "",
    val wordResults: List<WordMatchResult> = emptyList(),
    val scorePercent: Int = 0,
    val errorMessage: String? = null
)

class TtsHelper(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isReady = false
    private val scope = CoroutineScope(Dispatchers.Main)
    private var fallbackHighlightJob: Job? = null

    private val _highlightState = MutableStateFlow(WordRangeHighlight())
    val highlightState: StateFlow<WordRangeHighlight> = _highlightState.asStateFlow()

    private var currentWords: List<String> = emptyList()
    private var rangeCallbackReceived = false

    init {
        tts = TextToSpeech(appContext) { status ->
            isReady = (status == TextToSpeech.SUCCESS)
            if (isReady) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _highlightState.value = _highlightState.value.copy(
                            utteranceId = utteranceId.orEmpty(),
                            isSpeaking = true
                        )
                    }

                    override fun onRangeStart(
                        utteranceId: String?,
                        start: Int,
                        end: Int,
                        frame: Int
                    ) {
                        rangeCallbackReceived = true
                        fallbackHighlightJob?.cancel()
                        val idx = findWordIndexForCharOffset(start)
                        _highlightState.value = WordRangeHighlight(
                            utteranceId = utteranceId.orEmpty(),
                            activeWordIndex = idx,
                            isSpeaking = true
                        )
                    }

                    override fun onDone(utteranceId: String?) {
                        fallbackHighlightJob?.cancel()
                        _highlightState.value = WordRangeHighlight(
                            utteranceId = utteranceId.orEmpty(),
                            activeWordIndex = -1,
                            isSpeaking = false
                        )
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        fallbackHighlightJob?.cancel()
                        _highlightState.value = WordRangeHighlight(isSpeaking = false)
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        fallbackHighlightJob?.cancel()
                        _highlightState.value = WordRangeHighlight(isSpeaking = false)
                    }
                })
            } else {
                Log.w(TAG, "TextToSpeech init failed with status $status")
            }
        }
    }

    private var currentTextForOffsets: String = ""

    private fun findWordIndexForCharOffset(charStart: Int): Int {
        if (currentWords.isEmpty() || currentTextForOffsets.isEmpty()) return -1
        var cursor = 0
        currentWords.forEachIndexed { index, word ->
            val found = currentTextForOffsets.indexOf(word, startIndex = cursor)
            if (found != -1) {
                val end = found + word.length
                if (charStart <= end) return index
                cursor = end
            }
        }
        return currentWords.lastIndex
    }

    fun speak(
        text: String,
        isEnglish: Boolean = false,
        slow: Boolean = false,
        trackWordHighlight: Boolean = false
    ) {
        if (!isReady || text.isBlank()) return
        fallbackHighlightJob?.cancel()
        rangeCallbackReceived = false
        currentTextForOffsets = text
        currentWords = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }

        val locale = if (isEnglish) Locale.US else Locale("bn", "BD")
        val langResult = tts?.setLanguage(locale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            if (!isEnglish) {
                tts?.setLanguage(Locale("bn", "IN"))
            } else {
                tts?.setLanguage(Locale.ENGLISH)
            }
        }

        val rate = if (slow) 0.65f else 1.0f
        tts?.setSpeechRate(rate)
        val utteranceId = UUID.randomUUID().toString()
        _highlightState.value = WordRangeHighlight(
            utteranceId = utteranceId,
            activeWordIndex = if (trackWordHighlight && currentWords.isNotEmpty()) 0 else -1,
            isSpeaking = true
        )

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        if (trackWordHighlight && currentWords.isNotEmpty()) {
            fallbackHighlightJob = scope.launch {
                val stepMs = if (slow) 750L else 480L
                delay(400L)
                for (i in currentWords.indices) {
                    if (rangeCallbackReceived) break
                    _highlightState.value = WordRangeHighlight(
                        utteranceId = utteranceId,
                        activeWordIndex = i,
                        isSpeaking = true
                    )
                    delay(stepMs)
                }
                if (!rangeCallbackReceived) {
                    delay(300L)
                    _highlightState.value = WordRangeHighlight(isSpeaking = false)
                }
            }
        }
    }

    fun stop() {
        fallbackHighlightJob?.cancel()
        tts?.stop()
        _highlightState.value = WordRangeHighlight(isSpeaking = false)
    }

    fun playChime(positive: Boolean) {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            if (positive) {
                tg.startTone(ToneGenerator.TONE_PROP_ACK, 160)
            } else {
                tg.startTone(ToneGenerator.TONE_PROP_NACK, 180)
            }
            scope.launch {
                delay(240L)
                tg.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "ToneGenerator unavailable", e)
        }
    }

    fun shutdown() {
        fallbackHighlightJob?.cancel()
        tts?.stop()
        tts?.shutdown()
    }
}

class SpeechRecognizerHelper(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow(SpeechEvalState())
    val state: StateFlow<SpeechEvalState> = _state.asStateFlow()

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(expectedText: String, languageTag: String = "bn-BD") {
        if (!isAvailable()) {
            _state.value = SpeechEvalState(
                isListening = false,
                errorMessage = "এই ডিভাইসে ভয়েস রিকগনিশন সেবা পাওয়া যায়নি। অনুগ্রহ করে Google অ্যাপ চালু বা আপডেট করুন।"
            )
            return
        }

        try {
            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.value = SpeechEvalState(isListening = true)
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _state.value = _state.value.copy(isListening = false)
                    }

                    override fun onError(error: Int) {
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "স্পষ্টভাবে শোনা যায়নি, আবার বলুন।"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কোনো কথা শোনা যায়নি, মাইকে আবার বলুন।"
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "ভয়েস রিকগনিশনের জন্য নেটওয়ার্ক প্রয়োজন হতে পারে।"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "মাইক্রোফোনের অনুমতি প্রয়োজন।"
                            else -> "ভয়েস শোনায় সমস্যা হয়েছে (কোড: $error)"
                        }
                        _state.value = _state.value.copy(isListening = false, errorMessage = msg)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = matches?.firstOrNull().orEmpty()
                        evaluate(expectedText, spoken)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull().orEmpty()
                        if (partial.isNotBlank()) {
                            _state.value = _state.value.copy(recognizedText = partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            _state.value = SpeechEvalState(isListening = true)
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "SpeechRecognizer start failed", e)
            _state.value = SpeechEvalState(
                isListening = false,
                errorMessage = "মাইক্রোফোন চালু করা যায়নি: ${e.localizedMessage ?: "অজানা ত্রুটি"}"
            )
        }
    }

    fun evaluate(expectedText: String, spokenText: String) {
        val cleanExpected = expectedText
            .replace(Regex("[।.,!?\"'()\\[\\]]"), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        val spokenTokens = spokenText
            .replace(Regex("[।.,!?\"'()\\[\\]]"), " ")
            .trim()
            .lowercase()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .toSet()

        val results = cleanExpected.map { exp ->
            val normalized = exp.lowercase()
            val matched = spokenTokens.any { tok ->
                tok == normalized || tok.contains(normalized) || normalized.contains(tok)
            }
            WordMatchResult(expected = exp, isMatched = matched)
        }
        val score = if (results.isEmpty()) 0 else {
            (results.count { it.isMatched } * 100) / results.size
        }
        _state.value = SpeechEvalState(
            isListening = false,
            recognizedText = spokenText,
            wordResults = results,
            scorePercent = score,
            errorMessage = null
        )
    }

    fun clear() {
        _state.value = SpeechEvalState()
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }
}
