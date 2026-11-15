package ua.kpi.pw03.data.remote

import android.content.Context
import retrofit2.HttpException
import ua.kpi.pw03.i18n.AppStrings
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NetworkErrors {
    fun message(error: Throwable): String {
        return when {
            error is SocketTimeoutException -> AppStrings.TIMEOUT
            error is IOException && error.message == "offline" -> AppStrings.OFFLINE
            error is IOException -> AppStrings.OFFLINE
            error is HttpException && error.code() == 401 -> AppStrings.UNAUTHORIZED
            error is HttpException && error.code() >= 500 -> AppStrings.SERVER
            else -> AppStrings.GENERIC
        }
    }

    fun retryable(error: Throwable): Boolean {
        return error is SocketTimeoutException || (error is HttpException && error.code() >= 500)
    }

    fun log(context: Context, text: String) {
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        File(context.filesDir, "network_errors.log").appendText("$stamp $text\n")
    }

    fun readLog(context: Context): String {
        val file = File(context.filesDir, "network_errors.log")
        if (!file.exists()) return AppStrings.NO_LOG
        return file.readText().ifBlank { AppStrings.NO_LOG }
    }
}
