package com.example.workermanagement

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.workermanagement.databinding.ActivityPostWorkBinding
import com.example.workermanagement.databinding.ActivityRegister1Binding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostWork : AppCompatActivity() {

    private lateinit var binding: ActivityPostWorkBinding
    private lateinit var firebase3: FirebaseDatabase
    private lateinit var databaseReference3: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebase3 = FirebaseDatabase.getInstance()
        databaseReference3 = firebase3.reference.child("work")

        binding.postButton.setOnClickListener{
            val type = binding.type.text.toString()
            val pin = binding.pin.text.toString()
            val phone = binding.phone.text.toString()
            val name = binding.name.text.toString()
            val wage = binding.wage.text.toString()

            if(type.isNotEmpty() && pin.isNotEmpty() && phone.isNotEmpty() && name.isNotEmpty() && wage.isNotEmpty()){
                postwork(type,pin,phone,name,wage)
            } else{
                Toast.makeText(this@PostWork, "All fielsds are mandatory", Toast.LENGTH_SHORT).show()
            }
        }



    }

    private fun postwork(type: String, pin: String, phone: String, name:String, wage: String){
        databaseReference3.orderByChild("type").equalTo(type).addListenerForSingleValueEvent(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!dataSnapshot.exists()){
                    val id = databaseReference3.push().key
                    val userData = id?.let { WorkData(it, type,pin ,phone,name,wage ) }
                    databaseReference3.child(id!!).setValue(userData)
                    Toast.makeText(this@PostWork, "Posted Work Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@PostWork,Manage::class.java))
                    finish()

                } else {
                    Toast.makeText(this@PostWork, "Work already exists", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@PostWork, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}