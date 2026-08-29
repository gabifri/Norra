package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class BrevoSender(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class BrevoRecipient(
    @Json(name = "name") val name: String?,
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class BrevoEmailRequest(
    @Json(name = "sender") val sender: BrevoSender,
    @Json(name = "to") val to: List<BrevoRecipient>,
    @Json(name = "subject") val subject: String,
    @Json(name = "htmlContent") val htmlContent: String
)

interface BrevoApiService {
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("v3/smtp/email")
    suspend fun sendEmail(
        @Header("api-key") apiKey: String,
        @Body request: BrevoEmailRequest
    )
}

object BrevoRetrofitClient {
    private const val BASE_URL = "https://api.brevo.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: BrevoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(BrevoApiService::class.java)
    }
}

class EmailRepository {
    private val apiKey = BuildConfig.BREVO_API_KEY

    suspend fun sendWelcomeEmail(userEmail: String, userName: String, password: String = "") = withContext(Dispatchers.IO) {
        val cleanApiKey = apiKey.trim().trim('"').trim('\'')
        if (cleanApiKey.isBlank() ||
            cleanApiKey == "YOUR_BREVO_API_KEY" ||
            cleanApiKey == "MY_BREVO_API_KEY" ||
            cleanApiKey.startsWith("YOUR_") ||
            cleanApiKey.length < 10
        ) {
            android.util.Log.w("NorraEmail", "Brevo API Key is not configured or is a placeholder. Skipping welcome email.")
            return@withContext
        }

        val credentialsBlock = if (password.isNotBlank()) {
            """
            <div style="background-color: #f5f6fa; border-radius: 8px; padding: 15px; margin-top: 20px; text-align: left;">
                <h3 style="margin-top: 0; color: #333;">Vos informations de connexion</h3>
                <p style="margin-bottom: 5px;"><strong>Identifiant :</strong> $userEmail</p>
                <details style="cursor: pointer;">
                    <summary style="font-weight: bold; color: #6C7DFA; outline: none;">Afficher mon mot de passe</summary>
                    <div style="margin-top: 10px; padding: 10px; background-color: #e2e5fc; border-radius: 5px; font-family: monospace; font-size: 16px;">
                        $password
                    </div>
                </details>
            </div>
            """
        } else ""

        val htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333333; line-height: 1.6;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; text-align: center;">
                    <h2 style="color: #6C7DFA;">Bienvenue sur Norra ! 🌟</h2>
                    <p>Bonjour <b>${userName}</b>,</p>
                    <p>Nous sommes extrêmement heureux de vous compter parmi nous. Norra a été conçue pour être votre espace sûr, un refuge doux pour comprendre vos émotions et vous accompagner dans votre quotidien.</p>
                    $credentialsBlock
                    <p style="margin-top: 20px;">Prenez votre temps pour explorer l'application, tester les exercices de respiration et utiliser le journal selon vos envies. C'est votre espace, et il n'y a aucune pression.</p>
                    <br/>
                    <p>Prenez bien soin de vous,<br/><b>L'équipe Norra</b></p>
                </div>
            </body>
            </html>
        """.trimIndent()

        val request = BrevoEmailRequest(
            sender = BrevoSender(name = "Norra App", email = "helpmenorra@gmail.com"),
            to = listOf(BrevoRecipient(name = userName, email = userEmail)),
            subject = "Bienvenue sur Norra ! \uD83C\uDF1F",
            htmlContent = htmlContent
        )

        try {
            android.util.Log.i("NorraEmail", "Attempting to send email to $userEmail...")
            BrevoRetrofitClient.service.sendEmail(apiKey = cleanApiKey, request = request)
            android.util.Log.i("NorraEmail", "Welcome email sent successfully to $userEmail")
        } catch (e: HttpException) {
            val code = e.code()
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            if (code == 401) {
                android.util.Log.e("NorraEmail", "Brevo Authentication failed (HTTP 401): Invalid or unauthorized API key. Check BREVO_API_KEY in AI Studio Secrets. Details: $errorBody")
            } else {
                android.util.Log.e("NorraEmail", "Brevo API HTTP Error $code: $errorBody", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("NorraEmail", "Error sending email: ${e.message}", e)
        }
    }
}
