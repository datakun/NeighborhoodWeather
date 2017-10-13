package com.kimjunu.neighborhoodweather;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    final int REQUEST_PERMISSION = 1000;

    String[] permissionList = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (checkLocationPermission()) {
            // 퍼미션 허용되어있으면
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 메인 액티비티 실행
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        } else {
            // 퍼미선 요청
            ActivityCompat.requestPermissions(this, permissionList, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            if (checkLocationPermission()) {
                // 퍼미션 허용되어있으면 메인 액티비티 실행
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }
        }
    }

    boolean checkLocationPermission() {
        boolean isPermissionGranted = true;

        // 만약 하나라도 퍼미션 허용이 안되면 false
        for (String permission : permissionList)
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                isPermissionGranted = false;

        return isPermissionGranted;
    }
}
