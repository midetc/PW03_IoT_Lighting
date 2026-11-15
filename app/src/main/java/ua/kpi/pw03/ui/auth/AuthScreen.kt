package ua.kpi.pw03.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.pw03.data.model.UserRole
import ua.kpi.pw03.data.repo.ZoneRepository
import ua.kpi.pw03.i18n.AppStrings
import ua.kpi.pw03.ui.repo.ZoneCard

@Composable
fun AuthScreen(
    repository: ZoneRepository,
    onBack: () -> Unit,
    model: AuthViewModel = viewModel(factory = AuthViewModel.Factory(repository))
) {
    val state by model.state.collectAsStateWithLifecycle()
    var login by remember { mutableStateOf("operator") }
    var password by remember { mutableStateOf("operator") }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(AppStrings.MENU_AUTH, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(login, { login = it }, Modifier.fillMaxWidth(), label = { Text(AppStrings.LOGIN) })
        OutlinedTextField(
            password,
            { password = it },
            Modifier.fillMaxWidth(),
            label = { Text(AppStrings.PASSWORD) },
            visualTransformation = PasswordVisualTransformation()
        )
        Button(onClick = { model.signIn(login, password) }, modifier = Modifier.fillMaxWidth()) {
            Text(AppStrings.SIGN_IN)
        }
        if (state.role != UserRole.NONE) {
            Text("${AppStrings.ROLE}: ${state.role.name}")
            Text("${AppStrings.TOKEN}: ${state.token.take(28)}...")
            Button(onClick = model::loadProtected, modifier = Modifier.fillMaxWidth()) {
                Text(AppStrings.SECURE)
            }
        }
        if (state.role == UserRole.ADMIN) {
            Text(AppStrings.ADMIN_PANEL, style = MaterialTheme.typography.titleMedium)
            Button(onClick = model::clearCache, modifier = Modifier.fillMaxWidth()) {
                Text(AppStrings.CLEAR_CACHE)
            }
            Text(AppStrings.ERROR_LOG)
            Text(state.log)
        }
        if (state.message.isNotBlank()) Text(state.message)
        state.zones.forEach { ZoneCard(it, showLamps = true) }
        if (state.role != UserRole.NONE) {
            Button(onClick = model::signOut, modifier = Modifier.fillMaxWidth()) { Text(AppStrings.SIGN_OUT) }
        }
        TextButton(onClick = onBack) { Text(AppStrings.BACK) }
    }
}
