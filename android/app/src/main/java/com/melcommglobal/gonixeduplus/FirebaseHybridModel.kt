package com.melcommglobal.gonixeduplus

import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.GenerativeBackend
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.OnDeviceModelOption
import com.google.firebase.ai.type.PublicPreviewAPI

@OptIn(PublicPreviewAPI::class)
object FirebaseHybridModel {

    const val CLOUD_MODEL_NAME = "gemini-3.5-flash-lite"

    enum class ModelChoice {
        NANO_4_FULL_PREVIEW,
        NANO_4_FAST_PREVIEW,
        STABLE
    }

    fun createLocalModel(
        choice: ModelChoice = ModelChoice.NANO_4_FULL_PREVIEW
    ): GenerativeModel {

        val modelOption = when (choice) {
            ModelChoice.NANO_4_FULL_PREVIEW ->
                OnDeviceModelOption.PREVIEW

            ModelChoice.NANO_4_FAST_PREVIEW ->
                OnDeviceModelOption.PREVIEW_FAST

            ModelChoice.STABLE ->
                OnDeviceModelOption.STABLE
        }

        val firebaseAI = FirebaseAI.getInstance(
            FirebaseApp.getInstance(),
            GenerativeBackend.googleAI()
        )

        return firebaseAI.generativeModel(
            modelName = CLOUD_MODEL_NAME,
            onDeviceConfig = OnDeviceConfig(
                mode = InferenceMode.ONLY_ON_DEVICE,
                modelOption = modelOption
            )
        )
    }

    suspend fun getStatus(model: GenerativeModel): String {
        val extension = model.onDeviceExtension
            ?: return "UNAVAILABLE"

        return extension.checkStatus().toString()
    }

    suspend fun getModelName(model: GenerativeModel): String {
        val extension = model.onDeviceExtension
            ?: return "UNKNOWN"

        return extension.getModelName()
    }
}
