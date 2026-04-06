package com.example.kutuphane;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class kitapKaldir extends BaseActivity {

    EditText txtSilinecekKitap;
    Button btnSil, btnDon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitap_kaldir);

        txtSilinecekKitap = findViewById(R.id.txtSilinecekKitap);
        btnSil = findViewById(R.id.btnSil);
        btnDon = findViewById(R.id.btnKaldirmadanDon);

        btnDon.setOnClickListener(v -> {
            Intent i = new Intent(kitapKaldir.this, kitapListe.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });

        btnSil.setOnClickListener(v -> {
            String kitapAdi = txtSilinecekKitap.getText().toString();

            if (kitapAdi.isEmpty()) {
                Toast.makeText(kitapKaldir.this, "Lütfen silinecek kitap adını girin", Toast.LENGTH_SHORT).show();
            } else {
                kutuphanedb db = new kutuphanedb(kitapKaldir.this);
                db.kitapSil(kitapAdi);
                Toast.makeText(kitapKaldir.this, "Kitap başarıyla kütüphaneden kaldırıldı!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(kitapKaldir.this, kitapListe.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }
        });
    }
}
