package org.aditya1875.facenox.feature.screens.imageselection


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageSelectionViewModel : ViewModel() {

    private val _state = MutableStateFlow(ImageSelectionState())
    val state: StateFlow<ImageSelectionState> = _state.asStateFlow()

    fun onImageSelectedFromGallery(uri: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    imageSource = ImageSource.GALLERY
                )
            }

            try {
                // TODO: Load image preview

                _state.update {
                    it.copy(
                        selectedImageUri = uri,
                        previewImage = null,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load image"
                    )
                }
            }
        }
    }

    fun onImageCaptured(uri: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    imageSource = ImageSource.CAMERA
                )
            }

            try {
                // TODO: Load captured image

                _state.update {
                    it.copy(
                        selectedImageUri = uri,
                        previewImage = null,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load captured image"
                    )
                }
            }
        }
    }

    fun clearSelection() {
        _state.update { ImageSelectionState() }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}