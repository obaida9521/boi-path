package com.developerobaida.boipath.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.developerobaida.boipath.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signUp.setOnClickListener{
            val email = binding.email.text.toString().trim()
            val password = binding.pass.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"All field required",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length <= 6) {
                Toast.makeText(this, "Password must be greater than 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("Login Response", "createUserWithEmail:success")
                        val user = auth.currentUser

                        if (user!=null) updateUI()
                    } else {
                        Log.w("Login Response", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }
    private fun updateUI(){
        val intent = Intent(this,LogIn::class.java)
        startActivity(intent)
    }
}