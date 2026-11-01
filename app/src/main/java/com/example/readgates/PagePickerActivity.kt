package com.example.readgates

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PagePickerActivity : AppCompatActivity() {

    private lateinit var tvBookTitle: TextView
    private lateinit var tvPageInfo: TextView
    private lateinit var tvPageText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnSelectOne: Button
    private lateinit var btnSelectTwo: Button

    private var pages = listOf<String>()
    private var currentPage = 0
    private var bookTitle = ""

    companion object {
        private const val PAGE_SIZE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_picker)

        tvBookTitle = findViewById(R.id.tvBookTitle)
        tvPageInfo = findViewById(R.id.tvPageInfo)
        tvPageText = findViewById(R.id.tvPageText)
        progressBar = findViewById(R.id.progressBar)
        scrollView = findViewById(R.id.scrollView)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnSelectOne = findViewById(R.id.btnSelectOne)
        btnSelectTwo = findViewById(R.id.btnSelectTwo)

        bookTitle = intent.getStringExtra("title") ?: "Unknown"
        val fileUri = intent.getStringExtra("fileUri") ?: run {
            Toast.makeText(this, "No file provided", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        tvBookTitle.text = bookTitle

        btnPrev.setOnClickListener { navigatePage(-1) }
        btnNext.setOnClickListener { navigatePage(1) }
        btnSelectOne.setOnClickListener { saveSelection(1) }
        btnSelectTwo.setOnClickListener { saveSelection(2) }

        loadFromUri(Uri.parse(fileUri))
    }

    private fun loadFromUri(uri: Uri) {
        Thread {
            try {
                val raw = contentResolver.openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    ?: throw Exception("Could not read file")

                val chunks = mutableListOf<String>()
                var pos = 0
                while (pos < raw.length) {
                    var end = minOf(pos + PAGE_SIZE, raw.length)
                    if (end < raw.length) {
                        val spaceIdx = raw.lastIndexOf(' ', end)
                        if (spaceIdx > pos) end = spaceIdx
                    }
                    chunks.add(raw.substring(pos, end).trim())
                    pos = end
                }

                runOnUiThread {
                    pages = chunks
                    currentPage = 0
                    progressBar.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE
                    btnNext.isEnabled = pages.size > 1
                    btnSelectOne.isEnabled = true
                    btnSelectTwo.isEnabled = pages.size > 1
                    showPage()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to read file: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }.start()
    }


    private fun showPage() {
        tvPageText.text = pages[currentPage]
        tvPageInfo.text = "Page ${currentPage + 1} of ${pages.size}"
        scrollView.scrollTo(0, 0)
    }

    private fun navigatePage(delta: Int) {
        currentPage = (currentPage + delta).coerceIn(0, pages.size - 1)
        btnPrev.isEnabled = currentPage > 0
        btnNext.isEnabled = currentPage < pages.size - 1
        btnSelectTwo.isEnabled = currentPage < pages.size - 1
        showPage()
    }

    private fun saveSelection(pageCount: Int) {
        val endPage = minOf(currentPage + pageCount, pages.size)
        val combined = pages.subList(currentPage, endPage).joinToString("\n\n")
        val label = if (pageCount > 1) "Pages ${currentPage + 1}–$endPage" else "Page ${currentPage + 1}"
        BookPrefs.save(this, bookTitle, combined, currentPage + 1)
        Toast.makeText(this, "✅ Saved: $label of \"$bookTitle\"", Toast.LENGTH_LONG).show()
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1200)
    }
}