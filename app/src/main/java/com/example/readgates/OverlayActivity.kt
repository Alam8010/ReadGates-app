package com.example.readgates

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

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

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var btnMic: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)

        findViewById<TextView>(R.id.tvBookText).text = bookText

        btnMic = findViewById(R.id.btnMic)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken = matches?.firstOrNull() ?: ""
                checkMatch(spoken)
                btnMic.text = "🎤 Start Reading"
                btnMic.isEnabled = true
            }

            override fun onError(error: Int) {
                Toast.makeText(this@OverlayActivity, "Couldn't hear you. Try again.", Toast.LENGTH_SHORT).show()
                btnMic.text = "🎤 Start Reading"
                btnMic.isEnabled = true
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        btnMic.setOnClickListener {
            btnMic.text = "🎤 Listening..."
            btnMic.isEnabled = false
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer.startListening(intent)
        }
    }

    private fun checkMatch(spoken: String) {
        val spokenWords = spoken.lowercase().split(" ").toSet()
        val bookWords = bookText.lowercase().split(Regex("\\W+")).toSet()
        val common = spokenWords.intersect(bookWords)
        val score = if (spokenWords.isEmpty()) 0f else common.size.toFloat() / spokenWords.size.toFloat()

        if (score >= 0.7f) {
            Toast.makeText(this, "✅ Great reading! Unlocking...", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val percent = (score * 100).toInt()
            Toast.makeText(this, "❌ Only $percent% match. Try again!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}