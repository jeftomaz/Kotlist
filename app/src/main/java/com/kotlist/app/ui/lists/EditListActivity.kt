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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kotlist.app.data.model.ShoppingList
import com.kotlist.app.data.repository.ServiceLocator
import com.kotlist.app.data.repository.ShoppingListRepository
import com.kotlist.app.databinding.ActivityEditListBinding
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

    private var listCoverImageSelectedUri: String? = null

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
                    binding.editListImagePreview.setImageURI(uri)
                } catch (e: SecurityException) {
                    listCoverImageSelectedUri = uri.toString()
                    binding.editListImagePreview.setImageURI(uri)
                }
            }
        } else {
            Toast.makeText(this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show()
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
            val listTitle = binding.editListListNameInput.text.toString().trim()
            viewModel.saveList(listTitle, listCoverImageSelectedUri)
        }

        binding.editListCancelButton.setOnClickListener {
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

                // load list data
                launch {
                    viewModel.listToEdit.filterNotNull().collect { list ->
                        populateInputFields(list)
                    }
                }

                // name error message
                launch {
                    viewModel.listNameError.collect { errorMessage ->
                        binding.editListListNameInputWrapper.error = errorMessage
                    }
                }

                // update list event
                launch {
                    viewModel.updateEventMessage.collect { message ->
                        Toast.makeText(this@EditListActivity, message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                // observes confirmation dialog event
                launch {
                    viewModel.showDeleteConfirmation.collect {
                        showDeleteConfirmationDialog()
                    }
                }

                // delete list event
                launch {
                    viewModel.deleteEventMessage.collect { message ->
                        Toast.makeText(this@EditListActivity, message, Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@EditListActivity, ListsActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }

                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun populateInputFields(list: ShoppingList) {
//        binding.editListListNameInput.setText(list.title)
//
//        if(listCoverImageSelectedUri == null) {
//            if(list.coverImageUri != null)
//                binding.editListImagePreview.setImageURI(list.coverImageUri.toUri())
//            else
//                binding.editListImagePreview.setImageURI("android.resource://$packageName/${list.placeholderImageId}".toUri())
//        }
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