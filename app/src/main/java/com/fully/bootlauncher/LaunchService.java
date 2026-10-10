package com.fully.bootlauncher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public class LaunchService extends Service {
    private static final String CHANNEL_ID = "fully_boot_launcher";
    private static final int NOTIFICATION_ID = 1001;
    private static final long START_DELAY_MS = 15000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean launchScheduled;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (launchScheduled) return START_NOT_STICKY;
        launchScheduled = true;
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> {
            launchFully();
            stopForeground(true);
            stopSelf();
        }, START_DELAY_MS);
        return START_NOT_STICKY;
    }

    private void launchFully() {
        try {
            Intent launch = new Intent();
            launch.setComponent(new ComponentName("de.ozerov.fully", "de.ozerov.fully.MainActivity"));
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(launch);
        } catch (Exception firstError) {
            try {
                Intent fallback = getPackageManager().getLaunchIntentForPackage("de.ozerov.fully");
                if (fallback != null) {
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(fallback);
                }
            } catch (Exception ignored) {
                // Keep helper silent. Manual launch remains available from MainActivity.
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(getString(R.string.channel_description));
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        return builder
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setOngoing(true)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
