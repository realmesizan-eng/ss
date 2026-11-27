package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.DictionaryEntry
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "PathshalaAiRepo"

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String
)

data class DetectedWordItem(
    val word: String,
    val meaningBn: String,
    val pronunciationBn: String,
    val example: String
)

data class ImageAnalysisResult(
    val titleBn: String,
    val explanationBn: String,
    val explanationEn: String,
    val detectedWords: List<DetectedWordItem>
)

sealed interface AiResult<out T> {
    data class Success<T>(val data: T) : AiResult<T>
    data class Error(val message: String) : AiResult<Nothing>
}

class AiRepository {

    private val tutorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are 'শিখি এআই শিক্ষক' (Shikhi AI Teacher), a warm, patient, encouraging bilingual Bangla & English tutor for children and adult beginners in Bangladesh.
                    - Answer clearly in simple Bengali (with helpful English examples and Bangla-script pronunciations when teaching English).
                    - Keep answers concise, friendly, structured, and child-safe.
                    - When asked about a Bangla or English letter, word, spelling, or grammar rule, give a clear meaning, pronunciation in Bangla script, and 1-2 easy example sentences.
                    """.trimIndent()
                )
            }
        )
    }

    private val visionSchema = Schema.obj(
        properties = mapOf(
            "titleBn" to Schema.string(description = "Short Bangla title describing what is in the image"),
            "explanationBn" to Schema.string(description = "Friendly spoken-style 2-3 sentence explanation in Bengali of what the camera sees, helpful for a learner"),
            "explanationEn" to Schema.string(description = "Friendly 1-2 sentence explanation in simple English of what is in the image"),
            "detectedWords" to Schema.array(
                description = "Key objects or visible Bangla/English words found in the image (up to 8)",
                items = Schema.obj(
                    properties = mapOf(
                        "word" to Schema.string(description = "The English or Bangla word"),
                        "meaningBn" to Schema.string(description = "Simple Bengali meaning"),
                        "pronunciationBn" to Schema.string(description = "Pronunciation written in Bengali script"),
                        "example" to Schema.string(description = "Short example sentence")
                    )
                )
            )
        )
    )

    private val visionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = visionSchema
            }
        )
    }

    private val defineSchema = Schema.obj(
        properties = mapOf(
            "word" to Schema.string(description = "The queried word"),
            "bangla" to Schema.string(description = "Clear Bengali meaning"),
            "english" to Schema.string(description = "English word or equivalent"),
            "pronunciationBn" to Schema.string(description = "Pronunciation written in Bengali script"),
            "example" to Schema.string(description = "One simple example sentence")
        )
    )

    private val defineModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = defineSchema
            }
        )
    }

    fun streamTutorReply(
        history: List<ChatMessage>,
        userPrompt: String
    ): Flow<String> = flow {
        val chatHistory: List<Content> = history.map { msg ->
            content(role = if (msg.isUser) "user" else "model") {
                text(msg.text)
            }
        }
        val chat = tutorModel.startChat(history = chatHistory)
        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            chat.sendMessageStream(userPrompt).collect { chunk ->
                val part = chunk.text.orEmpty()
                if (part.isNotEmpty()) {
                    accumulated.append(part)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("এআই থেকে কোনো উত্তর পাওয়া যায়নি।")
        }
    }.catch { e ->
        if (e is CancellationException && e !is TimeoutCancellationException) throw e
        Log.e(TAG, "Tutor stream failed", e)
        throw e
    }

    suspend fun analyzeImage(bitmap: Bitmap, customPrompt: String): AiResult<ImageAnalysisResult> {
        return try {
            val scaled = scaleBitmapIfNeeded(bitmap, maxDim = 1024)
            val response = withTimeout(60_000L) {
                visionModel.generateContent(
                    content {
                        image(scaled)
                        text(
                            customPrompt.ifBlank {
                                "Look carefully at this photo from a learner's camera. Identify any printed Bangla/English text or everyday objects, explain what you see warmly in Bangla (for voice reading aloud) and simple English, and extract up to 8 useful vocabulary words with Bangla meaning and Bangla pronunciation."
                            }
                        )
                    }
                )
            }
            val raw = response.text ?: return AiResult.Error("ছবি বিশ্লেষণের উত্তর খালি এসেছে।")
            val json = JSONObject(raw)
            val wordsArr = json.optJSONArray("detectedWords")
            val words = mutableListOf<DetectedWordItem>()
            if (wordsArr != null) {
                for (i in 0 until wordsArr.length()) {
                    val o = wordsArr.getJSONObject(i)
                    words.add(
                        DetectedWordItem(
                            word = o.optString("word", ""),
                            meaningBn = o.optString("meaningBn", ""),
                            pronunciationBn = o.optString("pronunciationBn", ""),
                            example = o.optString("example", "")
                        )
                    )
                }
            }
            AiResult.Success(
                ImageAnalysisResult(
                    titleBn = json.optString("titleBn", "ছবি বিশ্লেষণ"),
                    explanationBn = json.optString("explanationBn", ""),
                    explanationEn = json.optString("explanationEn", ""),
                    detectedWords = words.filter { it.word.isNotBlank() }
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Image analysis timed out", e)
            AiResult.Error("ছবি বিশ্লেষণে ৬০ সেকেন্ডের বেশি সময় লেগেছে। আবার চেষ্টা করুন।")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Image analysis failed", e)
            AiResult.Error(e.localizedMessage ?: "ছবি বিশ্লেষণ ব্যর্থ হয়েছে। ইন্টারনেট সংযোগ পরীক্ষা করুন।")
        }
    }

    suspend fun defineWord(word: String): AiResult<DictionaryEntry> {
        return try {
            val response = withTimeout(45_000L) {
                defineModel.generateContent(
                    "Define the Bangla or English word '$word' for a beginner learner in Bangladesh. Provide Bengali meaning, English meaning/equivalent, pronunciation in Bengali script, and one easy example sentence."
                )
            }
            val raw = response.text ?: return AiResult.Error("শব্দের অর্থ পাওয়া যায়নি।")
            val json = JSONObject(raw)
            AiResult.Success(
                DictionaryEntry(
                    en = json.optString("english", word),
                    bn = json.optString("bangla", ""),
                    pronBn = json.optString("pronunciationBn", ""),
                    example = json.optString("example", ""),
                    fromServer = true
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Define word timed out", e)
            AiResult.Error("শব্দ খুঁজতে সময় বেশি লাগছে। আবার চেষ্টা করুন।")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Define word failed", e)
            AiResult.Error(e.localizedMessage ?: "অনলাইন শব্দ খোঁজা ব্যর্থ হয়েছে।")
        }
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
        val nw = (w * ratio).toInt().coerceAtLeast(1)
        val nh = (h * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, nw, nh, true)
    }
}
