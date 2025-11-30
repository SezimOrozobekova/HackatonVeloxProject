package com.example.velox

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.velox.login.compose.ReminderFrequency
import com.example.velox.network.ApiClient
import com.example.velox.utils.ApiConfig
import com.example.velox.login.viewModel.Task
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime

class ProcessAudioRecord : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var responseTextView: TextView
    private lateinit var recognizedTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_audio_record)

        recognizedTextView = findViewById(R.id.textView)
        responseTextView = findViewById(R.id.responce)

        val recognizedText = intent.getStringExtra("recognized_text")
        recognizedTextView.text = recognizedText

        sendPostRequestToBackend(recognizedText)
    }

    private fun sendPostRequestToBackend(txtRecognized: String?) {
        val prefs = getSharedPreferences("velox_prefs", MODE_PRIVATE)
        val accessToken = prefs.getString("access_token", null)

        if (accessToken == null) {
            updateResponse("Token not found. Please log in again.")
            return
        }

        val requestBody = JSONObject().put("text", txtRecognized)
            .toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}/api/process-text/")
            .post(requestBody)
            .addHeader("Authorization", "JWT $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                updateResponse("Connection error: ${e.localizedMessage}")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string().orEmpty()
                Log.d("ProcessAudioRecord", "Response body: $body")

                if (response.code == 401) {
                    ApiClient.refreshToken(this@ProcessAudioRecord) { success ->
                        if (success) sendPostRequestToBackend(txtRecognized)
                        else updateResponse("Session expired. Please log in again.")
                    }
                    return
                }

                try {
                    val json = JSONObject(body)

                    val date = json.getString("date")
                    val timeStart = json.optString("time_start", "00:00")
                    val localDateTime = LocalDateTime.parse("${date}T$timeStart")

                    val task = Task(
                        title = json.getString("title"),
                        categoryId = json.getInt("category"),
                        hasReminder = json.getBoolean("reminder"),
                        location = json.getString("location"),
                        notes = json.getString("notes"),
                        frequency = ReminderFrequency.valueOf(json.getString("frequency").uppercase()),
                        date = date,
                        timeStart = timeStart,
                        timeEnd = json.optString("time_end", ""),
                        localDateTime = localDateTime
                    )

                    val intent = Intent(this@ProcessAudioRecord, CreatedTaskAI::class.java).apply {
                        putExtra("task", task)
                    }
                    startActivity(intent)

                } catch (e: Exception) {
                    updateResponse("Parsing error: ${e.localizedMessage}")
                }
            }
        })
    }

    private fun updateResponse(message: String) {
        runOnUiThread {
            responseTextView.text = message
        }
    }
}
