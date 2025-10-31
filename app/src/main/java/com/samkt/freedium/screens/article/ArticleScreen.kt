package com.samkt.freedium.screens.article

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

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
                is ArticleScreenUiState.Error -> {}
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
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(900.dp)
                                                .background(Color.Cyan)
                                        )
                                    }

                                    is ArticleItem.Code -> {
                                        CodeBlock(
                                            code = item.content,
                                            language = item.language,
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
    language: String,
    modifier: Modifier = Modifier
) {
    var isCopied by remember { mutableStateOf(false) }
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
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = language.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        // Copy to clipboard
                        // ClipboardManager.setText(AnnotatedString(code))
                        isCopied = true
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



