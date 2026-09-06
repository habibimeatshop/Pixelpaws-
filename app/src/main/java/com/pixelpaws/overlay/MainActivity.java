package com.pixelpaws.overlay;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(28),dp(48),dp(28),dp(28)); root.setBackgroundColor(Color.rgb(255,248,239));
        TextView title=text("PIXEL PAWS",30,true); title.setTextColor(Color.rgb(70,58,68)); root.addView(title);
        TextView cat=text("ฅ^•ﻌ•^ฅ",42,true); cat.setTextColor(Color.rgb(133,89,62)); root.addView(cat,margin(0,8,0,20));
        root.addView(text("Kucing pixel cokelat yang hidup di bawah layar HP-mu. Ia bisa berjalan, berlari, bermain bola, lapar, tidur, dan meminta dimanja secara otomatis.",16,false),margin(0,0,0,24));
        status=text("",14,true); status.setTextColor(Color.rgb(91,77,86)); root.addView(status,margin(0,0,0,14));
        Button start=button("Tampilkan kucing"); start.setOnClickListener(v->startPet()); root.addView(start,full(0,0,0,10));
        Button stop=button("Sembunyikan kucing"); stop.setOnClickListener(v->{ stopService(new Intent(this,PetOverlayService.class)); updateStatus(); }); root.addView(stop,full(0,0,0,18));
        TextView help=text("Cara bermain\n\n• Kucing bergerak dan bermain sendiri\n• Ketuk kucing: elus dan buka menu\n• Seret: pindahkan ke kiri atau kanan\n• 🍗: beri makan (2 koin)\n• ●: ajak bermain bola dan dapat koin\n• ♡: manjakan kucing\n• Tekan lama: tidur\n\nCatatan: notifikasi kecil diperlukan Android agar pet tetap hidup di atas aplikasi lain.",14,false);
        help.setBackgroundColor(Color.rgb(255,238,224)); help.setPadding(dp(18),dp(18),dp(18),dp(18)); root.addView(help,full(0,0,0,0));
        setContentView(root); requestNotifications(); updateStatus();
    }

    private void startPet() {
        if (!Settings.canDrawOverlays(this)) {
            Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())); startActivity(i);
        } else { startForegroundService(new Intent(this,PetOverlayService.class)); status.setText("Kucing sedang aktif ✨"); }
    }

    private void requestNotifications() {
        if (Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},42);
    }

    @Override protected void onResume() { super.onResume(); updateStatus(); }
    private void updateStatus() { status.setText(Settings.canDrawOverlays(this)?"Izin overlay siap":"Izin “tampil di atas aplikasi lain” belum aktif"); }
    private TextView text(String value,int size,boolean bold) { TextView v=new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(Color.rgb(70,58,68)); if(bold)v.setTypeface(null,1); return v; }
    private Button button(String value) { Button b=new Button(this); b.setText(value); b.setTextSize(16); b.setAllCaps(false); return b; }
    private LinearLayout.LayoutParams margin(int l,int t,int r,int b) { LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT); p.setMargins(dp(l),dp(t),dp(r),dp(b)); return p; }
    private LinearLayout.LayoutParams full(int l,int t,int r,int b) { LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT); p.setMargins(dp(l),dp(t),dp(r),dp(b)); return p; }
    private int dp(int v) { return Math.round(v*getResources().getDisplayMetrics().density); }
}
