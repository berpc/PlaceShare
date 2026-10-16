package com.ramirezpenayo.placeshare.core.network

import com.ramirezpenayo.placeshare.core.network.BuildConfig

object CloudinaryConfig {
    val cloudName: String = BuildConfig.CLOUDINARY_CLOUD_NAME
    val uploadPreset: String = BuildConfig.CLOUDINARY_UPLOAD_PRESET
}
