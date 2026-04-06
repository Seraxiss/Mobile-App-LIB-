package com.example.kutuphane;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class istatistik extends BaseActivity {

    TextView txtToplamUye, txtToplamKitap, txtRafta, txtOduncte, txtYuzde;
    ProgressBar progressBarOdunc;
    Button btnGeri;
    kutuphanedb dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_istatistik);

        txtToplamUye = findViewById(R.id.txtToplamUye);
        txtToplamKitap = findViewById(R.id.txtToplamKitap);
        txtRafta = findViewById(R.id.txtRafta);
        txtOduncte = findViewById(R.id.txtOduncte);
        txtYuzde = findViewById(R.id.txtYuzde);
        progressBarOdunc = findViewById(R.id.progressBarOdunc);
        btnGeri = findViewById(R.id.btnGeriDon);
        dbHelper = new kutuphanedb(this);
        
        istatistikHesapla();

        // Geri Dön butonu
        btnGeri.setOnClickListener(v -> finish());
    }

    private void istatistikHesapla() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        int toplamKitap = 0;
        int oduncte = 0;

        // Toplam Üye Sayısı
        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM uyeler", null);
        if(c1.moveToFirst()) txtToplamUye.setText(String.valueOf(c1.getInt(0)));
        c1.close();

        // Toplam Kitap Sayısı
        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM kitaplar", null);
        if(c2.moveToFirst()) {
            toplamKitap = c2.getInt(0);
            txtToplamKitap.setText(String.valueOf(toplamKitap));
        }
        c2.close();

        // Raftaki Kitaplar (durum='rafta')
        Cursor c3 = db.rawQuery("SELECT COUNT(*) FROM kitaplar WHERE durum='rafta'", null);
        if(c3.moveToFirst()) txtRafta.setText(String.valueOf(c3.getInt(0)));
        c3.close();

        // Ödünç Verilenler (durum='odunc_verildi')
        Cursor c4 = db.rawQuery("SELECT COUNT(*) FROM kitaplar WHERE durum='odunc_verildi'", null);
        if(c4.moveToFirst()) {
            oduncte = c4.getInt(0);
            txtOduncte.setText(String.valueOf(oduncte));
        }
        c4.close();

        // ProgressBar Yüzde Hesabı
        if (toplamKitap > 0) {
            int yuzde = (oduncte * 100) / toplamKitap;
            progressBarOdunc.setProgress(yuzde);
            txtYuzde.setText("%" + yuzde);
        } else {
            progressBarOdunc.setProgress(0);
            txtYuzde.setText("%0");
        }
    }
}
