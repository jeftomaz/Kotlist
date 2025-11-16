package com.example.kotlist.ui.lists

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels // Import necessário
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// Import dos repositórios apenas para a Factory
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityAddListBinding

class AddListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddListBinding

    // O ViewModel
    private val viewModel: AddListViewModel by viewModels {
        AddListViewModelFactory(UserRepository, ShoppingListRepository)
    }

    // O estado da imagem selecionada e do placeholder permanecem aqui, estão ligados à View (Activity)
    private var listCoverImageSelectedUri: String? = null
    private var placeholderImageId: Int = -1

    // O Image Picker (Launcher) permanece na Activity
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

        // Pede ao ViewModel para carregar o placeholder inicial
        if (savedInstanceState == null) {
            viewModel.loadInitialPlaceholder()
        }
    }

    private fun setupListeners() {
        binding.addListAddImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.addListAddListButton.setOnClickListener {
            val listTitle = binding.addListListNameInput.text.toString().trim()

            // A Activity apenas delega a ação ao ViewModel
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
        // Observa o placeholder inicial
        viewModel.placeholderImageId.observe(this) { id ->
            placeholderImageId = id // Salva o ID
            // Exibe a imagem placeholder (apenas se o usuário não escolheu outra)
            if (listCoverImageSelectedUri == null) {
                binding.addListImagePreview.setImageURI("android.resource://$packageName/$id".toUri())
            }
        }

        // Observa erros de validação do nome
        viewModel.listNameError.observe(this) { errorMessage ->
            binding.addListListNameInputWrapper.error = errorMessage
        }

        // Observa a ordem de fechar a tela com sucesso
        viewModel.finishEvent.observe(this) { successMessage ->
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
            finish()
        }

        // Observa erros gerais
        viewModel.toastError.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // As funções createNewList, randomizeListCoverPlaceholder e createMockedList foram REMOVIDAS daqui e sua lógica movida para o ViewModel.
}