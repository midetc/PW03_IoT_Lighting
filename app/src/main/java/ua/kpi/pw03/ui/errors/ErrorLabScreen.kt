package ua.kpi.pw03.ui.errors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.pw03.data.model.NetScenario
import ua.kpi.pw03.data.repo.ZoneRepository
import ua.kpi.pw03.i18n.AppStrings

@Composable
fun ErrorLabScreen(
    repository: ZoneRepository,
    onBack: () -> Unit,
    model: ErrorLabViewModel = viewModel(factory = ErrorLabViewModel.Factory(repository))
) {
    val state by model.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(AppStrings.MENU_ERRORS, style = MaterialTheme.typography.titleLarge)
        Text(AppStrings.SCENARIO)
        NetScenario.entries.forEach { scenario ->
            Button(onClick = { model.run(scenario) }, modifier = Modifier.fillMaxWidth()) {
                Text(scenario.name)
            }
        }
        if (state.message.isNotBlank()) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
        }
        Text("${AppStrings.ERROR_LOG}:")
        Text(state.log)
        TextButton(onClick = onBack) { Text(AppStrings.BACK) }
    }
}
