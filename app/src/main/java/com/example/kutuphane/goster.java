package com.example.kutuphane;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;

public class goster extends BaseActivity {

    Button btnKitapIslemleri, btnHakkinda, btnCikis;
    TextView txtKarsilama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goster);

        btnKitapIslemleri = findViewById(R.id.btnKitapIslemleri);
        Button btnIstatistikler = findViewById(R.id.btnIstatistikler);
        btnHakkinda = findViewById(R.id.btnHakkinda);
        btnCikis = findViewById(R.id.btnCikis);
        txtKarsilama = findViewById(R.id.txtKarsilama);

        String kullaniciAdi = getIntent().getStringExtra("kullanici_adi");
        String kullaniciRol = getIntent().getStringExtra("kullanici_rol");

        if(kullaniciAdi != null) {
            String rolEki = (kullaniciRol != null && kullaniciRol.equals("Admin")) ? " (Yetkili)" : "";
            txtKarsilama.setText("Merhaba " + kullaniciAdi + rolEki + ",\nHoş Geldin!");
        }

        if (kullaniciRol != null && kullaniciRol.equals("Uye")) {
            btnKitapIslemleri.setText("Kitapları Ara ve Ödünç Al");
            btnIstatistikler.setVisibility(View.GONE); // Sadece Admin görebilir
        }

        // İstatistikler ekranı
        btnIstatistikler.setOnClickListener(v -> {
            Intent i = new Intent(goster.this, istatistik.class);
            startActivity(i);
        });
        

        // Kitap İşlemleri ekranı
        btnKitapIslemleri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("Uye".equals(kullaniciRol)) {
                    Intent i = new Intent(goster.this, kitapListe.class);
                    i.putExtra("kullanici_rol", kullaniciRol);
                    startActivity(i);
                } else {
                    Intent i = new Intent(goster.this, kitapIslemleri.class);
                    i.putExtra("kullanici_rol", kullaniciRol);
                    startActivity(i);
                }
            }
        });
        

        // Hakkında ekranı
        btnHakkinda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(goster.this, hakkinda.class);
                startActivity(i);
            }
        });

        // Çıkış yap butonu
        btnCikis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(goster.this, MainActivity.class);
                // Geri tuşuna basınca tekrar ana menüye girmeyi engellemek için activity geçmişini temizliyoruz
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });
    }
}
