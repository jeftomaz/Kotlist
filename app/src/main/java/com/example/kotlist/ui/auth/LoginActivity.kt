package com.example.kotlist.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.ActivityOptions

// imports locais
import com.example.kotlist.R
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityLoginBinding
import com.example.kotlist.ui.lists.ListsActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(UserRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.Companion.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.Companion.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        // ViewBinding configuration
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.loginMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar os cloques para >> delegar << ao viewModel
        binding.loginAccessButton.setOnClickListener {
            val email = binding.loginEmailInput.text.toString().trim()
            val password = binding.loginPasswordInput.text.toString().trim()

            // Activity só manda o comando
            viewModel.onLoginClicked(email, password)
        }

        binding.loginCreateAccountButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)

            // Atualização de recurso obsoleto
            // startActivity(intent)
            // overridePendingTransition(R.anim.slide_in_right_to_left, R.anim.slide_out_right_to_left)

            val options = ActivityOptions.makeCustomAnimation(this,
                R.anim.slide_in_right_to_left,
                R.anim.slide_out_right_to_left
            )

            startActivity(intent, options.toBundle())
        }

        setupObservers()
    }

    // Define os observers
    private fun setupObservers() {
        viewModel.emailError.observe(this){ errorMessage ->
            binding.loginEmailInputWrapper.error = errorMessage
        }
        viewModel.passwordError.observe(this){ errorMessage ->
            binding.loginPasswordInputWrapper.error = errorMessage
        }

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginUiState.Success -> {
                    Toast.makeText(this, "Bem-vindo(a), ${state.user.name}", Toast.LENGTH_SHORT).show()
                    navigateToMainScreen()
                }
                is LoginUiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                is LoginUiState.Loading -> {
                    // binding.loginProgressBar.visibility = View.VISIBLE
                }
                is LoginUiState.Idle -> {
                    // Estado inicial, não faz nada
                }
            }
        }
    }

    // Direciona o usuário para a tela principal
    private fun navigateToMainScreen() {
        val intent = Intent(this, ListsActivity::class.java).apply {
            putExtra(ListsActivity.Companion.CREATE_EXAMPLE_LIST, true)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Atualização de recurso obsoleto
        //startActivity(intent)
        //overridePendingTransition(R.anim.zoom_in, R.anim.fade_out)

        val options = ActivityOptions.makeCustomAnimation(this,
            R.anim.zoom_in,
            R.anim.fade_out
        )

        startActivity(intent, options.toBundle())
        finish()
    }
}