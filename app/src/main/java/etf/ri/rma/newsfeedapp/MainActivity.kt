package etf.ri.rma.newsfeedapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import etf.ri.rma.newsfeedapp.screen.FilterScreen
import etf.ri.rma.newsfeedapp.screen.NewsDetailsScreen
import etf.ri.rma.newsfeedapp.screen.NewsFeedScreen
import etf.ri.rma.newsfeedapp.ui.theme.NewsFeedAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsFeedAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "/home"
                ) {
                    // Main news list screen
                    composable("/home") {
                        NewsFeedScreen(navController)
                    }
                    // Advanced filters screen
                    composable("/filters") {
                        FilterScreen(navController)
                    }
                    // News details screen
                    composable(
                        route = "/details/{newsId}",
                        arguments = listOf(
                            navArgument("newsId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("newsId")
                            ?: return@composable
                        NewsDetailsScreen(navController, newsId = id)
                    }
                }
            }
        }
    }
}