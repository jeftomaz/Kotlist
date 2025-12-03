package com.kotlist.app.ui.lists

import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlist.app.data.model.ShoppingList
import com.kotlist.app.data.repository.ShoppingListRepository
import com.kotlist.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AddListUiState {
    data object Idle : AddListUiState()
    data object Loading : AddListUiState()
    data object Success : AddListUiState()
    data class Error(val message: String) : AddListUiState()
    data class ValidationFailure(val nameError: String?) : AddListUiState()
}

sealed class AddListEvent {
    data class ShowToast(val message: String) : AddListEvent()
    data class NavigateBack(val successMessage: String) : AddListEvent()
    data object OpenGallery : AddListEvent()
}

class AddListViewModel(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AddListUiState>(AddListUiState.Idle)
    val uiState: StateFlow<AddListUiState> = _uiState

    private val _permissionState = MutableStateFlow<GalleryPermissionState>(GalleryPermissionState.Idle)
    val permissionState: StateFlow<GalleryPermissionState> = _permissionState

    private val _placeholderImageId = MutableStateFlow<Int?>(null)
    val placeholderImageId: StateFlow<Int?> = _placeholderImageId

    private val _customImageUrl = MutableStateFlow<Uri?>(null)
    val customImageUrl: StateFlow<Uri?> = _customImageUrl

    private val _events = MutableSharedFlow<AddListEvent>()
    val events: SharedFlow<AddListEvent> = _events

    fun loadInitialPlaceholder() {
        _placeholderImageId.value = shoppingListRepository.getRandomPlaceholderId()
    }

    fun createNewList(name: String, customCoverImageUrl: Uri?) {
        if(name.isBlank()) {
            _uiState.value = AddListUiState.ValidationFailure(
                nameError = "A lista deve ter um nome"
            )
            return
        }

        _uiState.value = AddListUiState.Idle

        val userId = userRepository.getUserSignedIn()?.id
        if(userId == null) {
            _uiState.value = AddListUiState.Error("Usuário não encontrado. Faça login novamente.")
            return
        }

        _uiState.value = AddListUiState.Loading
        viewModelScope.launch {
            try {
                val newList = ShoppingList(
                    name = name.trim(),
                    ownerId = userId,
                    customCoverImageUrl = null,
                    placeholderImageId = _placeholderImageId.value ?: shoppingListRepository.getRandomPlaceholderId()
                )

                val listId = shoppingListRepository.createList(newList)

                if(customCoverImageUrl != null) {
                    val uploadedImageUrl = shoppingListRepository.uploadCoverImage(userId, customCoverImageUrl, listId)

                    val updatedList = newList.copy(
                        id = listId,
                        customCoverImageUrl = uploadedImageUrl
                    )
                    shoppingListRepository.updateList(updatedList)
                }

                _uiState.value = AddListUiState.Success
                _events.emit(AddListEvent.NavigateBack("Lista criada com sucesso!"))
            }
            catch(e: Exception) {
                _uiState.value = AddListUiState.Error("Erro ao criar lista: ${e.message}")
            }
        }
    }

    fun onAddImageClicked() {
        _permissionState.value = GalleryPermissionState.ShouldRequestPermission
    }

    fun onPermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        _permissionState.value = when {
            isGranted -> {
                viewModelScope.launch {
                    _events.emit(AddListEvent.OpenGallery)
                }
                GalleryPermissionState.PermissionGranted
            }
            shouldShowRationale -> GalleryPermissionState.ShouldShowRationale
            else -> GalleryPermissionState.PermissionDenied
        }
    }

    fun onImageSelected(imageUri: Uri?) {
        _customImageUrl.value = imageUri
    }

    fun resetPermissionState() {
        _permissionState.value = GalleryPermissionState.Idle
    }

    fun getRequiredPermission(): String {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}