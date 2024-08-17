package com.example.workermanagement

import android.annotation.SuppressLint
import android.content.Context
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
import com.example.workermanagement.databinding.ActivityLogin2Binding
import com.example.workermanagement.databinding.ActivityLoginBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login2 : AppCompatActivity() {

    private lateinit var binding: ActivityLogin2Binding
    private lateinit var firebase2: FirebaseDatabase
    private lateinit var databaseReference2: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        firebase2 = FirebaseDatabase.getInstance()
        databaseReference2 = firebase2.reference.child("managers")

        binding.loginbutton2.setOnClickListener{

            val Username2 = binding.Username2.text.toString()
            val Password2 = binding.Password2.text.toString()

            if(Username2.isNotEmpty() && Password2.isNotEmpty()){
                loginManager(Username2,Password2)
            } else{
                Toast.makeText(this@Login2, "All fielsds are mandatory", Toast.LENGTH_SHORT).show()
            }

        }

    }
    fun loginManager(username: String, password: String){
        databaseReference2.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.exists()){
                    for(userSnapshot in datasnapshot.children){
                        val userData = userSnapshot.getValue(ManagerData::class.java)

                        if(userData !=null && userData.password == password){
                            Toast.makeText(this@Login2, "Login Successful", Toast.LENGTH_SHORT).show()
                            val name = userData.name
                            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("name", name)
                            editor.apply()

                            startActivity(Intent(this@Login2, Manage::class.java))

                            finish()
                            return
                        }
                    }
                    Toast.makeText(this@Login2, "Login Successful", Toast.LENGTH_SHORT).show()

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Login2, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }


        })
    }


    fun registerMan(view: View){
        val intent = Intent(this, Register2::class.java)
        startActivity(intent)
    }

    fun manage(view: View){
        val intent = Intent(this, Manage::class.java)
        startActivity(intent)
    }
}
