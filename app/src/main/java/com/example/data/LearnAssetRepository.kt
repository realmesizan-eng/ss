package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val TAG = "LearnAssetRepo"

data class LetterItem(
    val char: String,
    val nameBn: String,
    val pronEn: String,
    val wordBn: String,
    val wordEn: String,
    val emoji: String
)

data class KaarItem(
    val symbol: String,
    val nameBn: String,
    val baseWithKo: String,
    val pronEn: String,
    val exampleWord: String
)

data class FolaItem(
    val symbol: String,
    val nameBn: String,
    val parts: String,
    val example: String,
    val pronEn: String
)

data class JuktobornoItem(
    val conjunct: String,
    val parts: String,
    val example: String,
    val pronEn: String
)

data class LettersBundle(
    val vowels: List<LetterItem> = emptyList(),
    val consonants: List<LetterItem> = emptyList(),
    val digits: List<LetterItem> = emptyList(),
    val kaars: List<KaarItem> = emptyList(),
    val folas: List<FolaItem> = emptyList(),
    val juktoborno: List<JuktobornoItem> = emptyList()
)

data class ReadingWord(
    val word: String,
    val meaning: String,
    val emoji: String
)

data class ReadingLesson(
    val id: String,
    val title: String,
    val text: String,
    val words: List<ReadingWord>
)

data class ReadingLevelData(
    val level: Int,
    val titleBn: String,
    val isPremium: Boolean,
    val lessons: List<ReadingLesson>
)

data class StoryItem(
    val id: String,
    val title: String,
    val imageKey: String,
    val lines: List<String>,
    val moral: String
)

data class PhonicsItem(
    val upper: String,
    val lower: String,
    val word: String,
    val bnMeaning: String,
    val pronBn: String,
    val emoji: String
)

data class TopicWord(
    val en: String,
    val bn: String,
    val pronBn: String,
    val emoji: String
)

data class EnglishTopic(
    val id: String,
    val titleBn: String,
    val emoji: String,
    val isPremium: Boolean,
    val words: List<TopicWord>
)

data class GrammarExample(
    val en: String,
    val bn: String,
    val pronBn: String
)

data class GrammarLesson(
    val id: String,
    val titleBn: String,
    val ruleBn: String,
    val examples: List<GrammarExample>
)

data class EnglishReadingItem(
    val id: String,
    val title: String,
    val text: String,
    val bnTranslation: String
)

data class EnglishBundle(
    val phonics: List<PhonicsItem> = emptyList(),
    val topics: List<EnglishTopic> = emptyList(),
    val grammar: List<GrammarLesson> = emptyList(),
    val reading: List<EnglishReadingItem> = emptyList()
)

data class PhraseItem(
    val en: String,
    val bn: String,
    val pronBn: String,
    val category: String
)

data class DictionaryEntry(
    val en: String,
    val bn: String,
    val pronBn: String,
    val example: String,
    val fromServer: Boolean = false
)

data class StrokePoint(val x: Float, val y: Float)

class LearnAssetRepository(private val context: Context) {

    private var cachedLetters: LettersBundle? = null
    private var cachedStrokes: Map<String, List<List<StrokePoint>>>? = null
    private var cachedReadingLevels: List<ReadingLevelData>? = null
    private var cachedStories: List<StoryItem>? = null
    private var cachedEnglish: EnglishBundle? = null
    private var cachedPhrases: List<PhraseItem>? = null
    private var cachedDictionary: List<DictionaryEntry>? = null

    private fun readAssetText(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }

