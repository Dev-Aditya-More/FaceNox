package org.aditya1875.facenox.feature.screens.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import org.aditya1875.facenox.core.ui.WindowWidthSizeClass
import org.aditya1875.facenox.core.ui.rememberWindowWidthSizeClass
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Suppress("EffectKeys")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    projectId: String?,
    imageUri: String,
    onBackClick: () -> Unit,
    onSaveClick: (String) -> Unit,
    onExportClick: (String) -> Unit,
    viewModel: EditorViewModel = koinViewModel(parameters = { parametersOf(projectId, imageUri) })
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val windowSize = rememberWindowWidthSizeClass()
    val isDesktop = windowSize == WindowWidthSizeClass.Expanded

    LaunchedEffect(onSaveClick, onBackClick, onExportClick) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is EditorEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is EditorEffect.ShowError -> snackbarHostState.showSnackbar(effect.message, duration = SnackbarDuration.Long)
                is EditorEffect.NavigateToProcessing -> onSaveClick(effect.projectId)
                is EditorEffect.NavigateBack -> onBackClick()
                is EditorEffect.ShareImage -> onExportClick(effect.uri)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isEditingMode) viewModel.onIntent(EditorIntent.CancelCrop)
                        else onBackClick()
                    }) {
                        Icon(
                            if (state.isEditingMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (state.isEditingMode) "Cancel" else "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onIntent(EditorIntent.Undo) },
                        enabled = state.canUndo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo, "Undo",
                            tint = if (state.canUndo) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onIntent(EditorIntent.Redo) },
                        enabled = state.canRedo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo, "Redo",
                            tint = if (state.canRedo) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(onClick = { viewModel.onIntent(EditorIntent.ResetToOriginal) }) {
                        Icon(Icons.Default.RestartAlt, "Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // On desktop the tools live in the side rail — no bottom bar needed
            if (!isDesktop && state.showToolPanel && !state.isEditingMode) {
                ToolPanel(
                    selectedTool = state.selectedTool,
                    onToolSelected = { viewModel.onIntent(EditorIntent.SelectTool(it)) }
                )
            }
        },
        floatingActionButton = {
            val toolActive = state.isEditingMode || state.selectedTool != EditorTool.SELECT
            if (toolActive) {
                FloatingActionButton(
                    onClick = {
                        if (state.isEditingMode) viewModel.onIntent(EditorIntent.ApplyCrop)
                        else viewModel.onIntent(EditorIntent.CloseToolOptions)
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) { Icon(Icons.Default.Check, "Done") }
            } else {
                FloatingActionButton(
                    onClick = { viewModel.onIntent(EditorIntent.Save) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) { Icon(Icons.Default.Save, "Save") }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.navigationBarsPadding()
    ) { padding ->
        if (isDesktop) {
            // ── Desktop layout: canvas on the left, side panel on the right ──
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    EditorCanvas(state = state, onIntent = viewModel::onIntent)
                    if (state.isProcessing || state.isSaving) {
                        ProcessingOverlay(if (state.isSaving) "Saving..." else "Processing...")
                    }
                }

                if (state.showToolPanel && !state.isEditingMode) {
                    DesktopSidePanel(state = state, onIntent = viewModel::onIntent)
                }
            }
        } else {
            // ── Mobile layout: canvas above, active tool panel stacked below it ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        EditorCanvas(state = state, onIntent = viewModel::onIntent)
                    }

                    if (state.showToolPanel && state.selectedTool != EditorTool.SELECT) {
                        EditorToolPanel(state = state, viewModel = viewModel)
                    }
                }

                if (state.isProcessing || state.isSaving) {
                    ProcessingOverlay(if (state.isSaving) "Saving..." else "Processing...")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Desktop side panel
// ---------------------------------------------------------------------------

@Composable
private fun DesktopSidePanel(
    state: EditorState,
    onIntent: (EditorIntent) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxHeight().width(280.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // space for FAB
        ) {
            // Tool selection — vertical list
            Spacer(Modifier.height(8.dp))
            Text(
                "Tools",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            listOf(
                Triple(Icons.Default.Crop, "Crop", EditorTool.CROP),
                Triple(Icons.Default.FilterVintage, "Filter", EditorTool.FILTER),
                Triple(Icons.Default.Tune, "Adjust", EditorTool.ADJUST),
                Triple(Icons.Default.Brush, "Draw", EditorTool.DRAW),
                Triple(Icons.Default.Face, "Face", EditorTool.FACE_EDIT),
            ).forEach { (icon, label, tool) ->
                ToolRow(
                    icon = icon,
                    label = label,
                    isSelected = state.selectedTool == tool,
                    onClick = { onIntent(EditorIntent.SelectTool(tool)) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Active tool options (reuse shared panel dispatch)
            EditorToolPanel(state = state, viewModel = null, onIntentDirect = onIntent)

        }
    }
}

@Composable
private fun ToolRow(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------------------
// Shared tool-panel dispatcher  (used by both mobile overlay and desktop side panel)
// ---------------------------------------------------------------------------

@Composable
private fun EditorToolPanel(
    state: EditorState,
    viewModel: EditorViewModel?,
    onIntentDirect: ((EditorIntent) -> Unit)? = null
) {
    val onIntent: (EditorIntent) -> Unit = { intent ->
        viewModel?.onIntent(intent) ?: onIntentDirect?.invoke(intent)
    }

    val isFaceSupported = viewModel?.isFaceDetectionSupported ?: false

    when (state.selectedTool) {
        EditorTool.CROP -> CropPanel(
            onRatioSelect = { ratio ->
                val image = state.currentImage ?: return@CropPanel
                val iw = image.width.toFloat()
                val ih = image.height.toFloat()
                val rect = if (ratio == null) {
                    Rect(0f, 0f, iw, ih)
                } else {
                    if (iw / ih > ratio) {
                        val tw = ih * ratio
                        Rect((iw - tw) / 2f, 0f, (iw + tw) / 2f, ih)
                    } else {
                        val th = iw / ratio
                        Rect(0f, (ih - th) / 2f, iw, (ih + th) / 2f)
                    }
                }
                onIntent(EditorIntent.UpdateCropRect(rect))
            },
            onRotateCW = { onIntent(EditorIntent.RotateClockwise) },
            onRotateCCW = { onIntent(EditorIntent.RotateCounterClockwise) }
        )
        EditorTool.FILTER -> FilterPanel(
            appliedFilters = state.appliedFilters,
            onFilterApply = { onIntent(EditorIntent.ApplyFilter(it)) },
            onFilterRemove = { onIntent(EditorIntent.RemoveFilter(it)) }
        )
        EditorTool.ADJUST -> AdjustmentPanel(
            brightness = state.brightness,
            contrast = state.contrast,
            saturation = state.saturation,
            onBrightnessChange = { onIntent(EditorIntent.UpdateBrightness(it)) },
            onContrastChange = { onIntent(EditorIntent.UpdateContrast(it)) },
            onSaturationChange = { onIntent(EditorIntent.UpdateSaturation(it)) }
        )
        EditorTool.DRAW -> DrawingPanel(
            brushSize = state.brushSize,
            brushColor = state.brushColor,
            onBrushSizeChange = { onIntent(EditorIntent.ChangeBrushSize(it)) },
            onBrushColorChange = { onIntent(EditorIntent.ChangeBrushColor(it)) }
        )
        EditorTool.FACE_EDIT -> FacePanel(
            detectedFaces = state.detectedFaces,
            isDetecting = state.isDetectingFaces,
            isSupported = isFaceSupported,
            onDetectFaces = { onIntent(EditorIntent.DetectFaces) },
            onBlurAllFaces = { onIntent(EditorIntent.BlurAllFaces) },
            onCropToFace = { onIntent(EditorIntent.CropToFace(it)) }
        )
        EditorTool.BACKGROUND_REMOVE -> BackgroundRemovePanel()
        else -> {}
    }
}

// ---------------------------------------------------------------------------
// EditorCanvas
// ---------------------------------------------------------------------------

@Composable
private fun EditorCanvas(state: EditorState, onIntent: (EditorIntent) -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageDisplaySize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(state.selectedTool) {
        scale = 1f
        offset = Offset.Zero
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        state.currentImage?.let { imageBitmap ->
            val colorMatrix = remember(state.brightness, state.contrast, state.saturation, state.appliedFilters) {
                buildColorMatrix(
                    brightness = state.brightness,
                    contrast = state.contrast,
                    saturation = state.saturation,
                    filter = state.appliedFilters
                )
            }

            Box {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(imageBitmap.width.toFloat() / imageBitmap.height.toFloat())
                        .clip(RoundedCornerShape(8.dp))
                        .onSizeChanged { imageDisplaySize = it }
                        .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
                        .pointerInput(state.selectedTool) {
                            if (state.selectedTool == EditorTool.CROP) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                                    offset += pan
                                }
                            }
                        },
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix)
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(imageBitmap.width.toFloat() / imageBitmap.height.toFloat())
                        .clip(RoundedCornerShape(8.dp))
                        .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
                        .pointerInput(state.selectedTool) {
                            if (state.selectedTool == EditorTool.DRAW) {
                                detectDragGestures(
                                    onDragStart = { onIntent(EditorIntent.StartDrawing(it)) },
                                    onDrag = { change, _ -> onIntent(EditorIntent.ContinueDrawing(change.position)) },
                                    onDragEnd = { onIntent(EditorIntent.EndDrawing) }
                                )
                            }
                        }
                ) {
                    state.drawingPaths.forEach { path ->
                        for (i in 0 until path.points.size - 1) {
                            drawLine(
                                color = path.color,
                                start = path.points[i],
                                end = path.points[i + 1],
                                strokeWidth = path.strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Face bounding boxes (shown in FACE_EDIT mode)
                    if (state.selectedTool == EditorTool.FACE_EDIT && state.detectedFaces.isNotEmpty()) {
                        state.detectedFaces.forEach { face ->
                            val scaleX = size.width / imageBitmap.width.toFloat()
                            val scaleY = size.height / imageBitmap.height.toFloat()
                            val fr = Rect(
                                left = face.rect.left * scaleX,
                                top = face.rect.top * scaleY,
                                right = face.rect.right * scaleX,
                                bottom = face.rect.bottom * scaleY
                            )
                            drawRect(color = Color(0xFF00E5FF), topLeft = fr.topLeft, size = fr.size, style = Stroke(width = 3f))
                            // Smile indicator dot
                            val isSmiling = (face.smilingProbability ?: 0f) > 0.6f
                            drawCircle(
                                color = if (isSmiling) Color(0xFF00E676) else Color(0xFFFFEB3B),
                                radius = 8f,
                                center = Offset(fr.right - 10f, fr.top + 10f)
                            )
                        }
                    }

                    if (state.selectedTool == EditorTool.CROP && state.cropRect != null) {
                        val rect = state.cropRect
                        val scaleX = size.width / imageBitmap.width.toFloat()
                        val scaleY = size.height / imageBitmap.height.toFloat()
                        val displayRect = Rect(
                            left = rect.left * scaleX,
                            top = rect.top * scaleY,
                            right = rect.right * scaleX,
                            bottom = rect.bottom * scaleY
                        )

                        drawRect(color = Color.Black.copy(alpha = 0.5f), size = Size(size.width, displayRect.top))
                        drawRect(color = Color.Black.copy(alpha = 0.5f), topLeft = Offset(0f, displayRect.bottom), size = Size(size.width, size.height - displayRect.bottom))
                        drawRect(color = Color.Black.copy(alpha = 0.5f), topLeft = Offset(0f, displayRect.top), size = Size(displayRect.left, displayRect.height))
                        drawRect(color = Color.Black.copy(alpha = 0.5f), topLeft = Offset(displayRect.right, displayRect.top), size = Size(size.width - displayRect.right, displayRect.height))

                        drawRect(color = Color.White, topLeft = displayRect.topLeft, size = displayRect.size, style = Stroke(width = 2f))

                        val thirdW = displayRect.width / 3f
                        val thirdH = displayRect.height / 3f
                        for (i in 1..2) {
                            drawLine(Color.White.copy(alpha = 0.4f), Offset(displayRect.left + thirdW * i, displayRect.top), Offset(displayRect.left + thirdW * i, displayRect.bottom), 0.5f)
                            drawLine(Color.White.copy(alpha = 0.4f), Offset(displayRect.left, displayRect.top + thirdH * i), Offset(displayRect.right, displayRect.top + thirdH * i), 0.5f)
                        }

                        val handleLen = 24f
                        val handleW = 3f
                        val corners = listOf(
                            displayRect.topLeft,
                            Offset(displayRect.right, displayRect.top),
                            Offset(displayRect.left, displayRect.bottom),
                            displayRect.bottomRight
                        )
                        corners.forEachIndexed { i, corner ->
                            val hx = if (i % 2 == 0) 1f else -1f
                            val vy = if (i < 2) 1f else -1f
                            drawLine(Color.White, corner, corner.copy(x = corner.x + hx * handleLen), handleW)
                            drawLine(Color.White, corner, corner.copy(y = corner.y + vy * handleLen), handleW)
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxWidth(0.9f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (state.isProcessing) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Image failed to load", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Panels (shared between mobile bottom-sheet and desktop side-panel)
// ---------------------------------------------------------------------------

data class CropRatio(val name: String, val ratio: Float?)

private val cropRatios = listOf(
    CropRatio("Free", null),
    CropRatio("1:1", 1f),
    CropRatio("4:3", 4f / 3f),
    CropRatio("3:4", 3f / 4f),
    CropRatio("16:9", 16f / 9f),
    CropRatio("9:16", 9f / 16f),
)

@Composable
private fun CropPanel(
    onRatioSelect: (Float?) -> Unit,
    onRotateCW: () -> Unit,
    onRotateCCW: () -> Unit
) {
    var selectedRatio by remember { mutableStateOf<Float?>(null) }
    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Rotate row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onRotateCCW, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.RotateLeft, contentDescription = "Rotate left", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("↺ 90°")
                }
                OutlinedButton(onClick = onRotateCW, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate right", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("↻ 90°")
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Text("Crop Ratio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Select a ratio, then tap ✓ to apply",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cropRatios) { cropRatio ->
                    CropRatioButton(
                        ratio = cropRatio,
                        isSelected = selectedRatio == cropRatio.ratio,
                        onClick = {
                            selectedRatio = cropRatio.ratio
                            onRatioSelect(cropRatio.ratio)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CropRatioButton(ratio: CropRatio, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(70.dp).height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(
                        width = if (ratio.ratio != null) { if (ratio.ratio > 1f) 40.dp else (40.dp * ratio.ratio) } else 40.dp,
                        height = if (ratio.ratio != null) { if (ratio.ratio < 1f) 40.dp else (40.dp / ratio.ratio) } else 40.dp
                    )
                    .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.height(4.dp))
            Text(
                ratio.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToolPanel(selectedTool: EditorTool, onToolSelected: (EditorTool) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolChip(Icons.Default.Crop, "Crop", selectedTool == EditorTool.CROP) { onToolSelected(EditorTool.CROP) }
            ToolChip(Icons.Default.FilterVintage, "Filter", selectedTool == EditorTool.FILTER) { onToolSelected(EditorTool.FILTER) }
            ToolChip(Icons.Default.Tune, "Adjust", selectedTool == EditorTool.ADJUST) { onToolSelected(EditorTool.ADJUST) }
            ToolChip(Icons.Default.Brush, "Draw", selectedTool == EditorTool.DRAW) { onToolSelected(EditorTool.DRAW) }
            ToolChip(Icons.Default.Face, "Face", selectedTool == EditorTool.FACE_EDIT) { onToolSelected(EditorTool.FACE_EDIT) }
        }
    }
}

@Composable
private fun ToolChip(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun FilterPanel(appliedFilters: List<FilterType>, onFilterApply: (FilterType) -> Unit, onFilterRemove: (FilterType) -> Unit) {
    val filters = remember { FilterType.entries.toList() }
    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filters) { filter ->
                    val applied = filter in appliedFilters
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (applied) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { if (applied) onFilterRemove(filter) else onFilterApply(filter) }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FilterVintage, null,
                                tint = if (applied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            filter.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (applied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjustmentPanel(
    brightness: Float, contrast: Float, saturation: Float,
    onBrightnessChange: (Float) -> Unit, onContrastChange: (Float) -> Unit, onSaturationChange: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Adjustments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            AdjustmentSlider("Brightness", brightness, onBrightnessChange)
            AdjustmentSlider("Contrast", contrast, onContrastChange)
            AdjustmentSlider("Saturation", saturation, onSaturationChange)
        }
    }
}

@Composable
private fun AdjustmentSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${(value * 100).toInt()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = -1f..1f)
    }
}

@Composable
private fun DrawingPanel(
    brushSize: Float, brushColor: Color,
    onBrushSizeChange: (Float) -> Unit, onBrushColorChange: (Color) -> Unit
) {
    val colors = listOf(Color.Black, Color.White, Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan)
    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Drawing Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Brush Size: ${brushSize.toInt()}")
            Slider(value = brushSize, onValueChange = onBrushSizeChange, valueRange = 1f..100f)
            Spacer(Modifier.height(12.dp))
            Text("Brush Color")
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp).clip(CircleShape).background(color)
                            .border(2.dp, if (color == brushColor) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                            .clickable { onBrushColorChange(color) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProcessingOverlay(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularWavyProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(message)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Face Panel
// ---------------------------------------------------------------------------

@Composable
private fun FacePanel(
    detectedFaces: List<FaceRect>,
    isDetecting: Boolean,
    isSupported: Boolean,
    onDetectFaces: () -> Unit,
    onBlurAllFaces: () -> Unit,
    onCropToFace: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Face Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            if (!isSupported) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Face detection is available on Android only", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                return@Column
            }

            if (isDetecting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("Detecting faces…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                return@Column
            }

            if (detectedFaces.isEmpty()) {
                Button(
                    onClick = onDetectFaces,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FaceRetouchingNatural, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Detect Faces", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap to automatically detect faces in the image.\nWorks best with clear, front-facing photos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                return@Column
            }

            // Faces found — global actions
            Text(
                "${detectedFaces.size} face${if (detectedFaces.size > 1) "s" else ""} detected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBlurAllFaces, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.BlurOn, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Blur ${if (detectedFaces.size > 1) "All" else ""}")
                }
                if (detectedFaces.size == 1) {
                    OutlinedButton(onClick = { onCropToFace(0) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Icon(Icons.Default.CropFree, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Crop to Face")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Per-face cards
            detectedFaces.forEachIndexed { index, face ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Face, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Face ${index + 1}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                face.smilingProbability?.let { prob ->
                                    Text(
                                        if (prob > 0.7f) "😊 Smiling" else if (prob > 0.3f) "🙂 Slight smile" else "😐 Neutral",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        if (detectedFaces.size > 1) {
                            IconButton(onClick = { onCropToFace(index) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.CropFree, "Crop to this face", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                if (index < detectedFaces.lastIndex) Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDetectFaces, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Re-detect", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Background Remove Panel
// ---------------------------------------------------------------------------

@Composable
private fun BackgroundRemovePanel() {
    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoFixHigh, null, tint = MaterialTheme.colorScheme.primary)
                Text("Background Removal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Construction, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        Text("Coming in next update", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "AI-powered background removal will use ML Kit Subject Segmentation to automatically detect and remove the background, leaving just the subject.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                    )
                }
            }
        }
    }
}
