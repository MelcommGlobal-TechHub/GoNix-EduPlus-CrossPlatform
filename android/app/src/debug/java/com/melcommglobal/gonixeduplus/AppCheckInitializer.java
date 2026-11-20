package com.melcommglobal.gonixeduplus;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory;

public final class AppCheckInitializer {
    private AppCheckInitializer() {}

    public static void initialize(Context context) {
        FirebaseApp.initializeApp(context);

        String siteKey = BuildConfig.ANDROID_RECAPTCHA_ENTERPRISE_SITE_KEY;
        if (siteKey == null || siteKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "Missing ANDROID_RECAPTCHA_ENTERPRISE_SITE_KEY build configuration."
            );
        }

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            RecaptchaAppCheckProviderFactory.getInstance(siteKey)
        );
    }
}
