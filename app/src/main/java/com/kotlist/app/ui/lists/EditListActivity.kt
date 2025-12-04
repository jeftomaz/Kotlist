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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.kotlist.app.data.model.ShoppingList
import com.kotlist.app.data.repository.ServiceLocator
import com.kotlist.app.data.repository.ShoppingListRepository
import com.kotlist.app.databinding.ActivityEditListBinding
import com.kotlist.app.extensions.placeholderIdToDrawable
import com.kotlist.app.extensions.showCustomDialog
import com.kotlist.app.extensions.showDeleteDialog
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class EditListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditListBinding

    private val shoppingListRepository by lazy {
        ServiceLocator.provideShoppingListRepository()
    }

    private val viewModel: EditListViewModel by viewModels {
        EditListViewModelFactory(shoppingListRepository)
    }

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val shouldShowRationale = !isGranted && shouldShowRequestPermissionRationale(
            viewModel.getRequiredPermission()
        )
        viewModel.onPermissionResult(isGranted, shouldShowRationale)
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            viewModel.onImageSelected(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.Companion.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.Companion.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        binding = ActivityEditListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.editListMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listId = intent.getStringExtra(EXTRA_LIST_ID)
        if(listId == null) {
            Toast.makeText(this, "Erro: ID da lista não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        viewModel.loadList(listId)
        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.editListAddImage.setOnClickListener {
            viewModel.onAddImageClicked()
        }

        binding.editListDeleteListButton.setOnClickListener {
            viewModel.onDeleteListClicked()
        }

        binding.editListSaveListButton.setOnClickListener {
            val listName = binding.editListListNameInput.text.toString().trim()
            val customImageUri = viewModel.customImageUrl.value
            viewModel.saveList(listName, customImageUri)
        }

        binding.editListCancelButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // observes ui state
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is EditListUiState.Idle -> {
                                setLoading(false)
                                clearErrors()
                            }
                            is EditListUiState.Loading -> {
                                setLoading(true)
                                clearErrors()
                            }
                            is EditListUiState.Success -> {
                                setLoading(false)
                            }
                            is EditListUiState.Error -> {
                                setLoading(false)
                                Toast.makeText(
                                    this@EditListActivity,
                                    state.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is EditListUiState.ValidationFailure -> {
                                setLoading(false)
                                binding.editListListNameInputWrapper.error = state.nameError
                            }
                        }
                    }
                }

                // observes ui events
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is EditListEvent.ShowToast -> {
                                Toast.makeText(
                                    this@EditListActivity,
                                    event.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is EditListEvent.NavigateBack -> {
                                Toast.makeText(
                                    this@EditListActivity,
                                    event.successMessage,
                                    Toast.LENGTH_SHORT
                                ).show()

                                if(event.navigateToListsScreen)
                                    navigateToListsScreen()
                                else
                                    finish()
                            }
                            is EditListEvent.OpenGallery -> {
                                val intent = Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                )
                                pickImageLauncher.launch(intent)
                            }
                            is EditListEvent.ShowDeleteConfirmation -> {
                                showDeleteConfirmationDialog()
                            }
                        }
                    }
                }

                // observes list data to load
                launch {
                    viewModel.listToEdit.filterNotNull().collect { list ->
                        populateInputFields(list)
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

                // observes custom image
                launch {
                    viewModel.customImageUrl.collect { imageUrl ->
                        if(imageUrl != null) {
                            loadImageWithProgress(imageUrl.toString())
                        }
                    }
                }
            }
        }
    }

    private fun populateInputFields(list: ShoppingList) {
        binding.editListListNameInput.setText(list.name)

        if(viewModel.customImageUrl.value == null) {
            if(list.customCoverImageUrl != null) {
                loadImageWithProgress(list.customCoverImageUrl)
            } else {
                binding.editListImagePreview.setImageResource(
                    placeholderIdToDrawable(list.placeholderImageId)
                )
            }
        }
    }

    private fun loadImageWithProgress(imageUrl: String) {
        binding.editListImageLoadingIndicator.visibility = View.VISIBLE

        binding.editListImagePreview.load(imageUrl) {
            crossfade(true)
            crossfade(300)
            listener(
                onSuccess = { _, _ ->
                    binding.editListImageLoadingIndicator.visibility = View.GONE
                },
                onError = { _, _ ->
                    binding.editListImageLoadingIndicator.visibility = View.GONE
                    Toast.makeText(
                        this@EditListActivity,
                        "Houve um erro ao carregar a capa da lista",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.editListLoadingIndicator.visibility = View.VISIBLE
            binding.editListListNameInputWrapper.visibility = View.GONE
            binding.editListImageLabel.visibility = View.GONE
            binding.editListImagePreview.visibility = View.GONE
            binding.editListAddImage.visibility = View.GONE
        } else {
            binding.editListLoadingIndicator.visibility = View.GONE
            binding.editListListNameInputWrapper.visibility = View.VISIBLE
            binding.editListImageLabel.visibility = View.VISIBLE
            binding.editListImagePreview.visibility = View.VISIBLE
            binding.editListAddImage.visibility = View.VISIBLE
        }

        binding.editListSaveListButton.isEnabled = !isLoading
        binding.editListCancelButton.isEnabled = !isLoading
        binding.editListDeleteListButton.isEnabled = !isLoading
    }

    private fun clearErrors() {
        binding.editListListNameInputWrapper.error = null
    }

    private fun navigateToListsScreen() {
        val intent = Intent(this@EditListActivity, ListsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
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

    private fun showDeleteConfirmationDialog() {
        showDeleteDialog(
            title = "Excluir Lista",
            message = "Tem certeza que deseja excluir essa lista? Esta ação não poderá ser desfeita.",
            positiveButtonText = "Excluir",
            negativeButtonText = "Cancelar",
            onPositiveClick = {
                viewModel.deleteList()
            }
        )
    }
}