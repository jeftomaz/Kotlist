package com.example.kotlist.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.kotlist.data.model.User
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.util.PasswordHasher
import kotlinx.coroutines.launch


sealed class SignUpState {
    data object Idle : SignUpState() // Estado ociosonal
    data object Loading : SignUpState() // Estadio de carregamento
    data object Success : SignUpState() // Estado de aprovação

    // Estado de erro genérico
    data class Error(val message: String) : SignUpState() // Estado de erro

    // Estado de falha >> gera uma mensagem para cada campo de erro
    data class ValidationFailure(
        val nameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null
    ) : SignUpState()
}

class SignUpViewModel (private val userRepository: UserRepository) : ViewModel() {

    // LiveData Privado >> a ser modificado apenas pelo ViewModel
    private val _signUpState = MutableLiveData<SignUpState>(SignUpState.Idle)

    // LiveData Público >> a ser observado pela Activity
    val signUpState: LiveData<SignUpState> = _signUpState

    // Função Pública >> a ser chamada pela Acitivty ao clique do botão "Cadastrar"
    fun signUp(name: String, email: String, password: String, confirmPassword: String) {

        val validation = validateInputs(name, email, password, confirmPassword)
        if (validation.isFailure) {

            validation.errorState?.let {
                _signUpState.value = it
            }
            return
        }

        _signUpState.value = SignUpState.Loading
        viewModelScope.launch {
            try {
                val hashedPassword = PasswordHasher.hashPassword(password)

                val newUser = User(name = name, email = email, password = hashedPassword)

                userRepository.signUpUser(newUser)

                _signUpState.postValue(SignUpState.Success)

            } catch (e: Exception) {
                _signUpState.postValue(SignUpState.Error("Ocorreu um erro: ${e.message}"))
            }
        }
    }

    // função para validar dados de entrada/login
    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): ValidationResult {
        var nameError: String? = null
        var emailError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null
        var hasError = false

        // validação do nome >> se é vazio e seu formato
        if (name.isEmpty()) {
            nameError = "O nome não pode ser vazio."
            hasError = true
        } else if (name.any { it.isDigit() }) {
            nameError = "O nome não pode conter números."
            hasError = true
        }

        // validação do e-mail >> se é vazio e seu formato
        if (email.isEmpty()) {
            emailError = "O e-mail não pode ser vazio."
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Insira um e-mail válido."
            hasError = true
        }

        // validação de senha >> se é vazia
        if (password.isEmpty()) {
            passwordError = "A senha não pode ser vazia."
            hasError = true
        }

        // validação de confirmação de senha >> se é vazia
        if (confirmPassword.isEmpty()) {
            confirmPasswordError = "A confirmação de senha não pode ser vazia."
            hasError = true
        }

        // Validação se as senhas coincidem >> somente se ambas não forem vazias
        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            passwordError = "As senhas não coincidem."
            confirmPasswordError = "As senhas não coincidem."
            hasError = true
        }

        // Retorna o resultado da validação
        return if (hasError) {
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

    // Classe de dados interna para organizar o retorno da validação de dados
    private data class ValidationResult(
        val isFailure: Boolean,
        val errorState: SignUpState.ValidationFailure? = null
    )
}