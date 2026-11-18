package com.melcommglobal.gonixeduplus;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.JSObject;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.google.mlkit.genai.prompt.java.GenerativeModelFutures;
import com.google.mlkit.genai.prompt.Generation;
import com.google.mlkit.genai.common.FeatureStatus;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

@CapacitorPlugin(name = "GeminiNano")
public class GeminiNanoPlugin extends Plugin {

    @PluginMethod
    public void getStatus(PluginCall call) {
        try {
            GenerativeModelFutures model =
                GenerativeModelFutures.from(Generation.INSTANCE.getClient());

            ListenableFuture<Integer> future = model.checkStatus();

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
                    call.reject(
                        "Gemini Nano status check failed: " +
                        error.getMessage(),
                        error
                    );
                }
            }, ContextCompat.getMainExecutor(getContext()));

        } catch (Exception error) {
            call.reject("Unable to initialize Gemini Nano", error);
        }
    }
}
