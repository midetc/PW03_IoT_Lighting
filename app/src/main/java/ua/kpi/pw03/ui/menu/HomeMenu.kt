package ua.kpi.pw03.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.kpi.pw03.i18n.AppStrings

@Composable
fun HomeMenu(
    onRepo: () -> Unit,
    onApi: () -> Unit,
    onAuth: () -> Unit,
    onErrors: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(AppStrings.APP_TITLE, style = MaterialTheme.typography.titleLarge)
        MenuButton(AppStrings.MENU_REPO, onRepo)
        MenuButton(AppStrings.MENU_API, onApi)
        MenuButton(AppStrings.MENU_AUTH, onAuth)
        MenuButton(AppStrings.MENU_ERRORS, onErrors)
    }
}

@Composable
private fun MenuButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label)
    }
}
