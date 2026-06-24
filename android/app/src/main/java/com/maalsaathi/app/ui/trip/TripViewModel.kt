package com.maalsaathi.app.ui.trip

import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.maalsaathi.app.data.models.EntryType
import com.maalsaathi.app.data.models.Trip
import com.maalsaathi.app.data.models.TripEntry
import com.maalsaathi.app.data.models.TripStatus
import com.maalsaathi.app.data.remote.OpenAiClient
import com.maalsaathi.app.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class AiExtraction(
    val origin: String = "",
    val destination: String = "",
    val cargoType: String = "",
    val cargoWeightTons: Double = 0.0,
    val freightAmount: Long = 0,
    val advanceAmount: Long = 0,
    val confidence: String = "high",
)

data class AiEntryResult(
    val type: EntryType = EntryType.EXPENSE,
    val category: String = "other",
    val amount: Long = 0,
    val note: String = "",
    val emoji: String = "📝",
)

sealed interface AiState {
    data object Idle : AiState
    data object Recording : AiState
    data object Transcribing : AiState
    data object Extracting : AiState
    data class TripExtracted(val data: AiExtraction, val rawText: String) : AiState
    data class EntryExtracted(val data: AiEntryResult, val rawText: String) : AiState
    data class Error(val message: String) : AiState
}

class TripViewModel(
    private val repo: TripRepository,
    private val cacheDir: File,
) : ViewModel() {

    val ongoingTrip = repo.getOngoingTrip().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val scheduledTrips = repo.getScheduledTrips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pastTrips = repo.getPastTrips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiState = MutableStateFlow<AiState>(AiState.Idle)
    val aiState: StateFlow<AiState> = _aiState

    private val _currentEntries = MutableStateFlow<List<TripEntry>>(emptyList())
    val currentEntries: StateFlow<List<TripEntry>> = _currentEntries

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun loadEntriesForTrip(tripId: String) {
        viewModelScope.launch {
            repo.getEntriesForTrip(tripId).collect { _currentEntries.value = it }
        }
    }

    fun extractTripFromText(text: String) {
        _aiState.value = AiState.Extracting
        viewModelScope.launch {
            OpenAiClient.extractTripDetails(text).fold(
                onSuccess = { json -> _aiState.value = AiState.TripExtracted(parseExtraction(json), text) },
                onFailure = { _aiState.value = AiState.Error("Samajh nahi aaya, dobara bolo") },
            )
        }
    }

    fun categorizeEntryFromText(text: String, tripId: String) {
        _aiState.value = AiState.Extracting
        viewModelScope.launch {
            OpenAiClient.categorizeEntry(text).fold(
                onSuccess = { json -> _aiState.value = AiState.EntryExtracted(parseEntryResult(json), text) },
                onFailure = { _aiState.value = AiState.Error("Samajh nahi aaya, dobara likho") },
            )
        }
    }

    @Suppress("DEPRECATION")
    fun startRecording() {
        try {
            val file = File(cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(cacheDir as android.content.Context? ?: return)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            audioFile = file
            _aiState.value = AiState.Recording
        } catch (e: Exception) {
            _aiState.value = AiState.Error("Mic nahi chala — permission check karo")
        }
    }

    fun stopRecordingForTrip() {
        stopRecorderInternal()
        val file = audioFile ?: run { _aiState.value = AiState.Error("Recording nahi mili"); return }
        _aiState.value = AiState.Transcribing
        viewModelScope.launch {
            OpenAiClient.transcribeAudio(file).fold(
                onSuccess = { text -> extractTripFromText(text) },
                onFailure = { _aiState.value = AiState.Error("Sun nahi paaya, dobara bolo") },
            )
        }
    }

    fun stopRecordingForEntry(tripId: String) {
        stopRecorderInternal()
        val file = audioFile ?: run { _aiState.value = AiState.Error("Recording nahi mili"); return }
        _aiState.value = AiState.Transcribing
        viewModelScope.launch {
            OpenAiClient.transcribeAudio(file).fold(
                onSuccess = { text -> categorizeEntryFromText(text, tripId) },
                onFailure = { _aiState.value = AiState.Error("Sun nahi paaya, dobara bolo") },
            )
        }
    }

    private fun stopRecorderInternal() {
        try { recorder?.stop(); recorder?.release() } catch (_: Exception) {}
        recorder = null
    }

    fun resetAiState() { _aiState.value = AiState.Idle }

    fun createTrip(extraction: AiExtraction): String {
        val id = UUID.randomUUID().toString()
        val trip = Trip(
            id = id,
            origin = extraction.origin,
            destination = extraction.destination,
            cargoType = extraction.cargoType,
            cargoWeightTons = extraction.cargoWeightTons,
            freightAmount = extraction.freightAmount,
            advanceAmount = extraction.advanceAmount,
            status = TripStatus.ONGOING,
            startTime = System.currentTimeMillis(),
        )
        viewModelScope.launch { repo.createTrip(trip) }
        return id
    }

    fun createScheduledTrip(origin: String, destination: String, partyName: String, scheduledDate: Long): String {
        val id = UUID.randomUUID().toString()
        val trip = Trip(
            id = id, origin = origin, destination = destination,
            partyName = partyName, status = TripStatus.SCHEDULED,
            startTime = scheduledDate, scheduledDate = scheduledDate,
        )
        viewModelScope.launch { repo.createTrip(trip) }
        return id
    }

    fun confirmEntry(result: AiEntryResult, tripId: String, rawText: String) {
        val entry = TripEntry(
            id = UUID.randomUUID().toString(),
            tripId = tripId, type = result.type, category = result.category,
            amount = result.amount, emoji = result.emoji, note = result.note,
            timestamp = System.currentTimeMillis(), rawText = rawText,
        )
        viewModelScope.launch { repo.addEntry(entry) }
        _aiState.value = AiState.Idle
    }

    fun endTrip(tripId: String) {
        viewModelScope.launch { repo.endTrip(tripId) }
    }

    fun cancelTrip(tripId: String) {
        viewModelScope.launch { repo.cancelTrip(tripId) }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch { repo.deleteEntry(entryId) }
    }

    fun startScheduledTrip(tripId: String) {
        viewModelScope.launch { repo.startScheduledTrip(tripId) }
    }

    suspend fun getTripById(id: String): Trip? = repo.getTripById(id)

    private fun parseExtraction(json: JsonObject): AiExtraction = AiExtraction(
        origin = json.get("origin")?.asString ?: "",
        destination = json.get("destination")?.asString ?: "",
        cargoType = json.get("cargoType")?.asString ?: "",
        cargoWeightTons = json.get("cargoWeightTons")?.asDouble ?: 0.0,
        freightAmount = json.get("freightAmount")?.asLong ?: 0,
        advanceAmount = json.get("advanceAmount")?.asLong ?: 0,
        confidence = json.get("confidence")?.asString ?: "high",
    )

    private fun parseEntryResult(json: JsonObject): AiEntryResult = AiEntryResult(
        type = when (json.get("type")?.asString) {
            "income" -> EntryType.INCOME; "note" -> EntryType.NOTE; else -> EntryType.EXPENSE
        },
        category = json.get("category")?.asString ?: "other",
        amount = json.get("amount")?.asLong ?: 0,
        note = json.get("note")?.asString ?: "",
        emoji = json.get("emoji")?.asString ?: "📝",
    )

    override fun onCleared() {
        super.onCleared()
        stopRecorderInternal()
    }

    class Factory(private val repo: TripRepository, private val cacheDir: File) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TripViewModel(repo, cacheDir) as T
    }
}
