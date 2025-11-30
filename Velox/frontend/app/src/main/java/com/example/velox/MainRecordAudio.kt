package com.example.velox

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.Chronometer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import android.Manifest
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat

class MainRecordAudio : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private lateinit var chronometer: Chronometer
    private var isRecording = false
    var recognizedText = ""
    private lateinit var progressBar: ProgressBar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        chronometer = findViewById(R.id.chronometer)
        progressBar = findViewById(R.id.progressBar3)
        progressBar.visibility = View.GONE





        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            startListening()

        }

        btnContinue.setOnClickListener {
            stopListening()
            progressBar.visibility = View.VISIBLE
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission is allowed, start listening
            startListening()
        } else {
            // permission is not allowed, display message
            Toast.makeText(this, "Permission denied. The app cannot record audio.", Toast.LENGTH_LONG).show()
        }
    }


    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {
                progressBar.visibility = View.VISIBLE
                chronometer.stop()
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onError(error: Int) {
                    progressBar.visibility = View.GONE
                    val message = when (error) {
                        SpeechRecognizer.ERROR_NETWORK -> "Network error. Check internet connection."
                        SpeechRecognizer.ERROR_NO_MATCH -> "Nothing recognized"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        else -> "Speech recognition error: $error"
                    }
                    chronometer.stop()
                    Toast.makeText(this@MainRecordAudio, message, Toast.LENGTH_LONG).show()
    //                startListening()
                }


            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let {
                    recognizedText = it.joinToString(" ") // Преобразуем список в строку
                    progressBar.visibility = View.GONE
                    val intent = Intent(this@MainRecordAudio, ProcessAudioRecord::class.java)
                    intent.putExtra("recognized_text", recognizedText)
                    startActivity(intent)
                }

            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")


        speechRecognizer.startListening(speechIntent)
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        isRecording = true
    }

    private fun stopListening() {
        if (isRecording) {
            speechRecognizer.stopListening()
            chronometer.stop()
            isRecording = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }
}
