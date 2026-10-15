package com.ramirezpenayo.placeshare.core.network

    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.withContext
    import okhttp3.MediaType.Companion.toMediaType
    import okhttp3.MultipartBody
    import okhttp3.OkHttpClient
    import okhttp3.Request
    import okhttp3.RequestBody.Companion.toRequestBody
    import org.json.JSONObject

    class CloudinaryUploader(
        private val client: OkHttpClient = OkHttpClient(),
        private val cloudName: String = CloudinaryConfig.cloudName,
        private val uploadPreset: String = CloudinaryConfig.uploadPreset
    ) {
        /** Sube una imagen JPEG y devuelve la URL segura para guardarla en Firestore. */
        suspend fun subirImagen(jpegBytes: ByteArray): String = withContext(Dispatchers.IO) {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart(
                    "file",
                    "lugar.jpg",
                    jpegBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val json = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw CloudinaryException("Error ${response.code}: $json")
                }
                JSONObject(json).getString("secure_url")
            }
        }
    }

    class CloudinaryException(message: String) : Exception(message)