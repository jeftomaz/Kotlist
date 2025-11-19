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
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.databinding.ActivityEditListBinding
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class EditListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditListBinding

    private val viewModel: EditListViewModel by viewModels {
        EditListViewModelFactory(ShoppingListRepository)
    }

    private var listCoverImageSelectedUri: String? = null

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
    }

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
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.editListDeleteListButton.setOnClickListener {
            viewModel.deleteList()
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
        binding.editListListNameInput.setText(list.title)

        if(listCoverImageSelectedUri == null) {
            if(list.coverImageUri != null)
                binding.editListImagePreview.setImageURI(list.coverImageUri.toUri())
            else
                binding.editListImagePreview.setImageURI("android.resource://$packageName/${list.placeholderImageId}".toUri())
        }
    }
}