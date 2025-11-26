package com.kotlist.app.ui.lists

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kotlist.app.data.repository.ServiceLocator
import com.kotlist.app.data.repository.ShoppingListRepository
import com.kotlist.app.databinding.ActivityAddListBinding
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class AddListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddListBinding

    private val userRepository by lazy {
        ServiceLocator.provideUserRepository(this)
    }

    private val viewModel: AddListViewModel by viewModels {
        AddListViewModelFactory(userRepository, ShoppingListRepository)
    }

    private var listCoverImageSelectedUri: String? = null
    private var placeholderImageId: Int = -1

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val shouldShowRationale = !isGranted && shouldShowRequestPermissionRationale(
            viewModel.getRequiredPermission()
        )
        viewModel.onPermissionResult(isGranted, shouldShowRationale)
    }

    private val pickImageFromGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    listCoverImageSelectedUri = uri.toString()
                    binding.addListImagePreview.setImageURI(uri)
                } catch (e: SecurityException) {
                    listCoverImageSelectedUri = uri.toString()
                    binding.addListImagePreview.setImageURI(uri)
                }
            }
        } else {
            Toast.makeText(this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.Companion.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.Companion.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        binding = ActivityAddListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.addListMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
        setupObservers()

        if(savedInstanceState == null) {
            viewModel.loadInitialPlaceholder()
        }
    }

    private fun setupListeners() {
        binding.addListAddImage.setOnClickListener {
            viewModel.onAddImageClicked()
        }

        binding.addListAddListButton.setOnClickListener {
            val listTitle = binding.addListListNameInput.text.toString().trim()

            viewModel.createNewList(
                listTitle = listTitle,
                coverImageUri = listCoverImageSelectedUri,
                placeholderImageId = placeholderImageId
            )
        }

        binding.addListCancelButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // observes gallery permission state
                launch {
                    viewModel.galleryPermissionState.collect { state ->
                        handleGalleryPermissionState(state)
                    }
                }

                // show placeholder on screen
                launch {
                    viewModel.placeholderImageId.filterNotNull().collect { id ->
                        placeholderImageId = id
                        if(listCoverImageSelectedUri == null) {
                            binding.addListImagePreview.setImageURI(
                                "android.resource://$packageName/$id".toUri()
                            )
                        }
                    }
                }

                // name input error
                launch {
                    viewModel.listNameError.collect { errorMessage ->
                        binding.addListListNameInputWrapper.error = errorMessage
                    }
                }

                // success state
                launch {
                    viewModel.finishEvent.collect { successMessage ->
                        Toast.makeText(this@AddListActivity, successMessage, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                // errors
                launch {
                    viewModel.toastError.collect { errorMessage ->
                        Toast.makeText(this@AddListActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleGalleryPermissionState(state: GalleryPermissionState) {
        when (state) {
            is GalleryPermissionState.ShouldRequestPermission -> {
                checkAndRequestPermission()
            }
            is GalleryPermissionState.PermissionGranted -> {
                openGallery()
            }
            is GalleryPermissionState.ShouldShowRationale -> {
                showPermissionRationaleDialog()
            }
            is GalleryPermissionState.PermissionDenied -> {
                showPermissionDeniedDialog()
            }
            is GalleryPermissionState.Idle -> { }
        }
    }

    private fun checkAndRequestPermission() {
        val permission = viewModel.getRequiredPermission()

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.onPermissionResult(isGranted = true, shouldShowRationale = false)
            }
            shouldShowRequestPermissionRationale(permission) -> {
                viewModel.onPermissionResult(isGranted = false, shouldShowRationale = true)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        pickImageFromGallery.launch(intent)
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissão necessária")
            .setMessage("Precisamos de acesso à galeria para você selecionar uma imagem de capa para a lista.")
            .setPositiveButton("Permitir") { dialog, _ ->
                requestPermissionLauncher.launch(viewModel.getRequiredPermission())
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissão negada")
            .setMessage("Você negou o acesso à galeria. Para que você possa adicionar imagens personalizadas, acesse as configurações do aplicativo e forneça acesso manualmente.")
            .setPositiveButton("Ir para as configurações") { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}