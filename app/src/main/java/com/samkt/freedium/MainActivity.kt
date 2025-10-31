package com.samkt.freedium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.samkt.freedium.screens.article.ArticleScreen
import com.samkt.freedium.screens.home.HomeScreen
import com.samkt.freedium.ui.theme.FreediumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreediumTheme {
                App()
            }
        }
    }
}

data object HomeScreen

data class ArticleScreen(val link: String)

@Composable
fun App() {
    val backStack = remember { mutableStateListOf<Any>(HomeScreen) }
    NavDisplay(
        backStack = backStack,
        entryProvider = { key ->
            when (key) {
                is HomeScreen -> NavEntry(key) {
                    HomeScreen(
                        onRead = { link ->
                            backStack.add(ArticleScreen(link))
                        }
                    )
                }

                is ArticleScreen -> NavEntry(key) {
                    ArticleScreen(
                        mediumLink = key.link,
                        onBackClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                else -> {
                    NavEntry(Unit) { Text(text = "Invalid Key: $it") }
                }
            }
        }
    )
}