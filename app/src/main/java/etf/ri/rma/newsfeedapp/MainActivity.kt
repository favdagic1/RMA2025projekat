package etf.ri.rma.newsfeedapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import etf.ri.rma.newsfeedapp.screen.FilterScreen
import etf.ri.rma.newsfeedapp.screen.NewsDetailsScreen
import etf.ri.rma.newsfeedapp.screen.NewsFeedScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            NewsFeedScreen(navController)
                        }
                        composable("filters") {
                            FilterScreen(navController)
                        }
                        composable(
                            route = "details/{newsId}",
                            arguments = listOf(navArgument("newsId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val newsId = backStackEntry.arguments?.getString("newsId") ?: return@composable
                            NewsDetailsScreen(navController, newsId)
                        }
                    }
                }
            }
        }
    }
}
