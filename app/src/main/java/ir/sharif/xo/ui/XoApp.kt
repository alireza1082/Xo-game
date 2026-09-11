package ir.sharif.xo.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.sharif.xo.GameBoardViewModel
import ir.sharif.xo.audio.SoundManager
import ir.sharif.xo.data.GamePreferences
import ir.sharif.xo.engine.GameMode
import ir.sharif.xo.engine.Player

private object Routes {
    const val HOME = "home"
    const val GAME = "game/{mode}/{human}"
    const val STATS = "stats"
    const val ABOUT = "about"
}

@Composable
fun XoApp(preferences: GamePreferences, soundManager: SoundManager) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                preferences = preferences,
                onStartGame = { mode, human -> navController.navigate("game/${mode.name}/${human.name}") },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) }
            )
        }
        composable(
            Routes.GAME,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("human") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode")?.let { runCatching { GameMode.valueOf(it) }.getOrNull() }
                ?: GameMode.TWO_PLAYERS
            val human = backStackEntry.arguments?.getString("human")?.let { runCatching { Player.valueOf(it) }.getOrNull() }
                ?: Player.X
            val gameViewModel: GameBoardViewModel = viewModel(
                factory = GameBoardViewModel.factory(mode, human, preferences, soundManager)
            )
            GameScreen(gameViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.STATS) { StatsScreen(preferences, onBack = { navController.popBackStack() }) }
        composable(Routes.ABOUT) { AboutScreen(onBack = { navController.popBackStack() }) }
    }
}
