package com.kotlist.app.ui.auth

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
import android.app.AlertDialog
import android.text.InputType
import android.util.Patterns
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kotlist.app.R
import com.kotlist.app.data.repository.ServiceLocator
import com.kotlist.app.databinding.ActivityLoginBinding
import com.kotlist.app.databinding.ComponentCustomInputDialogBinding
import com.kotlist.app.extensions.DialogInputConfig
import com.kotlist.app.extensions.showCustomInputDialog
import com.kotlist.app.ui.lists.ListsActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var passwordResetDialog: AlertDialog? = null
    private var passwordResetDialogBinding: ComponentCustomInputDialogBinding? = null

    private val userRepository by lazy {
        ServiceLocator.provideUserRepository()
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

        binding.loginPasswordRecoveryButton.setOnClickListener {
            showPasswordRecoveryDialog()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // observes states
                launch {
                    viewModel.loginState.collect { state ->
                        resetErrors()
                        setLoading(false)

                        when(state) {
                            is LoginUiState.Idle -> { }
                            is LoginUiState.Loading -> {
                                setLoading(true)
                            }
                            is LoginUiState.Success -> {
                                setLoading(false)
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Bem-vindo(a), ${state.user?.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToMainScreen()
                            }
                            is LoginUiState.Error -> {
                                setLoading(false)
                                Toast.makeText(
                                    this@LoginActivity,
                                    state.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is LoginUiState.ValidationFailure -> {
                                setLoading(false)
                                binding.loginEmailInputWrapper.error = state.emailError
                                binding.loginPasswordInputWrapper.error = state.passwordError
                            }
                        }
                    }
                }

                // observes recovery password dialog states
                launch {
                    viewModel.passwordResetDialogState.collect { state ->
                        when (state) {
                            is PasswordResetDialogState.Idle -> {
                                setDialogLoading(false)
                            }
                            is PasswordResetDialogState.Loading -> {
                                setDialogLoading(true)
                            }
                            is PasswordResetDialogState.Success -> {
                                setDialogLoading(false)
                                passwordResetDialog?.dismiss()
                                Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetPasswordResetDialogState()
                            }
                            is PasswordResetDialogState.Error -> {
                                setDialogLoading(false)
                                passwordResetDialogBinding?.customInputDialogInputWrapper?.error = state.message
                            }
                        }
                    }
                }
            }
        }
    }

    private fun resetErrors() {
        binding.loginEmailInputWrapper.error = null
        binding.loginPasswordInputWrapper.error = null
    }

    private fun setLoading(isLoading: Boolean) {
        if(isLoading) {
            binding.loginLoadingIndicator.visibility = View.VISIBLE
            binding.loginEmailInputWrapper.visibility = View.GONE
            binding.loginPasswordInputWrapper.visibility = View.GONE
        }
        else {
            binding.loginLoadingIndicator.visibility = View.GONE
            binding.loginEmailInputWrapper.visibility = View.VISIBLE
            binding.loginPasswordInputWrapper.visibility = View.VISIBLE
        }

        binding.loginAccessButton.isEnabled = !isLoading
        binding.loginCreateAccountButton.isEnabled = !isLoading
    }

    private fun setDialogLoading(isLoading: Boolean) {
        if(isLoading) {
            passwordResetDialogBinding?.customInputLoadingIndicator?.visibility = View.VISIBLE
            passwordResetDialogBinding?.customInputDialogInputWrapper?.visibility = View.GONE
        }
        else {
            passwordResetDialogBinding?.customInputLoadingIndicator?.visibility = View.GONE
            passwordResetDialogBinding?.customInputDialogInputWrapper?.visibility = View.VISIBLE
        }

        passwordResetDialogBinding?.customInputDialogPositiveButton?.isEnabled = !isLoading
        passwordResetDialogBinding?.customInputDialogNegativeButton?.isEnabled = !isLoading
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

    private fun showPasswordRecoveryDialog() {
        viewModel.resetPasswordResetDialogState()

        val (dialog, dialogBinding) = showCustomInputDialog(
            title = "Recuperar senha",
            message = "Insira o e-mail da sua conta. Iremos enviar um link para você redefinir sua senha.",
            positiveButtonText = "Enviar",
            negativeButtonText = "Cancelar",
            inputConfig = DialogInputConfig(
                label = "E-mail",
                inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                errorMessage = "Digite um email válido",
                validator = { email ->
                    Patterns.EMAIL_ADDRESS.matcher(email).matches()
                }
            ),
            onPositiveClick = { email ->
                viewModel.sendPasswordResetEmail(email)
            }
        )

        passwordResetDialog = dialog
        passwordResetDialogBinding = dialogBinding
        dialogBinding.customInputDialogInputWrapper.error = null
    }
}