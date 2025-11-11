package com.example.kotlist.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlist.data.model.User
import com.example.kotlist.data.repository.UserRepository

// Classe para representar os estaos da UI
sealed class LoginUiState {
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    object Loading : LoginUiState()
    object Idle : LoginUiState() // Estado inicial
}


class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Lógica de verificação do e-mail
    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    // Lógica de verificação da senha
    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    // Lógica de verificação do estado atual
    private val _loginState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val loginState: LiveData<LoginUiState> = _loginState

    // Função para realizar login ao clique do botão
    fun onLoginClicked(email: String, password: String) {

        // Verifica se o pedido é válido chamando as funções abaixo
        if (!isEmailValid(email) or !isPasswordValid(password)) {
            return
        }

        // Define o estado para carregando/loading
        _loginState.value = LoginUiState.Loading

        val user = userRepository.loginUser(email, password)

        // Verifica se o usuário não está vazio
        if (user != null) {
            _loginState.value = LoginUiState.Success(user)
        } else {
            _loginState.value = LoginUiState.Error("E-mail ou senha inválidos.")
        }
    }

    // Verificação se o e-mail é do tipo válido e não está vazio
    private fun isEmailValid(email: String): Boolean {
        _emailError.value = null
        if (email.isEmpty()) {
            _emailError.value = "Insira o e-mail para fazer login."
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Insira um e-mail válido."
            return false
        }
        return true
    }

    // Verificação se a senha é do tipo válido e não está vazia
    private fun isPasswordValid(password: String): Boolean {
        _passwordError.value = null
        if (password.isEmpty()) {
            _passwordError.value = "Insira a senha para fazer login."
            return false
        }
        return true
    }
}