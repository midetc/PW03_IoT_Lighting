package ua.kpi.pw03.ui.repo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.kpi.pw03.data.model.DataOrigin
import ua.kpi.pw03.data.model.LightingZone
import ua.kpi.pw03.data.repo.ZoneRepository
import ua.kpi.pw03.i18n.AppStrings

data class RepoUiState(
    val zones: List<LightingZone> = emptyList(),
    val origin: String = "",
    val total: Double = 0.0,
    val query: String = "",
    val sortByPower: Boolean = true,
    val name: String = "",
    val room: String = "",
    val wattage: String = "",
    val message: String = "",
    val loading: Boolean = false
)

class RepoViewModel(private val repository: ZoneRepository) : ViewModel() {
    private val _state = MutableStateFlow(RepoUiState())
    val state: StateFlow<RepoUiState> = _state.asStateFlow()

    init { refresh() }

    fun onQuery(value: String) = _state.update { it.copy(query = value) }
    fun onName(value: String) = _state.update { it.copy(name = value) }
    fun onRoom(value: String) = _state.update { it.copy(room = value) }
    fun onWattage(value: String) = _state.update { it.copy(wattage = value) }
    fun toggleSort() = _state.update { it.copy(sortByPower = !it.sortByPower) }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = AppStrings.LOADING) }
            val result = repository.loadZones(protectedCall = false)
            result.onSuccess { (zones, origin) ->
                _state.update {
                    it.copy(
                        zones = zones,
                        origin = if (origin == DataOrigin.NETWORK) AppStrings.SOURCE_REMOTE else AppStrings.SOURCE_CACHE,
                        total = zones.sumOf { zone -> zone.wattage * zone.brightness / 100.0 },
                        loading = false,
                        message = ""
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(loading = false, message = error.message ?: AppStrings.GENERIC) }
            }
        }
    }

    fun saveLocal() {
        val current = _state.value
        val watts = current.wattage.toDoubleOrNull()
        if (current.name.isBlank() || current.room.isBlank() || watts == null) {
            _state.update { it.copy(message = AppStrings.FILL) }
            return
        }
        viewModelScope.launch {
            repository.addLocal(current.name, current.room, watts)
            _state.update { it.copy(name = "", room = "", wattage = "", message = AppStrings.SOURCE_CACHE) }
            refresh()
        }
    }

    fun visible(): List<LightingZone> {
        val state = _state.value
        val filtered = state.zones.filter {
            state.query.isBlank() || it.room.contains(state.query, true) || it.name.contains(state.query, true)
        }
        return if (state.sortByPower) filtered.sortedByDescending { it.wattage } else filtered.sortedBy { it.name }
    }

    class Factory(private val repository: ZoneRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RepoViewModel(repository) as T
    }
}
