package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BreakSegment
import com.example.data.model.DestinationVisit
import com.example.data.model.IntegrityType
import com.example.data.model.SavedDestination
import com.example.data.model.SessionState
import com.example.data.model.TimelineEvent
import com.example.data.model.TransferSegment
import com.example.data.model.UserPreferences
import com.example.data.model.WorkSession
import com.example.location.GeofenceService
import com.example.repository.WorkSessionRepository
import com.example.util.Formatters
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SystemHealth(
    val hasFineLocationPermission: Boolean = false,
    val hasBackgroundLocationPermission: Boolean = false,
    val isGeofenceActive: Boolean = false,
    val lastValidationStatus: String = "OK"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val geofenceService = GeofenceService(application)
    private val repository = WorkSessionRepository(db.workSessionDao(), geofenceService)

    val allSessions: StateFlow<List<WorkSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val savedDestinations: StateFlow<List<SavedDestination>> = repository.savedDestinations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSession = MutableStateFlow<WorkSession?>(null)
    val activeSession: StateFlow<WorkSession?> = _activeSession.asStateFlow()

    private val _activeSessionState = MutableStateFlow(SessionState.NO_INICIADA)
    val activeSessionState: StateFlow<SessionState> = _activeSessionState.asStateFlow()

    private val _elapsedTimeStr = MutableStateFlow("00:00:00")
    val elapsedTimeStr: StateFlow<String> = _elapsedTimeStr.asStateFlow()

    private val _visits = MutableStateFlow<List<DestinationVisit>>(emptyList())
    val visits: StateFlow<List<DestinationVisit>> = _visits.asStateFlow()

    private val _transfers = MutableStateFlow<List<TransferSegment>>(emptyList())
    val transfers: StateFlow<List<TransferSegment>> = _transfers.asStateFlow()

    private val _breaks = MutableStateFlow<List<BreakSegment>>(emptyList())
    val breaks: StateFlow<List<BreakSegment>> = _breaks.asStateFlow()

    private val _timeline = MutableStateFlow<List<TimelineEvent>>(emptyList())
    val timeline: StateFlow<List<TimelineEvent>> = _timeline.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _systemHealth = MutableStateFlow(SystemHealth())
    val systemHealth: StateFlow<SystemHealth> = _systemHealth.asStateFlow()

    private var timerJob: Job? = null
    private var visitsCollectorJob: Job? = null
    private var transfersCollectorJob: Job? = null
    private var breaksCollectorJob: Job? = null
    private var timelineCollectorJob: Job? = null

    init {
        refreshActiveSession()
        updateHealthCheck()
        viewModelScope.launch {
            repository.getSavedDestinationsList()
        }
    }

    fun updateHealthCheck() {
        val fine = geofenceService.hasLocationPermission()
        val bg = geofenceService.hasBackgroundLocationPermission()
        val active = _activeSession.value != null && _activeSessionState.value != SessionState.NO_INICIADA && _activeSessionState.value != SessionState.CERRADA
        _systemHealth.value = SystemHealth(
            hasFineLocationPermission = fine,
            hasBackgroundLocationPermission = bg,
            isGeofenceActive = active,
            lastValidationStatus = if (fine) "OK" else "Permisos requeridos"
        )
    }

    fun refreshActiveSession() {
        viewModelScope.launch {
            val active = repository.getActiveSession()
            _activeSession.value = active
            if (active != null) {
                _activeSessionState.value = try {
                    SessionState.valueOf(active.state)
                } catch (e: Exception) {
                    SessionState.NO_INICIADA
                }
                observeSessionSubEntities(active.id)
                startTimer(active.startTime)
            } else {
                _activeSessionState.value = SessionState.NO_INICIADA
                _elapsedTimeStr.value = "00:00:00"
                stopTimer()
            }
            updateHealthCheck()
        }
    }

    private fun observeSessionSubEntities(sessionId: String) {
        visitsCollectorJob?.cancel()
        visitsCollectorJob = viewModelScope.launch {
            repository.getVisitsFlowForSession(sessionId).collectLatest { _visits.value = it }
        }

        transfersCollectorJob?.cancel()
        transfersCollectorJob = viewModelScope.launch {
            repository.getTransfersFlowForSession(sessionId).collectLatest { _transfers.value = it }
        }

        breaksCollectorJob?.cancel()
        breaksCollectorJob = viewModelScope.launch {
            repository.getBreaksFlowForSession(sessionId).collectLatest { _breaks.value = it }
        }

        timelineCollectorJob?.cancel()
        timelineCollectorJob = viewModelScope.launch {
            repository.getTimelineFlowForSession(sessionId).collectLatest { _timeline.value = it }
        }
    }

    private fun startTimer(startTimeHHMM: String) {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (true) {
                val nowTime = Formatters.getCurrentTimeHHMM()
                val minutes = Formatters.getMinutesBetween(startTimeHHMM, nowTime)
                val hours = minutes / 60
                val mins = minutes % 60
                val seconds = (System.currentTimeMillis() / 1000 % 60).toInt()
                _elapsedTimeStr.value = String.format("%02d:%02d:%02d", hours, mins, seconds)
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun startWorkDay(destination: String, alias: String?) {
        viewModelScope.launch {
            try {
                val session = repository.startSession(destination, alias)
                _activeSession.value = session
                _activeSessionState.value = SessionState.ESPERANDO
                observeSessionSubEntities(session.id)
                startTimer(session.startTime)
                showToast("Jornada iniciada para $destination. Esperando llegada.")
                updateHealthCheck()
            } catch (e: Exception) {
                showToast("Error al iniciar jornada: ${e.message}")
            }
        }
    }

    fun confirmArrival() {
        viewModelScope.launch {
            try {
                repository.confirmArrival()
                refreshActiveSession()
                showToast("Arribo confirmado. Estado: ACTIVA.")
            } catch (e: Exception) {
                showToast("Error al confirmar llegada")
            }
        }
    }

    fun startLunch() {
        viewModelScope.launch {
            try {
                repository.startLunch()
                refreshActiveSession()
                showToast("Almuerzo / Pausa iniciada")
            } catch (e: Exception) {
                showToast("Error al iniciar pausa")
            }
        }
    }

    fun endLunch() {
        viewModelScope.launch {
            try {
                repository.endLunch()
                refreshActiveSession()
                showToast("Pausa finalizada. Retorno a trabajo activo.")
            } catch (e: Exception) {
                showToast("Error al finalizar pausa")
            }
        }
    }

    fun startTransfer(destination: String, alias: String?) {
        viewModelScope.launch {
            try {
                repository.startTransfer(destination, alias)
                refreshActiveSession()
                showToast("Traslado iniciado hacia $destination")
            } catch (e: Exception) {
                showToast("Error al iniciar traslado")
            }
        }
    }

    fun completeTransfer() {
        viewModelScope.launch {
            try {
                repository.completeTransfer()
                refreshActiveSession()
                showToast("Traslado completado. Trabajo activo.")
            } catch (e: Exception) {
                showToast("Error al completar traslado")
            }
        }
    }

    fun finishWorkDay(lunchMinutes: Int) {
        viewModelScope.launch {
            try {
                repository.finishWorkDay(lunchMinutes)
                stopTimer()
                _activeSession.value = null
                _activeSessionState.value = SessionState.NO_INICIADA
                _elapsedTimeStr.value = "00:00:00"
                showToast("Jornada finalizada y guardada con éxito.")
                updateHealthCheck()
            } catch (e: Exception) {
                showToast("Error al cerrar jornada")
            }
        }
    }

    fun cancelWorkDay() {
        viewModelScope.launch {
            try {
                repository.cancelSession()
                stopTimer()
                _activeSession.value = null
                _activeSessionState.value = SessionState.NO_INICIADA
                _elapsedTimeStr.value = "00:00:00"
                showToast("Jornada cancelada.")
                updateHealthCheck()
            } catch (e: Exception) {
                showToast("Error al cancelar jornada")
            }
        }
    }

    fun addNote(note: String) {
        viewModelScope.launch {
            repository.addNote(note)
            refreshActiveSession()
            showToast("Nota agregada a la jornada")
        }
    }

    fun onGeofenceExit() {
        viewModelScope.launch {
            repository.onGeofenceExit()
            refreshActiveSession()
            showToast("Alerta: Salida de geocerca detectada")
        }
    }

    fun resumeFromExit() {
        viewModelScope.launch {
            repository.resumeFromExit()
            refreshActiveSession()
            showToast("Reingreso a geocerca confirmado")
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            repository.deleteSession(id)
            showToast("Jornada eliminada")
        }
    }

    fun editSession(id: String, patchDate: String?, patchStart: String?, patchEnd: String?, notes: String?) {
        viewModelScope.launch {
            repository.editSession(id, patchDate, patchStart, patchEnd, notes)
            showToast("Ajuste manual aplicado")
        }
    }

    fun savePreferences(prefs: UserPreferences) {
        viewModelScope.launch {
            repository.savePreferences(prefs)
            showToast("Ajustes guardados")
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val current = repository.getPreferences()
            val updated = current.copy(
                isOnboardingCompleted = true,
                isDisclaimerAccepted = true,
                updatedMs = System.currentTimeMillis()
            )
            repository.savePreferences(updated)
            showToast("Proceso de incorporación completado ✓")
        }
    }

    fun acceptDisclaimer() {
        viewModelScope.launch {
            val current = repository.getPreferences()
            val updated = current.copy(
                isDisclaimerAccepted = true,
                updatedMs = System.currentTimeMillis()
            )
            repository.savePreferences(updated)
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getPreferences()
            val updated = current.copy(
                hapticFeedbackEnabled = enabled,
                updatedMs = System.currentTimeMillis()
            )
            repository.savePreferences(updated)
            showToast(if (enabled) "Interacciones hápticas activadas" else "Interacciones hápticas desactivadas")
        }
    }

    fun setBatteryOptimizationConfigured(configured: Boolean) {
        viewModelScope.launch {
            val current = repository.getPreferences()
            val updated = current.copy(
                unrestrictedBatteryConfigured = configured,
                updatedMs = System.currentTimeMillis()
            )
            repository.savePreferences(updated)
            showToast(if (configured) "Uso de batería sin restricciones configurado" else "Batería marcada como estándar")
        }
    }

    fun setBackgroundExecution(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getPreferences()
            val updated = current.copy(
                backgroundExecutionEnabled = enabled,
                updatedMs = System.currentTimeMillis()
            )
            repository.savePreferences(updated)
            showToast(if (enabled) "Ejecución en segundo plano activada" else "Ejecución en segundo plano desactivada")
        }
    }

    fun restartOnboarding() {
        viewModelScope.launch {
            val current = repository.getPreferences()
            val updated = current.copy(
                isOnboardingCompleted = false,
                updatedMs = System.currentTimeMillis()
            )
            repository.savePreferences(updated)
            showToast("Reiniciando proceso de incorporación...")
        }
    }

    fun addSavedDestination(
        alias: String,
        address: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 150,
        category: String = "Obra"
    ) {
        viewModelScope.launch {
            val destination = SavedDestination(
                id = java.util.UUID.randomUUID().toString(),
                alias = alias,
                address = address,
                latitude = latitude,
                longitude = longitude,
                radiusMeters = radiusMeters,
                category = category
            )
            repository.addSavedDestination(destination)
            showToast("Ubicación de geocerca '$alias' guardada")
        }
    }

    fun deleteSavedDestination(id: String) {
        viewModelScope.launch {
            repository.deleteSavedDestination(id)
            showToast("Ubicación de geocerca eliminada")
        }
    }

    fun startWorkDayWithSavedDestination(dest: SavedDestination) {
        startWorkDay(destination = dest.address, alias = dest.alias)
    }

    suspend fun getWeeklySummary(weekStartIso: String, weekEndIso: String) =
        repository.getWeeklySummary(weekStartIso, weekEndIso)

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
