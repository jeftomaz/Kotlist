package com.kotlist.app.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.kotlist.app.data.model.User
import com.kotlist.app.data.repository.UserRepository
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

    private data class ValidationResult(
        val isFailure: Boolean,
        val errorState: SignUpState.ValidationFailure? = null
    )

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
                val newUser = User(name = name, email = email, password = password)

                userRepository.signUpUser(newUser)
                _signUpState.value = SignUpState.Success
            }
            catch (e: FirebaseAuthUserCollisionException) {
                // email already in usage exception
                _signUpState.value = SignUpState.Error("Erro: já existe uma conta com este e-mail.")
            }
            catch(e: FirebaseAuthWeakPasswordException) {
                // weak password exception
                _signUpState.value = SignUpState.Error("Erro: a senha deve ter pelo menos 6 caracteres.")
            }
            catch(e: FirebaseAuthInvalidCredentialsException) {
                // invalid email format exception
                _signUpState.value = SignUpState.Error("Erro: formato de e-mail inválido.")
            }
            catch(e: Exception) {
                // other exceptions
                _signUpState.value = SignUpState.Error("Erro: ${e.message}")
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
        } else if(password.length < 6) {
            passwordError = "A senha deve ter pelo menos 6 caracteres."
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
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            )
        } else {
            ValidationResult(isFailure = false)
        }
    }
}