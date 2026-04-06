package com.example.kutuphane;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class kitapIslemleri extends BaseActivity {

    Button btnEkle, btnListele, btnGeri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitap_islemleri);

        btnEkle = findViewById(R.id.btnYeniKitapEkle);
        btnListele = findViewById(R.id.btnKitaplariYonet);
        btnGeri = findViewById(R.id.btnGeriDon);
        
        String rol = getIntent().getStringExtra("kullanici_rol");
        if (rol != null && rol.equals("Uye")) {
            btnEkle.setVisibility(android.view.View.GONE);
            btnListele.setText("Kitapları Ara ve Ödünç Al");
        }

        // Kitap Ekle ekranına git
        btnEkle.setOnClickListener(v -> {
            Intent i = new Intent(kitapIslemleri.this, kitapekle.class);
            startActivity(i);
        });

        // Kitapları Listele ekranına git
        btnListele.setOnClickListener(v -> {
            Intent i = new Intent(kitapIslemleri.this, kitapListe.class);
            i.putExtra("kullanici_rol", rol);
            startActivity(i);
        });

        // Geri Dön (Ana Menüye)
        btnGeri.setOnClickListener(v -> finish());
    }
}
