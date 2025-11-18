package com.example.kotlist.ui.lists

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kotlist.data.repository.ServiceLocator
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityAddListBinding
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

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            listCoverImageSelectedUri = uri.toString()
            binding.addListImagePreview.setImageURI(uri)
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
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
}