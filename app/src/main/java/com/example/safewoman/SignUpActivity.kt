package com.example.safewoman

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.safewoman.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()
        // Initialize Firebase Realtime Database
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        // Signup button functionality
        binding.signupBtn.setOnClickListener {
            val signupUsername = binding.edtSignupUsername.text.toString()
            val signupPassword = binding.edtSignupPassword.text.toString()
            val signupName = binding.edtSignupName.text.toString()
            val signupEmail = binding.edtSignupEmail.text.toString()

            // Input validations
            when {
                signupName.isEmpty() -> {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }

                signupEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(signupEmail).matches() -> {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                }

                signupUsername.isEmpty() -> {
                    Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                }

                signupPassword.isEmpty() || signupPassword.length < 6 -> {
                    Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    createUserWithEmailPassword(signupName, signupEmail, signupUsername, signupPassword)
                }
            }
        }

        // Redirect to Login page
        binding.loginRedirectTxt.setOnClickListener {
            redirectToLoginPage()
        }
    }

    private fun createUserWithEmailPassword(name: String, email: String, username: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User successfully created with Firebase Authentication
                    val userId = firebaseAuth.currentUser?.uid

                    // Save the user data to Firebase Realtime Database
                    if (userId != null) {
                        val userData = UserData(name, email, username, password)
                        databaseReference.child(userId).setValue(userData)
                            .addOnCompleteListener {
                                Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()
                                redirectToLoginPage()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Failed to save user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun redirectToLoginPage() {
        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
