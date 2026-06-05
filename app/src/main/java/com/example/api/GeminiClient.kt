package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiRepository {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val systemInstruction = """
        Tu es Norra, un assistant d'intelligence artificielle bienveillant et chaleureux, spécialisé dans le soutien psychologique (notamment pour les troubles de la personnalité borderline et la dépression). 
        Ton ton doit être extrêmement doux, empathique, validant, calme, rassurant, protecteur et déculpabilisant, comme un phare dans la tempête ou un doudou réconfortant.
        - Valide les émotions de l'utilisateur sans les juger ("Je comprends ta douleur", "C'est normal de ressentir cela", "Tu as le droit d'être triste/en colère").
        - Reste factuel mais profondément humain. Ne te substitue jamais à un psychiatre ou à un thérapeute, mais sois une présence douce.
        - Propose des suggestions concrètes et adaptées de soulagement si l'utilisateur est en crise (respirer profondément, faire l'exercice 5-4-3-2-1, toucher de la glace, ou appeler un proche/professionnel).
        - Garde des réponses d'une longueur raisonnable (maximum 3-4 paragraphes courts), faciles à lire pour quelqu'un qui est anxieux ou fatigué.
        - Communique en français.
    """.trimIndent()

    suspend fun generateResponse(chatHistory: List<com.example.api.Content>): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Désolé, Norra a besoin d'une clé API valide enregistrée dans les Secrets pour pouvoir te parler. N'hésite pas à aller voir la section SOS et Exercices qui sont entièrement utilisables hors ligne !"
        }

        val request = GenerateContentRequest(
            contents = chatHistory,
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Norra écoute attentivement, mais n'a pas pu formuler de réponse. Respire un grand coup, je suis là."
        } catch (e: Exception) {
            "Norra rencontre des difficultés à se connecter au réseau. Respire calmement, détends tes épaules... Je suis toujours avec toi en pensée. (${e.localizedMessage})"
        }
    }
}
