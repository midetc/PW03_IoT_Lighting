package ua.kpi.pw03.data.remote

import android.util.Base64
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import ua.kpi.pw03.data.model.NetScenario
import java.net.SocketTimeoutException
import java.io.IOException

class MockApiInterceptor : Interceptor {
    private val gson = Gson()
    private val json = "application/json".toMediaType()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val scenario = ScenarioStore.current
        if (!path.endsWith("auth/login")) {
            when (scenario) {
                NetScenario.OFFLINE -> throw IOException("offline")
                NetScenario.TIMEOUT -> throw SocketTimeoutException("timeout")
                NetScenario.SERVER_ERROR -> return error(request, 500, "server")
                NetScenario.UNAUTHORIZED -> return error(request, 401, "unauthorized")
                NetScenario.SUCCESS -> Unit
            }
        }
        val body = when {
            path.endsWith("auth/login") -> loginBody(request)
            path.endsWith("secure/zones") -> secureZones(request)
            path.endsWith("forecast") -> gson.toJson(forecast())
            else -> gson.toJson(zonesPayload())
        }
        if (body == null) return error(request, 401, "unauthorized")
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body.toResponseBody(json))
            .build()
    }

    private fun loginBody(request: okhttp3.Request): String? {
        val raw = request.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        } ?: return null
        val creds = gson.fromJson(raw, LoginRequest::class.java)
        val role = when {
            creds.login == "admin" && creds.password == "admin" -> "ADMIN"
            creds.login == "operator" && creds.password == "operator" -> "OPERATOR"
            else -> return null
        }
        val exp = System.currentTimeMillis() + 10 * 60 * 1000
        val payload = Base64.encodeToString(
            """{"role":"$role","exp":$exp}""".toByteArray(),
            Base64.NO_WRAP
        )
        val token = "mock.$payload.sig"
        return gson.toJson(LoginResponse(token, role, exp))
    }

    private fun secureZones(request: okhttp3.Request): String? {
        val header = request.header("Authorization") ?: return null
        val token = header.removePrefix("Bearer ").trim()
        val parts = token.split(".")
        if (parts.size < 2) return null
        val json = String(Base64.decode(parts[1], Base64.NO_WRAP))
        if (!json.contains("OPERATOR") && !json.contains("ADMIN")) return null
        return gson.toJson(zonesPayload())
    }

    private fun zonesPayload(): ZonesResponse {
        return ZonesResponse(
            updatedAt = "2026-11-08T12:00:00Z",
            zones = listOf(
                ZoneDto(1, "Hall LED", "Hall", 48.0, 80, "online", listOf(LampDto("Strip A", 24.0), LampDto("Strip B", 24.0))),
                ZoneDto(2, "Kitchen spots", "Kitchen", 36.0, 60, "online", listOf(LampDto("Spot 1", 12.0), LampDto("Spot 2", 12.0), LampDto("Spot 3", 12.0))),
                ZoneDto(3, "Yard lanterns", "Yard", 90.0, 40, "offline", listOf(LampDto("Lantern", 90.0)))
            )
        )
    }

    private fun forecast(): List<ForecastDto> {
        return listOf(
            ForecastDto(1, 42.0),
            ForecastDto(2, 30.0),
            ForecastDto(3, 70.0)
        )
    }

    private fun error(request: okhttp3.Request, code: Int, message: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body("""{"error":"$message"}""".toResponseBody(json))
            .build()
    }
}
