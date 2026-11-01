package com.example.readgates

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BookImportActivity : AppCompatActivity() {

    private lateinit var tvFileName: TextView
    private lateinit var btnPickFile: Button
    private lateinit var btnBrowsePages: Button

    private var selectedUri: Uri? = null
    private var selectedFileName: String = ""

    companion object {
        private const val PICK_FILE_REQUEST = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_import)

        tvFileName = findViewById(R.id.tvFileName)
        btnPickFile = findViewById(R.id.btnPickFile)
        btnBrowsePages = findViewById(R.id.btnBrowsePages)

        btnPickFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }

        btnBrowsePages.setOnClickListener {
            val uri = selectedUri ?: return@setOnClickListener
            val intent = Intent(this, PagePickerActivity::class.java).apply {
                putExtra("title", selectedFileName)
                putExtra("fileUri", uri.toString())
            }
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedUri = uri
            selectedFileName = getFileName(uri)
            tvFileName.text = "📄 $selectedFileName"
            btnBrowsePages.isEnabled = true
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "Unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && idx != -1) name = cursor.getString(idx)
        }
        return name.removeSuffix(".txt")
    }
}