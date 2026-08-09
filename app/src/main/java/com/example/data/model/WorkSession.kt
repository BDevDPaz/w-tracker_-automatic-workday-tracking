package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class WorkSession(
    @PrimaryKey val id: String,
    val date: String, // YYYY-MM-DD
    val destination: String,
    val alias: String? = null,
    val startTime: String, // HH:MM
    val endTime: String? = null, // HH:MM
    val totalHours: Double = 0.0,
    val lunchDeductedMinutes: Int = 0,
    val overtimeHours: Double = 0.0,
    val snapshotRate: Double = 20.0,
    val totalEarnings: Double = 0.0,
    val integrityType: String = IntegrityType.VALID.name,
    val notes: String? = null,
    val state: String = SessionState.NO_INICIADA.name,
    val paidMinutes: Int = 0,
    val pauseMinutes: Int = 0,
    val transferMinutes: Int = 0,
    val destinationCount: Int = 1,
    val lat: Double? = null,
    val lng: Double? = null,
    val createdMs: Long = System.currentTimeMillis(),
    val updatedMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "destination_visits")
data class DestinationVisit(
    @PrimaryKey val id: String,
    val sessionId: String,
    val destination: String,
    val alias: String? = null,
    val enterTime: String,
    val exitTime: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

@Entity(tableName = "transfers")
data class TransferSegment(
    @PrimaryKey val id: String,
    val sessionId: String,
    val originDestination: String,
    val targetDestination: String,
    val startTime: String,
    val endTime: String? = null
)

@Entity(tableName = "breaks")
data class BreakSegment(
    @PrimaryKey val id: String,
    val sessionId: String,
    val startTime: String,
    val endTime: String? = null,
    val durationMinutes: Int = 0
)

@Entity(tableName = "timeline_events")
data class TimelineEvent(
    @PrimaryKey val id: String,
    val sessionId: String,
    val timestamp: String,
    val description: String,
    val status: String,
    val sequence: Int,
    val metadata: String? = null
)

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val id: String = "user_settings",
    val name: String = "",
    val hourlyRate: Double = 20.0,
    val reportEmail: String = "",
    val baseWorkHour: String = "08:00",
    val overtimeMultiplier: Double = 1.5,
    val lunchDiscountMinutes: Int = 30,
    val geofenceRadius: Int = 100,
    val dailyReminder: Boolean = true,
    val workdayAlerts: Boolean = true,
    val multiDestination: Boolean = true,
    val paidTransfers: Boolean = true,
    val destinationLimit: Int = 5,
    val isOnboardingCompleted: Boolean = false,
    val isDisclaimerAccepted: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val unrestrictedBatteryConfigured: Boolean = false,
    val backgroundExecutionEnabled: Boolean = true,
    val updatedMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "wal_log")
data class WalLogEntry(
    @PrimaryKey val id: String,
    val sessionId: String,
    val eventType: String,
    val payload: String,
    val createdMs: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
