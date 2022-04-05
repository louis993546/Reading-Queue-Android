package com.louis993546.readingqueue


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.room.Room
import coil.compose.AsyncImage
import com.louis993546.readingqueue.ui.theme.ReadingQueueTheme
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.DocElement
import it.skrape.selects.html5.head
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {
    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            /* context = */ applicationContext,
            /* klass = */ AppDatabase::class.java,
            /* name = */ "reading-queue"
        ).build()
    }

    val repo: ContentRepository by lazy {
        ContentRepository(db.contentDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadingQueueTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val coroutineScope = rememberCoroutineScope()

                val content by repo
                    .getAll()
                    .collectAsState(emptyList())

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        ReadingQueueBottomBarContainer(
                            currentDestination,
                            navController
                        )
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.FeedList.name,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.FeedList.name) {
                            FeedListScreen { navController.navigate(ReadingQueueTab.Feed.label) }
                        }
                        composable(ReadingQueueTab.Feed.label) {
                            FeedScreen(content = content) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    parse()
                                }
                            }
                        }
                        composable(ReadingQueueTab.Queue.label) { QueueScreen() }
                        composable(ReadingQueueTab.Search.label) { SearchScreen() }
                        composable(ReadingQueueTab.Favorite.label) { FavoriteScreen() }
                        composable(ReadingQueueTab.Settings.label) { SettingsScreen() }
                    }
                }

                LaunchedEffect("on start") {
                    navController.navigate(ReadingQueueTab.Feed.label)
                }
            }
        }
    }

    private suspend fun parse() {
        skrape(AsyncFetcher) {
            request {
//                url = "https://www.theverge.com/2022/4/5/23011377/germany-servers-russian-darknet-site-hydra-bitcoin"
//                url = "https://www.engadget.com/nikon-z-9-mirrorless-camera-review-143006924.html"
//                url = "https://www.youtube.com/watch?v=edNQeb0JDlk"
//                url = "https://stackoverflow.com/questions/16102226/link-from-html-href-to-native-app"
//                url = "https://en.wikipedia.org/wiki/RSS"
//                url = "https://github.com/skrapeit/skrape.it"
//                url = "https://beta.nebula.app/videos/wendover-the-incredible-logistics-behind-corn-farming" // somehow it gets nothing?
                url = "https://twitter.com/MILFWEEED/status/1510967445619712001"
            }
            response {
                htmlDocument {
                    Timber.tag("qqq article title").d(titleText)
                    head {
                        val properties = findAll("meta")
                            .filter { doc -> doc.hasAttribute("property") }

                        properties.forEach {
                            Timber.tag("qqqq").d(it.toString())
                        }

                        // based on open graph https://ogp.me/
                        val imageUrl = properties.findProperty("og:image")
                        val imageAlt = properties.findProperty("og:image:alt")
                        val author = properties.findProperty("article:author")
                        val description = properties.findProperty("og:description")
                        val siteName = properties.findProperty("og:site_name")
                        val title = properties.findProperty("og:title")
                        val type = properties.findProperty("og:type")
                        val publishTime = properties.findProperty("article:published_time")
                        val modifiedTime = properties.findProperty("article:modified_time")

                        Timber.tag("qqq").d("""
                            title: $title
                            description: $description
                            author: $author
                            site name: $siteName
                            type: $type
                            image: $imageUrl
                            imageAlt: $imageAlt
                            publish time: $publishTime
                            modified time: $modifiedTime
                        """.trimIndent())
                    }
                }
            }
        }
    }

    // TODO maybe add one to support array for images? https://ogp.me/#array
    private fun List<DocElement>.findProperty(
        of: String,
        attributeKey: String = "content"
    ): String? = firstOrNull { it.attribute("property") == of }
        ?.attribute(attributeKey)
}

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
) {
    var searchInput by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        TextField(
            value = searchInput,
            onValueChange = { searchInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
        )
    }
}

@Composable
fun FeedListScreen(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
) {
    Column(modifier = modifier) {
        Text("Feed 1", modifier = Modifier.clickable { onClick("feed 1") })
        Text("Feed 2", modifier = Modifier.clickable { onClick("feed 2") })
        Text("Feed 3", modifier = Modifier.clickable { onClick("feed 3") })
        Text("Feed 4", modifier = Modifier.clickable { onClick("feed 4") })
    }
}

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    content: List<Content>,
    onClick: () -> Unit,
) {
    Column(modifier = modifier) {
        var showMenu by remember { mutableStateOf(false) }

        TopAppBar(
            title = { Text("Feed") },
            actions = {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More option"
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(onClick = { /*TODO*/ }) {
                        Text("Change Layout")
                    }
                    DropdownMenuItem(onClick = { /*TODO*/ }) {
                        Text("Change Sorting")
                    }
                    DropdownMenuItem(onClick = onClick) {
                        Text("Test")
                    }
                }
            },
        )
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
//            content.forEach {
//                PhotoCard(it)
//            }
            items(content, key = { it.id }) {
                PhotoCard(text = it.title)
            }
        }
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
    text: String,
) {
    Box(
        modifier = modifier.shadow(elevation = 4.dp)
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1609054367623-4fd42d7ade3b?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2075&q=80",
            contentDescription = "TODO",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(MaterialTheme.shapes.medium)
        )
        Text(text, modifier = Modifier.padding(8.dp))
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
    Search("Search", Icons.Outlined.Search, Icons.Filled.Search),
    Favorite("Favorite", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    Settings("Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

enum class Screen {
    FeedList
}
