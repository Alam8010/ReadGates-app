package com.example.readgates

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class BookSearchActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var resultsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_search)

        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
        resultsContainer = findViewById(R.id.resultsContainer)

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isNotEmpty()) searchBooks(query)
            else Toast.makeText(this, "Enter a book title", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchBooks(query: String) {
        progressBar.visibility = View.VISIBLE
        btnSearch.isEnabled = false
        tvStatus.text = "Searching..."
        resultsContainer.removeAllViews()

        Thread {
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val json = URL("https://gutendex.com/books/?search=$encoded").readText()
                val results = JSONObject(json).getJSONArray("results")

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSearch.isEnabled = true

                    if (results.length() == 0) {
                        tvStatus.text = "No books found. Try another title."
                        return@runOnUiThread
                    }
                    tvStatus.text = "${results.length()} results:"

                    for (i in 0 until results.length()) {
                        val book = results.getJSONObject(i)
                        val title = book.getString("title")
                        val authorsArr = book.getJSONArray("authors")
                        val author = if (authorsArr.length() > 0)
                            authorsArr.getJSONObject(0).getString("name") else "Unknown"
                        val formats = book.getJSONObject("formats")

                        // Find any plain text URL
                        var textUrl: String? = null
                        val keys = formats.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            if (key.startsWith("text/plain")) {
                                val v = formats.getString(key)
                                if (v.isNotEmpty()) { textUrl = v; break }
                            }
                        }
                        if (textUrl == null) continue  // skip books with no plain text

                        // Build result card
                        val card = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(20, 16, 20, 16)
                            setBackgroundColor(0xFFF0F0F0.toInt())
                            val lp = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            lp.setMargins(0, 0, 0, 10)
                            layoutParams = lp
                            isClickable = true
                            isFocusable = true
                        }
                        val tvTitle = TextView(this).apply {
                            text = title
                            textSize = 16f
                            setTypeface(null, Typeface.BOLD)
                        }
                        val tvAuthor = TextView(this).apply {
                            text = "by $author"
                            textSize = 13f
                            setTextColor(0xFF666666.toInt())
                        }
                        card.addView(tvTitle)
                        card.addView(tvAuthor)

                        val finalUrl = textUrl
                        card.setOnClickListener {
                            val intent = Intent(this, PagePickerActivity::class.java)
                            intent.putExtra("title", title)
                            intent.putExtra("textUrl", finalUrl)
                            startActivity(intent)
                        }
                        resultsContainer.addView(card)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSearch.isEnabled = true
                    tvStatus.text = "Error: ${e.message}"
                }
            }
        }.start()
    }
}