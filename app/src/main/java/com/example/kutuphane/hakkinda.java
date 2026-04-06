package com.example.kutuphane;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class hakkinda extends BaseActivity {

    Button btnGeri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hakkinda);

        btnGeri = findViewById(R.id.btnGeri);

        btnGeri.setOnClickListener(v -> finish());
    }
}