package com.test.internalapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.test.internalapp.activity.ListenIncomingSMSActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    boolean finishSplashScreen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen splashScreen=     SplashScreen.installSplashScreen(this);
      /*  splashScreen.setKeepOnScreenCondition(new SplashScreen.KeepOnScreenCondition() {
            @Override
            public boolean shouldKeepOnScreen() {
                return finishSplashScreen;
            }
        });*/
        // Set up an OnPreDrawListener to the root view.
        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // Check if the initial data is ready.
                        if (!finishSplashScreen) {
                            // The content is ready; start drawing.
                            content.getViewTreeObserver().removeOnPreDrawListener(this);

                            startActivity(new Intent(SplashScreenActivity.this, ListenIncomingSMSActivity.class));
                            finish();

                            return true;
                        } else {
                            // The content is not ready; suspend.
                            return false;
                        }
                    }
                });
        // 5 seconds timeout to hide splash screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            finishSplashScreen = false;
        }, 3000);
    }
}