package com.rmtm.lifelog.feature.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.net.Uri
import coil.compose.AsyncImage
import com.rmtm.lifelog.core.model.Mood
import com.rmtm.lifelog.core.model.Photo
import com.rmtm.lifelog.util.toLocalDateTimeString
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * [상세 보기 화면]
 * 목록에서 항목을 클릭했을 때 보여주는 화면입니다.
 * - 일기의 모든 내용(날짜, 기분, 글, 사진)을 크게 보여줍니다.
 * - 삭제 버튼을 통해 기록을 지울 수 있습니다.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    state: StateFlow<DetailState>,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val uiState = state.collectAsStateWithLifecycle()
    val ui = uiState.value

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) } // Add state for edit dialog
    var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) }

    if (selectedPhotoIndex != null && ui.entry != null) {
        PhotoViewerDialog(
            photos = ui.entry.photos,
            initialIndex = selectedPhotoIndex!!,
            onDismiss = { selectedPhotoIndex = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ui.entry?.let { entry -> // Only show edit/delete if entry exists
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "수정")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (ui.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (ui.entry == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("기록을 찾을 수 없습니다.")
            }
        } else {
            val entry = ui.entry
            val mood = Mood.fromValue(entry.mood)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.createdAt.toLocalDateTimeString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = mood.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (entry.photos.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(entry.photos) { index, photo ->
                            Card(modifier = Modifier.clickable { selectedPhotoIndex = index }) {
                                                            AsyncImage(
                                                                model = Uri.parse("file://" + photo.uri),
                                                                contentDescription = null,
                                                                modifier = Modifier.size(120.dp),
                                                                contentScale = ContentScale.Crop
                                                            )                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("기록 삭제") },
            text = { Text("정말 이 기록을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // New Edit Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("기록 수정") },
            text = { Text("기록을 수정하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    ui.entry?.let { entry ->
                        onEdit(entry.id)
                    }
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoViewerDialog(
    photos: List<Photo>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photos.size }
    )
    val scope = rememberCoroutineScope()
    var isZoomed by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isZoomed
            ) { page ->
                ZoomableImage(
                    uri = Uri.parse("file://" + photos[page].uri).toString(),
                    modifier = Modifier.fillMaxSize(),
                    onZoomChanged = { isZoomed = it }
                )
            }

            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
            }

            // Previous Button
            if (pagerState.currentPage > 0) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "이전 사진", tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }

            // Next Button
            if (pagerState.currentPage < photos.size - 1) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "다음 사진", tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    uri: String,
    modifier: Modifier = Modifier,
    onZoomChanged: (Boolean) -> Unit
) {
    var scale by remember(uri) { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(scale) {
        onZoomChanged(scale > 1f)
        if (scale == 1f) {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .pointerInput(uri) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (scale == 1f && zoom == 1f) return@detectTransformGestures

                    scale = (scale * zoom).coerceIn(1f, 5f)

                    if (scale > 1f) {
                        val imageWidth = size.width
                        val imageHeight = size.height
                        val newOffsetX = offset.x + pan.x
                        val newOffsetY = offset.y + pan.y
                        val maxX = (imageWidth * (scale - 1f) / 2f)
                        val maxY = (imageHeight * (scale - 1f) / 2f)

                        offset = Offset(
                            x = newOffsetX.coerceIn(-maxX, maxX),
                            y = newOffsetY.coerceIn(-maxY, maxY)
                        )
                    }
                }
            }
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}