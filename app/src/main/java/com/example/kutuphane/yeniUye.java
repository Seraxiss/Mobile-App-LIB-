package com.example.kutuphane;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class yeniUye extends BaseActivity {

    EditText txtAd, txtSifre;
    Spinner spinnerRol;
    Button btnKaydet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yeni_uye);

        txtAd = findViewById(R.id.txtAd);
        txtSifre = findViewById(R.id.txtSifre);
        spinnerRol = findViewById(R.id.spinnerRol);
        btnKaydet = findViewById(R.id.btnKaydet);

        // Rol Seçenekleri (Admin / Üye)
        String[] roller = {"Normal Üye", "Admin (Kütüphaneci)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roller);
        spinnerRol.setAdapter(adapter);

        btnKaydet.setOnClickListener(v -> {
            String adStr = txtAd.getText().toString();
            String sifreStr = txtSifre.getText().toString();
            String secilenRol = spinnerRol.getSelectedItem().toString();

            // Veritabanına kaydedilecek asıl Rol kodu
            String rolKodu = secilenRol.contains("Admin") ? "Admin" : "Uye";

            if(adStr.isEmpty() || sifreStr.isEmpty()) {
                Toast.makeText(yeniUye.this, "Lütfen gerekli alanları doldurun", Toast.LENGTH_SHORT).show();
            } else {
                kutuphanedb db = new kutuphanedb(yeniUye.this);
                db.uyeEkle(adStr, sifreStr, rolKodu);
                Toast.makeText(yeniUye.this, "Üye kaydedildi: " + rolKodu, Toast.LENGTH_SHORT).show();
                
                Intent i = new Intent(yeniUye.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}