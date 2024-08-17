package com.example.workermanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Adapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workermanagement.databinding.ActivityManageBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Manage : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var database : FirebaseDatabase
    private lateinit var workReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "null")

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        database = FirebaseDatabase.getInstance()
        workReference = database.getReference("work")

        val query = workReference.orderByChild("name").equalTo(name)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val workList = mutableListOf<WorkData>()
                for (userSnapshot in snapshot.children) {
                    val work = userSnapshot.getValue(WorkData::class.java)
                    if (work != null) {
                        workList.add(work)
                    }
                }
                // Set the adapter with the retrieved data
                val adapter = AdapterClassManage(workList,workReference)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error retrieving data: ${error.message}")
            }
        })
    }

    fun post(view: View){
        val intent = Intent(this, PostWork::class.java)
        startActivity(intent)
    }
}