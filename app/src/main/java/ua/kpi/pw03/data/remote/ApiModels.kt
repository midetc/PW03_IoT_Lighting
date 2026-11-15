package ua.kpi.pw03.data.remote

data class LampDto(
    val name: String,
    val watts: Double
)

data class ZoneDto(
    val id: Int,
    val name: String,
    val room: String,
    val wattage: Double,
    val brightness: Int,
    val status: String,
    val lamps: List<LampDto>
)

data class ZonesResponse(
    val updatedAt: String,
    val zones: List<ZoneDto>
)

data class ForecastDto(
    val zoneId: Int,
    val predictedWatts: Double
)

data class LoginRequest(
    val login: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val role: String,
    val expiresAt: Long
)
