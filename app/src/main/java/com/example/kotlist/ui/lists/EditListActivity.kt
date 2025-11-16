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
import com.example.kotlist.data.model.ShoppingList
// Import do repositório apenas para a Factory
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.databinding.ActivityEditListBinding

class EditListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditListBinding

    // O ViewModel
    private val viewModel: EditListViewModel by viewModels {
        EditListViewModelFactory(ShoppingListRepository)
    }

    // Estado da imagem selecionada (ligado à View)
    private var listCoverImageSelectedUri: String? = null

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
        // CREATE_EXAMPLE_LIST não é usado aqui, pode ser removido
    }

    // Seletor de imagem (ligado à View)
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            listCoverImageSelectedUri = uri.toString()
            binding.editListImagePreview.setImageURI(uri)
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

        binding = ActivityEditListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.editListMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Pega o ID e manda o ViewModel carregar
        val listId = intent.getStringExtra(EXTRA_LIST_ID)
        if (listId == null) {
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
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.editListDeleteListButton.setOnClickListener {
            // Apenas avisa o ViewModel
            viewModel.deleteList()
        }

        binding.editListSaveListButton.setOnClickListener {
            val listTitle = binding.editListListNameInput.text.toString().trim()

            // Apenas avisa o ViewModel
            viewModel.saveList(listTitle, listCoverImageSelectedUri)
        }

        binding.editListCancelButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        // Observa a lista para preencher os campos
        viewModel.listToEdit.observe(this) { list ->
            populateInputFields(list)
        }

        // Observa erros de validação
        viewModel.listNameError.observe(this) { errorMessage ->
            binding.editListListNameInputWrapper.error = errorMessage
        }

        // Observa sucesso na atualização
        viewModel.updateSuccessEvent.observe(this) { successMessage ->
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
            finish()
        }

        // Observa sucesso na exclusão
        viewModel.deleteSuccessEvent.observe(this) { successMessage ->
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()

            // Navega de volta para a ListsActivity, limpando a pilha
            val intent = Intent(this, ListsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // Função de UI: Apenas preenche os campos com os dados recebidos
    private fun populateInputFields(list: ShoppingList) {
        binding.editListListNameInput.setText(list.title)

        // Se o usuário já selecionou uma nova imagem, não sobrescreva
        if (listCoverImageSelectedUri == null) {
            if (list.coverImageUri != null)
                binding.editListImagePreview.setImageURI(list.coverImageUri.toUri())
            else
                binding.editListImagePreview.setImageURI("android.resource://$packageName/${list.placeholderImageId}".toUri())
        }
    }

    // A função validateAndUpdateList foi removida e a lógica foi movida para o ViewModel
}