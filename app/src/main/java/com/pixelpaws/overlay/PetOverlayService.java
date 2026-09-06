package com.pixelpaws.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;

public class PetOverlayService extends Service {
    private static final String CHANNEL="pixel_pet_active";
    private WindowManager windowManager;
    private PixelPetView petView;
    private WindowManager.LayoutParams params;
    private PetState state;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable decay = new Runnable() {
        @Override public void run() { if (state != null) state.decay(); if (petView != null) petView.invalidate(); handler.postDelayed(this, 60000); }
    };

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        Notification notification = new Notification.Builder(this, CHANNEL)
                .setContentTitle("Pixel Paws sedang menemani")
                .setContentText("Ketuk tombol Stop di aplikasi untuk menyembunyikan")
                .setSmallIcon(com.pixelpaws.overlay.R.drawable.app_icon).setOngoing(true).build();
        startForeground(7, notification);
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return; }
        state = new PetState(this);
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        int width=dp(170), height=dp(245);
        params = new WindowManager.LayoutParams(width,height,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START; params.x=dp(12); params.y=getResources().getDisplayMetrics().heightPixels-height-dp(60);
        petView = new PixelPetView(this,state,(dx,dy)->{
            params.x += Math.round(dx); params.y += Math.round(dy);
            windowManager.updateViewLayout(petView,params);
        });
        windowManager.addView(petView,params);
        handler.postDelayed(decay,60000);
    }

    private void createChannel() {
        NotificationChannel channel=new NotificationChannel(CHANNEL,"Pixel pet aktif",NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Diperlukan Android agar pet tetap tampil di layar");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private int dp(int v) { return Math.round(v*getResources().getDisplayMetrics().density); }
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (petView!=null && windowManager!=null) windowManager.removeView(petView);
        petView=null; super.onDestroy();
    }
}
