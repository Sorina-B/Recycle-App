package com.example.recycleapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recycleapp.databinding.ActivityHistoryBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryActivity : AppCompatActivity() {


    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<ScanHistory>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.fabScanNew.setOnClickListener {
            finish()
        }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = HistoryAdapter(historyList)
        binding.historyRecyclerView.adapter = historyAdapter
        fetchHistoryData()
    }

    private fun fetchHistoryData() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val historyRef =
            FirebaseDatabase.getInstance().reference.child("users").child(uid).child("history")
        historyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                historyList.clear()
                for (scanSnapshot in snapshot.children) {
                    val scanItem = scanSnapshot.getValue(ScanHistory::class.java)
                    if (scanItem != null) {
                        historyList.add(scanItem)
                    }
                }
                    historyList.sortByDescending { it.timestamp }
                    historyAdapter.notifyDataSetChanged()
                    if (historyList.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.historyRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.historyRecyclerView.visibility = View.VISIBLE
                    }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@HistoryActivity,
                    "Database Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
