package com.kotlist.app.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.kotlist.app.data.model.User
import com.kotlist.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: User?) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    data class ValidationFailure(
        val emailError: String? = null,
        val passwordError: String? = null
    ) : LoginUiState()
}

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private data class ValidationResult(
        val isFailure: Boolean,
        val errorState: LoginUiState.ValidationFailure? = null
    )

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun login(email: String, password: String) {
        val validation = validateInputs(email, password)

        if(validation.isFailure) {
            validation.errorState?.let { _loginState.value = it }
            return
        }

        _loginState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val loginUser = User(
                    email = email,
                    password = password,
                    name = ""
                )

                val user = userRepository.signInUser(loginUser)
                _loginState.value = LoginUiState.Success(user)
            }
            catch(e: FirebaseAuthInvalidUserException) {
                _loginState.value = LoginUiState.Error("Erro: usuário não encontrado.")
            }
            catch(e: FirebaseAuthInvalidCredentialsException) {
                _loginState.value = LoginUiState.Error("Erro: e-mail ou senha inválidos.")
            }
            catch(e: Exception) {
                _loginState.value = LoginUiState.Error("Erro: ${e.message}")
            }
        }
    }

    private fun validateInputs(email: String, password: String) : ValidationResult {
        var emailError: String? = null
        var passwordError: String? = null
        var hasError: Boolean = false

        if(email.isEmpty()) {
            emailError = "Insira o e-mail para fazer login."
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Insira um e-mail válido."
            hasError = true
        }

        if(password.isEmpty()) {
            passwordError = "Insira a senha para fazer login."
            hasError = true
        }

        return if(hasError) {
            ValidationResult(
                isFailure = true,
                errorState = LoginUiState.ValidationFailure(
                    emailError = emailError,
                    passwordError = passwordError
                )
            )
        }
        else {
            ValidationResult(isFailure = false)
        }
    }
}