package com.fully.bootlauncher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = "FullyBootLauncher";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView view = new TextView(this);
        view.setText("Fully Boot Launcher\n\nLaunching Fully Kiosk Browser…");
        view.setTextSize(24f);
        view.setPadding(48, 48, 48, 48);
        setContentView(view);

        Log.i(TAG, "MainActivity started");
        launchFully();
    }

    private void launchFully() {
        try {
            Intent launch = new Intent();
            launch.setComponent(new ComponentName("de.ozerov.fully", "de.ozerov.fully.MainActivity"));
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.i(TAG, "Starting Fully MainActivity");
            startActivity(launch);
            finish();
        } catch (Exception firstError) {
            Intent fallback = getPackageManager().getLaunchIntentForPackage("de.ozerov.fully");
            if (fallback != null) {
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fallback);
                finish();
            } else {
                TextView view = new TextView(this);
                view.setText("Fully Kiosk Browser is not installed.\nExpected package: de.ozerov.fully");
                view.setTextSize(22f);
                view.setPadding(48, 48, 48, 48);
                setContentView(view);
            }
        }
    }
}
