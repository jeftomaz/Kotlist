package com.example.kotlist.layoutlogic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityMainTempBinding

// ACTIVITY MAIN TEMPORARIA PARA TESTES

class MainTempActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainTempBinding

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
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
        binding = ActivityMainTempBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainTempMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.mainAddItens.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java).apply {
                putExtra(EXTRA_LIST_ID, ShoppingListRepository.getUserLists(UserRepository.getUserLoggedIn()!!.id).first().id)
            }
            startActivity(intent)
        }

        binding.mainAddListas.setOnClickListener {
            val intent = Intent(this, AddListActivity::class.java)
            startActivity(intent)
        }

        binding.mainListItens.setOnClickListener {
            val intent = Intent(this, ItemListActivity::class.java).apply {
                putExtra(EXTRA_LIST_ID, ShoppingListRepository.getUserLists(UserRepository.getUserLoggedIn()!!.id).first().id)
            }
            startActivity(intent)
        }
    }
}