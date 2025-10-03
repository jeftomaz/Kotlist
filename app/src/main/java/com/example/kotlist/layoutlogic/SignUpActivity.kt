package com.example.kotlist.layoutlogic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlist.R
import com.example.kotlist.data.model.User
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivitySignupBinding
import com.example.kotlist.util.PasswordHasher

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
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

        binding.signupSignupButton.setOnClickListener {
            val name = binding.signupNameInput.text.toString().trim()
            val email = binding.signupEmailInput.text.toString().trim()
            val password = binding.signupPasswordInput.text.toString().trim()
            val confirmPassword = binding.signupConfirmPasswordInput.text.toString().trim()

            validateAndSignUp(name, email, password, confirmPassword)
        }

        binding.signupBackButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left_to_right, R.anim.slide_out_left_to_right)
        }
    }

    private fun validateAndSignUp(name: String, email: String, password: String, confirmPassword: String) {
        binding.signupNameInputWrapper.error = null
        binding.signupEmailInputWrapper.error = null
        binding.signupPasswordInputWrapper.error = null
        binding.signupConfirmPasswordInputWrapper.error = null

        // validates empty inputs
        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            if(name.isEmpty())
                binding.signupNameInputWrapper.error = "O nome não pode ser vazio."

            if(email.isEmpty())
                binding.signupEmailInputWrapper.error = "O e-mail não pode ser vazio."

            if(password.isEmpty())
                binding.signupPasswordInputWrapper.error = "A senha não pode ser vazia."

            if(confirmPassword.isEmpty())
                binding.signupConfirmPasswordInputWrapper.error = "A confirmação de senha não pode ser vazia."

            Toast.makeText(this, "Preencha todos os campos para cadastrar.", Toast.LENGTH_SHORT).show()
            return
        }

        // validates numbers in name
        if(name.any { it.isDigit() }) {
            binding.signupNameInputWrapper.error = "O nome não pode conter números."
            return
        }

        // validates email format
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signupEmailInputWrapper.error = "Insira um e-mail válido."
            return
        }

        // validates password confirmation
        if(password != confirmPassword) {
            binding.signupPasswordInputWrapper.error = "As senhas não coincidem."
            binding.signupConfirmPasswordInputWrapper.error = "As senhas não coincidem."
            return
        }

        val newUser = User(name = name, email = email, password = PasswordHasher.hashPassword(password))

        UserRepository.signUpUser(newUser)

        Toast.makeText(this, "Conta criada com sucesso", Toast.LENGTH_LONG).show()
        finish()
    }
}