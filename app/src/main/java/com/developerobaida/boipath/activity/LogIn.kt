package com.developerobaida.boipath.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.developerobaida.boipath.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LogIn : AppCompatActivity() {
    lateinit var binding: ActivityLogInBinding
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser
        if (user != null) {

            if (user.isEmailVerified) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                showEmailVerificationDialog(user)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.signUp.setOnClickListener {
            startActivity(Intent(this,SignUp::class.java))
        }


        binding.login.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.pass.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) return@setOnClickListener

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("SIGN_IN", "signInWithEmail:success")
                        val user = auth.currentUser

                        if (user != null) {
                            if (user.isEmailVerified) {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                showEmailVerificationDialog(user)
                            }
                        }
                    } else {
                        Log.w("SIGN_IN", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }

    }


    private fun showEmailVerificationDialog(user: FirebaseUser) {
        AlertDialog.Builder(this)
            .setTitle("Email Not Verified")
            .setMessage("Your email is not verified. Please check your inbox and verify your email.")
            .setPositiveButton("Resend Verification Email") { _, _ ->
                user.sendEmailVerification()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to send email: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("OK", null)
            .show()
    }
}