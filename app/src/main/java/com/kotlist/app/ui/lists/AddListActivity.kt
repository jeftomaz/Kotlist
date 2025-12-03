package com.kotlist.app.ui.lists

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
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
import coil.load
import com.kotlist.app.data.repository.ServiceLocator
import com.kotlist.app.data.repository.ShoppingListRepository
import com.kotlist.app.databinding.ActivityAddListBinding
import com.kotlist.app.extensions.placeholderIdToDrawable
import com.kotlist.app.extensions.showCustomDialog
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class AddListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddListBinding

    private val userRepository by lazy {
        ServiceLocator.provideUserRepository()
    }

    private val shoppingListRepository by lazy {
        ServiceLocator.provideShoppingListRepository()
    }

    private val viewModel: AddListViewModel by viewModels {
        AddListViewModelFactory(userRepository, shoppingListRepository)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val shouldShowRationale = shouldShowRequestPermissionRationale(viewModel.getRequiredPermission())
        viewModel.onPermissionResult(isGranted, shouldShowRationale)
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            viewModel.onImageSelected(uri)
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
        viewModel.loadInitialPlaceholder()
    }

    private fun setupListeners() {
        binding.addListAddImageButton.setOnClickListener {
            viewModel.onAddImageClicked()
        }

        binding.addListAddListButton.setOnClickListener {
            val name = binding.addListListNameInput.text.toString().trim()
            val customCoverImageUrl = viewModel.customImageUrl.value

            viewModel.createNewList(name, customCoverImageUrl)
        }

        binding.addListCancelButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // observes ui state
                launch {
                    viewModel.uiState.collect() { state ->
                        when(state) {
                            is AddListUiState.Idle -> {
                                setLoading(false)
                                clearErrors()
                            }
                            is AddListUiState.Loading -> {
                                setLoading(true)
                                clearErrors()
                            }
                            is AddListUiState.Success -> {
                                setLoading(false)
                            }
                            is AddListUiState.Error -> {
                                setLoading(false)
                                Toast.makeText(
                                    this@AddListActivity,
                                    state.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is AddListUiState.ValidationFailure -> {
                                setLoading(false)
                                binding.addListListNameInputWrapper.error = state.nameError
                            }
                        }
                    }
                }

                // observes ui events
                launch {
                    viewModel.events.collect { event ->
                        when(event) {
                            is AddListEvent.ShowToast -> {
                                Toast.makeText(
                                    this@AddListActivity,
                                    event.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is AddListEvent.NavigateBack -> {
                                Toast.makeText(
                                    this@AddListActivity,
                                    event.successMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            is AddListEvent.OpenGallery -> {
                                val intent = Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                )
                                pickImageLauncher.launch(intent)
                            }
                        }
                    }
                }

                // observes gallery permission state
                launch {
                    viewModel.permissionState.collect { state ->
                        when (state) {
                            is GalleryPermissionState.Idle -> { }
                            is GalleryPermissionState.ShouldRequestPermission -> {
                                requestPermissionLauncher.launch(viewModel.getRequiredPermission())
                                viewModel.resetPermissionState()
                            }
                            is GalleryPermissionState.PermissionGranted -> {
                                // event handled by ViewModel
                            }
                            is GalleryPermissionState.PermissionDenied -> {
                                showPermissionDeniedDialog()
                            }
                            is GalleryPermissionState.ShouldShowRationale -> {
                                showPermissionRationaleDialog()
                            }
                        }
                    }
                }

                // observes placeholder ID
                launch {
                    viewModel.placeholderImageId.collect { placeholderId ->
                        placeholderId?.let {
                            binding.addListImagePreview.setImageResource(placeholderIdToDrawable(placeholderId))
                        }
                    }
                }

                // observes custom image
                launch {
                    viewModel.customImageUrl.collect { imageUrl ->
                        if(imageUrl != null)
                            binding.addListImagePreview.load(imageUrl)
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if(isLoading) {
            binding.addListLoadingIndicator.visibility = View.VISIBLE
            binding.addListImagePreview.visibility = View.GONE
            binding.addListAddImageButton.visibility = View.GONE
        }
        else {
            binding.addListLoadingIndicator.visibility = View.GONE
            binding.addListImagePreview.visibility = View.VISIBLE
            binding.addListAddImageButton.visibility = View.VISIBLE
        }

        binding.addListAddListButton.isEnabled = !isLoading
        binding.addListCancelButton.isEnabled = !isLoading
    }

    private fun clearErrors() {
        binding.addListListNameInputWrapper.error = null
    }

    private fun showPermissionRationaleDialog() {
        showCustomDialog(
            title = "Permissão necessária",
            message = "É necessário conceder acesso à sua galeria para que você possa selecionar uma capa de lista personalizada.",
            positiveButtonText = "Entendi",
            negativeButtonText = "Cancelar",
            onPositiveClick = {
                requestPermissionLauncher.launch(viewModel.getRequiredPermission())
            }
        )
    }

    private fun showPermissionDeniedDialog() {
        showCustomDialog(
            title = "Permissão negada",
            message = "Você negou o acesso à sua galeria. Para adicionar capas personalizadas, acesse as configurações do aplicativo e forneça permissão manualmente.",
            positiveButtonText = "Conceder",
            negativeButtonText = "Cancelar",
            onPositiveClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        )
    }
}