package ua.kpi.pw03.data.model

data class LightingZone(
    val id: Int,
    val name: String,
    val room: String,
    val wattage: Double,
    val brightness: Int,
    val status: String,
    val lamps: List<String>,
    val forecastWatts: Double
)

enum class DataOrigin {
    NETWORK,
    CACHE
}

data class ZoneBundle(
    val zones: List<LightingZone>,
    val origin: DataOrigin,
    val totalPower: Double
)

enum class UserRole {
    OPERATOR,
    ADMIN,
    NONE
}

data class Session(
    val login: String,
    val role: UserRole,
    val token: String,
    val expiresAt: Long
)

enum class NetScenario {
    SUCCESS,
    OFFLINE,
    TIMEOUT,
    SERVER_ERROR,
    UNAUTHORIZED
}
