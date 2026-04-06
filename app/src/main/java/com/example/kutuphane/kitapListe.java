package com.example.kutuphane;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class kitapListe extends BaseActivity {

    private static final int SES_KODU = 1001;

    ListView listViewKitaplar;
    Button btnGeri;
    EditText txtArama;
    ImageView btnSesliArama;
    KitapAdapter adapter;
    ArrayList<Kitap> kitaplar;
    kutuphanedb db;
    String seciliKategori = "";
    
    Button btnCatTumu, btnCatRoman, btnCatBilim, btnCatTarih;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitap_liste);

        listViewKitaplar = findViewById(R.id.listViewKitaplar);
        btnGeri = findViewById(R.id.btnGeri);
        txtArama = findViewById(R.id.txtArama);
        btnSesliArama = findViewById(R.id.btnSesliArama);

        btnCatTumu = findViewById(R.id.btnCatTumu);
        btnCatRoman = findViewById(R.id.btnCatRoman);
        btnCatBilim = findViewById(R.id.btnCatBilim);
        btnCatTarih = findViewById(R.id.btnCatTarih);
        
        btnCatTumu.setOnClickListener(v -> kategoriSec(btnCatTumu, ""));
        btnCatRoman.setOnClickListener(v -> kategoriSec(btnCatRoman, "Roman"));
        btnCatBilim.setOnClickListener(v -> kategoriSec(btnCatBilim, "Bilim"));
        btnCatTarih.setOnClickListener(v -> kategoriSec(btnCatTarih, "Tarih"));

        db = new kutuphanedb(this);
        
        listeyiDoldur();

        // Arama işlemi (Filtreleme)
        txtArama.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Sesli Arama İşlemi
        btnSesliArama.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Aranacak Kitabı Söyleyin...");
            try {
                startActivityForResult(intent, SES_KODU);
            } catch (Exception e) {
                Toast.makeText(this, "Sesli arama cihazınızda desteklenmiyor.", Toast.LENGTH_SHORT).show();
            }
        });

        // Kitap Kaldır butonu tanımlaması ve yönlendirme
        Button btnKitapKaldir = findViewById(R.id.btnKitapKaldir);
        btnKitapKaldir.setOnClickListener(v -> {
            Intent i = new Intent(kitapListe.this, kitapKaldir.class);
            startActivity(i);
        });
        
        // Kitap Düzenle butonu tanımlaması ve yönlendirme
        Button btnKitapDuzenle = findViewById(R.id.btnKitapDuzenle);
        btnKitapDuzenle.setOnClickListener(v -> {
            Intent i = new Intent(kitapListe.this, kitapDuzenle.class);
            startActivity(i);
        });

        String rol = getIntent().getStringExtra("kullanici_rol");
        if (rol != null && rol.equals("Uye")) {
            btnKitapKaldir.setVisibility(android.view.View.GONE);
            btnKitapDuzenle.setVisibility(android.view.View.GONE);
        }
        
        btnGeri.setOnClickListener(v -> {
            Intent i = new Intent(kitapListe.this, goster.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });

        // Sol üst ok butonuna basıldığında Kitap İşlemleri menüsüne veya Ana Menüye dönme
        android.widget.ImageView btnGeriOk = findViewById(R.id.btnGeriOk);
        btnGeriOk.setOnClickListener(v -> {
            if ("Uye".equals(rol)) {
                Intent i = new Intent(kitapListe.this, goster.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(kitapListe.this, kitapIslemleri.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }
        });

        // Listeden bir kitaba tıklandığında Dialog açma (Ödünç Alma / İade Etme)
        listViewKitaplar.setOnItemClickListener((parent, view, position, id) -> {
            Kitap seciliKitap = adapter.getItem(position);
            String kitapAdi = seciliKitap.getAd();

            String oduncAlanKisi = "Giriş Yapan Kullanıcı"; 

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(kitapListe.this);
            builder.setTitle("Kitap İşlemi: " + kitapAdi);

            if ("odunc_verildi".equals(seciliKitap.getDurum())) {
                builder.setMessage("Bu kitap ödünç verilmiş. İade edildi olarak işaretlensin mi?");
                builder.setPositiveButton("İade Al", (dialog, which) -> {
                    db.kitapIadeEt(kitapAdi);
                    seciliKitap.setDurum("rafta");
                    seciliKitap.setIadeTarihi(null);
                    adapter.notifyDataSetChanged();
                    
                    try {
                        // Use a ContextThemeWrapper to avoid styling crashes on custom dialog views
                        android.content.Context dialogContext = new android.view.ContextThemeWrapper(kitapListe.this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
                        android.widget.RatingBar puanRatingBar = new android.widget.RatingBar(dialogContext);
                        puanRatingBar.setNumStars(5);
                        puanRatingBar.setStepSize(1.0f);
                        puanRatingBar.setRating(3);
                        
                        int dp16 = (int) (16 * getResources().getDisplayMetrics().density);
                        android.widget.FrameLayout container = new android.widget.FrameLayout(dialogContext);
                        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
                        params.gravity = android.view.Gravity.CENTER;
                        params.setMargins(dp16, dp16, dp16, dp16);
                        puanRatingBar.setLayoutParams(params);
                        container.addView(puanRatingBar);
    
                        android.app.AlertDialog.Builder puanBuilder = new android.app.AlertDialog.Builder(kitapListe.this);
                        puanBuilder.setTitle("Kitabı Puanla");
                        puanBuilder.setMessage("Bu kitabı nasıl buldun?");
                        puanBuilder.setView(container);
                        puanBuilder.setPositiveButton("Puanla", (puanDialog, puanWhich) -> {
                            float puan = puanRatingBar.getRating();
                            if (puan > 0) {
                                db.kitapPuanla(kitapAdi, puan);
                                
                                int mevcutYorum = seciliKitap.getYorumSayisi();
                                float mevcutPuanT = seciliKitap.getPuan() * mevcutYorum;
                                seciliKitap.setYorumSayisi(mevcutYorum + 1);
                                seciliKitap.setPuan((mevcutPuanT + puan) / (mevcutYorum + 1));
                                adapter.notifyDataSetChanged();

                                android.widget.Toast.makeText(kitapListe.this, "Kitap iade alındı ve " + (int)puan + " yıldız verildi!", android.widget.Toast.LENGTH_SHORT).show();
                            } else {
                                android.widget.Toast.makeText(kitapListe.this, "Kitap İade Alındı!", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                        puanBuilder.setNegativeButton("Puan Verme", (puanDialog, puanWhich) -> {
                            android.widget.Toast.makeText(kitapListe.this, "Kitap İade Alındı!", android.widget.Toast.LENGTH_SHORT).show();
                        });
                        puanBuilder.show();
                    } catch (Exception e) {
                        // Fallback completely if Rating bar fails to render
                        android.widget.Toast.makeText(kitapListe.this, "Kitap İade Alındı! (Puanlama geçildi)", android.widget.Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            } else {
                builder.setMessage("Bu kitabı ödünç almak/vermek istiyor musunuz? (15 Gün Süre Verilir)");
                builder.setPositiveButton("Ödünç Ver/Al", (dialog, which) -> {
                    db.kitapOduncVer(kitapAdi, oduncAlanKisi);
                    
                    seciliKitap.setDurum("odunc_verildi");
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                    seciliKitap.setIadeTarihi(sdf.format(new java.util.Date(System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000))));
                    adapter.notifyDataSetChanged();
                    
                    android.widget.Toast.makeText(kitapListe.this, "Kitap Ödünç Verildi!", android.widget.Toast.LENGTH_SHORT).show();
                });
            }

            builder.setNegativeButton("İptal", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SES_KODU && resultCode == RESULT_OK && data != null) {
            ArrayList<String> sonuc = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(sonuc != null && !sonuc.isEmpty()) {
                txtArama.setText(sonuc.get(0));
            }
        }
    }

    // Ekstra Kategori Seçim Metodu
    private void kategoriSec(Button secilenBtn, String kIsmi) {
        btnCatTumu.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        btnCatRoman.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        btnCatBilim.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        btnCatTarih.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        
        secilenBtn.setBackgroundColor(android.graphics.Color.parseColor("#2196F3"));
        btnCatTumu.setTextColor(android.graphics.Color.BLACK);
        btnCatRoman.setTextColor(android.graphics.Color.BLACK);
        btnCatBilim.setTextColor(android.graphics.Color.BLACK);
        btnCatTarih.setTextColor(android.graphics.Color.BLACK);
        secilenBtn.setTextColor(android.graphics.Color.WHITE);
        
        seciliKategori = kIsmi;
        listeyiDoldur();
    }

    // Listeyi her defasında en baştan okuyan metod
    private void listeyiDoldur() {
        try {
            ArrayList<Kitap> tumKitaplar = db.kitaplariGetirList();
            kitaplar = new ArrayList<>();
            
            for(Kitap k : tumKitaplar) {
                if(seciliKategori.isEmpty() || (k.getKategori() != null && k.getKategori().toLowerCase().contains(seciliKategori.toLowerCase()))) {
                    kitaplar.add(k);
                }
            }
            
            adapter = new KitapAdapter(this, kitaplar);
            listViewKitaplar.setAdapter(adapter);

            // Arama kutusunda metin varsa filtrelemeyi tekrar uygula
            if (txtArama != null && txtArama.getText() != null && txtArama.getText().length() > 0) {
                adapter.getFilter().filter(txtArama.getText());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Hata listeyiDoldur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Sayfaya kitap silinip veya eklenip tekrar gelindiğinde listenin otomatik yenilenmesi (Dinamik)
    @Override
    protected void onResume() {
        super.onResume();
        listeyiDoldur();
    }
}