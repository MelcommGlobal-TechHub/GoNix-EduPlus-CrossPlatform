package com.melcommglobal.gonixeduplus

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.google.firebase.ai.DownloadStatus
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.type.PublicPreviewAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@OptIn(PublicPreviewAPI::class)
@CapacitorPlugin(name = "GeminiNano")
class GeminiNanoPlugin : Plugin() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun selectedModel(call: PluginCall): FirebaseHybridModel.ModelChoice {
        return when (call.getString("modelChoice")) {
            "NANO_4_FAST_PREVIEW" ->
                FirebaseHybridModel.ModelChoice.NANO_4_FAST_PREVIEW
            "STABLE" ->
                FirebaseHybridModel.ModelChoice.STABLE
            else ->
                FirebaseHybridModel.ModelChoice.NANO_4_FULL_PREVIEW
        }
    }

    @PluginMethod
    fun getStatus(call: PluginCall) {
        scope.launch {
            try {
                val model = FirebaseHybridModel.createLocalModel(selectedModel(call))
                val extension = model.onDeviceExtension
                    ?: throw IllegalStateException("On-device inference is unavailable")

                val status = extension.checkStatus().toString()
                    .substringAfterLast(".")
                    .uppercase()

                val result = JSObject()
                result.put("status", status)
                result.put("source", "ON_DEVICE")
                result.put("model", FirebaseHybridModel.CLOUD_MODEL_NAME)
                result.put("modelChoice", selectedModel(call).name)
                result.put("modelName", extension.getModelName())
                call.resolve(result)
            } catch (error: Exception) {
                call.reject("Firebase AI Logic status check failed: ${error.message}", error)
            }
        }
    }

    @PluginMethod
    fun downloadModel(call: PluginCall) {
        scope.launch {
            try {
                val model = FirebaseHybridModel.createLocalModel(selectedModel(call))
                val extension = model.onDeviceExtension
                    ?: throw IllegalStateException("On-device inference is unavailable")

                val current = extension.checkStatus().toString()
                    .substringAfterLast(".")
                    .uppercase()

                if (current == "AVAILABLE") {
                    val result = JSObject()
                    result.put("status", "AVAILABLE")
                    result.put("progress", 100)
                    call.resolve(result)
                    return@launch
                }

                if (current != "DOWNLOADABLE") {
                    throw IllegalStateException(
                        "Model cannot be downloaded. Current status: $current"
                    )
                }

                extension.download().collect { downloadStatus ->
                    val event = JSObject()

                    when (downloadStatus) {
                        is DownloadStatus.DownloadStarted ->
                            event.put("phase", "started")

                        is DownloadStatus.DownloadInProgress ->
                            event.put("phase", "downloading")

                        DownloadStatus.DownloadCompleted ->
                            event.put("phase", "completed")

                        is DownloadStatus.DownloadFailed -> {
                            event.put("phase", "failed")
                            event.put("message", downloadStatus.toString())
                        }
                    }

                    notifyListeners("downloadProgress", event)

                    if (downloadStatus is DownloadStatus.DownloadFailed) {
                        throw IllegalStateException(downloadStatus.toString())
                    }
                }

                val finalStatus = extension.checkStatus().toString()
                    .substringAfterLast(".")
                    .uppercase()

                if (finalStatus != "AVAILABLE") {
                    throw IllegalStateException(
                        "Download ended but model status is $finalStatus"
                    )
                }

                val result = JSObject()
                result.put("status", "AVAILABLE")
                result.put("progress", 100)
                call.resolve(result)
            } catch (error: Exception) {
                call.reject("Firebase AI Logic model download failed: ${error.message}", error)
            }
        }
    }

    @PluginMethod
    fun generate(call: PluginCall) {
        val prompt = call.getString("prompt")?.trim().orEmpty()

        if (prompt.isEmpty()) {
            call.reject("A prompt is required.")
            return
        }

        if (prompt.length > 12000) {
            call.reject("Prompt exceeds the 12,000 character limit.")
            return
        }

        scope.launch {
            try {
                val model = FirebaseHybridModel.createLocalModel(selectedModel(call))
                val extension = model.onDeviceExtension
                    ?: throw IllegalStateException("On-device inference is unavailable")

                val status = extension.checkStatus().toString()
                    .substringAfterLast(".")
                    .uppercase()

                if (status != "AVAILABLE") {
                    throw IllegalStateException(
                        "Gemini Nano is not ready. Current status: $status"
                    )
                }

                val response = model.generateContent(prompt)
                val text = response.text.orEmpty()

                if (response.inferenceSource != InferenceSource.ON_DEVICE) {
                    throw IllegalStateException(
                        "Local-only inference did not report ON_DEVICE."
                    )
                }

                if (text.isBlank()) {
                    throw IllegalStateException("Gemini Nano returned an empty response.")
                }

                val result = JSObject()
                result.put("text", text)
                result.put("source", "ON_DEVICE")
                result.put("modelChoice", selectedModel(call).name)
                call.resolve(result)
            } catch (error: Exception) {
                call.reject("Firebase AI Logic on-device inference failed: ${error.message}", error)
            }
        }
    }
}
