package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_destinations")
data class SavedDestination(
    @PrimaryKey val id: String,
    val alias: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int = 150,
    val category: String = "Obra",
    val visitCount: Int = 0,
    val isPrimary: Boolean = false,
    val createdMs: Long = System.currentTimeMillis()
)
