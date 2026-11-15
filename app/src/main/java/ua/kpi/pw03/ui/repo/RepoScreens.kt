package ua.kpi.pw03.ui.repo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.pw03.data.model.LightingZone
import ua.kpi.pw03.data.repo.ZoneRepository
import ua.kpi.pw03.i18n.AppStrings

@Composable
fun RepoScreen(
    repository: ZoneRepository,
    onBack: () -> Unit,
    model: RepoViewModel = viewModel(factory = RepoViewModel.Factory(repository))
) {
    val state by model.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(AppStrings.MENU_REPO, style = MaterialTheme.typography.titleLarge)
        Text(state.origin)
        Text("${AppStrings.TOTAL}: ${"%.1f".format(state.total)} ${AppStrings.WATTS}")
        OutlinedTextField(
            value = state.query,
            onValueChange = model::onQuery,
            label = { Text(AppStrings.FILTER_HINT) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = model::toggleSort, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.sortByPower) AppStrings.SORT_POWER else AppStrings.SORT_NAME)
        }
        Button(onClick = model::refresh, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
            Text(AppStrings.REFRESH)
        }
        OutlinedTextField(state.name, model::onName, Modifier.fillMaxWidth(), label = { Text(AppStrings.NAME) })
        OutlinedTextField(state.room, model::onRoom, Modifier.fillMaxWidth(), label = { Text(AppStrings.ROOM) })
        OutlinedTextField(state.wattage, model::onWattage, Modifier.fillMaxWidth(), label = { Text(AppStrings.WATTAGE) })
        Button(onClick = model::saveLocal, modifier = Modifier.fillMaxWidth()) { Text(AppStrings.SAVE) }
        if (state.message.isNotBlank()) Text(state.message)
        model.visible().forEach { zone -> ZoneCard(zone, showLamps = false) }
        TextButton(onClick = onBack) { Text(AppStrings.BACK) }
    }
}

@Composable
fun ApiScreen(
    repository: ZoneRepository,
    onBack: () -> Unit,
    model: RepoViewModel = viewModel(factory = RepoViewModel.Factory(repository))
) {
    val state by model.state.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(AppStrings.MENU_API, style = MaterialTheme.typography.titleLarge)
        Text(state.origin.ifBlank { AppStrings.LOADING })
        Button(onClick = model::refresh, modifier = Modifier.fillMaxWidth()) { Text(AppStrings.REFRESH) }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(model.visible(), key = { it.id }) { zone -> ZoneCard(zone, showLamps = true) }
        }
        TextButton(onClick = onBack) { Text(AppStrings.BACK) }
    }
}

@Composable
fun ZoneCard(zone: LightingZone, showLamps: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${zone.name} · ${zone.room}", style = MaterialTheme.typography.titleMedium)
            Text("${zone.wattage} ${AppStrings.WATTS}, ${zone.brightness}%, ${zone.status}")
            if (showLamps) {
                Text("${AppStrings.LAMPS}: ${zone.lamps.joinToString()}")
                Text("${AppStrings.FORECAST}: ${zone.forecastWatts} ${AppStrings.WATTS}")
            }
        }
    }
}
