package com.family.messenger.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.family.messenger.R
import com.family.messenger.data.FirebaseRepository
import com.family.messenger.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            val displayName = binding.displayNameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            if (displayName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, R.string.error_fields_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, R.string.error_passwords_dont_match, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            register(email, password, displayName)
        }

        binding.loginTextView.setOnClickListener {
            finish()
        }
    }

    private fun register(email: String, password: String, displayName: String) {
        showLoading(true)

        lifecycleScope.launch {
            val result = repository.signUp(email, password, displayName)

            showLoading(false)

            if (result.isSuccess) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Registration successful!",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToMain()
            } else {
                Toast.makeText(
                    this@RegisterActivity,
                    result.exceptionOrNull()?.message ?: getString(R.string.error_registration_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.registerButton.isEnabled = !show
        binding.displayNameEditText.isEnabled = !show
        binding.emailEditText.isEnabled = !show
        binding.passwordEditText.isEnabled = !show
        binding.confirmPasswordEditText.isEnabled = !show
    }
}
