package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun getHelpResponse(apiKey: String, userMessage: String, chatHistory: List<GeminiContent> = emptyList()): String {
        if (apiKey.isBlank()) {
            return "Please ask the admin to configure the Gemini API Key in the Admin Panel to enable live support!"
        }

        // Construct history and prompt
        val contentsList = mutableListOf<GeminiContent>()
        contentsList.addAll(chatHistory)
        contentsList.add(GeminiContent(parts = listOf(GeminiPart(text = userMessage))))

        val systemPrompt = "You are a helpful support chatbot for Survo Quick. " +
                "Survo Quick is an Android application where users can complete tasks and surveys to earn coins. " +
                "These coins can be redeemed for UPI cash, Paytm cash, or Amazon Vouchers. " +
                "The conversion rate is set dynamically by the admin (e.g., 10 coins = 10 INR). " +
                "Users can take CPX Research Surveys directly on their Home screen when the link is added. " +
                "Be polite, helpful, and concise. Offer quick answers about the app and general tech support."

        val request = GeminiRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I received an empty response. Please try asking again."
        } catch (e: Exception) {
            "Could not connect to AI Support: ${e.localizedMessage ?: e.message}"
        }
    }
}
