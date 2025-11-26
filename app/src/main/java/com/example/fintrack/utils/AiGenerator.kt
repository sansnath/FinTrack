package com.example.fintrack.utils

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel

suspend fun generateAiRecommendation(
    context: Context,
    text: String
): String {

    val apiKey = GeminiHelper.loadApiKey(context)

    val model = GenerativeModel(
        modelName = "gemini-3-pro-preview",
        apiKey = apiKey
    )

    val response = model.generateContent(text)

    return response.text ?: "Tidak dapat menghasilkan rekomendasi AI."
}
