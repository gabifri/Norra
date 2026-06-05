package com.example.data

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

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val emotionType: String, // "default", "colere", "triste", "vide", "seul", "peur", "honte", "idk", "autre"
    val intensity: Int, // 1 to 5
    val note: String
)

@Entity(tableName = "contact_people")
data class ContactPerson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val relation: String, // "Ami", "Thérapeute", "Famille", "Soutien"
    val isCustom: Boolean = true
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosageMg: Int,
    val pillCount: Int,
    val timeToTake: String, // e.g. "08:00"
    val reminderEnabled: Boolean = true,
    val intakes: String = "" // "time,count;time,count" - overrides timeToTake/pillCount if not empty
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val dateString: String, // "YYYY-MM-DD"
    val isTaken: Boolean,
    val intakeIndex: Int = 0
)

@Entity(tableName = "crisis_logs")
data class CrisisLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val exerciseId: String, // "bubble_burst", "box_breathing", etc.
    val worked: Boolean
)

@Dao
interface NorraDao {
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<MoodEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntry)

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteMoodById(id: Long)

    @Query("SELECT * FROM contact_people")
    fun getAllContacts(): Flow<List<ContactPerson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactPerson)

    @Query("DELETE FROM contact_people WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("SELECT * FROM medications")
    fun getAllMedications(): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Long)

    @Query("SELECT * FROM medication_logs WHERE dateString = :dateString")
    fun getMedicationLogsByDate(dateString: String): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId AND dateString = :dateString AND intakeIndex = :intakeIndex LIMIT 1")
    suspend fun getMedicationLogSync(medicationId: Long, dateString: String, intakeIndex: Int): MedicationLog?

    @Query("SELECT * FROM medication_logs")
    fun getAllMedicationLogs(): Flow<List<MedicationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(log: MedicationLog)

    @Query("SELECT * FROM crisis_logs ORDER BY timestamp DESC")
    fun getAllCrisisLogs(): Flow<List<CrisisLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrisisLog(log: CrisisLog)

    @Query("DELETE FROM crisis_logs WHERE id = :id")
    suspend fun deleteCrisisLogById(id: Long)
}

@Database(entities = [MoodEntry::class, ContactPerson::class, Medication::class, MedicationLog::class, CrisisLog::class], version = 4, exportSchema = false)
abstract class NorraDatabase : RoomDatabase() {
    abstract fun norraDao(): NorraDao
}

class NorraRepository(private val dao: NorraDao) {
    val allMoods: Flow<List<MoodEntry>> = dao.getAllMoods()
    val allContacts: Flow<List<ContactPerson>> = dao.getAllContacts()
    val allMedications: Flow<List<Medication>> = dao.getAllMedications()
    val allMedicationLogs: Flow<List<MedicationLog>> = dao.getAllMedicationLogs()
    val allCrisisLogs: Flow<List<CrisisLog>> = dao.getAllCrisisLogs()

    suspend fun insertMood(mood: MoodEntry) = dao.insertMood(mood)
    suspend fun deleteMoodById(id: Long) = dao.deleteMoodById(id)

    suspend fun insertContact(contact: ContactPerson) = dao.insertContact(contact)
    suspend fun deleteContactById(id: Long) = dao.deleteContactById(id)

    suspend fun insertMedication(medication: Medication): Long = dao.insertMedication(medication)
    suspend fun deleteMedicationById(id: Long) = dao.deleteMedicationById(id)

    fun getMedicationLogsByDate(dateString: String) = dao.getMedicationLogsByDate(dateString)
    suspend fun getMedicationLogSync(medicationId: Long, dateString: String, intakeIndex: Int) = dao.getMedicationLogSync(medicationId, dateString, intakeIndex)
    suspend fun insertMedicationLog(log: MedicationLog) = dao.insertMedicationLog(log)

    suspend fun insertCrisisLog(log: CrisisLog) = dao.insertCrisisLog(log)
    suspend fun deleteCrisisLogById(id: Long) = dao.deleteCrisisLogById(id)
}

object DatabaseProvider {
    private var db: NorraDatabase? = null

    private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE medications ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE medications ADD COLUMN intakes TEXT NOT NULL DEFAULT ''")
        }
    }

    fun getDatabase(context: Context): NorraDatabase {
        return db ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                NorraDatabase::class.java,
                "norra_database"
            )
            .addMigrations(MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
            db = instance
            instance
        }
    }
}
