package com.melcommglobal.gonixeduplus;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.JSObject;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.google.mlkit.genai.prompt.java.GenerativeModelFutures;
import com.google.mlkit.genai.prompt.Generation;
import com.google.mlkit.genai.common.FeatureStatus;
import com.google.mlkit.genai.common.DownloadCallback;
import com.google.mlkit.genai.common.GenAiException;
import com.google.mlkit.genai.prompt.GenerateContentResponse;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

@CapacitorPlugin(name = "GeminiNano")
public class GeminiNanoPlugin extends Plugin {

    private GenerativeModelFutures getModel() {
        return GenerativeModelFutures.from(
            Generation.INSTANCE.getClient()
        );
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        try {
            ListenableFuture<Integer> future = getModel().checkStatus();

            Futures.addCallback(future, new FutureCallback<Integer>() {
                @Override
                public void onSuccess(Integer status) {
                    JSObject result = new JSObject();
                    String availability;

                    if (status == FeatureStatus.AVAILABLE) {
                        availability = "AVAILABLE";
                    } else if (status == FeatureStatus.DOWNLOADABLE) {
                        availability = "DOWNLOADABLE";
                    } else if (status == FeatureStatus.DOWNLOADING) {
                        availability = "DOWNLOADING";
                    } else {
                        availability = "UNAVAILABLE";
                    }

                    result.put("status", availability);
                    result.put("source", "ON_DEVICE");
                    call.resolve(result);
                }

                @Override
                public void onFailure(@NonNull Throwable error) {
                    reject(call, "Gemini Nano status check failed", error);
                }
            }, ContextCompat.getMainExecutor(getContext()));

        } catch (Exception error) {
            reject(call, "Unable to initialize Gemini Nano", error);
        }
    }

    @PluginMethod
    public void downloadModel(PluginCall call) {
        try {
            GenerativeModelFutures model = getModel();

            ListenableFuture<Integer> statusFuture = model.checkStatus();

            Futures.addCallback(statusFuture, new FutureCallback<Integer>() {
                @Override
                public void onSuccess(Integer status) {
                    if (status == FeatureStatus.AVAILABLE) {
                        JSObject result = new JSObject();
                        result.put("status", "AVAILABLE");
                        result.put("progress", 100);
                        call.resolve(result);
                        return;
                    }

                    if (status != FeatureStatus.DOWNLOADABLE) {
                        call.reject(
                            status == FeatureStatus.DOWNLOADING
                                ? "Gemini Nano is already downloading."
                                : "Gemini Nano is unavailable on this device."
                        );
                        return;
                    }

                    try {
                        ListenableFuture<Void> downloadFuture =
                            model.download(new DownloadCallback() {
                                @Override
                                public void onDownloadStarted(long totalBytes) {
                                    JSObject event = new JSObject();
                                    event.put("phase", "started");
                                    event.put("totalBytes", totalBytes);
                                    notifyListeners("downloadProgress", event);
                                }

                                @Override
                                public void onDownloadProgress(long downloadedBytes) {
                                    JSObject event = new JSObject();
                                    event.put("phase", "downloading");
                                    event.put("downloadedBytes", downloadedBytes);
                                    notifyListeners("downloadProgress", event);
                                }

                                @Override
                                public void onDownloadCompleted() {
                                    JSObject event = new JSObject();
                                    event.put("phase", "completed");
                                    notifyListeners("downloadProgress", event);
                                }

                                @Override
                                public void onDownloadFailed(
                                    @NonNull GenAiException error
                                ) {
                                    JSObject event = new JSObject();
                                    event.put("phase", "failed");
                                    event.put("message", error.getMessage());
                                    notifyListeners("downloadProgress", event);
                                }
                            });

                        Futures.addCallback(
                            downloadFuture,
                            new FutureCallback<Void>() {
                                @Override
                                public void onSuccess(Void ignored) {
                                    JSObject result = new JSObject();
                                    result.put("status", "AVAILABLE");
                                    result.put("progress", 100);
                                    call.resolve(result);
                                }

                                @Override
                                public void onFailure(@NonNull Throwable error) {
                                    reject(
                                        call,
                                        "Gemini Nano download failed",
                                        error
                                    );
                                }
                            },
                            ContextCompat.getMainExecutor(getContext())
                        );

                    } catch (Exception error) {
                        reject(call, "Unable to start model download", error);
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable error) {
                    reject(call, "Unable to check model before download", error);
                }
            }, ContextCompat.getMainExecutor(getContext()));

        } catch (Exception error) {
            reject(call, "Unable to initialize model download", error);
        }
    }

    @PluginMethod
    public void generate(PluginCall call) {
        String prompt = call.getString("prompt", "");

        if (prompt == null || prompt.trim().isEmpty()) {
            call.reject("A prompt is required.");
            return;
        }

        if (prompt.length() > 12000) {
            call.reject("Prompt exceeds the 12,000 character limit.");
            return;
        }

        try {
            GenerativeModelFutures model = getModel();

            ListenableFuture<Integer> statusFuture = model.checkStatus();

            Futures.addCallback(statusFuture, new FutureCallback<Integer>() {
                @Override
                public void onSuccess(Integer status) {
                    if (status != FeatureStatus.AVAILABLE) {
                        call.reject(
                            "Gemini Nano is not ready. Current status: " +
                            statusName(status)
                        );
                        return;
                    }

                    try {
                        ListenableFuture<GenerateContentResponse> future =
                            model.generateContent(prompt.trim());

                        Futures.addCallback(
                            future,
                            new FutureCallback<GenerateContentResponse>() {
                                @Override
                                public void onSuccess(
                                    GenerateContentResponse response
                                ) {
                                    String text = response.getCandidates().isEmpty() ? "" : response.getCandidates().get(0).getText();

                                    if (text == null || text.trim().isEmpty()) {
                                        call.reject(
                                            "Gemini Nano returned an empty response."
                                        );
                                        return;
                                    }

                                    JSObject result = new JSObject();
                                    result.put("text", text);
                                    result.put("source", "ON_DEVICE");
                                    call.resolve(result);
                                }

                                @Override
                                public void onFailure(@NonNull Throwable error) {
                                    reject(
                                        call,
                                        "Gemini Nano inference failed",
                                        error
                                    );
                                }
                            },
                            ContextCompat.getMainExecutor(getContext())
                        );

                    } catch (Exception error) {
                        reject(call, "Unable to start Gemini Nano inference", error);
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable error) {
                    reject(call, "Unable to verify Gemini Nano availability", error);
                }
            }, ContextCompat.getMainExecutor(getContext()));

        } catch (Exception error) {
            reject(call, "Unable to initialize Gemini Nano inference", error);
        }
    }

    private String statusName(Integer status) {
        if (status == FeatureStatus.AVAILABLE) return "AVAILABLE";
        if (status == FeatureStatus.DOWNLOADABLE) return "DOWNLOADABLE";
        if (status == FeatureStatus.DOWNLOADING) return "DOWNLOADING";
        return "UNAVAILABLE";
    }

    private void reject(
        PluginCall call,
        String message,
        Throwable error
    ) {
        Exception cause = error instanceof Exception
            ? (Exception) error
            : new Exception(error);

        call.reject(
            message + ": " + error.getMessage(),
            cause
        );
    }
}
