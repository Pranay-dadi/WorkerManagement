package com.example.workermanagement

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Search : AppCompatActivity() {

    private lateinit var recyclerViewSearch: RecyclerView
    private lateinit var database : FirebaseDatabase
    private lateinit var workReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recyclerViewSearch = findViewById(R.id.recyclerViewSearch)

        recyclerViewSearch.layoutManager = LinearLayoutManager(this)
        recyclerViewSearch.setHasFixedSize(true)

        database = FirebaseDatabase.getInstance()
        workReference = database.getReference("work")

        workReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val workList = mutableListOf<WorkData>()
                for (userSnapshot in snapshot.children) {
                    val work = userSnapshot.getValue(WorkData::class.java)
                    if (work != null) {
                        workList.add(work)
                    }
                }
                // Set the adapter with the retrieved data
                val adapter = AdapterClassSearch(workList)
                recyclerViewSearch.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error retrieving data: ${error.message}")
            }
        })
    }
}