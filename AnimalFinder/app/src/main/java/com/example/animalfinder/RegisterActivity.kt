package com.example.animalfinder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*

private const val TAG = "RegisterActivity"
class RegisterActivity : AppCompatActivity() {

    private var firebaseUserID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val auth = FirebaseAuth.getInstance()
        val firestoredb = FirebaseFirestore.getInstance()

        btnRegister.setOnClickListener {
            btnRegister.isEnabled = false
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this,"Email/Password cant be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                btnRegister.isEnabled = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    firebaseUserID = auth.currentUser!!.uid
                    val documentReference = firestoredb.collection("users").document(firebaseUserID)
                    val userHashMap = HashMap<String, Any>()
                    userHashMap["username"] = etUserName.text.toString()
                    userHashMap["age"] = Integer.valueOf(etAge.text.toString())
                    documentReference.set(userHashMap).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "$user", Toast.LENGTH_SHORT).show()
                            goLoginActivity()
                        }
                    }
                } else {
                    Log.e(TAG, "createUserWithEmailAndPassword failed", task.exception)
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun goLoginActivity() {
        Log.i(TAG, "goLoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}