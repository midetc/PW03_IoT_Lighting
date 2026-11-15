package ua.kpi.pw03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ua.kpi.pw03.data.repo.ZoneRepository
import ua.kpi.pw03.ui.auth.AuthScreen
import ua.kpi.pw03.ui.errors.ErrorLabScreen
import ua.kpi.pw03.ui.menu.HomeMenu
import ua.kpi.pw03.ui.repo.ApiScreen
import ua.kpi.pw03.ui.repo.RepoScreen
import ua.kpi.pw03.ui.theme.Pw03Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pw03Theme {
                Surface(modifier = Modifier.fillMaxSize().padding(top = 28.dp)) {
                    AppNav()
                }
            }
        }
    }
}

@Composable
private fun AppNav() {
    val context = LocalContext.current
    val repository = remember { ZoneRepository(context) }
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeMenu(
                onRepo = { nav.navigate("repo") },
                onApi = { nav.navigate("api") },
                onAuth = { nav.navigate("auth") },
                onErrors = { nav.navigate("errors") }
            )
        }
        composable("repo") { RepoScreen(repository, onBack = { nav.popBackStack() }) }
        composable("api") { ApiScreen(repository, onBack = { nav.popBackStack() }) }
        composable("auth") { AuthScreen(repository, onBack = { nav.popBackStack() }) }
        composable("errors") { ErrorLabScreen(repository, onBack = { nav.popBackStack() }) }
    }
}
