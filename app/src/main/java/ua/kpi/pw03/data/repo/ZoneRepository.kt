package ua.kpi.pw03.data.repo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import ua.kpi.pw03.data.local.AppDatabase
import ua.kpi.pw03.data.local.ZoneEntity
import ua.kpi.pw03.data.model.DataOrigin
import ua.kpi.pw03.data.model.LightingZone
import ua.kpi.pw03.data.model.Session
import ua.kpi.pw03.data.model.UserRole
import ua.kpi.pw03.data.remote.ApiClient
import ua.kpi.pw03.data.remote.LoginRequest
import ua.kpi.pw03.data.remote.NetworkErrors
import ua.kpi.pw03.data.remote.TokenStore
import ua.kpi.pw03.data.remote.ZoneDto
import java.io.IOException

class ZoneRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dao = AppDatabase.get(appContext).zoneDao()
    private val tokens = TokenStore(appContext)
    private val api = ApiClient.create(tokens)

    fun session(): Session? = tokens.read()

    fun signOut() = tokens.clear()

    suspend fun login(login: String, password: String): Result<Session> {
        return try {
            val response = api.login(LoginRequest(login.trim(), password))
            val role = runCatching { UserRole.valueOf(response.role) }.getOrDefault(UserRole.OPERATOR)
            val session = Session(login.trim(), role, response.token, response.expiresAt)
            tokens.save(session)
            Result.success(session)
        } catch (error: Exception) {
            NetworkErrors.log(appContext, "login ${NetworkErrors.message(error)}")
            Result.failure(error)
        }
    }

    suspend fun loadZones(protectedCall: Boolean): Result<Pair<List<LightingZone>, DataOrigin>> {
        if (!isOnline()) {
            NetworkErrors.log(appContext, "offline")
            val cached = dao.all().map { it.toModel() }
            return if (cached.isEmpty()) {
                Result.failure(IOException("offline"))
            } else {
                Result.success(cached to DataOrigin.CACHE)
            }
        }
        return try {
            val zones = fetchWithRetry(protectedCall)
            dao.clear()
            dao.insertAll(zones.map { it.toEntity() })
            Result.success(zones to DataOrigin.NETWORK)
        } catch (error: Exception) {
            NetworkErrors.log(appContext, NetworkErrors.message(error))
            val cached = dao.all().map { it.toModel() }
            if (cached.isEmpty()) Result.failure(error) else Result.success(cached to DataOrigin.CACHE)
        }
    }

    suspend fun addLocal(name: String, room: String, wattage: Double) {
        val id = (dao.all().maxOfOrNull { it.id } ?: 10) + 1
        dao.insert(
            ZoneEntity(
                id = id,
                name = name,
                room = room,
                wattage = wattage,
                brightness = 100,
                status = "local",
                lamps = name,
                forecastWatts = wattage
            )
        )
    }

    suspend fun clearCache() = dao.clear()

    fun errorLog(): String = NetworkErrors.readLog(appContext)

    private suspend fun fetchWithRetry(protectedCall: Boolean): List<LightingZone> {
        var last: Exception? = null
        repeat(2) { attempt ->
            try {
                val zones = if (protectedCall) api.getSecureZones() else api.getZones()
                val forecast = runCatching { api.getForecast() }.getOrDefault(emptyList())
                val byId = forecast.associate { it.zoneId to it.predictedWatts }
                return zones.zones.map { dto -> dto.toModel(byId[dto.id] ?: dto.wattage) }
            } catch (error: Exception) {
                last = error
                if (attempt == 0 && NetworkErrors.retryable(error)) return@repeat
                throw error
            }
        }
        throw last ?: IllegalStateException("request failed")
    }

    private fun isOnline(): Boolean {
        val manager = appContext.getSystemService(ConnectivityManager::class.java) ?: return true
        val network = manager.activeNetwork ?: return false
        val caps = manager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

private fun ZoneDto.toModel(forecast: Double): LightingZone {
    return LightingZone(
        id = id,
        name = name,
        room = room,
        wattage = wattage,
        brightness = brightness,
        status = status,
        lamps = lamps.map { it.name },
        forecastWatts = forecast
    )
}

private fun LightingZone.toEntity(): ZoneEntity {
    return ZoneEntity(id, name, room, wattage, brightness, status, lamps.joinToString(", "), forecastWatts)
}

private fun ZoneEntity.toModel(): LightingZone {
    return LightingZone(
        id, name, room, wattage, brightness, status,
        lamps.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        forecastWatts
    )
}
