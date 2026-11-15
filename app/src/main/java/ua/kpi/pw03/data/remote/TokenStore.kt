package ua.kpi.pw03.data.remote

import android.content.Context
import ua.kpi.pw03.data.model.Session
import ua.kpi.pw03.data.model.UserRole

class TokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("pw03_session", Context.MODE_PRIVATE)

    fun save(session: Session) {
        prefs.edit()
            .putString("login", session.login)
            .putString("role", session.role.name)
            .putString("token", session.token)
            .putLong("expires", session.expiresAt)
            .apply()
    }

    fun read(): Session? {
        val token = prefs.getString("token", null) ?: return null
        val role = runCatching { UserRole.valueOf(prefs.getString("role", "NONE")!!) }
            .getOrDefault(UserRole.NONE)
        return Session(
            login = prefs.getString("login", "") ?: "",
            role = role,
            token = token,
            expiresAt = prefs.getLong("expires", 0L)
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun currentToken(): String? {
        val session = read() ?: return null
        if (session.expiresAt < System.currentTimeMillis()) return null
        return session.token
    }
}
