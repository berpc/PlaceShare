package com.ramirezpenayo.placeshare.core.network

object CloudinaryConfig {
    val cloudName: String = BuildConfig.cloudName.ifEmpty { BuildConfig.CLOUDINARY_CLOUD_NAME }
    val uploadPreset: String = BuildConfig.uploadPreset.ifEmpty { BuildConfig.CLOUDINARY_UPLOAD_PRESET }
}
