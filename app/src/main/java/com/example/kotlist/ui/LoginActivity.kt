package com.example.kotlist.ui

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
import com.example.kotlist.databinding.ActivityLoginBinding
import com.example.kotlist.ui.ListsActivity.Companion.CREATE_EXAMPLE_LIST
import com.example.kotlist.util.PasswordHasher

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.loginMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginAccessButton.setOnClickListener {
            val email = binding.loginEmailInput.text.toString().trim()
            val password = binding.loginPasswordInput.text.toString().trim()

            validateLogin(email, password)
//            logUserMocked()
        }

        binding.loginCreateAccountButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right_to_left, R.anim.slide_out_right_to_left)
        }
    }

    private fun validateLogin(email: String, password: String) {
        binding.loginEmailInputWrapper.error = null
        binding.loginPasswordInputWrapper.error = null

        if(email.isEmpty() || password.isEmpty()) {
            if(email.isEmpty())
                binding.loginEmailInputWrapper.error = "Insira o e-mail para fazer login."

            if(password.isEmpty())
                binding.loginPasswordInputWrapper.error = "Insira a senha para fazer login."

            Toast.makeText(this, "Insira todos os dados para fazer login.", Toast.LENGTH_SHORT).show()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.loginEmailInputWrapper.error = "Insira um e-mail válido."
            return
        }

        val user = UserRepository.getUserByEmail(email)

        if(user != null && PasswordHasher.checkPassword(password, user.password)) {
            UserRepository.setUserLoggedIn(user)
            Toast.makeText(this, "Bem-vindo(a), ${user.name}", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ListsActivity::class.java).apply {
                putExtra(CREATE_EXAMPLE_LIST, true)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.zoom_in, R.anim.fade_out)
        }
        else {
            Toast.makeText(this, "E-mail ou senha inválidos.", Toast.LENGTH_SHORT).show()
        }
    }

    // dev environment (user mock)
    private fun logUserMocked() {
        val user = User(name = "Admin", email = "admin@gmail.com", password = "admin")
        UserRepository.signUpUser(user)
        UserRepository.setUserLoggedIn(user)

        val intent = Intent(this, ListsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.zoom_in, R.anim.fade_out)
    }
}