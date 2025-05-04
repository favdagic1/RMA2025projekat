package etf.ri.rma.newsfeedapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                    navController    = navController,
                    startDestination = "home",
                    modifier         = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        NewsFeedScreen(navController)
                    }
                    composable("filters") {
                        FilterScreen(navController)
                    }
                    composable("details/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")!!
                        NewsDetailsScreen(navController, newsId = id)
                    }
                }
            }
        }
    }
}
