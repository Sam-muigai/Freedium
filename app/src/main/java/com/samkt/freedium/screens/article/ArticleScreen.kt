package com.samkt.freedium.screens.article

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.samkt.freedium.R

@Composable
fun ArticleScreen(
    articleViewModel: ArticleViewModel = viewModel(),
    mediumLink: String,
    onBackClick: () -> Unit
) {
    LaunchedEffect(mediumLink) {
        articleViewModel.getArticle(
            mediumUrl = mediumLink
        )
    }
    ArticleScreenContent(
        articleScreenState = articleViewModel.articleScreenState.collectAsStateWithLifecycle().value,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreenContent(
    modifier: Modifier = Modifier,
    articleScreenState: ArticleScreenUiState,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                title = {},
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            targetState = articleScreenState
        ) { articleScreenState ->
            when (articleScreenState) {
                is ArticleScreenUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            articleScreenState.message,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }

                ArticleScreenUiState.Idle -> {}
                ArticleScreenUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ArticleScreenUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        LazyColumn {
                            item {
                                Text(
                                    text = articleScreenState.content.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                    ),
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                            item {
                                Spacer(Modifier.height(16.dp))
                            }
                            items(articleScreenState.content.content) { item ->
                                when (item) {
                                    is ArticleItem.Text -> {
                                        Text(
                                            text = item.content,
                                            style = when (item.type) {
                                                TextType.HEADING -> MaterialTheme.typography.titleLarge
                                                TextType.PARAGRAPH -> MaterialTheme.typography.bodyLarge
                                                TextType.NORMAL -> MaterialTheme.typography.bodyLarge
                                            },
                                            fontWeight = if (item.type == TextType.HEADING) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Serif,
                                            lineHeight = 24.sp,
                                            modifier = Modifier.padding(bottom = 16.dp),
                                            textDecoration = when (item.type) {
                                                TextType.HEADING -> TextDecoration.Underline
                                                else -> TextDecoration.None
                                            }
                                        )
                                    }

                                    is ArticleItem.Image -> {
                                        AsyncImage(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                            model = item.url,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    is ArticleItem.Code -> {
                                        CodeBlock(
                                            code = item.content,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier
) {
    var isCopied by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_code),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        isCopied = true
                        clipboard.setText(AnnotatedString(code))
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = if (isCopied) "Copied!" else "Copy",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            CodeContent(
                code = code,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun CodeContent(
    code: String,
    modifier: Modifier = Modifier
) {

    Text(
        text = code,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}



