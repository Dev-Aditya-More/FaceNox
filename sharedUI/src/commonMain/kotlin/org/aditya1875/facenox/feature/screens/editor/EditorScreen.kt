package org.aditya1875.facenox.feature.screens.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    projectId: String?,
    imageUri: String,
    onBackClick: () -> Unit,
    onSaveClick: (String) -> Unit,
    onExportClick: (String) -> Unit,
    viewModel: EditorViewModel = koinViewModel(
        parameters = {
            parametersOf(projectId, imageUri)
        }
    )
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is EditorEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                is EditorEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Long
                    )
                }

                is EditorEffect.NavigateToProcessing -> {
                    onSaveClick(effect.projectId)
                }

                is EditorEffect.NavigateBack -> {
                    onBackClick()
                }

                is EditorEffect.ShareImage -> {
                    onExportClick(effect.uri)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onIntent(EditorIntent.Undo) },
                        enabled = state.canUndo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (state.canUndo) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            }
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onIntent(EditorIntent.Redo) },
                        enabled = state.canRedo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (state.canRedo) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            }
                        )
                    }
                    IconButton(onClick = { viewModel.onIntent(EditorIntent.ResetToOriginal) }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (state.showToolPanel) {
                ToolPanel(
                    selectedTool = state.selectedTool,
                    onToolSelected = { tool ->
                        viewModel.onIntent(EditorIntent.SelectTool(tool))
                    }
                )
            }
        },
        floatingActionButton = {
            when {
                state.showToolOptions -> {
                    FloatingActionButton(
                        onClick = {
                            viewModel.onIntent(EditorIntent.CloseToolOptions)
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done")
                    }
                }
                else -> {
                    FloatingActionButton(
                        onClick = { viewModel.onIntent(EditorIntent.Save) },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.navigationBarsPadding()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            EditorCanvas(
                state = state,
                onIntent = viewModel::onIntent
            )

            if (state.showToolPanel) {
                when (state.selectedTool) {
                    EditorTool.CROP -> {
                        CropPanel(
                            onRatioSelect = { ratio ->
                                val image = state.image ?: return@CropPanel
                                if (ratio == null) {
                                    viewModel.onIntent(EditorIntent.UpdateCropRect(Rect.Zero))
                                } else {
                                    val rect = viewModel.createCropRect(
                                        image.width.toFloat(),
                                        image.height.toFloat(),
                                        ratio
                                    )
                                    viewModel.onIntent(EditorIntent.UpdateCropRect(rect))
                                    viewModel.onIntent(EditorIntent.ApplyCrop)
                                }
                            }
                        )
                    }

                    EditorTool.FILTER -> {
                        FilterPanel(
                            appliedFilters = state.appliedFilters,
                            onFilterApply = {
                                viewModel.onIntent(EditorIntent.ApplyFilter(it))
                            },
                            onFilterRemove = {
                                viewModel.onIntent(EditorIntent.RemoveFilter(it))
                            }
                        )
                    }

                    EditorTool.ADJUST -> {
                        AdjustmentPanel(
                            brightness = state.brightness,
                            contrast = state.contrast,
                            saturation = state.saturation,
                            onBrightnessChange = {
                                viewModel.onIntent(
                                    EditorIntent.UpdateBrightness(
                                        it
                                    )
                                )
                            },
                            onContrastChange = { viewModel.onIntent(EditorIntent.UpdateContrast(it)) },
                            onSaturationChange = {
                                viewModel.onIntent(
                                    EditorIntent.UpdateSaturation(
                                        it
                                    )
                                )
                            }
                        )
                    }

                    EditorTool.DRAW -> {
                        DrawingPanel(
                            brushSize = state.brushSize,
                            brushColor = state.brushColor,
                            onBrushSizeChange = { viewModel.onIntent(EditorIntent.ChangeBrushSize(it)) },
                            onBrushColorChange = {
                                viewModel.onIntent(
                                    EditorIntent.ChangeBrushColor(
                                        it
                                    )
                                )
                            }
                        )
                    }

                    else -> {}
                }
            }

            if (state.isProcessing || state.isSaving) {
                ProcessingOverlay(
                    message = if (state.isSaving) "Saving..." else "Processing..."
                )
            }
        }
    }
}

@Composable
private fun EditorCanvas(
    state: EditorState,
    onIntent: (EditorIntent) -> Unit
) {
    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Reset when tool changes
    LaunchedEffect(state.selectedTool) {
        scale = 1f
        offset = Offset.Zero
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        state.image?.let { imageBitmap ->
            val colorMatrix = remember(
                state.brightness,
                state.contrast,
                state.saturation,
                state.appliedFilters
            ) {
                buildColorMatrix(
                    brightness = state.brightness,
                    contrast = state.contrast,
                    saturation = state.saturation,
                    filter = state.appliedFilters
                )
            }

            Box {
                // IMAGE with zoom/pan
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(imageBitmap.width.toFloat() / imageBitmap.height.toFloat())
                        .clip(RoundedCornerShape(8.dp))
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .pointerInput(state.selectedTool) {
                            // Zoom/Pan for CROP mode
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

                // CANVAS for drawing - separate pointer input
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(imageBitmap.width.toFloat() / imageBitmap.height.toFloat())
                        .clip(RoundedCornerShape(8.dp))
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .pointerInput(state.selectedTool) {
                            // Drawing for DRAW mode
                            if (state.selectedTool == EditorTool.DRAW) {
                                detectDragGestures(
                                    onDragStart = { dragOffset ->
                                        onIntent(EditorIntent.StartDrawing(dragOffset))
                                    },
                                    onDrag = { change, _ ->
                                        onIntent(EditorIntent.ContinueDrawing(change.position))
                                    },
                                    onDragEnd = {
                                        onIntent(EditorIntent.EndDrawing)
                                    }
                                )
                            }
                        }
                ) {
                    // Draw all paths
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
                }
            }

        } ?: Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (state.isProcessing) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(
                    text = "Image failed to load",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// Crop ratios
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
    onRatioSelect: (Float?) -> Unit
) {
    var selectedRatio by remember { mutableStateOf<Float?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Crop Ratio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
private fun CropRatioButton(
    ratio: CropRatio,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(70.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = if (ratio.ratio != null) {
                            if (ratio.ratio > 1f) 40.dp else (40.dp * ratio.ratio)
                        } else 40.dp,
                        height = if (ratio.ratio != null) {
                            if (ratio.ratio < 1f) 40.dp else (40.dp / ratio.ratio)
                        } else 40.dp
                    )
                    .border(
                        width = 2.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ratio.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun ToolPanel(
    selectedTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Tool(
                icon = Icons.Default.Crop,
                label = "Crop",
                isSelected = selectedTool == EditorTool.CROP,
                onClick = { onToolSelected(EditorTool.CROP) }
            )
            Tool(
                icon = Icons.Default.FilterVintage,
                label = "Filter",
                isSelected = selectedTool == EditorTool.FILTER,
                onClick = { onToolSelected(EditorTool.FILTER) }
            )
            Tool(
                icon = Icons.Default.Tune,
                label = "Adjust",
                isSelected = selectedTool == EditorTool.ADJUST,
                onClick = { onToolSelected(EditorTool.ADJUST) }
            )
            Tool(
                icon = Icons.Default.Brush,
                label = "Draw",
                isSelected = selectedTool == EditorTool.DRAW,
                onClick = { onToolSelected(EditorTool.DRAW) }
            )
            Tool(
                icon = Icons.Default.Face,
                label = "Face",
                isSelected = selectedTool == EditorTool.FACE_CUT,
                onClick = { onToolSelected(EditorTool.FACE_CUT) }
            )
        }
    }
}

@Composable
private fun Tool(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            }
        )
    }
}

@Composable
private fun FilterPanel(
    appliedFilters: List<FilterType>,
    onFilterApply: (FilterType) -> Unit,
    onFilterRemove: (FilterType) -> Unit
) {
    val filters = listOf(
        FilterType.NONE,
        FilterType.GRAYSCALE,
        FilterType.SEPIA,
        FilterType.VINTAGE,
        FilterType.WARM,
        FilterType.COOL,
        FilterType.HIGH_CONTRAST
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filters) { filter ->
                    FilterButton(
                        filter = filter,
                        isApplied = filter in appliedFilters,
                        onClick = {
                            if (filter in appliedFilters) {
                                onFilterRemove(filter)
                            } else {
                                onFilterApply(filter)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterButton(
    filter: FilterType,
    isApplied: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isApplied) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterVintage,
                contentDescription = null,
                tint = if (isApplied) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = if (isApplied) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun AdjustmentPanel(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Adjustments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            AdjustmentSlider("Brightness", brightness, onBrightnessChange)
            AdjustmentSlider("Contrast", contrast, onContrastChange)
            AdjustmentSlider("Saturation", saturation, onSaturationChange)
        }
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${(value * 100).toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -1f..1f,
            steps = 9
        )
    }
}


@Composable
private fun DrawingPanel(
    brushSize: Float,
    brushColor: Color,
    onBrushSizeChange: (Float) -> Unit,
    onBrushColorChange: (Color) -> Unit
) {
    val colors = listOf(
        Color.Black, Color.White, Color.Red, Color.Green,
        Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Drawing Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Brush Size: ${brushSize.toInt()}")
            Slider(
                value = brushSize,
                onValueChange = onBrushSizeChange,
                valueRange = 1f..100f
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Brush Color")
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = 2.dp,
                                color = if (color == brushColor) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                },
                                shape = CircleShape
                            )
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularWavyProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(message)
            }
        }
    }
}