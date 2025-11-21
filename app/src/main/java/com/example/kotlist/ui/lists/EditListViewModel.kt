package com.example.kotlist.ui.lists

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditListViewModel(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private var listOnEditing: ShoppingList? = null
    private val _listToEdit = MutableStateFlow<ShoppingList?>(null)
    val listToEdit: StateFlow<ShoppingList?> = _listToEdit

    private val _listNameError = MutableStateFlow<String?>(null)
    val listNameError: StateFlow<String?> = _listNameError

    private val _updateEventMessage = MutableSharedFlow<String>()
    val updateEventMessage: SharedFlow<String> = _updateEventMessage

    private val _deleteEventMessage = MutableSharedFlow<String>()
    val deleteEventMessage: SharedFlow<String> = _deleteEventMessage

    private val _showDeleteConfirmation = MutableSharedFlow<Unit>()
    val showDeleteConfirmation: SharedFlow<Unit> = _showDeleteConfirmation

    private val _galleryPermissionState = MutableSharedFlow<GalleryPermissionState>()
    val galleryPermissionState: SharedFlow<GalleryPermissionState> = _galleryPermissionState

    fun loadList(listId: String) {
        listOnEditing = shoppingListRepository.getListById(listId)
        _listToEdit.value = listOnEditing
    }

    fun saveList(newTitle: String, newImageUri: String?) {
        if(newTitle.isEmpty()) {
            _listNameError.value = "A lista deve ter um nome."
            return
        }

        _listNameError.value = null

        val currentList = listOnEditing ?: return

        val updatedList = currentList.copy(
            title = newTitle,
            coverImageUri = newImageUri ?: currentList.coverImageUri
        )

        shoppingListRepository.updateList(updatedList)

        viewModelScope.launch {
            _updateEventMessage.emit("Lista atualizada com sucesso!")
        }
    }

    fun onDeleteListClicked() {
        viewModelScope.launch {
            _showDeleteConfirmation.emit(Unit)
        }
    }

    fun deleteList() {
        val listId = listOnEditing?.id ?: return
        shoppingListRepository.deleteList(listId)

        viewModelScope.launch {
            _deleteEventMessage.emit("Lista excluída com sucesso!")
        }
    }

    fun onAddImageClicked() {
        viewModelScope.launch {
            _galleryPermissionState.emit(GalleryPermissionState.ShouldRequestPermission)
        }
    }

    fun onPermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        viewModelScope.launch {
            when {
                isGranted -> _galleryPermissionState.emit(GalleryPermissionState.PermissionGranted)
                shouldShowRationale -> _galleryPermissionState.emit(GalleryPermissionState.ShouldShowRationale)
                else -> _galleryPermissionState.emit(GalleryPermissionState.PermissionDenied)
            }
        }
    }

    fun getRequiredPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}