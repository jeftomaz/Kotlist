package com.kotlist.app.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlist.app.data.model.User
import com.kotlist.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    object Loading : LoginUiState()
    object Idle : LoginUiState()
}

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun login(email: String, password: String) {
        if(!isEmailValid(email) or !isPasswordValid(password))
            return

        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            val user = userRepository.loginUser(email, password)

            if(user != null)
                _loginState.value = LoginUiState.Success(user)
            else
                _loginState.value = LoginUiState.Error("E-mail ou senha inválidos.")
        }
    }

    private fun isEmailValid(email: String): Boolean {
        _emailError.value = null

        if(email.isEmpty()) {
            _emailError.value = "Insira o e-mail para fazer login."
            return false
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Insira um e-mail válido."
            return false
        }

        return true
    }

    private fun isPasswordValid(password: String): Boolean {
        _passwordError.value = null

        if(password.isEmpty()) {
            _passwordError.value = "Insira a senha para fazer login."
            return false
        }

        return true
    }
}