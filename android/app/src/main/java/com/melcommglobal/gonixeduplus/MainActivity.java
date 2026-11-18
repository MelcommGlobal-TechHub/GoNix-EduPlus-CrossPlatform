package com.melcommglobal.gonixeduplus;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppCheckInitializer.initialize(this);
        registerPlugin(GeminiNanoPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
