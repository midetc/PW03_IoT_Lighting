package ua.kpi.pw03.ui.errors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.kpi.pw03.data.model.DataOrigin
import ua.kpi.pw03.data.model.NetScenario
import ua.kpi.pw03.data.remote.NetworkErrors
import ua.kpi.pw03.data.remote.ScenarioStore
import ua.kpi.pw03.data.repo.ZoneRepository
import ua.kpi.pw03.i18n.AppStrings

data class ErrorUiState(
    val message: String = "",
    val log: String = ""
)

class ErrorLabViewModel(private val repository: ZoneRepository) : ViewModel() {
    private val _state = MutableStateFlow(ErrorUiState(log = repository.errorLog()))
    val state: StateFlow<ErrorUiState> = _state.asStateFlow()

    fun run(scenario: NetScenario) {
        ScenarioStore.current = scenario
        viewModelScope.launch {
            val result = repository.loadZones(protectedCall = false)
            ScenarioStore.current = NetScenario.SUCCESS
            result.onSuccess { (_, origin) ->
                val text = when {
                origin == DataOrigin.CACHE && scenario != NetScenario.SUCCESS ->
                    "${label(scenario)} / ${AppStrings.SOURCE_CACHE}"
                scenario == NetScenario.TIMEOUT || scenario == NetScenario.SERVER_ERROR -> AppStrings.RETRY_OK
                else -> AppStrings.OK
            }
                _state.update { it.copy(message = text, log = repository.errorLog()) }
            }.onFailure { error ->
                _state.update {
                    it.copy(message = NetworkErrors.message(error), log = repository.errorLog())
                }
            }
        }
    }

    private fun label(scenario: NetScenario): String {
        return when (scenario) {
            NetScenario.OFFLINE -> AppStrings.OFFLINE
            NetScenario.TIMEOUT -> AppStrings.TIMEOUT
            NetScenario.UNAUTHORIZED -> AppStrings.UNAUTHORIZED
            NetScenario.SERVER_ERROR -> AppStrings.SERVER
            NetScenario.SUCCESS -> AppStrings.OK
        }
    }

    class Factory(private val repository: ZoneRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ErrorLabViewModel(repository) as T
    }
}
