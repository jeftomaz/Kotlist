package com.example.kotlist.ui.lists

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.databinding.ActivityEditListBinding

class EditListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditListBinding
    private lateinit var listOnEditing: ShoppingList
    private var listCoverImageSelectedUri: String? = null

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
        const val CREATE_EXAMPLE_LIST = "CREATE_EXAMPLE_LIST"
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

        // ViewBinding configuration
        binding = ActivityEditListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.editListMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listOnEditing = ShoppingListRepository.getListById(intent.getStringExtra(EXTRA_LIST_ID)!!)!!
        populateInputFields(listOnEditing)

        binding.editListAddImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.editListDeleteListButton.setOnClickListener {
            ShoppingListRepository.deleteList(intent.getStringExtra(EXTRA_LIST_ID)!!)
            Toast.makeText(this, "Lista excluída.", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ListsActivity::class.java)
            startActivity(intent)
        }

        binding.editListSaveListButton.setOnClickListener {
            val listTitle: String = binding.editListListNameInput.text.toString().trim()
            val coverImageUri: String? = listCoverImageSelectedUri

            validateAndUpdateList(listTitle, coverImageUri)
        }

        binding.editListCancelButton.setOnClickListener {
            finish()
        }
    }

    fun populateInputFields(list: ShoppingList) {
        binding.editListListNameInput.setText(list.title)

        if(list.coverImageUri != null)
            binding.editListImagePreview.setImageURI(list.coverImageUri.toUri())
        else
            binding.editListImagePreview.setImageURI("android.resource://$packageName/${list.placeholderImageId}".toUri())
    }

    fun validateAndUpdateList(listTitle: String, coverImageUri: String?) {
        if(listTitle.isEmpty()) {
            binding.editListListNameInputWrapper.error = "A lista deve ter um nome."
            return
        }

        val updatedList = listOnEditing.copy(
            title = listTitle,
            coverImageUri = coverImageUri ?: listOnEditing.coverImageUri,
            placeholderImageId = listOnEditing.placeholderImageId,
            userId = listOnEditing.userId
        )

        ShoppingListRepository.updateList(updatedList)

        Toast.makeText(this, "Lista atualizada com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}