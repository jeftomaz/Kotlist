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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

// imports locais
import com.example.kotlist.R
import com.example.kotlist.data.repository.ServiceLocator
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityLoginBinding
import com.example.kotlist.ui.lists.ListsActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val userRepository by lazy {
        ServiceLocator.provideUserRepository(this)
    }

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(userRepository)
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.loginMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.loginAccessButton.setOnClickListener {
            val email = binding.loginEmailInput.text.toString().trim()
            val password = binding.loginPasswordInput.text.toString().trim()
            viewModel.login(email, password)
        }

        binding.loginCreateAccountButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this,
                R.anim.slide_in_right_to_left,
                R.anim.slide_out_right_to_left
            )
            startActivity(intent, options.toBundle())
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // email error
                launch {
                    viewModel.emailError.collect { errorMessage ->
                        binding.loginEmailInputWrapper.error = errorMessage
                    }
                }

                // password error
                launch {
                    viewModel.passwordError.collect { errorMessage ->
                        binding.loginPasswordInputWrapper.error = errorMessage
                    }
                }

                // activity states
                launch {
                    viewModel.loginState.collect { state ->
                        when(state) {
                            is LoginUiState.Idle -> { }
                            is LoginUiState.Success -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Bem-vindo(a), ${state.user.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToMainScreen()
                            }
                            is LoginUiState.Error -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    state.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is LoginUiState.Loading -> {
                                // binding.loginProgressBar.isVisible = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, ListsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val options = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.zoom_in,
            R.anim.fade_out
        )

        startActivity(intent, options.toBundle())
        finish()
    }
}