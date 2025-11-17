package com.example.kotlist.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlist.data.model.User
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.util.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SignUpState {
    data object Idle : SignUpState()
    data object Loading : SignUpState()
    data object Success : SignUpState()
    data class Error(val message: String) : SignUpState()
    data class ValidationFailure(
        val nameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null
    ) : SignUpState()
}

class SignUpViewModel (private val userRepository: UserRepository) : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        val validation = validateInputs(name, email, password, confirmPassword)
        if(validation.isFailure) {
            validation.errorState?.let { _signUpState.value = it }
            return
        }
        _signUpState.value = SignUpState.Loading
        viewModelScope.launch {
            try {
                val hashedPassword = PasswordHasher.hashPassword(password)
                val newUser = User(name = name, email = email, password = hashedPassword)

                userRepository.signUpUser(newUser)

                _signUpState.value = SignUpState.Success

            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("Ocorreu um erro: ${e.message}")
            }
        }
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): ValidationResult {
        var nameError: String? = null
        var emailError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null
        var hasError = false

        if(name.isEmpty()) {
            nameError = "O nome não pode ser vazio."
            hasError = true
        } else if (name.any { it.isDigit() }) {
            nameError = "O nome não pode conter números."
            hasError = true
        }

        if(email.isEmpty()) {
            emailError = "O e-mail não pode ser vazio."
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Insira um e-mail válido."
            hasError = true
        }

        if(password.isEmpty()) {
            passwordError = "A senha não pode ser vazia."
            hasError = true
        }

        if(confirmPassword.isEmpty()) {
            confirmPasswordError = "A confirmação de senha não pode ser vazia."
            hasError = true
        }

        if(password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            passwordError = "As senhas não coincidem."
            confirmPasswordError = "As senhas não coincidem."
            hasError = true
        }

        return if(hasError) {
            ValidationResult(
                isFailure = true,
                errorState = SignUpState.ValidationFailure(
                    nameError, emailError, passwordError, confirmPasswordError
                )
            )
        } else {
            ValidationResult(isFailure = false)
        }
    }

    private data class ValidationResult(
        val isFailure: Boolean,
        val errorState: SignUpState.ValidationFailure? = null
    )
}