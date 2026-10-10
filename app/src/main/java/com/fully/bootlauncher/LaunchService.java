package com.fully.bootlauncher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

public class LaunchService extends Service {
    private static final String TAG = "FullyBootLauncher";
    private static final String CHANNEL_ID = "fully_boot_launcher";
    private static final int NOTIFICATION_ID = 1001;

    // Retry after boot because many TV firmwares start their own launcher late.
    private static final long[] RETRIES_MS = {12000L, 25000L, 40000L};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean launchScheduled;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());

        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "FullyBootLauncher:BootLaunch");
                wakeLock.acquire(60000L);
            }
        } catch (Throwable t) {
            Log.w(TAG, "WakeLock unavailable", t);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (launchScheduled) return START_NOT_STICKY;
        launchScheduled = true;

        Log.i(TAG, "LaunchService started; scheduling retries");

        for (int i = 0; i < RETRIES_MS.length; i++) {
            final int attempt = i + 1;
            handler.postDelayed(() -> launchHelperActivity(attempt), RETRIES_MS[i]);
        }

        handler.postDelayed(() -> {
            releaseWakeLock();
            stopForeground(true);
            stopSelf();
        }, 50000L);

        return START_NOT_STICKY;
    }

    private void launchHelperActivity(int attempt) {
        try {
            Log.i(TAG, "Launch attempt " + attempt);
            Intent helper = new Intent(this, MainActivity.class);
            helper.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(helper);
            Log.i(TAG, "MainActivity start requested on attempt " + attempt);
        } catch (Throwable t) {
            Log.e(TAG, "Launch attempt failed " + attempt, t);
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

    private void releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        releaseWakeLock();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
