package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BreakSegment
import com.example.data.model.DestinationVisit
import com.example.data.model.SavedDestination
import com.example.data.model.TimelineEvent
import com.example.data.model.TransferSegment
import com.example.data.model.UserPreferences
import com.example.data.model.WalLogEntry
import com.example.data.model.WorkSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkSessionDao {

    // --- WorkSession Queries ---
    @Query("SELECT * FROM sessions ORDER BY date DESC, startTime DESC")
    fun getAllSessions(): Flow<List<WorkSession>>

    @Query("SELECT * FROM sessions ORDER BY date DESC, startTime DESC")
    suspend fun getAllSessionsList(): List<WorkSession>

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): WorkSession?

    @Query("SELECT * FROM sessions WHERE state != 'CERRADA' AND state != 'NO_INICIADA' LIMIT 1")
    suspend fun getActiveSession(): WorkSession?

    @Query("SELECT * FROM sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, startTime DESC")
    fun getSessionsByPeriod(startDate: String, endDate: String): Flow<List<WorkSession>>

    @Query("SELECT * FROM sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, startTime DESC")
    suspend fun getSessionsListByPeriod(startDate: String, endDate: String): List<WorkSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkSession)

    @Update
    suspend fun updateSession(session: WorkSession)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    // --- DestinationVisits Queries ---
    @Query("SELECT * FROM destination_visits WHERE sessionId = :sessionId ORDER BY enterTime ASC")
    fun getVisitsForSession(sessionId: String): Flow<List<DestinationVisit>>

    @Query("SELECT * FROM destination_visits WHERE sessionId = :sessionId ORDER BY enterTime ASC")
    suspend fun getVisitsListForSession(sessionId: String): List<DestinationVisit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: DestinationVisit)

    @Query("DELETE FROM destination_visits WHERE sessionId = :sessionId")
    suspend fun deleteVisitsForSession(sessionId: String)

    // --- Transfers Queries ---
    @Query("SELECT * FROM transfers WHERE sessionId = :sessionId ORDER BY startTime ASC")
    fun getTransfersForSession(sessionId: String): Flow<List<TransferSegment>>

    @Query("SELECT * FROM transfers WHERE sessionId = :sessionId ORDER BY startTime ASC")
    suspend fun getTransfersListForSession(sessionId: String): List<TransferSegment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferSegment)

    @Query("DELETE FROM transfers WHERE sessionId = :sessionId")
    suspend fun deleteTransfersForSession(sessionId: String)

    // --- Breaks Queries ---
    @Query("SELECT * FROM breaks WHERE sessionId = :sessionId ORDER BY startTime ASC")
    fun getBreaksForSession(sessionId: String): Flow<List<BreakSegment>>

    @Query("SELECT * FROM breaks WHERE sessionId = :sessionId ORDER BY startTime ASC")
    suspend fun getBreaksListForSession(sessionId: String): List<BreakSegment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreak(breakSegment: BreakSegment)

    @Query("DELETE FROM breaks WHERE sessionId = :sessionId")
    suspend fun deleteBreaksForSession(sessionId: String)

    // --- TimelineEvents Queries ---
    @Query("SELECT * FROM timeline_events WHERE sessionId = :sessionId ORDER BY sequence ASC")
    fun getTimelineEventsForSession(sessionId: String): Flow<List<TimelineEvent>>

    @Query("SELECT * FROM timeline_events WHERE sessionId = :sessionId ORDER BY sequence ASC")
    suspend fun getTimelineListForSession(sessionId: String): List<TimelineEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvent(event: TimelineEvent)

    @Query("DELETE FROM timeline_events WHERE sessionId = :sessionId")
    suspend fun deleteTimelineEventsForSession(sessionId: String)

    // --- Preferences Queries ---
    @Query("SELECT * FROM user_preferences WHERE id = 'user_settings' LIMIT 1")
    fun getUserPreferencesFlow(): Flow<UserPreferences?>

    @Query("SELECT * FROM user_preferences WHERE id = 'user_settings' LIMIT 1")
    suspend fun getUserPreferences(): UserPreferences?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPreferences(prefs: UserPreferences)

    // --- WAL Log ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalEntry(entry: WalLogEntry)

    @Query("SELECT * FROM wal_log WHERE sessionId = :sessionId ORDER BY createdMs ASC")
    suspend fun getWalEntriesForSession(sessionId: String): List<WalLogEntry>

    // --- Saved Destinations Queries ---
    @Query("SELECT * FROM saved_destinations ORDER BY createdMs DESC")
    fun getAllSavedDestinations(): Flow<List<SavedDestination>>

    @Query("SELECT * FROM saved_destinations ORDER BY createdMs DESC")
    suspend fun getSavedDestinationsList(): List<SavedDestination>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedDestination(destination: SavedDestination)

    @Query("DELETE FROM saved_destinations WHERE id = :id")
    suspend fun deleteSavedDestinationById(id: String)
}
