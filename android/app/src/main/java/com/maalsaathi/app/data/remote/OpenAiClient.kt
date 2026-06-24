package com.maalsaathi.app.data.remote

import com.maalsaathi.app.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

object OpenAiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey get() = BuildConfig.OPENAI_API_KEY

    suspend fun transcribeAudio(audioFile: File): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("language", "hi")
                .addFormDataPart("file", audioFile.name, audioFile.asRequestBody("audio/m4a".toMediaType()))
                .build()

            val request = Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val json = JsonParser.parseString(response.body?.string() ?: "{}").asJsonObject
            json.get("text")?.asString ?: throw Exception("No transcription text")
        }
    }

    suspend fun extractTripDetails(text: String): Result<JsonObject> = withContext(Dispatchers.IO) {
        chatCompletion(
            system = """Extract trip details from Hindi/Hinglish/English text. Return ONLY valid JSON, no markdown, no explanation.
Expected format: {"origin":"","destination":"","cargoType":"","cargoWeightTons":0,"freightAmount":0,"advanceAmount":0,"confidence":"high/medium/low"}
Never hallucinate values. Leave 0 or empty string if not mentioned.""",
            user = text,
        )
    }

    suspend fun categorizeEntry(text: String): Result<JsonObject> = withContext(Dispatchers.IO) {
        chatCompletion(
            system = """Categorize this Hindi/Hinglish expense/income/note. Return ONLY valid JSON, no markdown.
Expected format: {"type":"expense/income/note","category":"diesel/toll/food/repair/tyre/other","amount":0,"note":"","emoji":"⛽/🎟️/🍽️/🔧/🛞/📝"}
If amount is not mentioned, set 0. For notes, set amount 0 and type "note".""",
            user = text,
        )
    }

    private fun chatCompletion(system: String, user: String): Result<JsonObject> = runCatching {
        val payload = JsonObject().apply {
            addProperty("model", "gpt-4o-mini")
            addProperty("temperature", 0.1)
            add("messages", Gson().toJsonTree(listOf(
                mapOf("role" to "system", "content" to system),
                mapOf("role" to "user", "content" to user),
            )))
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val json = JsonParser.parseString(body).asJsonObject
        val content = json.getAsJsonArray("choices")
            .get(0).asJsonObject
            .getAsJsonObject("message")
            .get("content").asString
            .trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        JsonParser.parseString(content).asJsonObject
    }
}