    suspend fun getLetters(): LettersBundle = withContext(Dispatchers.IO) {
        cachedLetters?.let { return@withContext it }
        try {
            val root = JSONObject(readAssetText("learn/letters.json"))
            val vowels = parseLettersArray(root.optJSONArray("vowels"))
            val consonants = parseLettersArray(root.optJSONArray("consonants"))
            val digits = parseLettersArray(root.optJSONArray("digits"))

            val kaars = mutableListOf<KaarItem>()
            val kaarsArr = root.optJSONArray("kaars")
            if (kaarsArr != null) {
                for (i in 0 until kaarsArr.length()) {
                    val o = kaarsArr.getJSONObject(i)
                    kaars.add(
                        KaarItem(
                            symbol = o.getString("symbol"),
                            nameBn = o.getString("nameBn"),
                            baseWithKo = o.getString("baseWithKo"),
                            pronEn = o.getString("pronEn"),
                            exampleWord = o.getString("exampleWord")
                        )
                    )
                }
            }

            val folas = mutableListOf<FolaItem>()
            val folasArr = root.optJSONArray("folas")
            if (folasArr != null) {
                for (i in 0 until folasArr.length()) {
                    val o = folasArr.getJSONObject(i)
                    folas.add(
                        FolaItem(
                            symbol = o.getString("symbol"),
                            nameBn = o.getString("nameBn"),
                            parts = o.getString("parts"),
                            example = o.getString("example"),
                            pronEn = o.getString("pronEn")
                        )
                    )
                }
            }

            val juktos = mutableListOf<JuktobornoItem>()
            val jukArr = root.optJSONArray("juktoborno")
            if (jukArr != null) {
                for (i in 0 until jukArr.length()) {
                    val o = jukArr.getJSONObject(i)
                    juktos.add(
                        JuktobornoItem(
                            conjunct = o.getString("conjunct"),
                            parts = o.getString("parts"),
                            example = o.getString("example"),
                            pronEn = o.getString("pronEn")
                        )
                    )
                }
            }

            LettersBundle(vowels, consonants, digits, kaars, folas, juktos).also {
                cachedLetters = it
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading letters.json", e)
            LettersBundle()
        }
    }

    private fun parseLettersArray(arr: org.json.JSONArray?): List<LetterItem> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            LetterItem(
                char = o.getString("char"),
                nameBn = o.getString("nameBn"),
                pronEn = o.getString("pronEn"),
                wordBn = o.getString("wordBn"),
                wordEn = o.getString("wordEn"),
                emoji = o.optString("emoji", "✨")
            )
        }
    }

    suspend fun getStrokesMap(): Map<String, List<List<StrokePoint>>> = withContext(Dispatchers.IO) {
        cachedStrokes?.let { return@withContext it }
        val result = mutableMapOf<String, List<List<StrokePoint>>>()
        try {
            val root = JSONObject(readAssetText("learn/strokes.json"))
            val strokesObj = root.getJSONObject("strokes")
            val keys = strokesObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val strokesJson = strokesObj.getJSONArray(key)
                val strokes = mutableListOf<List<StrokePoint>>()
                for (s in 0 until strokesJson.length()) {
                    val ptsJson = strokesJson.getJSONArray(s)
                    val pts = mutableListOf<StrokePoint>()
                    for (p in 0 until ptsJson.length()) {
                        val pt = ptsJson.getJSONArray(p)
                        pts.add(StrokePoint(pt.getDouble(0).toFloat(), pt.getDouble(1).toFloat()))
                    }
                    if (pts.isNotEmpty()) strokes.add(pts)
                }
                result[key] = strokes
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading strokes.json", e)
        }
        cachedStrokes = result
        result
    }

    suspend fun getReadingLevels(): List<ReadingLevelData> = withContext(Dispatchers.IO) {
        cachedReadingLevels?.let { return@withContext it }
        val levels = (1..5).mapNotNull { lvl ->
            try {
                val root = JSONObject(readAssetText("learn/reading_level$lvl.json"))
                val lessonsArr = root.getJSONArray("lessons")
                val lessons = (0 until lessonsArr.length()).map { i ->
                    val lo = lessonsArr.getJSONObject(i)
                    val wArr = lo.optJSONArray("words")
                    val words = if (wArr != null) {
                        (0 until wArr.length()).map { wi ->
                            val wo = wArr.getJSONObject(wi)
                            ReadingWord(
                                word = wo.getString("word"),
                                meaning = wo.optString("meaning", ""),
                                emoji = wo.optString("emoji", "📖")
                            )
                        }
                    } else emptyList()
                    ReadingLesson(
                        id = lo.getString("id"),
                        title = lo.getString("title"),
                        text = lo.getString("text"),
                        words = words
                    )
                }
                ReadingLevelData(
                    level = root.optInt("level", lvl),
                    titleBn = root.getString("titleBn"),
                    isPremium = root.optBoolean("isPremium", lvl >= 3),
                    lessons = lessons
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed loading reading_level$lvl.json", e)
                null
            }
        }
        cachedReadingLevels = levels
        levels
    }

    suspend fun getStories(): List<StoryItem> = withContext(Dispatchers.IO) {
        cachedStories?.let { return@withContext it }
        try {
            val root = JSONObject(readAssetText("learn/stories.json"))
            val arr = root.getJSONArray("stories")
            val list = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val lArr = o.getJSONArray("lines")
                val lines = (0 until lArr.length()).map { li -> lArr.getString(li) }
                StoryItem(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    imageKey = o.optString("imageKey", "img_story_nature"),
                    lines = lines,
                    moral = o.optString("moral", "")
                )
            }
            cachedStories = list
            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading stories.json", e)
            emptyList()
        }
    }

    suspend fun getEnglishBundle(): EnglishBundle = withContext(Dispatchers.IO) {
        cachedEnglish?.let { return@withContext it }
        try {
            val root = JSONObject(readAssetText("learn/words_en.json"))
            val phonicsArr = root.getJSONArray("phonics")
            val phonics = (0 until phonicsArr.length()).map { i ->
                val o = phonicsArr.getJSONObject(i)
                PhonicsItem(
                    upper = o.getString("upper"),
                    lower = o.getString("lower"),
                    word = o.getString("word"),
                    bnMeaning = o.getString("bnMeaning"),
                    pronBn = o.getString("pronBn"),
                    emoji = o.optString("emoji", "🔤")
                )
            }

            val topicsArr = root.getJSONArray("topics")
            val topics = mutableListOf<EnglishTopic>()
            for (i in 0 until topicsArr.length()) {
                val o = topicsArr.getJSONObject(i)
                val wArr = o.getJSONArray("words")
                val words = (0 until wArr.length()).map { wi ->
                    val wo = wArr.getJSONObject(wi)
                    TopicWord(
                        en = wo.getString("en"),
                        bn = wo.getString("bn"),
                        pronBn = wo.getString("pronBn"),
                        emoji = wo.optString("emoji", "✨")
                    )
                }
                topics.add(
                    EnglishTopic(
                        id = o.getString("id"),
                        titleBn = o.getString("titleBn"),
                        emoji = o.optString("emoji", "📘"),
                        isPremium = o.optBoolean("isPremium", true),
                        words = words
                    )
                )
            }
            // Add Numbers 1-100 topic dynamically
            topics.add(
                EnglishTopic(
                    id = "numbers_1_100",
                    titleBn = "সংখ্যা ১-১০০ (Numbers)",
                    emoji = "🔢",
                    isPremium = true,
                    words = generateNumbers1To100()
                )
            )

            val gArr = root.getJSONArray("grammar")
            val grammar = (0 until gArr.length()).map { i ->
                val o = gArr.getJSONObject(i)
                val exArr = o.getJSONArray("examples")
                val examples = (0 until exArr.length()).map { ei ->
                    val eo = exArr.getJSONObject(ei)
                    GrammarExample(
                        en = eo.getString("en"),
                        bn = eo.getString("bn"),
                        pronBn = eo.getString("pronBn")
                    )
                }
                GrammarLesson(
                    id = o.getString("id"),
                    titleBn = o.getString("titleBn"),
                    ruleBn = o.getString("ruleBn"),
                    examples = examples
                )
            }

            val rArr = root.getJSONArray("englishReading")
            val reading = (0 until rArr.length()).map { i ->
                val o = rArr.getJSONObject(i)
                EnglishReadingItem(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    text = o.getString("text"),
                    bnTranslation = o.getString("bnTranslation")
                )
            }

            EnglishBundle(phonics, topics, grammar, reading).also { cachedEnglish = it }
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading words_en.json", e)
            EnglishBundle()
        }
    }

    suspend fun getPhrases(): List<PhraseItem> = withContext(Dispatchers.IO) {
        cachedPhrases?.let { return@withContext it }
        try {
            val root = JSONObject(readAssetText("learn/phrases.json"))
            val arr = root.getJSONArray("phrases")
            val list = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                PhraseItem(
                    en = o.getString("en"),
                    bn = o.getString("bn"),
                    pronBn = o.getString("pronBn"),
                    category = o.optString("category", "Daily")
                )
            }
            cachedPhrases = list
            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading phrases.json", e)
            emptyList()
        }
    }

    suspend fun getDictionary(): List<DictionaryEntry> = withContext(Dispatchers.IO) {
        cachedDictionary?.let { return@withContext it }
        try {
            val root = JSONObject(readAssetText("learn/dictionary_500.json"))
            val arr = root.getJSONArray("entries")
            val list = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                DictionaryEntry(
                    en = o.getString("en"),
                    bn = o.getString("bn"),
                    pronBn = o.getString("pronBn"),
                    example = o.optString("ex", "")
                )
            }
            cachedDictionary = list
            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading dictionary_500.json", e)
            emptyList()
        }
    }

    private fun generateNumbers1To100(): List<TopicWord> {
        val onesEn = listOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine")
        val teensEn = listOf("Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
        val tensEn = listOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")
        val onesPron = listOf("", "ওয়ান", "টু", "থ্রি", "ফোর", "ফাইভ", "সিক্স", "সেভেন", "এইট", "নাইন")
        val teensPron = listOf("টেন", "ইলেভেন", "টুয়েলভ", "থার্টিন", "ফোরটিন", "ফিফটিন", "সিক্সটিন", "সেভেনটিন", "এইটিন", "নাইনটিন")
        val tensPron = listOf("", "", "টুয়েন্টি", "থার্টি", "ফর্টি", "ফিফটি", "সিক্সটি", "সেভেন্টি", "এইটি", "নাইন্টি")
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

        fun toBnDigits(n: Int): String = n.toString().map { bnDigits[it - '0'] }.joinToString("")

        return (1..100).map { n ->
            val enWord = when {
                n == 100 -> "One Hundred"
                n < 10 -> onesEn[n]
                n < 20 -> teensEn[n - 10]
                n % 10 == 0 -> tensEn[n / 10]
                else -> "${tensEn[n / 10]}-${onesEn[n % 10]}"
            }
            val pron = when {
                n == 100 -> "ওয়ান হান্ড্রেড"
                n < 10 -> onesPron[n]
                n < 20 -> teensPron[n - 10]
                n % 10 == 0 -> tensPron[n / 10]
                else -> "${tensPron[n / 10]} ${onesPron[n % 10]}"
            }
            TopicWord(
                en = "$n — $enWord",
                bn = "বাংলা: ${toBnDigits(n)}",
                pronBn = pron,
                emoji = "🔢"
            )
        }
    }
}
