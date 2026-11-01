package com.example.readgates

import android.content.Context

object BookPrefs {
    private const val PREFS_NAME = "readgate_book"

    fun save(context: Context, title: String, pageText: String, pageNum: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("book_title", title)
            .putString("book_text", pageText)
            .putInt("book_page", pageNum)
            .apply()
    }

    fun getText(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("book_text", null)

    fun getTitle(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("book_title", null)

    fun getPage(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("book_page", 0)
}