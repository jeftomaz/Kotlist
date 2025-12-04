package com.kotlist.app.ui.lists

import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlist.app.data.model.ShoppingList
import com.kotlist.app.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EditListUiState {
    data object Idle : EditListUiState()
    data object Loading : EditListUiState()
    data object Success : EditListUiState()
    data class Error(val message: String) : EditListUiState()
    data class ValidationFailure(val nameError: String?) : EditListUiState()
}

sealed class EditListEvent {
    data class ShowToast(val message: String) : EditListEvent()
    data class NavigateBack(val successMessage: String, val navigateToListsScreen: Boolean = false) : EditListEvent()
    data object OpenGallery : EditListEvent()
    data object ShowDeleteConfirmation : EditListEvent()
}

class EditListViewModel(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditListUiState>(EditListUiState.Idle)
    val uiState: StateFlow<EditListUiState> = _uiState

    private val _listToEdit = MutableStateFlow<ShoppingList?>(null)
    val listToEdit: StateFlow<ShoppingList?> = _listToEdit

    private val _permissionState = MutableStateFlow<GalleryPermissionState>(GalleryPermissionState.Idle)
    val permissionState: StateFlow<GalleryPermissionState> = _permissionState

    private val _customImageUrl = MutableStateFlow<Uri?>(null)
    val customImageUrl: StateFlow<Uri?> = _customImageUrl

    private val _events = MutableSharedFlow<EditListEvent>()
    val events: SharedFlow<EditListEvent> = _events

    fun loadList(listId: String) {
        _uiState.value = EditListUiState.Loading
        viewModelScope.launch {
            try {
                val list = shoppingListRepository.getListById(listId)
                if(list != null) {
                    _listToEdit.value = list
                    _uiState.value = EditListUiState.Idle
                } else {
                    _uiState.value = EditListUiState.Error("Lista não encontrada")
                }
            } catch (e: Exception) {
                _uiState.value = EditListUiState.Error("Erro ao carregar lista: ${e.message}")
            }
        }
    }

    fun saveList(newName: String, newImageUri: Uri?) {
        if(newName.isBlank()) {
            _uiState.value = EditListUiState.ValidationFailure(
                nameError = "A lista deve ter um nome"
            )
            return
        }

        val currentList = _listToEdit.value
        if(currentList == null) {
            _uiState.value = EditListUiState.Error("Erro: lista não encontrada")
            return
        }

        _uiState.value = EditListUiState.Loading
        viewModelScope.launch {
            try {
                val updatedList = currentList.copy(
                    name = newName.trim()
                )

                if(newImageUri != null) {
                    val uploadedImageUrl = shoppingListRepository.uploadCoverImage(
                        userId = currentList.ownerId,
                        imageUri = newImageUri,
                        listId = currentList.id
                    )

                    val listWithNewImage = updatedList.copy(
                        customCoverImageUrl = uploadedImageUrl
                    )
                    shoppingListRepository.updateList(listWithNewImage)
                } else {
                    shoppingListRepository.updateList(updatedList)
                }

                _uiState.value = EditListUiState.Success
                _events.emit(EditListEvent.NavigateBack("Lista atualizada com sucesso!"))
            } catch (e: Exception) {
                _uiState.value = EditListUiState.Error("Erro ao atualizar lista: ${e.message}")
            }
        }
    }

    fun onDeleteListClicked() {
        viewModelScope.launch {
            _events.emit(EditListEvent.ShowDeleteConfirmation)
        }
    }

    fun deleteList() {
        val list = _listToEdit.value ?: run {
            _uiState.value = EditListUiState.Error("Erro: lista não encontrada")
            return
        }

        _uiState.value = EditListUiState.Loading
        viewModelScope.launch {
            try {
                shoppingListRepository.deleteList(list)
                _uiState.value = EditListUiState.Success
                _events.emit(
                    EditListEvent.NavigateBack(
                        successMessage = "Lista excluída com sucesso!",
                        navigateToListsScreen = true
                    )
                )
            } catch (e: Exception) {
                _uiState.value = EditListUiState.Error("Erro ao excluir lista: ${e.message}")
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
                    _events.emit(EditListEvent.OpenGallery)
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}