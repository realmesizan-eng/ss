package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ai.configureAppCheck
import com.example.ui.PathshalaViewModel
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiTutorScreen
import com.example.ui.screens.AlphabetScreen
import com.example.ui.screens.EnglishScreen
import com.example.ui.screens.LearnHubScreen
import com.example.ui.screens.LearnRoutes
import com.example.ui.screens.LiveCameraScreen
import com.example.ui.screens.MyWordsScreen
import com.example.ui.screens.OnboardingOverlay
import com.example.ui.screens.PremiumLicenseScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.ReadingScreen
import com.example.ui.screens.TablesNumbersScreen
import com.example.ui.screens.TracingScreen
import com.example.ui.screens.VocabScreen
import com.example.ui.theme.PathshalaTheme

private data class TopTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

private object TabRoutes {
    const val LEARN = "tab_learn"
    const val CAMERA = "tab_camera"
    const val TUTOR = "tab_tutor"
    const val PROFILE = "tab_profile"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Must configure App Check BEFORE any Firebase.ai call or ViewModel creation.
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            val viewModel: PathshalaViewModel = viewModel()
            val progress by viewModel.progressState.collectAsStateWithLifecycle()

            PathshalaTheme(darkTheme = progress.readerDarkMode) {
                if (!progress.onboardingDone) {
                    OnboardingOverlay(
                        onComplete = { name, goal ->
                            viewModel.completeOnboarding(name, goal)
                        }
                    )
                } else {
                    PathshalaMainShell(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun PathshalaMainShell(viewModel: PathshalaViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val progress by viewModel.progressState.collectAsStateWithLifecycle()
    val savedWords by viewModel.savedWords.collectAsStateWithLifecycle()
    val letterProgress by viewModel.letterProgressList.collectAsStateWithLifecycle()
    val licenseRequests by viewModel.licenseRequests.collectAsStateWithLifecycle()
    val letters by viewModel.letters.collectAsStateWithLifecycle()
    val strokesMap by viewModel.strokesMap.collectAsStateWithLifecycle()
    val readingLevels by viewModel.readingLevels.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val englishBundle by viewModel.englishBundle.collectAsStateWithLifecycle()
    val phrases by viewModel.phrases.collectAsStateWithLifecycle()
    val dictionary by viewModel.dictionary.collectAsStateWithLifecycle()
    val ttsHighlight by viewModel.ttsHighlight.collectAsStateWithLifecycle()
    val speechEval by viewModel.speechEval.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val chatStreamingText by viewModel.chatStreamingText.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()
    val capturedBitmap by viewModel.capturedBitmap.collectAsStateWithLifecycle()
    val cameraAiState by viewModel.cameraAiState.collectAsStateWithLifecycle()
    val defineAiState by viewModel.defineAiState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        val msg = toastMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeToast()
        }
    }

    val tabs = remember {
        listOf(
            TopTab(TabRoutes.LEARN, "শিখুন", Icons.Default.School, "nav_tab_learn"),
            TopTab(TabRoutes.CAMERA, "ক্যামেরা", Icons.Default.CameraAlt, "nav_tab_camera"),
            TopTab(TabRoutes.TUTOR, "এআই শিক্ষক", Icons.Default.Psychology, "nav_tab_tutor"),
            TopTab(TabRoutes.PROFILE, "প্রোফাইল", Icons.Default.Person, "nav_tab_profile")
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in setOf(
        TabRoutes.LEARN,
        TabRoutes.CAMERA,
        TabRoutes.TUTOR,
        TabRoutes.PROFILE
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, maxLines = 1) },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = TabRoutes.LEARN
            ) {
                composable(TabRoutes.LEARN) {
                    LearnHubScreen(
                        progress = progress,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable(TabRoutes.CAMERA) {
                    LiveCameraScreen(
                        capturedBitmap = capturedBitmap,
                        cameraAiState = cameraAiState,
                        onAnalyzeBitmap = { bmp, prompt, speakAloud, speakEn ->
                            viewModel.analyzePhotoAndSpeak(bmp, prompt, speakAloud, speakEn)
                        },
                        onSpeakBangla = { viewModel.speakBangla(it) },
                        onSpeakEnglish = { viewModel.speakEnglish(it) },
                        onStopSpeak = { viewModel.stopSpeaking() },
                        onSaveWord = { w, m, p, ex ->
                            viewModel.saveWordToMyWords(w, m, p, ex, source = "camera")
                        },
                        onClearAnalysis = { viewModel.clearCameraAnalysis() }
                    )
                }

                composable(TabRoutes.TUTOR) {
                    AiTutorScreen(
                        messages = chatMessages,
                        streamingText = chatStreamingText,
                        errorMessage = chatError,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onSpeakBangla = { viewModel.speakBangla(it) },
                        onClearChat = { viewModel.clearChatHistory() }
                    )
                }

                composable(TabRoutes.PROFILE) {
                    ProfileScreen(
                        progress = progress,
                        letterProgress = letterProgress,
                        savedWords = savedWords,
                        onUpdateReaderPrefs = { dark, size -> viewModel.setReaderPreferences(dark, size) },
                        onOpenPremium = { navController.navigate(LearnRoutes.PREMIUM) },
                        onOpenAdminPanel = { navController.navigate(LearnRoutes.ADMIN) }
                    )
                }

                composable(LearnRoutes.ALPHABET) {
                    AlphabetScreen(
                        bundle = letters,
                        onSpeakBangla = { viewModel.speakBangla(it) },
                        onSpeakEnglish = { viewModel.speakEnglish(it) },
                        onSaveWord = { w, m, p ->
                            viewModel.saveWordToMyWords(w, m, p, source = "alphabet")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(LearnRoutes.TRACING) {
                    TracingScreen(
                        bundle = letters,
                        strokesMap = strokesMap,
                        onSpeakBangla = { viewModel.speakBangla(it) },
                        onSpeakEnglish = { viewModel.speakEnglish(it) },
                        onRecordResult = { ch, cat, acc ->
                            viewModel.recordLetterTraceResult(ch, cat, acc)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(LearnRoutes.READING) {
                    ReadingScreen(
                        levels = readingLevels,
                        stories = stories,
                        progress = progress,
                        highlightState = ttsHighlight,
                        speechEval = speechEval,
                        onSpeakBangla = { t, slow, track ->
                            viewModel.speakBangla(t, slow, track)
                        },
                        onStopSpeak = { viewModel.stopSpeaking() },
                        onStartListening = { expected ->
                            viewModel.speechRecognizer.startListening(expected, "bn-BD")
                        },
                        onClearSpeech = { viewModel.speechRecognizer.clear() },
                        onMarkLessonComplete = { lvl, id, total ->
                            viewModel.markReadingLessonComplete(lvl, id, total)
                        },
                        onCompleteStory = { viewModel.completeStory(it) },
                        onSaveWord = { w, m, p ->
                            viewModel.saveWordToMyWords(w, m, p, source = "reading")
                        },
                        onUpdateReaderPrefs = { dark, size ->
                            viewModel.setReaderPreferences(dark, size)
                        },
                        onOpenPremium = { navController.navigate(LearnRoutes.PREMIUM) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(LearnRoutes.ENGLISH) {
                    EnglishScreen(
                        bundle = englishBundle,
                        phrases = phrases,
                        progress = progress,
                        speechEval = speechEval,
                        onSpeakEnglish = { viewModel.speakEnglish(it) },
                        onSpeakBangla = { viewModel.speakBangla(it) },
                        onStartEnglishListening = { expected ->
                            viewModel.speechRecognizer.startListening(expected, "en-US")
                        },
                        onSaveWord = { w, m, p ->
                            viewModel.saveWordToMyWords(w, m, p, source = "english")
                        },
                        onOpenTracing = { navController.navigate(LearnRoutes.TRACING) },
                        onOpenPremium = { navController.navigate(LearnRoutes.PREMIUM) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(LearnRoutes.VOCAB) {
                    VocabScreen(
                        dictionary = dictionary,
                        defineAiState = defineAiState,
                        progress = progress,
                        onDefineOnline = { viewModel.defineWordOnline(it) },
                        onSpeakEnglish = { viewModel.speakEnglish(it) },
                        onSpeakBangla = { viewModel.speakBangla(it) },
                        onSaveWord = { w, m, p, ex ->
                            viewModel.saveWordToMyWords(w, m, p, ex, source = "vocab")
                        },
                        onOpenPremium = { navController.navigate(LearnRoutes.PREMIUM) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(LearnRoutes.QUIZ) {
                    QuizScreen(
                        bundle = letters,
                        progress = progress,
                        onSpeakBangla = { viewModel.speakBangla(it) },
                        onPlayChime = { viewModel.tts.playChime(it) },
                        onQuizFinished = { viewModel.recordQuizFinish(it) },
                        onOpenPremium = { navController.navigate(LearnRoutes.PREMIUM) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(LearnRoutes.MY_WORDS) {
                    MyWordsScreen(
                        words = savedWords,
                        onSpeakBangla = { viewModel.speakBangla(it) },
                        onSpeakEnglish = { viewModel.speakEnglish(it) },
                        onAddWord = { w, m, p, ex ->
                            viewModel.saveWordToMyWords(w, m, p, ex, source = "manual")
                        },
                        onToggleMastered = { viewModel.toggleWordMastered(it) },
                        onDeleteWord = { viewModel.deleteSavedWord(it) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(LearnRoutes.TABLES) {
                    TablesNumbersScreen(
                        onSpeakBangla = { viewModel.speakBangla(it) },
                        onSpeakEnglish = { viewModel.speakEnglish(it) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(LearnRoutes.PREMIUM) {
                    PremiumLicenseScreen(
                        progress = progress,
                        onSubmitRequest = { phone, trx, note ->
                            viewModel.submitBkashLicenseRequest(phone, trx, note)
                        },
                        onActivateKey = { key -> viewModel.activateWithLicenseKey(key) },
                        onShowMessage = { viewModel.showMessage(it) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(LearnRoutes.ADMIN) {
                    AdminPanelScreen(
                        progress = progress,
                        requests = licenseRequests,
                        savedWordsCount = savedWords.size,
                        tracedLettersCount = letterProgress.size,
                        onGenerateKey = { seed -> viewModel.adminGenerateKey(seed) },
                        onApproveRequest = { req -> viewModel.adminApproveRequest(req) },
                        onDeleteRequest = { id -> viewModel.adminDeleteRequest(id) },
                        onTogglePremium = { enabled -> viewModel.adminTogglePremium(enabled) },
                        onUpdateSettings = { pin, bkash, wa, fee ->
                            viewModel.adminUpdateSettings(pin, bkash, wa, fee)
                        },
                        onShowMessage = { viewModel.showMessage(it) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
