package com.example.kotlist.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.kotlist.data.repository.UserRepository

// Recebe as mesmas dependências do ViewModel >> UserRepository
class SignUpViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {

    // Métod0 chamado pelo sistema ao solicitar o ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignUpViewModel(userRepository) as T
        }

        // Se for um ViewModel desconhecido, lança uma exceção
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}