package brawijaya.example.purisaehomestay.data.repository

import brawijaya.example.purisaehomestay.BuildConfig as AppBuildConfig

object BuildConfig {
    val CLOUDINARY_CLOUD_NAME: String
        get() = AppBuildConfig.CLOUDINARY_CLOUD_NAME

    val CLOUDINARY_API_KEY: String
        get() = AppBuildConfig.CLOUDINARY_API_KEY

    val CLOUDINARY_API_SECRET: String
        get() = AppBuildConfig.CLOUDINARY_API_SECRET
}