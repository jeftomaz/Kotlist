package com.example.kotlist.layoutlogic

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlist.R
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityAddListBinding
import com.example.kotlist.databinding.ActivityLoginBinding
import androidx.core.net.toUri

class AddListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddListBinding
    private var listCoverImageSelectedUri: String? = null
    private var placeholderImageId: Int = -1

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if(uri != null) {

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
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        // ViewBinding configuration
        binding = ActivityAddListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.addListMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        placeholderImageId = randomizeListCoverPlaceholder()

        binding.addListAddImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.addListAddListButton.setOnClickListener {
            val listTitle: String = binding.addListListNameInput.text.toString().trim()
            val coverImageUri: String? = listCoverImageSelectedUri
            val placeholderImageId: Int = placeholderImageId
            val userId: String? = UserRepository.getUserLoggedIn()?.id

            createNewList(listTitle, coverImageUri, placeholderImageId, userId)
//            createMockedList()
        }

        binding.addListCancelButton.setOnClickListener {
            finish()
        }
    }

    fun randomizeListCoverPlaceholder(): Int {
        val randomizedPlaceholder = ShoppingListRepository.getRandomPlaceholderId()
        binding.addListImagePreview.setImageURI("android.resource://$packageName/$randomizedPlaceholder".toUri())
        return randomizedPlaceholder
    }

    fun createNewList(listTitle: String, coverImageUri: String?, placeholderImageId: Int, userId: String?) {
        if(listTitle.isEmpty()) {
            binding.addListListNameInputWrapper.error = "A lista deve ter um nome."
            return
        }

        if(userId == null) {
            Toast.makeText(this, "Houve um erro ao tentar criar a lista.", Toast.LENGTH_SHORT).show()
            return
        }

        val newList = ShoppingList(title = listTitle, coverImageUri = coverImageUri, placeholderImageId = placeholderImageId, userId = userId)
        ShoppingListRepository.addList(newList)

        Toast.makeText(this, "Nova lista criada com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }

    // dev environment
    fun createMockedList() {
        val newList = ShoppingList(title =  "Lista teste", coverImageUri = null, placeholderImageId = randomizeListCoverPlaceholder(), userId = UserRepository.getUserLoggedIn()?.id
            ?: "")
        ShoppingListRepository.addList(newList)

        Toast.makeText(this, "Nova lista criada com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}