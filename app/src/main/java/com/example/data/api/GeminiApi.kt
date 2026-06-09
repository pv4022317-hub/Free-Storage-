package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiApi {
    private const val TAG = "GeminiApi"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if the API Key is configured and looks valid.
     */
    fun isApiKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.contains("PLACEHOLDER")
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Translates a natural language search query into clean filters using Gemini.
     * Retuns null if any error or fallback occurs.
     */
    suspend fun translateSearchQuery(userQuery: String): SearchFilterResult? = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) return@withContext null

        val prompt = """
            Analyze the following user search query for a photo gallery application: "$userQuery"
            Extract search attributes. Match against the search intent.
            Return a valid JSON object with the following fields EXACTLY (do not include markdown wrappers):
            {
               "query": "original query summary",
               "tags": ["extracted", "related", "tags"],
               "faces": ["extracted", "names", "of", "people"],
               "locations": ["extracted", "locations"],
               "objects": ["extracted", "objects"],
               "isVideo": null, // boolean true/false if they specifically asked for video/photos, else null
               "isFavorite": null // boolean true if they asked for favorites, starred, else null
            }
            Do not explain. Just output the clean JSON.
        """.trimIndent()

        val jsonResponse = makePostRequest(MODEL_NAME, prompt) ?: return@withContext null
        try {
            val text = parseTextResponse(jsonResponse) ?: return@withContext null
            // Strip markdown block markers if model returned them
            val cleanedText = cleanJsonString(text)
            val json = JSONObject(cleanedText)
            
            val tagsList = mutableListOf<String>()
            val tagsArray = json.optJSONArray("tags")
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tagsList.add(tagsArray.getString(i).lowercase())
                }
            }

            val facesList = mutableListOf<String>()
            val facesArray = json.optJSONArray("faces")
            if (facesArray != null) {
                for (i in 0 until facesArray.length()) {
                    facesList.add(facesArray.getString(i).lowercase())
                }
            }

            val locationsList = mutableListOf<String>()
            val locationsArray = json.optJSONArray("locations")
            if (locationsArray != null) {
                for (i in 0 until locationsArray.length()) {
                    locationsList.add(locationsArray.getString(i).lowercase())
                }
            }

            val objectsList = mutableListOf<String>()
            val objectsArray = json.optJSONArray("objects")
            if (objectsArray != null) {
                for (i in 0 until objectsArray.length()) {
                    objectsList.add(objectsArray.getString(i).lowercase())
                }
            }

            val isVideo = if (json.isNull("isVideo")) null else json.getBoolean("isVideo")
            val isFavorite = if (json.isNull("isFavorite")) null else json.getBoolean("isFavorite")

            SearchFilterResult(
                query = json.optString("query", userQuery),
                tags = tagsList,
                faces = facesList,
                locations = locationsList,
                objects = objectsList,
                isVideo = isVideo,
                isFavorite = isFavorite
            )
        } catch (e: Exception) {
            Log.e(TAG, "Parsing search filter response failed: ${e.message}. Raw text: $jsonResponse", e)
            null
        }
    }

    /**
     * Generates a poetic or smart Caption, Tags, Faces, and and Objects for a given photo description.
     */
    suspend fun analyzeNewPhoto(title: String, location: String, promptTheme: String): PhotoAnalysisResult? = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) return@withContext null

        val prompt = """
            Generate full AI-powered gallery categorization and a smart descriptive caption.
            Input theme: "$promptTheme"
            Input title: "$title"
            Input location: "$location"
            
            Return a valid JSON object (no explaining, no code blocks):
            {
              "caption": "Generte a gorgeous, poetic 1-sentence caption",
              "tags": ["relevant", "tags"],
              "faces": ["extracted name if there is any person inside topic context, or empty list"],
              "objects": ["identified objects in the visual scene background or foreground"],
              "score": 4.8 // estimated aesthetic design rating out of 5.0
            }
        """.trimIndent()

        val jsonResponse = makePostRequest(MODEL_NAME, prompt) ?: return@withContext null
        try {
            val text = parseTextResponse(jsonResponse) ?: return@withContext null
            val cleanedText = cleanJsonString(text)
            val json = JSONObject(cleanedText)

            val tagsList = mutableListOf<String>()
            val tagsArr = json.optJSONArray("tags")
            if (tagsArr != null) {
                for (i in 0 until tagsArr.length()) {
                    tagsList.add(tagsArr.getString(i))
                }
            }

            val facesList = mutableListOf<String>()
            val facesArr = json.optJSONArray("faces")
            if (facesArr != null) {
                for (i in 0 until facesArr.length()) {
                    facesList.add(facesArr.getString(i))
                }
            }

            val objectsList = mutableListOf<String>()
            val objectsArr = json.optJSONArray("objects")
            if (objectsArr != null) {
                for (i in 0 until objectsArr.length()) {
                    objectsList.add(objectsArr.getString(i))
                }
            }

            PhotoAnalysisResult(
                caption = json.optString("caption", "A beautiful captured memory."),
                tags = tagsList,
                faces = facesList,
                objects = objectsList,
                rating = json.optDouble("score", 4.5).toFloat()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Parsing photo analysis failed: $e", e)
            null
        }
    }

    /**
     * Clean code block ticks: ```json ```
     */
    private fun cleanJsonString(text: String): String {
        var result = text.trim()
        if (result.startsWith("```")) {
            result = result.removePrefix("```json").removePrefix("```JSON").removePrefix("```")
            if (result.endsWith("```")) {
                result = result.substring(0, result.lastIndexOf("```"))
            }
        }
        return result.trim()
    }

    private suspend fun makePostRequest(model: String, prompt: String): String? {
        val apiKey = getApiKey()
        val url = "$BASE_URL$model:generateContent?key=$apiKey"

        val requestBodyJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()

        partObj.put("text", prompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        requestBodyJson.put("contents", contentsArray)

        // Set response schema to json to enforce matching structures where supported
        val generationConfig = JSONObject()
        generationConfig.put("responseMimeType", "application/json")
        requestBodyJson.put("generationConfig", generationConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call unsuccessful. Code: ${response.code}, Msg: ${response.message}, Body: $errorBody")
                    null
                } else {
                    response.body?.string()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network call failed with exception", e)
            null
        }
    }

    private fun parseTextResponse(rawResponse: String): String? {
        return try {
            val json = JSONObject(rawResponse)
            val candidates = json.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val part = parts.getJSONObject(0)
            part.getString("text")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse text from Gemini response", e)
            null
        }
    }
}

data class SearchFilterResult(
    val query: String,
    val tags: List<String>,
    val faces: List<String>,
    val locations: List<String>,
    val objects: List<String>,
    val isVideo: Boolean?,
    val isFavorite: Boolean?
)

data class PhotoAnalysisResult(
    val caption: String,
    val tags: List<String>,
    val faces: List<String>,
    val objects: List<String>,
    val rating: Float
)
