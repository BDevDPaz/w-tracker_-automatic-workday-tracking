package com.example.repository

import com.example.data.local.WorkSessionDao
import com.example.data.model.BreakSegment
import com.example.data.model.DestinationVisit
import com.example.data.model.IntegrityType
import com.example.data.model.SavedDestination
import com.example.data.model.SessionState
import com.example.data.model.TimelineEvent
import com.example.data.model.TransferSegment
import com.example.data.model.UserPreferences
import com.example.data.model.WalLogEntry
import com.example.data.model.WorkSession
import com.example.location.Coordinates
import com.example.location.GeofenceService
import com.example.util.Formatters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class WeeklySummaryModel(
    val periodLabel: String,
    val totalHours: Double,
    val totalEarnings: Double,
    val sessionCount: Int,
    val manualAdjustments: Int,
    val overtimeHours: Double,
    val baseEarnings: Double,
    val overtimeEarnings: Double,
    val baseRate: Double,
    val overtimeMultiplier: Double
)

class WorkSessionRepository(
    private val dao: WorkSessionDao,
    private val geofenceService: GeofenceService
) {

    val allSessions: Flow<List<WorkSession>> = dao.getAllSessions()

    val savedDestinations: Flow<List<SavedDestination>> = dao.getAllSavedDestinations()

    suspend fun getSavedDestinationsList(): List<SavedDestination> {
        val list = dao.getSavedDestinationsList()
        if (list.isEmpty()) {
            val defaults = listOf(
                SavedDestination(
                    id = UUID.randomUUID().toString(),
                    alias = "Oficina Central",
                    address = "Av. Las Américas 15-20, Zona 13, Guatemala",
                    latitude = 14.5880,
                    longitude = -90.5280,
                    radiusMeters = 100,
                    category = "Oficina",
                    isPrimary = true
                ),
                SavedDestination(
                    id = UUID.randomUUID().toString(),
                    alias = "Obra San José",
                    address = "Km 18.5 Carretera a El Salvador",
                    latitude = 14.5500,
                    longitude = -90.4500,
                    radiusMeters = 200,
                    category = "Obra",
                    isPrimary = false
                ),
                SavedDestination(
                    id = UUID.randomUUID().toString(),
                    alias = "Cliente Alfa - Planta Norte",
                    address = "Zona Industrial Norte, Lote 45",
                    latitude = 14.6500,
                    longitude = -90.5100,
                    radiusMeters = 150,
                    category = "Cliente",
                    isPrimary = false
                )
            )
            defaults.forEach { dao.insertSavedDestination(it) }
            return dao.getSavedDestinationsList()
        }
        return list
    }

    suspend fun addSavedDestination(destination: SavedDestination) {
        dao.insertSavedDestination(destination)
    }

    suspend fun deleteSavedDestination(id: String) {
        dao.deleteSavedDestinationById(id)
    }

    val userPreferences: Flow<UserPreferences> = dao.getUserPreferencesFlow().map {
        it ?: UserPreferences()
    }

    suspend fun getPreferences(): UserPreferences {
        return dao.getUserPreferences() ?: UserPreferences().also { dao.saveUserPreferences(it) }
    }

    suspend fun savePreferences(prefs: UserPreferences) {
        dao.saveUserPreferences(prefs)
    }

    suspend fun getActiveSession(): WorkSession? {
        return dao.getActiveSession()
    }

    suspend fun getVisitsListForSession(sessionId: String): List<DestinationVisit> {
        return dao.getVisitsListForSession(sessionId)
    }

    suspend fun getTransfersListForSession(sessionId: String): List<TransferSegment> {
        return dao.getTransfersListForSession(sessionId)
    }

    suspend fun getBreaksListForSession(sessionId: String): List<BreakSegment> {
        return dao.getBreaksListForSession(sessionId)
    }

    suspend fun getTimelineListForSession(sessionId: String): List<TimelineEvent> {
        return dao.getTimelineListForSession(sessionId)
    }

    fun getVisitsFlowForSession(sessionId: String): Flow<List<DestinationVisit>> {
        return dao.getVisitsForSession(sessionId)
    }

    fun getTransfersFlowForSession(sessionId: String): Flow<List<TransferSegment>> {
        return dao.getTransfersForSession(sessionId)
    }

    fun getBreaksFlowForSession(sessionId: String): Flow<List<BreakSegment>> {
        return dao.getBreaksForSession(sessionId)
    }

    fun getTimelineFlowForSession(sessionId: String): Flow<List<TimelineEvent>> {
        return dao.getTimelineEventsForSession(sessionId)
    }

    suspend fun startSession(destination: String, alias: String?): WorkSession {
        val existingActive = dao.getActiveSession()
        if (existingActive != null) {
            return existingActive
        }

        val prefs = getPreferences()
        val nowIso = Formatters.getCurrentDateIso()
        val timeNow = Formatters.getCurrentTimeHHMM()
        val sessionId = UUID.randomUUID().toString()

        val coords = geofenceService.geocodeAddress(destination)

        val session = WorkSession(
            id = sessionId,
            date = nowIso,
            destination = destination,
            alias = alias,
            startTime = timeNow,
            endTime = null,
            snapshotRate = prefs.hourlyRate,
            integrityType = IntegrityType.VALID.name,
            state = SessionState.ESPERANDO.name,
            lat = coords?.lat,
            lng = coords?.lng
        )

        dao.insertSession(session)

        val event = TimelineEvent(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            timestamp = System.currentTimeMillis().toString(),
            description = "Jornada configurada para $destination",
            status = "pending",
            sequence = 1
        )
        dao.insertTimelineEvent(event)

        writeWal(sessionId, "START_SESSION", "{\"destination\": \"$destination\"}")

        return session
    }

    suspend fun confirmArrival() {
        val session = dao.getActiveSession() ?: return
        if (session.state != SessionState.ESPERANDO.name) return

        val nowTime = Formatters.getCurrentTimeHHMM()

        val visit = DestinationVisit(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            destination = session.destination,
            alias = session.alias,
            enterTime = nowTime,
            exitTime = null,
            lat = session.lat,
            lng = session.lng
        )
        dao.insertVisit(visit)

        val updated = session.copy(
            startTime = nowTime,
            state = SessionState.ACTIVA.name,
            updatedMs = System.currentTimeMillis()
        )
        dao.updateSession(updated)

        val event = TimelineEvent(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            timestamp = System.currentTimeMillis().toString(),
            description = "Llegada confirmada al destino: ${session.destination}",
            status = "active",
            sequence = 2
        )
        dao.insertTimelineEvent(event)

        recalculateMetrics(session.id)
        writeWal(session.id, "CONFIRM_ARRIVAL", "{\"enterTime\": \"$nowTime\"}")
    }

    suspend fun startLunch() {
        val session = dao.getActiveSession() ?: return
        if (session.state != SessionState.ACTIVA.name && session.state != SessionState.SALIDA.name) return

        val nowTime = Formatters.getCurrentTimeHHMM()

        // Close current visit if open
        val visits = dao.getVisitsListForSession(session.id)
        if (visits.isNotEmpty()) {
            val last = visits.last()
            if (last.exitTime == null) {
                dao.insertVisit(last.copy(exitTime = nowTime))
            }
        }

        val breakSeg = BreakSegment(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            startTime = nowTime,
            endTime = null,
            durationMinutes = 0
        )
        dao.insertBreak(breakSeg)

        val updated = session.copy(
            state = SessionState.PAUSADA.name,
            updatedMs = System.currentTimeMillis()
        )
        dao.updateSession(updated)

        val event = TimelineEvent(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            timestamp = System.currentTimeMillis().toString(),
            description = "Almuerzo iniciado",
            status = "warning",
            sequence = System.currentTimeMillis().toInt()
        )
        dao.insertTimelineEvent(event)

        recalculateMetrics(session.id)
        writeWal(session.id, "START_LUNCH", "{\"startTime\": \"$nowTime\"}")
    }

    suspend fun endLunch() {
        val session = dao.getActiveSession() ?: return
        if (session.state != SessionState.PAUSADA.name) return

        val nowTime = Formatters.getCurrentTimeHHMM()

        val breaks = dao.getBreaksListForSession(session.id)
        if (breaks.isNotEmpty()) {
            val last = breaks.last()
            if (last.endTime == null) {
                val duration = Formatters.getMinutesBetween(last.startTime, nowTime)
                dao.insertBreak(last.copy(endTime = nowTime, durationMinutes = duration))
            }
        }

        // Re-open visit for current destination
        val visit = DestinationVisit(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            destination = session.destination,
            alias = session.alias,
            enterTime = nowTime,
            exitTime = null,
            lat = session.lat,
            lng = session.lng
        )
        dao.insertVisit(visit)

        val updated = session.copy(
            state = SessionState.ACTIVA.name,
            updatedMs = System.currentTimeMillis()
        )
        dao.updateSession(updated)

        val event = TimelineEvent(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            timestamp = System.currentTimeMillis().toString(),
            description = "Almuerzo finalizado. Retorno a trabajo.",
            status = "active",
            sequence = System.currentTimeMillis().toInt()
        )
        dao.insertTimelineEvent(event)

        recalculateMetrics(session.id)
        writeWal(session.id, "END_LUNCH", "{\"endTime\": \"$nowTime\"}")
    }

    suspend fun startTransfer(destination: String, alias: String?) {
        val session = dao.getActiveSession() ?: return
        val nowTime = Formatters.getCurrentTimeHHMM()

        if (session.state == SessionState.PAUSADA.name) {
            val breaks = dao.getBreaksListForSession(session.id)
            if (breaks.isNotEmpty()) {
                val last = breaks.last()
                if (last.endTime == null) {
                    val duration = Formatters.getMinutesBetween(last.startTime, nowTime)
                    dao.insertBreak(last.copy(endTime = nowTime, durationMinutes = duration))
                }
            }
        } else {
            val visits = dao.getVisitsListForSession(session.id)
            if (visits.isNotEmpty()) {
                val last = visits.last()
                if (last.exitTime == null) {
                    dao.insertVisit(last.copy(exitTime = nowTime))
                }
            }
        }

        val transfer = TransferSegment(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            originDestination = session.destination,
            targetDestination = destination,
            startTime = nowTime,
            endTime = null
        )
        dao.insertTransfer(transfer)

        val coords = geofenceService.geocodeAddress(destination)

        val updated = session.copy(
            destination = destination,
            alias = alias,
            state = SessionState.TRASLADO.name,
            lat = coords?.lat ?: session.lat,
            lng = coords?.lng ?: session.lng,
            destinationCount = session.destinationCount + 1,
            updatedMs = System.currentTimeMillis()
        )
        dao.updateSession(updated)

        val event = TimelineEvent(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            timestamp = System.currentTimeMillis().toString(),
            description = "Inicio de traslado hacia: $destination",
            status = "pending",
            sequence = System.currentTimeMillis().toInt()
        )
        dao.insertTimelineEvent(event)

        recalculateMetrics(session.id)
        writeWal(session.id, "START_TRANSFER", "{\"target\": \"$destination\"}")
    }

    suspend fun completeTransfer() {
        val session = dao.getActiveSession() ?: return
        if (session.state != SessionState.TRASLADO.name) return

        val nowTime = Formatters.getCurrentTimeHHMM()

        val transfers = dao.getTransfersListForSession(session.id)
        if (transfers.isNotEmpty()) {
            val last = transfers.last()
            if (last.endTime == null) {
                dao.insertTransfer(last.copy(endTime = nowTime))
            }
        }

        val visit = DestinationVisit(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            destination = session.destination,
            alias = session.alias,
            enterTime = nowTime,
            exitTime = null,
            lat = session.lat,
            lng = session.lng
        )
        dao.insertVisit(visit)

        val updated = session.copy(
            state = SessionState.ACTIVA.name,
            updatedMs = System.currentTimeMillis()
        )
        dao.updateSession(updated)

        val event = TimelineEvent(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            timestamp = System.currentTimeMillis().toString(),
            description = "Llegada al nuevo destino: ${session.destination}",
            status = "active",
            sequence = System.currentTimeMillis().toInt()
        )
        dao.insertTimelineEvent(event)

        recalculateMetrics(session.id)
        writeWal(session.id, "COMPLETE_TRANSFER", "{\"enterTime\": \"$nowTime\"}")
    }

    suspend fun onGeofenceExit() {
        val session = dao.getActiveSession() ?: return
        if (session.state != SessionState.ACTIVA.name) return

        val updated = session.copy(
            state = SessionState.SALIDA.name,
            updatedMs = System.currentTimeMillis()
        )
        dao.updateSession(updated)

        val event = TimelineEvent(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            timestamp = System.currentTimeMillis().toString(),
            description = "Salida detectada del perímetro de geocerca",
            status = "warning",
            sequence = System.currentTimeMillis().toInt()
        )
        dao.insertTimelineEvent(event)

        writeWal(session.id, "GEOFENCE_EXIT", "{}")
    }

    suspend fun resumeFromExit() {
        val session = dao.getActiveSession() ?: return
        if (session.state != SessionState.SALIDA.name) return

        val updated = session.copy(
            state = SessionState.ACTIVA.name,
            updatedMs = System.currentTimeMillis()
        )
        dao.updateSession(updated)

        val event = TimelineEvent(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            timestamp = System.currentTimeMillis().toString(),
            description = "Reingreso confirmado a la zona de trabajo",
            status = "active",
            sequence = System.currentTimeMillis().toInt()
        )
        dao.insertTimelineEvent(event)

        writeWal(session.id, "RESUME_EXIT", "{}")
    }

    suspend fun finishWorkDay(lunchMinutes: Int): WorkSession? {
        val session = dao.getActiveSession() ?: return null
        val nowTime = Formatters.getCurrentTimeHHMM()

        // Close any open visits, transfers, breaks
        val visits = dao.getVisitsListForSession(session.id)
        if (visits.isNotEmpty()) {
            val last = visits.last()
            if (last.exitTime == null) {
                dao.insertVisit(last.copy(exitTime = nowTime))
            }
        }

        val transfers = dao.getTransfersListForSession(session.id)
        if (transfers.isNotEmpty()) {
            val last = transfers.last()
            if (last.endTime == null) {
                dao.insertTransfer(last.copy(endTime = nowTime))
            }
        }

        val breaks = dao.getBreaksListForSession(session.id)
        if (breaks.isNotEmpty()) {
            val last = breaks.last()
            if (last.endTime == null) {
                val dur = Formatters.getMinutesBetween(last.startTime, nowTime)
                dao.insertBreak(last.copy(endTime = nowTime, durationMinutes = dur))
            }
        }

        val updated = session.copy(
            endTime = nowTime,
            state = SessionState.CERRADA.name,
            lunchDeductedMinutes = lunchMinutes,
            updatedMs = System.currentTimeMillis()
        )
        dao.updateSession(updated)

        val event = TimelineEvent(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            timestamp = System.currentTimeMillis().toString(),
            description = "Jornada completada y cerrada. Almuerzo descontado: $lunchMinutes min",
            status = "closed",
            sequence = System.currentTimeMillis().toInt()
        )
        dao.insertTimelineEvent(event)

        recalculateMetrics(session.id)
        writeWal(session.id, "CLOSE_SESSION", "{\"endTime\": \"$nowTime\", \"lunchMinutes\": $lunchMinutes}")

        return dao.getSessionById(session.id)
    }

    suspend fun cancelSession() {
        val session = dao.getActiveSession() ?: return
        dao.deleteVisitsForSession(session.id)
        dao.deleteTransfersForSession(session.id)
        dao.deleteBreaksForSession(session.id)
        dao.deleteTimelineEventsForSession(session.id)
        dao.deleteSessionById(session.id)
    }

    suspend fun addNote(note: String) {
        val session = dao.getActiveSession() ?: return
        val updated = session.copy(notes = note, updatedMs = System.currentTimeMillis())
        dao.updateSession(updated)
    }

    suspend fun deleteSession(id: String) {
        dao.deleteVisitsForSession(id)
        dao.deleteTransfersForSession(id)
        dao.deleteBreaksForSession(id)
        dao.deleteTimelineEventsForSession(id)
        dao.deleteSessionById(id)
    }

    suspend fun editSession(
        id: String,
        patchDate: String?,
        patchStart: String?,
        patchEnd: String?,
        notes: String?
    ) {
        val session = dao.getSessionById(id) ?: return
        var date = session.date
        var start = session.startTime
        var end = session.endTime
        var userNotes = session.notes

        if (!patchDate.isNull_or_blank_custom()) date = patchDate!!
        if (!patchStart.isNull_or_blank_custom()) start = patchStart!!
        if (!patchEnd.isNull_or_blank_custom()) end = patchEnd
        if (notes != null) userNotes = notes

        val updated = session.copy(
            date = date,
            startTime = start,
            endTime = end,
            notes = userNotes,
            integrityType = IntegrityType.MANUAL.name,
            updatedMs = System.currentTimeMillis()
        )
        dao.updateSession(updated)
        recalculateMetrics(id)
    }

    suspend fun getWeeklySummary(weekStartIso: String, weekEndIso: String): WeeklySummaryModel {
        val prefs = getPreferences()
        val list = dao.getSessionsListByPeriod(weekStartIso, weekEndIso)
            .filter { it.state == SessionState.CERRADA.name }

        var totalHours = 0.0
        var totalEarnings = 0.0
        var manualAdjustments = 0
        var overtimeHours = 0.0
        var overtimeEarnings = 0.0

        for (session in list) {
            totalHours += session.totalHours
            totalEarnings += session.totalEarnings
            overtimeHours += session.overtimeHours

            if (session.integrityType == IntegrityType.MANUAL.name) {
                manualAdjustments++
            }

            val rate = session.snapshotRate
            val otHours = session.overtimeHours
            overtimeEarnings += otHours * rate * prefs.overtimeMultiplier
        }

        val baseEarnings = Math.max(0.0, totalEarnings - overtimeEarnings)
        val periodLabel = Formatters.formatWeekRangeLabel(weekStartIso, weekEndIso)

        return WeeklySummaryModel(
            periodLabel = periodLabel,
            totalHours = totalHours,
            totalEarnings = totalEarnings,
            sessionCount = list.size,
            manualAdjustments = manualAdjustments,
            overtimeHours = overtimeHours,
            baseEarnings = baseEarnings,
            overtimeEarnings = overtimeEarnings,
            baseRate = prefs.hourlyRate,
            overtimeMultiplier = prefs.overtimeMultiplier
        )
    }

    private suspend fun recalculateMetrics(sessionId: String) {
        val session = dao.getSessionById(sessionId) ?: return
        val visits = dao.getVisitsListForSession(sessionId)
        val transfers = dao.getTransfersListForSession(sessionId)
        val breaks = dao.getBreaksListForSession(sessionId)
        val prefs = getPreferences()

        var productiveMin = 0
        for (v in visits) {
            val end = v.exitTime ?: Formatters.getCurrentTimeHHMM()
            productiveMin += Formatters.getMinutesBetween(v.enterTime, end)
        }

        var transferMin = 0
        for (t in transfers) {
            val end = t.endTime ?: Formatters.getCurrentTimeHHMM()
            transferMin += Formatters.getMinutesBetween(t.startTime, end)
        }

        var breakMin = 0
        for (b in breaks) {
            val end = b.endTime ?: Formatters.getCurrentTimeHHMM()
            breakMin += Formatters.getMinutesBetween(b.startTime, end)
        }

        val paidMinRaw = productiveMin + (if (prefs.paidTransfers) transferMin else 0)
        val finalPaidMin = Math.max(0, paidMinRaw - session.lunchDeductedMinutes)
        val totalHours = finalPaidMin / 60.0

        val baseWorkHours = try {
            val p = prefs.baseWorkHour.split(":")
            p[0].toDouble() + (p.getOrNull(1)?.toDouble() ?: 0.0) / 60.0
        } catch (e: Exception) { 8.0 }

        val overtimeHours = Math.max(0.0, totalHours - baseWorkHours)
        val totalEarnings = Formatters.calculateEarnings(
            totalHours,
            session.snapshotRate,
            overtimeHours,
            prefs.overtimeMultiplier
        )

        val updated = session.copy(
            totalHours = totalHours,
            overtimeHours = overtimeHours,
            totalEarnings = totalEarnings,
            paidMinutes = finalPaidMin,
            pauseMinutes = breakMin + session.lunchDeductedMinutes,
            transferMinutes = transferMin,
            destinationCount = if (visits.isNotEmpty()) visits.size else 1,
            updatedMs = System.currentTimeMillis()
        )
        dao.updateSession(updated)
    }

    private suspend fun writeWal(sessionId: String, eventType: String, payload: String) {
        try {
            val entry = WalLogEntry(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                eventType = eventType,
                payload = payload,
                createdMs = System.currentTimeMillis(),
                synced = true
            )
            dao.insertWalEntry(entry)
        } catch (e: Exception) {
            // Log warning
        }
    }

    private fun String?.isNull_or_blank_custom(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}
