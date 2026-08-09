package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(amount)
    }

    fun formatHours(hours: Double): String {
        return String.format(Locale.US, "%.1f h", hours)
    }

    fun formatDate(isoDate: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(isoDate) ?: return isoDate
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale("es", "US"))
            formatter.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatTime12(hhmm: String?): String {
        if (hhmm.isNullOrBlank()) return "--:--"
        return try {
            val parts = hhmm.split(":")
            if (parts.size < 2) return "--:--"
            val h = parts[0].toIntOrNull() ?: return "--:--"
            val m = parts[1]
            val ampm = if (h >= 12) "PM" else "AM"
            val h12 = if (h % 12 == 0) 12 else h % 12
            "$h12:$m $ampm"
        } catch (e: Exception) {
            "--:--"
        }
    }

    fun formatTimeRange(start: String, end: String?): String {
        val formattedEnd = if (end.isNullOrBlank()) "En curso" else formatTime12(end)
        return "${formatTime12(start)} — $formattedEnd"
    }

    fun getCurrentDateIso(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }

    fun getCurrentTimeHHMM(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.US)
        return formatter.format(Date())
    }

    fun getWeekStartIso(offsetWeeks: Int = 0): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val diff = (if (day == Calendar.SUNDAY) -6 else Calendar.MONDAY - day) + offsetWeeks * 7
        calendar.add(Calendar.DAY_OF_MONTH, diff)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(calendar.time)
    }

    fun getWeekEndIso(weekStartIso: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(weekStartIso) ?: return weekStartIso
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_MONTH, 6)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            formatter.format(calendar.time)
        } catch (e: Exception) {
            weekStartIso
        }
    }

    fun formatWeekRangeLabel(weekStartIso: String, weekEndIso: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val start = parser.parse(weekStartIso) ?: return "$weekStartIso - $weekEndIso"
            val end = parser.parse(weekEndIso) ?: return "$weekStartIso - $weekEndIso"
            val fmt = SimpleDateFormat("MMM d", Locale("es", "US"))
            val fmtEnd = SimpleDateFormat("MMM d, yyyy", Locale("es", "US"))
            "${fmt.format(start)} - ${fmtEnd.format(end)}"
        } catch (e: Exception) {
            "$weekStartIso - $weekEndIso"
        }
    }

    fun getMinutesBetween(startHHMM: String, endHHMM: String): Int {
        return try {
            val p1 = startHHMM.split(":")
            val p2 = endHHMM.split(":")
            val m1 = p1[0].toInt() * 60 + p1[1].toInt()
            val m2 = p2[0].toInt() * 60 + p2[1].toInt()
            var diff = m2 - m1
            if (diff < 0) diff += 1440 // Midnight cross
            diff
        } catch (e: Exception) {
            0
        }
    }

    fun calculateEarnings(
        totalHours: Double,
        baseRate: Double,
        overtimeHours: Double,
        overtimeMultiplier: Double
    ): Double {
        val baseHours = Math.max(0.0, totalHours - overtimeHours)
        val baseEarnings = baseHours * baseRate
        val otEarnings = overtimeHours * baseRate * overtimeMultiplier
        return Math.round((baseEarnings + otEarnings) * 100.0) / 100.0
    }

    private fun String?.isNull_or_blank_custom(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}
