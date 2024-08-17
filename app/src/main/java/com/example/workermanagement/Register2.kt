package com.example.workermanagement

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.databinding.ActivityRegister1Binding
import com.example.workermanagement.databinding.ActivityRegister2Binding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Register2 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister2Binding
    private lateinit var firebase2: FirebaseDatabase
    private lateinit var databaseReference2: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        firebase2 = FirebaseDatabase.getInstance()
        databaseReference2 = firebase2.reference.child("managers")

        binding.CreateButtonR2.setOnClickListener{
            val UsernameR2 = binding.UsernameR2.text.toString()
            val PasswordR2 = binding.PasswordR2.text.toString()
            val PhoneR2 = binding.PhoneR2.text.toString()
            val NameR2 = binding.NameR2.text.toString()
            val DateR2 = binding.DateR2.text.toString()

            if(UsernameR2.isNotEmpty() && PasswordR2.isNotEmpty() && PhoneR2.isNotEmpty() && NameR2.isNotEmpty() && DateR2.isNotEmpty()){
                register2(UsernameR2,PasswordR2,PhoneR2,NameR2,DateR2)
            } else{
                Toast.makeText(this@Register2, "All fielsds are mandatory", Toast.LENGTH_SHORT).show()
            }
        }



    }

    private fun register2(username: String, password: String, phone: String, name:String, date: String){
        databaseReference2.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!dataSnapshot.exists()){
                    val id = databaseReference2.push().key
                    val userData = ManagerData(id, username,password, phone, name,date )
                    databaseReference2.child(id!!).setValue(userData)
                    Toast.makeText(this@Register2, "Registration Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@Register2,Login2::class.java))
                    finish()

                } else {
                    Toast.makeText(this@Register2, "User already exists", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Register2, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}