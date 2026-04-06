package com.example.kutuphane;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class kitapDuzenle extends BaseActivity {

    EditText txtEskiAd, txtYeniAd, txtYeniYazar;
    Button btnGuncelle, btnGeriDon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitap_duzenle);

        txtEskiAd = findViewById(R.id.txtEskiAd);
        txtYeniAd = findViewById(R.id.txtYeniAd);
        txtYeniYazar = findViewById(R.id.txtYeniYazar);
        btnGuncelle = findViewById(R.id.btnGuncelle);
        btnGeriDon = findViewById(R.id.btnGeriDon);

        // Geri Dön Butonu
        btnGeriDon.setOnClickListener(v -> {
            Intent i = new Intent(kitapDuzenle.this, kitapListe.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });

        // Kitabı Güncelle Butonu
        btnGuncelle.setOnClickListener(v -> {
            String eskiAdStr = txtEskiAd.getText().toString();
            String yeniAdStr = txtYeniAd.getText().toString();
            String yeniYazarStr = txtYeniYazar.getText().toString();

            if (eskiAdStr.isEmpty() || yeniAdStr.isEmpty() || yeniYazarStr.isEmpty()) {
                Toast.makeText(kitapDuzenle.this, "Lütfen tüm kutuları eksiksiz doldurun!", Toast.LENGTH_SHORT).show();
            } else {
                kutuphanedb db = new kutuphanedb(kitapDuzenle.this);
                db.kitapGuncelle(eskiAdStr, yeniAdStr, yeniYazarStr);
                
                Toast.makeText(kitapDuzenle.this, "Kitap başarıyla güncellendi!", Toast.LENGTH_SHORT).show();
                
                Intent i = new Intent(kitapDuzenle.this, kitapListe.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }
        });
    }
}
