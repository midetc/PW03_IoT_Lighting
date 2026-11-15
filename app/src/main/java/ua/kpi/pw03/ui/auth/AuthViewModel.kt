package ua.kpi.pw03.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.kpi.pw03.data.model.LightingZone
import ua.kpi.pw03.data.model.UserRole
import ua.kpi.pw03.data.remote.NetworkErrors
import ua.kpi.pw03.data.repo.ZoneRepository
import ua.kpi.pw03.i18n.AppStrings

data class AuthUiState(
    val role: UserRole = UserRole.NONE,
    val token: String = "",
    val message: String = "",
    val zones: List<LightingZone> = emptyList(),
    val log: String = ""
)

class AuthViewModel(private val repository: ZoneRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init { restore() }

    private fun restore() {
        val session = repository.session() ?: return
        if (session.expiresAt < System.currentTimeMillis()) {
            repository.signOut()
            _state.update { it.copy(message = AppStrings.EXPIRED) }
            return
        }
        _state.update { it.copy(role = session.role, token = session.token, log = repository.errorLog()) }
    }

    fun signIn(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            _state.update { it.copy(message = AppStrings.FILL) }
            return
        }
        viewModelScope.launch {
            val result = repository.login(login, password)
            result.onSuccess { session ->
                _state.update {
                    it.copy(role = session.role, token = session.token, message = AppStrings.LOGIN_OK, log = repository.errorLog())
                }
            }.onFailure { error ->
                _state.update { it.copy(message = NetworkErrors.message(error).let { AppStrings.BAD_CREDENTIALS }) }
            }
        }
    }

    fun loadProtected() {
        if (_state.value.token.isBlank()) {
            _state.update { it.copy(message = AppStrings.NO_TOKEN) }
            return
        }
        viewModelScope.launch {
            val result = repository.loadZones(protectedCall = true)
            result.onSuccess { (zones, _) ->
                _state.update { it.copy(zones = zones, message = AppStrings.OK) }
            }.onFailure { error ->
                _state.update { it.copy(message = NetworkErrors.message(error), log = repository.errorLog()) }
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            _state.update { it.copy(zones = emptyList(), message = AppStrings.SOURCE_CACHE) }
        }
    }

    fun signOut() {
        repository.signOut()
        _state.value = AuthUiState(message = AppStrings.SIGN_OUT)
    }

    class Factory(private val repository: ZoneRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repository) as T
    }
}
