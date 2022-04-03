package com.louis993546.readingqueue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.louis993546.readingqueue.ui.theme.ReadingQueueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadingQueueTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { ReadingQueueBottomBarContainer(currentDestination, navController) }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = ReadingQueueTab.Feed.label,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(ReadingQueueTab.Feed.label) { FeedScreen() }
                        composable(ReadingQueueTab.Queue.label) { QueueScreen() }
                        composable(ReadingQueueTab.Favorite.label) { FavoriteScreen() }
                        composable(ReadingQueueTab.Settings.label) { SettingsScreen() }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PhotoCard(modifier = Modifier.padding(16.dp))
    }
}

// TODO see if it makes more sense to have separate list
@Composable
fun ContentList(
    modifier: Modifier,
    displayMode: DisplayMode,
    content: List<Content>,
) {

}

enum class DisplayMode {
    PhotoCard
}

data class Content(
    val id: String,
    val title: String,
    val subtitle: String,
    val isFavorite: Boolean = false,
    val isInQueue: Boolean = false,
    val photoUrl: String? = null,
    // TODO sth about the source of data (publication/
    // TODO sth about when (how many days? date of fetching?)
)

@Composable
fun QueueScreen(
    modifier: Modifier = Modifier,
) {

}

@Composable
fun FavoriteScreen(
    modifier: Modifier = Modifier,
) {

}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {

}

/**
 * a 3:2 card that showcase the photo first and foremost
 */
@Composable
fun PhotoCard(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.Red)
    ) {
        Text("testing", modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun ReadingQueueBottomBarContainer(
    currentDestination: NavDestination?,
    navController: NavController,
) {
    ReadingQueueBottomBar(
        isSelected = { tab ->
            currentDestination?.hierarchy?.any { it.route == tab.label } == true
        }
    ) { tab ->
        navController.navigate(tab.label) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }

    }
}

@Composable
fun ReadingQueueBottomBar(
    modifier: Modifier = Modifier,
    isSelected: (ReadingQueueTab) -> Boolean,
    onClick: (ReadingQueueTab) -> Unit,
) {
    BottomNavigation(
        modifier = modifier,
    ) {
        ReadingQueueTab.values().forEachIndexed { index, tab ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected(tab)) tab.iconFocus else tab.iconNormal,
                        contentDescription = "tab.label${if (isSelected(tab)) "Selected" else ""}"
                    )
                },
                label = { Text(tab.label) },
                selected = isSelected(tab),
                onClick = { onClick(tab) },
            )
        }
    }
}

enum class ReadingQueueTab(
    val label: String,
    val iconNormal: ImageVector,
    val iconFocus: ImageVector,
) {
    Feed("Feed", Icons.Outlined.Feed, Icons.Filled.Feed),
    Queue("Queue", Icons.Outlined.Inbox, Icons.Filled.Inbox),
    Favorite("Favorite", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    Settings("Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

