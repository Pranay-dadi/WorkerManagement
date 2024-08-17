package com.example.workermanagement

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.databinding.ActivityRegister1Binding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Register1 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister1Binding
    private lateinit var firebase1: FirebaseDatabase
    private lateinit var databaseReference1: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        firebase1 = FirebaseDatabase.getInstance()
        databaseReference1 = firebase1.reference.child("workers")

        binding.CreateButtonR1.setOnClickListener{
            val UsernameR1 = binding.UsernameR1.text.toString()
            val PasswordR1 = binding.PasswordR1.text.toString()
            val PhoneR1 = binding.PhoneR1.text.toString()
            val AadharR1 = binding.AaadharR1.text.toString()
            val DateR1 = binding.DateR1.text.toString()

            if(UsernameR1.isNotEmpty() && PasswordR1.isNotEmpty() && PhoneR1.isNotEmpty() && AadharR1.isNotEmpty() && DateR1.isNotEmpty()){
                register1(UsernameR1,PasswordR1,PhoneR1,AadharR1,DateR1)
            } else{
                Toast.makeText(this@Register1, "All fielsds are mandatory", Toast.LENGTH_SHORT).show()
            }
        }



    }

    private fun register1(username: String, password: String, phone: String, aadhar:String, date: String){
        databaseReference1.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!dataSnapshot.exists()){
                    val id = databaseReference1.push().key
                    val userData = WorkerData(id, username,password, phone,aadhar, date )
                    databaseReference1.child(id!!).setValue(userData)
                    Toast.makeText(this@Register1, "Registration Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@Register1,Login::class.java))
                    finish()

                } else {
                    Toast.makeText(this@Register1, "User already exists", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Register1, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}