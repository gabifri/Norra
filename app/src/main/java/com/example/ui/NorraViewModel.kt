package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ContactPerson
import com.example.data.MoodEntry
import com.example.data.NorraRepository
import com.example.api.GeminiRepository
import com.example.api.Content
import com.example.api.Part
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "norra"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class NorraViewModel(private val repository: NorraRepository, private val sharedPreferences: android.content.SharedPreferences) : ViewModel() {

    private val geminiRepository = GeminiRepository()

    // Observe Room DB flows
    val moodHistory: StateFlow<List<MoodEntry>> = repository.allMoods
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customContacts: StateFlow<List<ContactPerson>> = repository.allContacts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val medications: StateFlow<List<com.example.data.Medication>> = repository.allMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allMedicationLogs: StateFlow<List<com.example.data.MedicationLog>> = repository.allMedicationLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val crisisHistory: StateFlow<List<com.example.data.CrisisLog>> = repository.allCrisisLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getMedicationLogsByDate(dateString: String) = repository.getMedicationLogsByDate(dateString)

    fun addMedication(name: String, dosageMg: Int, pillCount: Int, timeToTake: String, reminderEnabled: Boolean = true, intakes: String = "", onAdded: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insertMedication(com.example.data.Medication(
                name = name,
                dosageMg = dosageMg,
                pillCount = pillCount,
                timeToTake = timeToTake,
                reminderEnabled = reminderEnabled,
                intakes = intakes
            ))
            onAdded(id)
        }
    }

    fun updateMedication(id: Long, name: String, dosageMg: Int, pillCount: Int, timeToTake: String, reminderEnabled: Boolean = true, intakes: String = "", onUpdated: () -> Unit) {
        viewModelScope.launch {
            repository.insertMedication(com.example.data.Medication(
                id = id,
                name = name,
                dosageMg = dosageMg,
                pillCount = pillCount,
                timeToTake = timeToTake,
                reminderEnabled = reminderEnabled,
                intakes = intakes
            ))
            onUpdated()
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            repository.deleteMedicationById(id)
        }
    }

    fun addCrisisLog(exerciseId: String, worked: Boolean) {
        viewModelScope.launch {
            val log = com.example.data.CrisisLog(
                timestamp = System.currentTimeMillis(),
                exerciseId = exerciseId,
                worked = worked
            )
            repository.insertCrisisLog(log)
        }
    }

    fun deleteCrisisLog(id: Long) {
        viewModelScope.launch {
            repository.deleteCrisisLogById(id)
        }
    }

    fun toggleMedicationLog(medicationId: Long, dateString: String, isTaken: Boolean, intakeIndex: Int = 0) {
        viewModelScope.launch {
            val existingLog = repository.getMedicationLogSync(medicationId, dateString, intakeIndex)
            if (existingLog != null) {
                repository.insertMedicationLog(existingLog.copy(isTaken = isTaken))
            } else {
                repository.insertMedicationLog(com.example.data.MedicationLog(
                    medicationId = medicationId,
                    dateString = dateString,
                    isTaken = isTaken,
                    intakeIndex = intakeIndex
                ))
            }
        }
    }

    // Current tab index
    private val _currentTab = MutableStateFlow(0) // 0: Norra & SOS, 1: Suivi, 2: Exercices, 3: Contacts
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _activeExercise = MutableStateFlow<String?>(null)
    val activeExercise: StateFlow<String?> = _activeExercise.asStateFlow()

    private val _directLaunchExercise = MutableStateFlow(false)
    val directLaunchExercise: StateFlow<Boolean> = _directLaunchExercise.asStateFlow()

    fun setActiveExercise(exerciseId: String?, directLaunch: Boolean = false) {
        _activeExercise.value = exerciseId
        _directLaunchExercise.value = directLaunch
    }

    fun selectTab(index: Int) {
        _currentTab.value = index
    }

    // Favorite Exercises
    private val _favoriteExercises = MutableStateFlow(
        sharedPreferences.getString("favorite_exercises_csv", "")
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toSet() ?: emptySet()
    )
    val favoriteExercises: StateFlow<Set<String>> = _favoriteExercises.asStateFlow()

    fun toggleFavoriteExercise(exerciseId: String) {
        val current = _favoriteExercises.value.toMutableSet()
        if (current.contains(exerciseId)) {
            current.remove(exerciseId)
        } else {
            current.add(exerciseId)
        }
        _favoriteExercises.value = current
        sharedPreferences.edit()
            .putString("favorite_exercises_csv", current.joinToString(","))
            .apply()
    }

    // Global Sound settings
    private val _globalSoundEnabled = MutableStateFlow(sharedPreferences.getBoolean("global_sound", true))
    val globalSoundEnabled: StateFlow<Boolean> = _globalSoundEnabled.asStateFlow()

    fun setGlobalSoundEnabled(enabled: Boolean) {
        _globalSoundEnabled.value = enabled
        sharedPreferences.edit().putBoolean("global_sound", enabled).apply()
    }

    // Psychologist appointments and notes
    private val _psyAppointmentDate = MutableStateFlow(sharedPreferences.getString("psy_appointment_date", "") ?: "")
    val psyAppointmentDate: StateFlow<String> = _psyAppointmentDate.asStateFlow()

    private val _psyNotes = MutableStateFlow(sharedPreferences.getString("psy_notes_text", "") ?: "")
    val psyNotes: StateFlow<String> = _psyNotes.asStateFlow()

    private val _psyNotesListJson = MutableStateFlow(sharedPreferences.getString("psy_notes_list_json", "[]") ?: "[]")
    val psyNotesListJson: StateFlow<String> = _psyNotesListJson.asStateFlow()

    private val _psyNotesExpanded = MutableStateFlow(false)
    val psyNotesExpanded: StateFlow<Boolean> = _psyNotesExpanded.asStateFlow()

    fun setPsyNotesExpanded(expanded: Boolean) {
        _psyNotesExpanded.value = expanded
    }

    fun savePsyAppointment(context: android.content.Context, dateTime: String) {
        _psyAppointmentDate.value = dateTime
        sharedPreferences.edit().putString("psy_appointment_date", dateTime).apply()
        if (dateTime.isNotBlank()) {
            com.example.util.NotificationHelper.schedulePsychAppointmentAlarms(context, dateTime)
        }
    }

    fun savePsyNotes(text: String) {
        _psyNotes.value = text
        sharedPreferences.edit().putString("psy_notes_text", text).apply()
    }

    fun savePsyNotesListJson(json: String) {
        _psyNotesListJson.value = json
        sharedPreferences.edit().putString("psy_notes_list_json", json).apply()
    }

    // Login & Onboarding States
    private val _isUserLoggedIn = MutableStateFlow(sharedPreferences.getBoolean("is_logged_in", false))
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _globalVibrationEnabled = MutableStateFlow(sharedPreferences.getBoolean("global_vibration", true))
    val globalVibrationEnabled: StateFlow<Boolean> = _globalVibrationEnabled.asStateFlow()

    fun setGlobalVibrationEnabled(enabled: Boolean) {
        _globalVibrationEnabled.value = enabled
        sharedPreferences.edit().putBoolean("global_vibration", enabled).apply()
    }

    private val _globalReminderEnabled = MutableStateFlow(sharedPreferences.getBoolean("global_med_reminder", true))
    val globalReminderEnabled: StateFlow<Boolean> = _globalReminderEnabled.asStateFlow()

    fun setGlobalReminderEnabled(enabled: Boolean) {
        _globalReminderEnabled.value = enabled
        sharedPreferences.edit().putBoolean("global_med_reminder", enabled).apply()
    }

    private val _globalReminderTime = MutableStateFlow(sharedPreferences.getString("global_med_time", "08:00") ?: "08:00")
    val globalReminderTime: StateFlow<String> = _globalReminderTime.asStateFlow()

    fun setGlobalReminderTime(time: String) {
        _globalReminderTime.value = time
        sharedPreferences.edit().putString("global_med_time", time).apply()
    }

    private val _userEmail = MutableStateFlow(sharedPreferences.getString("user_email", "") ?: "")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow(sharedPreferences.getString("user_name", "Perrine") ?: "Perrine")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _whyUseNorra = MutableStateFlow("Gérer mes émotions")
    val whyUseNorra: StateFlow<String> = _whyUseNorra.asStateFlow()

    private val _diagnosedDisorder = MutableStateFlow("Aucun diagnostic particulier")
    val diagnosedDisorder: StateFlow<String> = _diagnosedDisorder.asStateFlow()

    private val _referredBy = MutableStateFlow("Recherche personnelle")
    val referredBy: StateFlow<String> = _referredBy.asStateFlow()

    fun updateUserName(name: String) {
        _userName.value = name
        sharedPreferences.edit().putString("user_name", name).apply()
    }

    fun setLoginState(loggedIn: Boolean, email: String = "", name: String = "Perrine") {
        _isUserLoggedIn.value = loggedIn
        _userEmail.value = email
        _userName.value = name
        
        sharedPreferences.edit()
            .putBoolean("is_logged_in", loggedIn)
            .putString("user_email", email)
            .putString("user_name", name)
            .apply()
        if (loggedIn && email.isNotBlank()) {
            // No pre-seeding of any data when a new account is created or logged-in, ensuring it starts completely empty!
            _whyUseNorra.value = "Gérer mes émotions"
            _diagnosedDisorder.value = "Aucun diagnostic particulier"
            _referredBy.value = "Recherche personnelle"
        }
    }

    fun updateQcm(why: String, disorder: String, referral: String) {
        _whyUseNorra.value = why
        _diagnosedDisorder.value = disorder
        _referredBy.value = referral
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun setUserEmail(email: String) {
        _userEmail.value = email
    }

    // Active Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("norra", "Bonjour, je suis Norra. 💜 Comment te sens-tu en cet instant ? Sache que tu n'es pas seul(e). Je suis là pour t'écouter, sans aucun jugement.")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage("user", text)
        _chatMessages.value = _chatMessages.value + userMsg

        _chatLoading.value = true

        viewModelScope.launch {
            // Compile history with screenplay-style context to ensure perfect API response
            val compiledContext = buildString {
                append("La discussion se déroule en direct. Voici les derniers échanges pour le contexte :\n\n")
                _chatMessages.value.takeLast(6).forEach { msg ->
                    if (msg.sender == "user") {
                        append("Utilisateur : ${msg.text}\n")
                    } else {
                        append("Norra : ${msg.text}\n")
                    }
                }
                append("\nUtilisateur : $text\n")
                append("S'il te plaît, réponds chaleureusement en tant que Norra :")
            }

            val apiHistory = listOf(
                Content(parts = listOf(Part(text = compiledContext)))
            )

            val reply = geminiRepository.generateResponse(apiHistory)
            _chatMessages.value = _chatMessages.value + ChatMessage("norra", reply)
            _chatLoading.value = false
        }
    }

    // Clear active chat conversation
    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage("norra", "J'ai effacé notre discussion précédente pour que tu te sentes l'esprit plus léger. Comment puis-je t'accompagner maintenant ? 🌸")
        )
    }

    // Logging daily emotions
    fun addMoodEntry(emotion: String, intensity: Int, note: String) {
        viewModelScope.launch {
            val entry = MoodEntry(
                timestamp = System.currentTimeMillis(),
                emotionType = emotion.lowercase(),
                intensity = intensity,
                note = note
            )
            repository.insertMood(entry)
        }
    }

    fun deleteMoodEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteMoodById(id)
        }
    }

    // Contact integration
    fun addContact(name: String, phone: String, relation: String) {
        viewModelScope.launch {
            repository.insertContact(ContactPerson(name = name, phone = phone, relation = relation, isCustom = true))
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            repository.deleteContactById(id)
        }
    }

    // Seeds default contacts if database is empty
    fun seedDefaultContactsIfEmpty() {
        viewModelScope.launch {
            // Check will be handled dynamically or in initial view
        }
    }

    fun resetAllUserData(context: android.content.Context) {
        viewModelScope.launch {
            // Cancel scheduled notification/appointment alarms
            com.example.util.NotificationHelper.cancelAllAlarms(context)

            // Clear Room database entirely using Room's built-in clearAllTables in IO dispatcher
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.data.DatabaseProvider.getDatabase(context).clearAllTables()
                }
            } catch (e: Exception) {
                android.util.Log.e("NorraVM", "Failed clearAllTables", e)
            }

            // Clear all SharedPreferences values
            sharedPreferences.edit().clear().apply()

            // Reset all local StateFlows to their clean/default values
            _isUserLoggedIn.value = false
            _userEmail.value = ""
            _userName.value = "Perrine"
            _whyUseNorra.value = "Gérer mes émotions"
            _diagnosedDisorder.value = "Aucun diagnostic particulier"
            _referredBy.value = "Recherche personnelle"
            _favoriteExercises.value = emptySet()
            _globalSoundEnabled.value = true
            _globalVibrationEnabled.value = true
            _globalReminderEnabled.value = true
            _globalReminderTime.value = "08:00"
            _psyAppointmentDate.value = ""
            _psyNotes.value = ""
            _psyNotesListJson.value = "[]"
            _psyNotesExpanded.value = false
            _chatMessages.value = listOf(
                ChatMessage("norra", "Bonjour, je suis Norra. 💜 Comment te sens-tu en cet instant ? Sache que tu n'es pas seul(e). Je suis là pour t'écouter, sans aucun jugement.")
            )
        }
    }

    suspend fun getExportJsonString(): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val moodList = moodHistory.value
            val crisisList = crisisHistory.value
            
            val jsonObject = org.json.JSONObject()
            jsonObject.put("version", 1.0)
            jsonObject.put("backupDate", java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()))
            jsonObject.put("user", userName.value)
            jsonObject.put("email", userEmail.value)
            
            val moodsArr = org.json.JSONArray()
            for (m in moodList) {
                val mObj = org.json.JSONObject()
                mObj.put("timestamp", m.timestamp)
                mObj.put("emotionType", m.emotionType)
                mObj.put("intensity", m.intensity)
                mObj.put("note", m.note)
                moodsArr.put(mObj)
            }
            jsonObject.put("moods", moodsArr)
            
            val crisisArr = org.json.JSONArray()
            for (c in crisisList) {
                val cObj = org.json.JSONObject()
                cObj.put("timestamp", c.timestamp)
                cObj.put("exerciseId", c.exerciseId)
                cObj.put("worked", c.worked)
                crisisArr.put(cObj)
            }
            jsonObject.put("crisis", crisisArr)
            
            val medList = medications.value
            val medLogList = allMedicationLogs.value
            
            val medArr = org.json.JSONArray()
            for (m in medList) {
                val mObj = org.json.JSONObject()
                mObj.put("id", m.id)
                mObj.put("name", m.name)
                mObj.put("dosageMg", m.dosageMg)
                mObj.put("pillCount", m.pillCount)
                mObj.put("timeToTake", m.timeToTake)
                mObj.put("reminderEnabled", m.reminderEnabled)
                mObj.put("intakes", m.intakes)
                medArr.put(mObj)
            }
            jsonObject.put("medications", medArr)
            
            val logArr = org.json.JSONArray()
            for (l in medLogList) {
                val lObj = org.json.JSONObject()
                lObj.put("id", l.id)
                lObj.put("medicationId", l.medicationId)
                lObj.put("dateString", l.dateString)
                lObj.put("isTaken", l.isTaken)
                lObj.put("intakeIndex", l.intakeIndex)
                logArr.put(lObj)
            }
            jsonObject.put("medicationLogs", logArr)
            
            jsonObject.toString(2)
        }
    }
    
    fun importDataFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val jsonObject = org.json.JSONObject(jsonString)
                if (jsonObject.has("user")) setUserName(jsonObject.getString("user"))
                if (jsonObject.has("email")) setUserEmail(jsonObject.getString("email"))
                
                if (jsonObject.has("moods")) {
                    val moodsArr = jsonObject.getJSONArray("moods")
                    for (i in 0 until moodsArr.length()) {
                        val mObj = moodsArr.getJSONObject(i)
                        val timestamp = if (mObj.has("timestamp")) mObj.getLong("timestamp") else System.currentTimeMillis()
                        val emotionType = if (mObj.has("emotionType")) mObj.getString("emotionType") else ""
                        val intensity = if (mObj.has("intensity")) mObj.getInt("intensity") else 3
                        val note = if (mObj.has("note")) mObj.getString("note") else ""
                        repository.insertMood(MoodEntry(timestamp = timestamp, emotionType = emotionType, intensity = intensity, note = note))
                    }
                }
                
                if (jsonObject.has("crisis")) {
                    val crisisArr = jsonObject.getJSONArray("crisis")
                    for (i in 0 until crisisArr.length()) {
                        val cObj = crisisArr.getJSONObject(i)
                        val timestamp = if (cObj.has("timestamp")) cObj.getLong("timestamp") else System.currentTimeMillis()
                        val exerciseId = if (cObj.has("exerciseId")) cObj.getString("exerciseId") else ""
                        val worked = if (cObj.has("worked")) cObj.getBoolean("worked") else false
                        repository.insertCrisisLog(com.example.data.CrisisLog(timestamp = timestamp, exerciseId = exerciseId, worked = worked))
                    }
                }
                
                val medIdMapping = mutableMapOf<Long, Long>()
                if (jsonObject.has("medications")) {
                    val medArr = jsonObject.getJSONArray("medications")
                    for (i in 0 until medArr.length()) {
                        val mObj = medArr.getJSONObject(i)
                        val oldId = if (mObj.has("id")) mObj.getLong("id") else -1L
                        val med = com.example.data.Medication(
                            name = if (mObj.has("name")) mObj.getString("name") else "",
                            dosageMg = if (mObj.has("dosageMg")) mObj.getInt("dosageMg") else 0,
                            pillCount = if (mObj.has("pillCount")) mObj.getInt("pillCount") else 1,
                            timeToTake = if (mObj.has("timeToTake")) mObj.getString("timeToTake") else "08:00",
                            reminderEnabled = if (mObj.has("reminderEnabled")) mObj.getBoolean("reminderEnabled") else true,
                            intakes = if (mObj.has("intakes")) mObj.getString("intakes") else ""
                        )
                        val newId = repository.insertMedication(med)
                        if (oldId != -1L) medIdMapping[oldId] = newId
                    }
                }
                
                if (jsonObject.has("medicationLogs")) {
                    val logArr = jsonObject.getJSONArray("medicationLogs")
                    for (i in 0 until logArr.length()) {
                        val lObj = logArr.getJSONObject(i)
                        val oldMedId = if (lObj.has("medicationId")) lObj.getLong("medicationId") else -1L
                        val newMedId = medIdMapping[oldMedId] ?: oldMedId
                        if (newMedId != -1L) {
                            val log = com.example.data.MedicationLog(
                                medicationId = newMedId,
                                dateString = if (lObj.has("dateString")) lObj.getString("dateString") else "",
                                isTaken = if (lObj.has("isTaken")) lObj.getBoolean("isTaken") else false,
                                intakeIndex = if (lObj.has("intakeIndex")) lObj.getInt("intakeIndex") else 0
                            )
                            repository.insertMedicationLog(log)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NorraVM", "Failed to parse import JSON", e)
            }
        }
    }
}

class NorraViewModelFactory(private val repository: NorraRepository, private val sharedPreferences: android.content.SharedPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NorraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NorraViewModel(repository, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
