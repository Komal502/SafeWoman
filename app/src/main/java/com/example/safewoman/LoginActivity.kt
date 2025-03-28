package com.example.safewoman


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.safewoman.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Check login status
        checkLoginStatus()

        // Set up button click listeners
        setupClickListeners()
    }

    private fun checkLoginStatus() {
        val sharedPrefs = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        if (sharedPrefs.getBoolean("isLoggedIn", false)) {
            navigateToMainActivity()
        }
    }

    private fun setupClickListeners() {
        binding.loginBtn.setOnClickListener {
            val email = binding.edtLoginUsername.text.toString().trim()
            val password = binding.edtLoginPassword.text.toString().trim()

            if (validateFields(email, password)) {
                loginUser(email, password)
            }
        }

        binding.signupRedirectTxt.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun validateFields(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.edtLoginUsername.error = "Email cannot be empty"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtLoginUsername.error = "Enter a valid email address"
            return false
        }

        if (password.isEmpty()) {
            binding.edtLoginPassword.error = "Password cannot be empty"
            return false
        }

        if (password.length < 6) {
            binding.edtLoginPassword.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun loginUser(email: String, password: String) {
        Log.d("LoginActivity", "Attempting login for email: $email")

        // Disable the login button to prevent multiple clicks
        binding.loginBtn.isEnabled = false

        // Use Firebase Authentication to sign in the user
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Re-enable the login button after the task is complete
                binding.loginBtn.isEnabled = true

                if (task.isSuccessful) {
                    // Login successful
                    val user = auth.currentUser
                    saveLoginStatus(user?.email)
                    navigateToMainActivity()
                } else {
                    // Handle Firebase Authentication errors more specifically
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password"
                        is FirebaseAuthUserCollisionException -> "User with this email already exists"
                        else -> "Authentication failed: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveLoginStatus(email: String?) {
        val sharedPrefs = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putBoolean("isLoggedIn", true)
            .putString("userEmail", email)
            .apply()

        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        Log.d("LoginActivity", "Navigating to MainActivity")
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }
}
