package com.example.kotlist

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
class AddNewListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddListBinding

    // função inicial para instanciar o binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.buttonSaveList.setOnClickListener {
            saveNewList()
        }
    }

    private fun saveNewList() {
        val listTitle = binding.addListListNameInput.text.toString().trim()

        if (listTitle.isEmpty()) {
            Toast.makeText(this, "Por favor, insira um nome para a lista.", Toast.LENGTH_SHORT).show()
            return
        }

        // Tem que fazer o banco por aqui -> database.insert(listTitle) etc etc

        Toast.makeText(this, "Lista '$listTitle' criada com sucesso!", Toast.LENGTH_SHORT).show()

        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
            finish()
            return true
        }
    }
}