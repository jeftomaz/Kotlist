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
import androidx.core.net.toUri

class AddListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddListBinding
    private var listCoverImageSelectedUri: String? = null
    private var placeholderImageId: Int = -1

    // VARIÁVEIS DE ESTADO ADICIONADAS
    private var isEditMode = false
    private var listIdToEdit: String? = null
    private var listToEdit: ShoppingList? = null

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

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
        // CHAVE PARA O MODO EDIÇÃO
        const val EXTRA_IS_EDIT_MODE = "EXTRA_IS_EDIT_MODE"
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

        binding = ActivityAddListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.addListMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // LÓGICA DE CARREGAMENTO DE MODO
        listIdToEdit = intent.getStringExtra(EXTRA_LIST_ID)
        isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT_MODE, false)

        if (isEditMode && listIdToEdit != null) {
            listToEdit = ShoppingListRepository.getListById(listIdToEdit!!)
            setupEditMode() // Configura a tela para edição
        } else {
            // Se NÃO for edição, RANDOMIZA o placeholder (Lógica Original)
            placeholderImageId = randomizeListCoverPlaceholder()
        }

        binding.addListAddImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.addListAddListButton.setOnClickListener {
            val listTitle: String = binding.addListListNameInput.text.toString().trim()
            val coverImageUri: String? = listCoverImageSelectedUri
            val placeholderImageId: Int = placeholderImageId
            val userId: String? = UserRepository.getUserLoggedIn()?.id

            // CHAMA A FUNÇÃO UNIFICADA
            handleSaveOrUpdateList(listTitle, coverImageUri, placeholderImageId, userId)
        }

        binding.addListCancelButton.setOnClickListener {
            finish()
        }
    }

    fun randomizeListCoverPlaceholder(): Int {
        val randomizedPlaceholder = ShoppingListRepository.getRandomPlaceholderId()
        // Esta linha agora só é executada se NÃO estiver no modo edição
        binding.addListImagePreview.setImageURI("android.resource://$packageName/$randomizedPlaceholder".toUri())
        return randomizedPlaceholder // Retorna o ID do placeholder
    }

    // RENOMEADA E REFATORADA: Trata tanto Criação quanto Edição
    fun handleSaveOrUpdateList(listTitle: String, coverImageUri: String?, placeholderImageId: Int, userId: String?) {
        if(listTitle.isEmpty()) {
            binding.addListListNameInputWrapper.error = "A lista deve ter um nome."
            return
        }

        if(userId == null) {
            Toast.makeText(this, "Houve um erro ao tentar criar/atualizar a lista.", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode && listToEdit != null) {
            // LÓGICA DE EDIÇÃO (UPDATE)
            val updatedList = listToEdit!!.copy(
                title = listTitle,
                // Prioriza a nova imagem (coverImageUri), senão mantém a existente
                coverImageUri = coverImageUri ?: listToEdit!!.coverImageUri,
                // Mantém o placeholder original
                placeholderImageId = listToEdit!!.placeholderImageId
            )
            ShoppingListRepository.updateList(updatedList)
            Toast.makeText(this, "Lista atualizada com sucesso!", Toast.LENGTH_SHORT).show()

        } else {
            // LÓGICA DE CRIAÇÃO (ADD)
            val newList = ShoppingList(title = listTitle, coverImageUri = coverImageUri, placeholderImageId = placeholderImageId, userId = userId)
            ShoppingListRepository.addList(newList)
            Toast.makeText(this, "Nova lista criada com sucesso!", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    // FUNÇÃO AUXILIAR: Configura a UI e os campos para o modo edição
    private fun setupEditMode() {
        binding.addListTitle.text = "Editar Lista"
        binding.addListAddListButton.text = "Salvar"

        listToEdit?.let { list ->
            binding.addListListNameInput.setText(list.title)

            // Define o estado inicial para a imagem
            listCoverImageSelectedUri = list.coverImageUri
            placeholderImageId = list.placeholderImageId

            // Carrega a imagem: usa a URI real se existir, senão o placeholder
            val imageUriToLoad = list.coverImageUri
                ?: "android.resource://$packageName/${list.placeholderImageId}"
            binding.addListImagePreview.setImageURI(imageUriToLoad.toUri())
        }
    }

    // dev environment (Mantenha ou remova)
    fun createMockedList() {
        val newList = ShoppingList(title =  "Lista teste", coverImageUri = null, placeholderImageId = randomizeListCoverPlaceholder(), userId = UserRepository.getUserLoggedIn()?.id
            ?: "")
        ShoppingListRepository.addList(newList)

        Toast.makeText(this, "Nova lista criada com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}