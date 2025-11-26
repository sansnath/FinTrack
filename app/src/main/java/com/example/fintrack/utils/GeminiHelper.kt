package com.example.fintrack.utils

import android.content.Context
import java.util.Properties

object GeminiHelper {

    fun loadApiKey(context: Context): String {
        val properties = Properties()
        val inputStream = context.assets.open("gemini.properties")
        properties.load(inputStream)
        return properties.getProperty("GEMINI_API_KEY") ?: ""
    }
}
