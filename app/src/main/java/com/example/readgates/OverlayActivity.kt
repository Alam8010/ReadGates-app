package com.example.readgates

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class OverlayActivity : Activity() {

    private val bookText = """
        It was the best of times, it was the worst of times, it was the age of wisdom, 
        it was the age of foolishness, it was the epoch of belief, it was the epoch of 
        incredulity, it was the season of Light, it was the season of Darkness, it was 
        the spring of hope, it was the winter of despair, we had everything before us, 
        we had nothing before us, we were all going direct to Heaven, we were all going 
        direct the other way.

        There were a king with a large jaw and a queen with a plain face, on the throne 
        of England; there were a king with a large jaw and a queen with a fair face, on 
        the throne of France. In both countries it was clearer than crystal to the lords 
        of the State preserves of loaves and fishes, that things in general were settled 
        for ever.
    """.trimIndent()

    private var spokenSoFar = ""
    private lateinit var tvProgress: TextView
    private lateinit var btnCheck: Button
    private lateinit var btnMic: Button
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)

        findViewById<TextView>(R.id.tvBookText).text = bookText
        tvProgress = findViewById(R.id.tvProgress)
        btnCheck = findViewById(R.id.btnCheck)
        btnMic = findViewById(R.id.btnMic)

        setupSpeechRecognizer()

        btnMic.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                startListening()
            }
        }

        btnCheck.setOnClickListener {
            checkMatch()
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val newText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: ""
                spokenSoFar += " $newText"
                val wordCount = spokenSoFar.trim().split(Regex("\\W+")).filter { it.isNotEmpty() }.size
                tvProgress.text = "Words read: $wordCount"
                btnCheck.isEnabled = true
                btnMic.text = "🎤 Continue Reading"
                btnMic.isEnabled = true
                Toast.makeText(this@OverlayActivity, "Got it! Keep reading or tap Done.", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: Int) {
                btnMic.text = "🎤 Start Reading"
                btnMic.isEnabled = true
                Toast.makeText(this@OverlayActivity, "Mic error ($error). Try again.", Toast.LENGTH_SHORT).show()
            }

            override fun onReadyForSpeech(params: Bundle?) { btnMic.text = "🎤 Listening..." }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        btnMic.isEnabled = false
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
        }
        speechRecognizer.startListening(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening()
        }
    }

    private fun checkMatch() {
        val spokenWords = spokenSoFar.lowercase().split(Regex("\\W+")).filter { it.isNotEmpty() }.toSet()
        val bookWords = bookText.lowercase().split(Regex("\\W+")).filter { it.isNotEmpty() }.toSet()
        val common = spokenWords.intersect(bookWords)
        val score = if (bookWords.isEmpty()) 0f else common.size.toFloat() / bookWords.size.toFloat()
        val percent = (score * 100).toInt()

        if (score >= 0.5f) {
            AppBlockerService.unlockUntil = System.currentTimeMillis() + 300000L // 5 minutes
            Toast.makeText(this, "✅ $percent% — Unlocking!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "❌ Only $percent% — keep reading!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppBlockerService.overlayActive = false
        speechRecognizer.destroy()
    }
}