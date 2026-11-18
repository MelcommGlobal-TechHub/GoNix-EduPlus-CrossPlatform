package com.melcommglobal.gonixeduplus;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

public final class AppCheckInitializer {
    private AppCheckInitializer() {}

    public static void initialize(Context context) {
        FirebaseApp.initializeApp(context);
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        );
    }
}
