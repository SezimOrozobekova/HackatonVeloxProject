package com.example.velox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.velox.login.LoginComposeActivity
import com.example.velox.login.viewModel.Task
import com.example.velox.network.ApiClient

class CreatedTaskAI : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_created_task_ai)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recycleView = findViewById<RecyclerView>(R.id.rvAiCreatedTask)
        recycleView.layoutManager = LinearLayoutManager(this)

        val task = intent.getSerializableExtra("task") as? Task
        if (task != null) {
            val taskList = listOf(task)
            recycleView.adapter = AdapterCreatedTaskAI(taskList)
        } else {
            recycleView.adapter = AdapterCreatedTaskAI(emptyList())
        }

        val saveButton = findViewById<Button>(R.id.btnSave)
        saveButton.setOnClickListener {
            val taskAi = intent.getSerializableExtra("task") as? Task
            if (taskAi != null) {
                ApiClient.createTask(this, taskAi) { success ->
                    if (success) {
                        val intent = Intent(this, LoginComposeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }

                }
            }
        }


    }
}