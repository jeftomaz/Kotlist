package com.example.kotlist.ui.auth

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // novo import
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// local
import com.example.kotlist.R
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivitySignupBinding

// Removidos imports de Patterns, User e PasswordHasher


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    // Instância do repositório de usuários
    private val userRepository = UserRepository()

    // Inicialização do ViewModel utilizando o Factory
    private val viewModel: SignUpViewModel by viewModels {
        SignUpViewModelFactory(userRepository)
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
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.signupMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
        observerViewModel()

    }

    // View sinaliza ao ViewModel sobre o clique
    private fun setupClickListeners() {
        binding.signupSignupButton.setOnClickListener {
            val name = binding.signupNameInput.text.toString().trim()
            val email = binding.signupEmailInput.text.toString().trim()
            val password = binding.signupPasswordInput.text.toString().trim()
            val confirmPassword = binding.signupConfirmPasswordInput.text.toString().trim()

            viewModel.signUp(name, email, password, confirmPassword)
        }

        binding.signupBackButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            val options = ActivityOptions.makeCustomAnimation(this,
                R.anim.slide_in_left_to_right,
                R.anim.slide_out_left_to_right
            )

            startActivity(intent, options.toBundle())
        }
    }

    private fun observerViewModel(){
        viewModel.signUpState.observe(this) { state ->
            // Os estados anteriores são limpos
            resetErrors()
            // Esconde o loading por padrão
            setLoading(false)

            when (state) {
                is SignUpState.Idle -> {
                    // Nada a fazer
                }
                is SignUpState.Loading -> {
                    setLoading(true)
                }
                is SignUpState.Success -> {
                    setLoading(false)
                    Toast.makeText(this, "Conta criada com sucesso", Toast.LENGTH_LONG).show()
                    finish()
                }
                is SignUpState.Error -> {
                    setLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is SignUpState.ValidationFailure -> {
                    setLoading(false)
                    binding.signupNameInputWrapper.error = state.nameError
                    binding.signupEmailInputWrapper.error = state.emailError
                    binding.signupPasswordInputWrapper.error = state.passwordError
                    binding.signupConfirmPasswordInputWrapper.error = state.confirmPasswordError
                }
            }
        }
    }

    // Função auxiliar para carregamento
    private fun setLoading(isLoading: Boolean) {
        binding.signupSignupButton.isEnabled = !isLoading
        binding.signupBackButton.isEnabled = !isLoading
    }

    // Função auxiliar para limpar erros
    private fun resetErrors() {
        binding.signupNameInputWrapper.error = null
        binding.signupEmailInputWrapper.error = null
        binding.signupPasswordInputWrapper.error = null
        binding.signupConfirmPasswordInputWrapper.error = null
    }
}