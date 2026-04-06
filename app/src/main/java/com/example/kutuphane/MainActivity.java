package com.example.kutuphane;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends BaseActivity {

    EditText txtAd, txtSifre;
    Button btnGirisYap;
    TextView txtYeniUye;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);

        txtAd = findViewById(R.id.txtGirisAd);
        txtSifre = findViewById(R.id.txtGirisSifre);
        btnGirisYap = findViewById(R.id.btnGirisYap);
        txtYeniUye = findViewById(R.id.txtYeniUye);

        btnGirisYap.setOnClickListener(v -> {
            String adStr = txtAd.getText().toString();
            String sifreStr = txtSifre.getText().toString();

            if (adStr.isEmpty() || sifreStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Lütfen ad ve şifre girin", Toast.LENGTH_SHORT).show();
            } else {
                kutuphanedb db = new kutuphanedb(MainActivity.this);
                if (db.uyeKontrol(adStr, sifreStr)) {
                    Toast.makeText(MainActivity.this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show();
                    
                    String rol = db.uyeRolGetir(adStr);

                    Intent i = new Intent(MainActivity.this, SplashActivity.class);
                    i.putExtra("kullanici_adi", adStr);
                    i.putExtra("kullanici_rol", rol);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Hatalı kullanıcı adı veya şifre", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtYeniUye.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, yeniUye.class);
            startActivity(i);
        });
    }
}