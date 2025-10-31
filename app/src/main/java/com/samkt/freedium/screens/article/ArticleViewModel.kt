package com.samkt.freedium.screens.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class ArticleViewModel : ViewModel() {


    private val _articleScreenState =
        MutableStateFlow<ArticleScreenUiState>(ArticleScreenUiState.Idle)
    val articleScreenState = _articleScreenState.asStateFlow()

    fun getArticle(
        mediumUrl: String
    ) {
        _articleScreenState.update { ArticleScreenUiState.Loading }
        viewModelScope.launch {
            Fuel.get("https://freedium-mirror.cfd/$mediumUrl")
                .responseString { _, _, result ->
                    when (result) {
                        is Result.Success -> {
                            _articleScreenState.update {
                                ArticleScreenUiState.Success(
                                    extractArticleWithMetadata(result.get())
                                )
                            }
                        }

                        is Result.Failure -> {
                            _articleScreenState.update { ArticleScreenUiState.Error("Error occurred, please try again") }
                        }
                    }
                }
        }
    }
}


sealed interface ArticleScreenUiState {
    data class Success(val content: Article) : ArticleScreenUiState
    data object Loading : ArticleScreenUiState
    data class Error(val message: String) : ArticleScreenUiState
    data object Idle : ArticleScreenUiState
}


sealed class ArticleItem {
    data class Text(val content: String, val type: TextType) : ArticleItem()
    data class Image(val url: String) : ArticleItem()
    data class Code(val content: String, val language: String) : ArticleItem()
}

enum class TextType {
    HEADING, PARAGRAPH, NORMAL
}


fun extractArticleWithMetadata(htmlString: String): Article {
    val doc = Jsoup.parse(htmlString)

    val title = doc.selectFirst("h1")?.text() ?: ""
    val articleContent = parseArticleContent(htmlString)
    val author = doc.selectFirst(".font-semibold.text-gray-900")?.text() ?: ""

    val tags = doc.select("a[href*=/tag/] span").map { it.text() }

    return Article(title, articleContent, author, tags)
}

fun parseArticleContent(htmlString: String): List<ArticleItem> {
    val doc = Jsoup.parse(htmlString)
    val articleElement = doc.selectFirst(".main-content")
    val items = mutableListOf<ArticleItem>()

    articleElement?.children()?.forEach { element ->
        when {
            element.tagName() == "img" -> {
                val imgUrl = element.attr("src")
                items.add(ArticleItem.Image(imgUrl))
            }

            element.tagName() == "pre" -> {
                val codeElement = element.selectFirst("code")
                val codeContent = codeElement?.text() ?: element.text()
                val language = codeElement?.className()?.findLanguage() ?: "text"
                items.add(ArticleItem.Code(codeContent, language))
            }

            element.tagName() == "p" -> {
                items.add(ArticleItem.Text(element.text(), TextType.PARAGRAPH))
            }

            element.tagName().matches("h[1-6]".toRegex()) -> {
                items.add(ArticleItem.Text(element.text(), TextType.HEADING))
            }

            element.hasText() -> {
                items.add(ArticleItem.Text(element.text(), TextType.NORMAL))
            }
        }
    }

    return items
}

data class Article(
    val title: String,
    val content: List<ArticleItem>,
    val author: String,
    val tags: List<String>
)

private fun String.findLanguage(): String {
    return this.split(" ")
        .find { it.startsWith("language-") }
        ?.removePrefix("language-")
        ?: "text"
}
