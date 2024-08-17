package com.example.workermanagement

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.workermanagement.databinding.ActivityLoginBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebase1: FirebaseDatabase
    private lateinit var databaseReference1: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebase1 = FirebaseDatabase.getInstance()
        databaseReference1 = firebase1.reference.child("workers")

        binding.loginbutton.setOnClickListener{

            val Username = binding.Username.text.toString()
            val Password = binding.Password.text.toString()

            if(Username.isNotEmpty() && Password.isNotEmpty()){
                loginWorker(Username,Password)
            } else{
                Toast.makeText(this@Login, "All fielsds are mandatory", Toast.LENGTH_SHORT).show()
            }

        }

        }
    private fun loginWorker(username: String, password: String){
        databaseReference1.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.exists()){
                    for(userSnapshot in datasnapshot.children){
                        val userData = userSnapshot.getValue(WorkerData::class.java)

                        if(userData !=null && userData.password == password){
                            Toast.makeText(this@Login, "Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Login, Search::class.java))
                            finish()
                            return
                        }
                    }
                    Toast.makeText(this@Login, "Login Successful", Toast.LENGTH_SHORT).show()

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Login, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }


        })
    }

    fun registerWork(view: View){
        val intent = Intent(this, Register1::class.java)
        startActivity(intent)
    }

    fun search(view: View){
        val intent = Intent(this, Search::class.java)
        startActivity(intent)
    }
    }
