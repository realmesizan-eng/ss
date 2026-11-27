package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiRepository
import com.example.ai.AiResult
import com.example.ai.ChatMessage
import com.example.ai.ImageAnalysisResult
import com.example.audio.SpeechEvalState
import com.example.audio.SpeechRecognizerHelper
import com.example.audio.TtsHelper
import com.example.audio.WordRangeHighlight
import com.example.data.DictionaryEntry
import com.example.data.EnglishBundle
import com.example.data.LearnAssetRepository
import com.example.data.LettersBundle
import com.example.data.PhraseItem
import com.example.data.ReadingLevelData
import com.example.data.StoryItem
import com.example.data.StrokePoint
import com.example.data.local.LetterProgressEntity
import com.example.data.local.LicenseManager
import com.example.data.local.LicenseRequestEntity
import com.example.data.local.PathshalaDatabase
import com.example.data.local.ProgressStore
import com.example.data.local.SavedWordEntity
import com.example.data.local.UserProgressState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "PathshalaViewModel"

sealed interface CameraAiState {
    data object Idle : CameraAiState
    data object Analyzing : CameraAiState
    data class Success(val result: ImageAnalysisResult) : CameraAiState
    data class Error(val message: String) : CameraAiState
}

sealed interface DefineAiState {
    data object Idle : DefineAiState
    data object Loading : DefineAiState
    data class Success(val entry: DictionaryEntry) : DefineAiState
    data class Error(val message: String) : DefineAiState
}

class PathshalaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PathshalaDatabase.getInstance(application)
    private val progressStore = ProgressStore(application)
    private val assetRepo = LearnAssetRepository(application)
    private val aiRepo = AiRepository()

    val tts = TtsHelper(application)
    val speechRecognizer = SpeechRecognizerHelper(application)

    val progressState: StateFlow<UserProgressState> = progressStore.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProgressState())

    val savedWords: StateFlow<List<SavedWordEntity>> = db.savedWordDao().observeAllWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val letterProgressList: StateFlow<List<LetterProgressEntity>> = db.letterProgressDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val licenseRequests: StateFlow<List<LicenseRequestEntity>> = db.licenseRequestDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ttsHighlight: StateFlow<WordRangeHighlight> = tts.highlightState
    val speechEval: StateFlow<SpeechEvalState> = speechRecognizer.state

    private val _letters = MutableStateFlow(LettersBundle())
    val letters: StateFlow<LettersBundle> = _letters.asStateFlow()

    private val _strokesMap = MutableStateFlow<Map<String, List<List<StrokePoint>>>>(emptyMap())
    val strokesMap: StateFlow<Map<String, List<List<StrokePoint>>>> = _strokesMap.asStateFlow()

    private val _readingLevels = MutableStateFlow<List<ReadingLevelData>>(emptyList())
    val readingLevels: StateFlow<List<ReadingLevelData>> = _readingLevels.asStateFlow()

    private val _stories = MutableStateFlow<List<StoryItem>>(emptyList())
    val stories: StateFlow<List<StoryItem>> = _stories.asStateFlow()

    private val _englishBundle = MutableStateFlow(EnglishBundle())
    val englishBundle: StateFlow<EnglishBundle> = _englishBundle.asStateFlow()

    private val _phrases = MutableStateFlow<List<PhraseItem>>(emptyList())
    val phrases: StateFlow<List<PhraseItem>> = _phrases.asStateFlow()

    private val _dictionary = MutableStateFlow<List<DictionaryEntry>>(emptyList())
    val dictionary: StateFlow<List<DictionaryEntry>> = _dictionary.asStateFlow()

    // AI Tutor Chat State
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "welcome",
                isUser = false,
                text = "আসসালামু আলাইকুম / নমস্কার! আমি তোমার শিখি এআই শিক্ষক। বাংলা বা ইংরেজি বর্ণ, বানান, অর্থ বা যেকোনো প্রশ্ন আমাকে জিজ্ঞাসা করতে পারো।"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatStreamingText = MutableStateFlow<String?>(null)
    val chatStreamingText: StateFlow<String?> = _chatStreamingText.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    // Live Camera / Image Analysis State
    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    private val _cameraAiState = MutableStateFlow<CameraAiState>(CameraAiState.Idle)
    val cameraAiState: StateFlow<CameraAiState> = _cameraAiState.asStateFlow()

    // Dictionary AI /define State
    private val _defineAiState = MutableStateFlow<DefineAiState>(DefineAiState.Idle)
    val defineAiState: StateFlow<DefineAiState> = _defineAiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadAllAssets()
    }

    private fun loadAllAssets() {
        viewModelScope.launch {
            _letters.value = assetRepo.getLetters()
            _strokesMap.value = assetRepo.getStrokesMap()
            _readingLevels.value = assetRepo.getReadingLevels()
            _stories.value = assetRepo.getStories()
            _englishBundle.value = assetRepo.getEnglishBundle()
            _phrases.value = assetRepo.getPhrases()
            _dictionary.value = assetRepo.getDictionary()
        }
    }

    fun consumeToast() {
        _toastMessage.value = null
    }

    fun showMessage(msg: String) {
        _toastMessage.value = msg
    }

    fun completeOnboarding(name: String, goal: String) {
        viewModelScope.launch {
            progressStore.saveOnboarding(name, goal)
        }
    }

    fun speakBangla(text: String, slow: Boolean = false, trackHighlight: Boolean = false) {
        tts.speak(text, isEnglish = false, slow = slow, trackWordHighlight = trackHighlight)
    }

    fun speakEnglish(text: String, slow: Boolean = false, trackHighlight: Boolean = false) {
        tts.speak(text, isEnglish = true, slow = slow, trackWordHighlight = trackHighlight)
    }

    fun stopSpeaking() {
        tts.stop()
    }

    fun recordLetterTraceResult(char: String, category: String, accuracyPercent: Int) {
        val stars = when {
            accuracyPercent >= 85 -> 3
            accuracyPercent >= 70 -> 2
            accuracyPercent >= 60 -> 1
            else -> 0
        }
        viewModelScope.launch {
            val existing = db.letterProgressDao().getByChar(char)
            val newProgress = LetterProgressEntity(
                letterChar = char,
                category = category,
                bestStars = maxOf(existing?.bestStars ?: 0, stars),
                attempts = (existing?.attempts ?: 0) + 1,
                mistakes = (existing?.mistakes ?: 0) + if (stars == 0) 1 else 0,
                bestAccuracy = maxOf(existing?.bestAccuracy ?: 0, accuracyPercent),
                updatedAt = System.currentTimeMillis()
            )
            db.letterProgressDao().upsert(newProgress)
            if (stars > 0) {
                progressStore.recordActivity(starsEarned = stars, lessonId = "trace_$char")
                tts.playChime(true)
            } else {
                tts.playChime(false)
            }
        }
    }

    fun markReadingLessonComplete(level: Int, lessonId: String, totalLessonsInLevel: Int) {
        viewModelScope.launch {
            val currentCompleted = progressState.value.completedLessonIds + lessonId
            val completedInLevel = currentCompleted.count { it.startsWith("r${level}_") }
            val badge = if (level == 1 && completedInLevel >= 1) "প্রথম পাঠ সমাপ্ত" else null
            progressStore.recordActivity(starsEarned = 3, lessonId = lessonId, badge = badge)
            if (completedInLevel >= totalLessonsInLevel || completedInLevel >= 1) {
                // Completing level lessons unlocks the next level cleanly
                if (level < 5) {
                    progressStore.unlockReadingLevel(level + 1)
                }
            }
            tts.playChime(true)
        }
    }

    fun completeStory(storyId: String) {
        viewModelScope.launch {
            progressStore.recordActivity(starsEarned = 3, lessonId = storyId, badge = "গল্পপ্রেমী")
            tts.playChime(true)
        }
    }

    fun recordQuizFinish(scoreOutOfTen: Int) {
        viewModelScope.launch {
            progressStore.incrementQuizCount()
            val stars = (scoreOutOfTen / 2).coerceAtLeast(1)
            val badge = if (scoreOutOfTen >= 8) "কুইজ চ্যাম্পিয়ন" else null
            progressStore.recordActivity(starsEarned = stars, badge = badge)
        }
    }

    fun setReaderPreferences(darkMode: Boolean, fontSizeSp: Int) {
        viewModelScope.launch {
            progressStore.setReaderPreferences(darkMode, fontSizeSp)
        }
    }

    fun saveWordToMyWords(
        word: String,
        meaningBn: String,
        pronunciationBn: String,
        example: String = "",
        source: String = "manual"
    ) {
        if (word.isBlank()) return
        viewModelScope.launch {
            val count = db.savedWordDao().countByWord(word.trim())
            if (count > 0) {
                _toastMessage.value = "'$word' ইতিমধ্যেই আমার শব্দ তালিকায় আছে।"
            } else {
                db.savedWordDao().insertWord(
                    SavedWordEntity(
                        word = word.trim(),
                        meaningBn = meaningBn.trim(),
                        pronunciationBn = pronunciationBn.trim(),
                        example = example.trim(),
                        source = source
                    )
                )
                progressStore.recordActivity(starsEarned = 1)
                _toastMessage.value = "'$word' আমার শব্দে যোগ করা হয়েছে!"
            }
        }
    }

    fun toggleWordMastered(word: SavedWordEntity) {
        viewModelScope.launch {
            db.savedWordDao().updateMastered(word.id, !word.isMastered)
        }
    }

    fun deleteSavedWord(id: Int) {
        viewModelScope.launch {
            db.savedWordDao().deleteById(id)
        }
    }

    // AI Tutor Multi-turn Chat
    fun sendChatMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _chatStreamingText.value != null) return

        val historySnapshot = _chatMessages.value.filter { it.id != "welcome" }
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = trimmed
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _chatError.value = null
        _chatStreamingText.value = ""

        viewModelScope.launch {
            var latestReply = ""
            try {
                aiRepo.streamTutorReply(historySnapshot, trimmed).collect { accumulated ->
                    latestReply = accumulated
                    _chatStreamingText.value = accumulated
                }
                if (latestReply.isNotBlank()) {
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        id = UUID.randomUUID().toString(),
                        isUser = false,
                        text = latestReply
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat error", e)
                _chatError.value = e.localizedMessage ?: "এআই শিক্ষকের উত্তর আনতে সমস্যা হয়েছে।"
            } finally {
                _chatStreamingText.value = null
            }
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(_chatMessages.value.first())
        _chatError.value = null
        _chatStreamingText.value = null
    }

    // Live Camera / Photo Analysis + Voice Readout
    fun analyzePhotoAndSpeak(
        bitmap: Bitmap,
        prompt: String = "",
        speakAloud: Boolean = true,
        speakEnglishMode: Boolean = false
    ) {
        _capturedBitmap.value = bitmap
        _cameraAiState.value = CameraAiState.Analyzing
        viewModelScope.launch {
            when (val res = aiRepo.analyzeImage(bitmap, prompt)) {
                is AiResult.Success -> {
                    _cameraAiState.value = CameraAiState.Success(res.data)
                    progressStore.recordActivity(starsEarned = 1)
                    if (speakAloud) {
                        val speechText = if (speakEnglishMode && res.data.explanationEn.isNotBlank()) {
                            res.data.explanationEn
                        } else {
                            "${res.data.titleBn}। ${res.data.explanationBn}"
                        }
                        tts.speak(speechText, isEnglish = speakEnglishMode)
                    }
                }
                is AiResult.Error -> {
                    _cameraAiState.value = CameraAiState.Error(res.message)
                }
            }
        }
    }

    fun clearCameraAnalysis() {
        _capturedBitmap.value = null
        _cameraAiState.value = CameraAiState.Idle
        tts.stop()
    }

    // Online Dictionary /define fallback
    fun defineWordOnline(word: String) {
        val cleaned = word.trim()
        if (cleaned.isEmpty()) return
        val state = progressState.value
        if (!state.isPremium && state.dailyAiDefineCount >= 5) {
            _defineAiState.value = DefineAiState.Error(
                "ফ্রি ভার্সনে দিনে ৫টি অনলাইন শব্দ খোঁজা যায়। সীমাহীন ব্যবহারের জন্য প্রিমিয়াম লাইসেন্স নিন।"
            )
            return
        }
        _defineAiState.value = DefineAiState.Loading
        viewModelScope.launch {
            when (val res = aiRepo.defineWord(cleaned)) {
                is AiResult.Success -> {
                    progressStore.incrementAiDefineCount()
                    _defineAiState.value = DefineAiState.Success(res.data)
                }
                is AiResult.Error -> {
                    _defineAiState.value = DefineAiState.Error(res.message)
                }
            }
        }
    }

    fun resetDefineState() {
        _defineAiState.value = DefineAiState.Idle
    }

    // License & Admin Panel Actions
    fun submitBkashLicenseRequest(buyerPhone: String, trxId: String, note: String) {
        if (buyerPhone.isBlank() || trxId.isBlank()) {
            _toastMessage.value = "অনুগ্রহ করে বিকাশ নম্বর ও ট্রানজেকশন আইডি (TrxID) লিখুন।"
            return
        }
        viewModelScope.launch {
            db.licenseRequestDao().insert(
                LicenseRequestEntity(
                    buyerPhone = buyerPhone.trim(),
                    trxId = trxId.trim().uppercase(),
                    amountTaka = progressState.value.licenseFeeTaka,
                    status = "PENDING",
                    note = note.trim()
                )
            )
            _toastMessage.value = "আবেদন জমা হয়েছে! অ্যাডমিন যাচাই করে WhatsApp (${progressState.value.whatsappNumber}) এ লাইসেন্স কী পাঠাবেন।"
        }
    }

    fun activateWithLicenseKey(keyInput: String): Boolean {
        val cleaned = keyInput.trim().uppercase()
        return if (LicenseManager.isValidLicenseKey(cleaned)) {
            viewModelScope.launch {
                progressStore.activatePremium(cleaned)
                _toastMessage.value = "অভিনন্দন! শিখি প্রিমিয়াম লাইসেন্স সক্রিয় হয়েছে।"
            }
            true
        } else {
            _toastMessage.value = "লাইসেন্স কী সঠিক নয়। WhatsApp (${progressState.value.whatsappNumber}) থেকে সঠিক কী নিন।"
            false
        }
    }

    fun adminGenerateKey(seed: String): String {
        return LicenseManager.generateLicenseKey(seed)
    }

    fun adminApproveRequest(request: LicenseRequestEntity): String {
        val key = LicenseManager.generateLicenseKey("${request.buyerPhone}_${request.trxId}")
        viewModelScope.launch {
            db.licenseRequestDao().approve(request.id, "APPROVED", key)
            _toastMessage.value = "লাইসেন্স কী তৈরি হয়েছে: $key"
        }
        return key
    }

    fun adminDeleteRequest(id: Int) {
        viewModelScope.launch {
            db.licenseRequestDao().deleteById(id)
        }
    }

    fun adminTogglePremium(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                val key = LicenseManager.generateLicenseKey("ADMIN_DIRECT")
                progressStore.activatePremium(key)
            } else {
                progressStore.setPremiumStatus(false)
            }
        }
    }

    fun adminUpdateSettings(newPin: String, bkash: String, whatsapp: String, fee: Int) {
        viewModelScope.launch {
            progressStore.updateAdminConfig(newPin, bkash, whatsapp, fee)
            _toastMessage.value = "অ্যাডমিন কনফিগারেশন সংরক্ষিত হয়েছে।"
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
        speechRecognizer.destroy()
    }
}
