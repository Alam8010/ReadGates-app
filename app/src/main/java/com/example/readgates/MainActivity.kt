package com.example.readgates

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnChooseBook).setOnClickListener {
            startActivity(Intent(this, BookImportActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val title = BookPrefs.getTitle(this)
        val page = BookPrefs.getPage(this)
        findViewById<TextView>(R.id.tvCurrentBook).text = if (title != null)
            "\"$title\"\nPage $page selected ✅"
        else
            "No book selected yet."
    }
}