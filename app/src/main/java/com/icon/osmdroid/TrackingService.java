package com.icon.osmdroid;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class TrackingService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static Handler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.twotone_navigation_black_48)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        if (handler!=null) {
            try {
                handler.postDelayed(timerRunnable, 1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
    public Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            checkRouteHeadings();
            handler.postDelayed(this, 1000);
        }
    };

    public void play(){
        checkRouteHeadings();
    }

    private void checkRouteHeadings() {
        try {
            Intent intent = new Intent();
            intent.setAction("SendMessage");
            intent.putExtra("checkValue", true);
            intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            sendBroadcast(intent);
            Log.d("ExcLog:", "Send Broadcast: ");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ExcLog:", "Send Exception: " + e.getLocalizedMessage());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}
