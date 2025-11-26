package com.kotlist.app.ui.lists

sealed class GalleryPermissionState {
    object Idle : GalleryPermissionState()
    object PermissionGranted : GalleryPermissionState()
    object ShouldRequestPermission : GalleryPermissionState()
    object ShouldShowRationale : GalleryPermissionState()
    object PermissionDenied : GalleryPermissionState()
}