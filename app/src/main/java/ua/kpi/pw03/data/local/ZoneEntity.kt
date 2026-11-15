package ua.kpi.pw03.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zones")
data class ZoneEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val room: String,
    val wattage: Double,
    val brightness: Int,
    val status: String,
    val lamps: String,
    val forecastWatts: Double
)
