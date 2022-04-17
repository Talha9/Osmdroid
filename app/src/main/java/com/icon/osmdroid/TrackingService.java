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
    private Notification notification;
    PendingIntent pStopSelf;
    public String ACTION_STOP_SERVICE = "STOP";
    private Intent intent;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
                if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                    try {
                        handler.removeCallbacks(timerRunnable);
                        stopSelf();
                        Log.d("ACTION_STOP_SERVICE", "::: Stop Service");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        Intent stopSelf = new Intent(this, MainActivity.class);
        stopSelf.setAction(ACTION_STOP_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
            pStopSelf =   PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_IMMUTABLE);
        }else{
            pStopSelf =    PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Navigation")
                .setSmallIcon(R.drawable.twotone_navigation_black_48)
                .setContentIntent(pStopSelf)
                .build();
        startForeground(1, notification);

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
           intent = new Intent();
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
