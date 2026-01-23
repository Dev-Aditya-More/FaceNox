package org.aditya1875.facenox.feature.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    projectId: String?,
    imageUri: String,
    onBackClick: () -> Unit,
    onSaveClick: (String) -> Unit,
    onExportClick: (String) -> Unit,
    viewModel: EditorViewModel = viewModel { EditorViewModel(projectId, imageUri) }
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
                    // TODO: Handle share
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
                            Icons.Default.Undo,
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
                            Icons.Default.Redo,
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
            if (!state.isProcessing && !state.isSaving) {
                FloatingActionButton(
                    onClick = { viewModel.onIntent(EditorIntent.Save) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            EditorCanvas(state)

            when (state.selectedTool) {
                EditorTool.FILTER -> {
                    FilterPanel(
                        appliedFilters = state.appliedFilters,
                        onFilterApply = { viewModel.onIntent(EditorIntent.ApplyFilter(it)) },
                        onFilterRemove = { viewModel.onIntent(EditorIntent.RemoveFilter(it)) }
                    )
                }
                EditorTool.ADJUST -> {
                    AdjustmentPanel(
                        brightness = state.brightness,
                        contrast = state.contrast,
                        saturation = state.saturation,
                        onBrightnessChange = { viewModel.onIntent(EditorIntent.UpdateBrightness(it)) },
                        onContrastChange = { viewModel.onIntent(EditorIntent.UpdateContrast(it)) },
                        onSaturationChange = { viewModel.onIntent(EditorIntent.UpdateSaturation(it)) }
                    )
                }
                EditorTool.DRAW -> {
                    DrawingPanel(
                        brushSize = state.brushSize,
                        brushColor = state.brushColor,
                        onBrushSizeChange = { viewModel.onIntent(EditorIntent.ChangeBrushSize(it)) },
                        onBrushColorChange = { viewModel.onIntent(EditorIntent.ChangeBrushColor(it)) }
                    )
                }
                else -> {}
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
private fun EditorCanvas(state: EditorState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Image Canvas\n(${state.imageUri})",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.5f)
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
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
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
        }
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
            valueRange = -1f..1f
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
        }
    }
}

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
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(message)
            }
        }
    }
}