package com.example.kutuphane;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logoSplash);

        // Simple pulse animation for the logo
        ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(
                logo,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.2f, 1.0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.2f, 1.0f)
        );
        scaleAnim.setDuration(1500);
        scaleAnim.start();

        // Pass along the intents from Login
        String username = getIntent().getStringExtra("kullanici_adi");
        String role = getIntent().getStringExtra("kullanici_rol");

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, goster.class);
            if (username != null) intent.putExtra("kullanici_adi", username);
            if (role != null) intent.putExtra("kullanici_rol", role);
            startActivity(intent);
            finish();
        }, 2000); // 2 seconds delay
    }
}
