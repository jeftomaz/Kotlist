package com.kotlist.app.ui.lists

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

class AddListViewModel(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {
    private val _placeholderImageId = MutableStateFlow<Int?>(null)
    val placeholderImageId: StateFlow<Int?> = _placeholderImageId

    private val _listNameError = MutableStateFlow<String?>(null)
    val listNameError: StateFlow<String?> = _listNameError

    private val _toastError = MutableSharedFlow<String>()
    val toastError: SharedFlow<String> = _toastError

    private val _finishEvent = MutableSharedFlow<String>()
    val finishEvent: SharedFlow<String> = _finishEvent

    private val _galleryPermissionState = MutableSharedFlow<GalleryPermissionState>()
    val galleryPermissionState: SharedFlow<GalleryPermissionState> = _galleryPermissionState

    fun loadInitialPlaceholder() {
        _placeholderImageId.value = shoppingListRepository.getRandomPlaceholderId()
    }

    fun createNewList(listTitle: String, coverImageUri: String?, placeholderImageId: Int) {
        if(listTitle.isEmpty()) {
            _listNameError.value = "A lista deve ter um nome."
            return
        }

        _listNameError.value = null

//        val userId = userRepository.getUserLoggedIn()?.id
        val userId = null
        if(userId == null) {
            viewModelScope.launch {
                _toastError.emit("Houve um erro ao tentar criar a lista. (Usuário não encontrado)")
            }
            return
        }

//        val newList = ShoppingList(
//            title = listTitle,
//            coverImageUri = coverImageUri,
//            placeholderImageId = placeholderImageId,
//            userId = userId
//        )

//        shoppingListRepository.addList(newList)

        viewModelScope.launch {
            _finishEvent.emit("Nova lista criada com sucesso!")
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
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}