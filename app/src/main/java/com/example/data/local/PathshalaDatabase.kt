package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_words")
data class SavedWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val meaningBn: String,
    val pronunciationBn: String,
    val example: String = "",
    val source: String = "manual",
    val isMastered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "letter_progress")
data class LetterProgressEntity(
    @PrimaryKey val letterChar: String,
    val category: String,
    val bestStars: Int = 0,
    val attempts: Int = 0,
    val mistakes: Int = 0,
    val bestAccuracy: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "license_requests")
data class LicenseRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val buyerPhone: String,
    val trxId: String,
    val amountTaka: Int = 100,
    val status: String = "PENDING", // PENDING, APPROVED
    val generatedKey: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface SavedWordDao {
    @Query("SELECT * FROM saved_words ORDER BY createdAt DESC")
    fun observeAllWords(): Flow<List<SavedWordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: SavedWordEntity)

    @Query("UPDATE saved_words SET isMastered = :mastered WHERE id = :id")
    suspend fun updateMastered(id: Int, mastered: Boolean)

    @Query("DELETE FROM saved_words WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM saved_words WHERE LOWER(word) = LOWER(:word)")
    suspend fun countByWord(word: String): Int
}

@Dao
interface LetterProgressDao {
    @Query("SELECT * FROM letter_progress ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<LetterProgressEntity>>

    @Query("SELECT * FROM letter_progress WHERE letterChar = :letterChar LIMIT 1")
    suspend fun getByChar(letterChar: String): LetterProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LetterProgressEntity)
}

@Dao
interface LicenseRequestDao {
    @Query("SELECT * FROM license_requests ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<LicenseRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: LicenseRequestEntity)

    @Query("UPDATE license_requests SET status = :status, generatedKey = :key WHERE id = :id")
    suspend fun approve(id: Int, status: String, key: String)

    @Query("DELETE FROM license_requests WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Database(
    entities = [SavedWordEntity::class, LetterProgressEntity::class, LicenseRequestEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PathshalaDatabase : RoomDatabase() {
    abstract fun savedWordDao(): SavedWordDao
    abstract fun letterProgressDao(): LetterProgressDao
    abstract fun licenseRequestDao(): LicenseRequestDao

    companion object {
        @Volatile
        private var INSTANCE: PathshalaDatabase? = null

        fun getInstance(context: Context): PathshalaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PathshalaDatabase::class.java,
                    "pathshala_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
