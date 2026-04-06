package com.example.kutuphane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import android.net.Uri;
import android.provider.MediaStore;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.moduleinstall.ModuleInstall;
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import org.json.JSONArray;
import org.json.JSONObject;

public class kitapekle extends BaseActivity {

    EditText txtKitapAdi, txtYazarAdi, txtSayfaSayisi;
    Spinner spinnerKategori;
    Button btnKitapKaydet, btnScan, btnSelectImage, btnFetchCover;
    ImageView imgPreview;
    Uri selectedImageUri;
    String internetCoverUrl = null; // İnternetten alınan kapak URL'si

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitapekle);

        txtKitapAdi = findViewById(R.id.kitapAdi);
        txtYazarAdi = findViewById(R.id.yazarAdi);
        txtSayfaSayisi = findViewById(R.id.sayfaSayisi);
        spinnerKategori = findViewById(R.id.spinnerKategori);
        btnKitapKaydet = findViewById(R.id.btnKitapKaydet);
        btnScan = findViewById(R.id.btnScan);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnFetchCover = findViewById(R.id.btnFetchCover);
        imgPreview = findViewById(R.id.imgBookCoverPreview);

        // Kategori Seçenekleri
        String[] kategoriler = {"Roman", "Bilim Kurgu", "Tarih", "Eğitim", "Çocuk", "Diğer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kategoriler);
        spinnerKategori.setAdapter(adapter);

        // Barcode Scanner Setup
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .enableAutoZoom()
                .build();
        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, options);

        btnScan.setOnClickListener(v -> {
            scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String rawValue = barcode.getRawValue();
                    Toast.makeText(this, "Barkod Okundu, bilgiler aranıyor...", Toast.LENGTH_SHORT).show();
                    
                    // Kitap adı, yazar adı, sayfa sayısı çek
                    fetchBookDetailsFromISBN(rawValue);
                    
                    // Otomatik olarak internetten kapak çek
                    fetchCoverFromInternet(rawValue, true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Tarama Başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        // Image Selection (Galeriden)
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1001);
        });

        // İNTERNETTEN KAPAK ÇEK butonu
        btnFetchCover.setOnClickListener(v -> {
            String kitapAdi = txtKitapAdi.getText().toString().trim();
            if (kitapAdi.isEmpty()) {
                Toast.makeText(this, "Önce kitap adını girin!", Toast.LENGTH_SHORT).show();
            } else {
                fetchCoverFromInternet(kitapAdi, false);
            }
        });

        ImageView btnGeriOk = findViewById(R.id.btnGeriOk);
        btnGeriOk.setOnClickListener(v -> finish());

        btnKitapKaydet.setOnClickListener(v -> {
            String kitapAdiStr = txtKitapAdi.getText().toString();
            String yazarAdiStr = txtYazarAdi.getText().toString();
            String sayfaSayisiStr = txtSayfaSayisi.getText().toString();
            String kategoriStr = spinnerKategori.getSelectedItem().toString();

            // Önce internet URL'si, yoksa galeri seçimi
            String resimYolu = "";
            if (internetCoverUrl != null && !internetCoverUrl.isEmpty()) {
                resimYolu = internetCoverUrl;
            } else if (selectedImageUri != null) {
                resimYolu = selectedImageUri.toString();
            }

            if (kitapAdiStr.isEmpty() || yazarAdiStr.isEmpty() || sayfaSayisiStr.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm bilgileri doldurun", Toast.LENGTH_SHORT).show();
            } else {
                kutuphanedb db = new kutuphanedb(this);
                db.kitapEkle(kitapAdiStr, yazarAdiStr, sayfaSayisiStr, kategoriStr, resimYolu);
                Toast.makeText(this, "Kitap listeye eklendi", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Open Library Covers API ile kitap kapağı çeker.
     * isIsbn=true ise ISBN kodu olarak arar, false ise kitap başlığına göre arar.
     */
    private void fetchCoverFromInternet(String query, boolean isIsbn) {
        Toast.makeText(this, "Kapak aranıyor...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                String coverUrl;
                if (isIsbn) {
                    // ISBN'e göre direkt kapak URL'si oluştur
                    coverUrl = "https://covers.openlibrary.org/b/isbn/" + query + "-L.jpg";
                } else {
                    // Başlığa göre Open Library Search API'yi kullan
                    String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
                    String searchUrl = "https://openlibrary.org/search.json?title=" + encodedQuery + "&limit=1";
                    
                    java.net.URL url = new java.net.URL(searchUrl);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    
                    java.io.InputStream is = conn.getInputStream();
                    java.util.Scanner sc = new java.util.Scanner(is).useDelimiter("\\A");
                    String json = sc.hasNext() ? sc.next() : "";
                    conn.disconnect();
                    
                    // JSON'dan cover_i değerini basit regex ile çek
                    coverUrl = null;
                    java.util.regex.Matcher m = java.util.regex.Pattern
                        .compile("\"cover_i\":(\\d+)")
                        .matcher(json);
                    if (m.find()) {
                        String coverId = m.group(1);
                        coverUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
                    } else {
                        // cover_i yoksa ISBN ile dene
                        java.util.regex.Matcher isbnMatcher = java.util.regex.Pattern
                            .compile("\"isbn\":\\[\"([^\"]+)\"")
                            .matcher(json);
                        if (isbnMatcher.find()) {
                            String isbn = isbnMatcher.group(1);
                            coverUrl = "https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg";
                        }
                    }
                }

                final String finalUrl = coverUrl;
                runOnUiThread(() -> {
                    if (finalUrl != null) {
                        internetCoverUrl = finalUrl;
                        selectedImageUri = null; // Galeri seçimini temizle
                        Glide.with(this)
                            .load(finalUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable   .ic_menu_gallery)
                            .into(imgPreview);
                        Toast.makeText(this, "Kapak bulundu!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Kapak bulunamadı. Galeriden seçin.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "İnternet bağlantısı hatası: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    /**
     * Google Books API üzerinden ISBN ile kitap detaylarını çeker.
     */
    private void fetchBookDetailsFromISBN(String isbn) {
        new Thread(() -> {
            try {
                String searchUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
                java.net.URL url = new java.net.URL(searchUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                java.io.InputStream is = conn.getInputStream();
                java.util.Scanner sc = new java.util.Scanner(is).useDelimiter("\\A");
                String jsonStr = sc.hasNext() ? sc.next() : "";
                conn.disconnect();

                JSONObject jsonObject = new JSONObject(jsonStr);
                if (jsonObject.has("items")) {
                    JSONArray items = jsonObject.getJSONArray("items");
                    if (items.length() > 0) {
                        JSONObject volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo");
                        String title = volumeInfo.optString("title", "");
                        
                        String author = "";
                        if (volumeInfo.has("authors")) {
                            JSONArray authors = volumeInfo.getJSONArray("authors");
                            if (authors.length() > 0) {
                                author = authors.getString(0);
                            }
                        }

                        String pages = volumeInfo.optString("pageCount", "");

                        final String finalTitle = title;
                        final String finalAuthor = author;
                        final String finalPages = pages;

                        runOnUiThread(() -> {
                            if (!finalTitle.isEmpty()) txtKitapAdi.setText(finalTitle);
                            if (!finalAuthor.isEmpty()) txtYazarAdi.setText(finalAuthor);
                            if (!finalPages.isEmpty()) txtSayfaSayisi.setText(finalPages);
                        });
                    }
                } else {
                    // Google'da yoksa Open Library API ile ara
                    fetchFromOpenLibrary(isbn);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Hata durumunda da Open Library API ile şansımızı deneyelim
                fetchFromOpenLibrary(isbn);
            }
        }).start();
    }

    /**
     * Yedek (Fallback) olarak Open Library API üzerinden kitap detaylarını çeker.
     */
    private void fetchFromOpenLibrary(String isbn) {
        new Thread(() -> {
            try {
                String openLibUrl = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";
                java.net.URL url = new java.net.URL(openLibUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                java.io.InputStream is = conn.getInputStream();
                java.util.Scanner sc = new java.util.Scanner(is).useDelimiter("\\A");
                String jsonStr = sc.hasNext() ? sc.next() : "";
                conn.disconnect();

                JSONObject jsonObject = new JSONObject(jsonStr);
                String bookKey = "ISBN:" + isbn;
                if (jsonObject.has(bookKey)) {
                    JSONObject bookData = jsonObject.getJSONObject(bookKey);
                    String title = bookData.optString("title", "");
                    
                    String author = "";
                    if (bookData.has("authors")) {
                        JSONArray authors = bookData.getJSONArray("authors");
                        if (authors.length() > 0) {
                            author = authors.getJSONObject(0).optString("name", "");
                        }
                    }

                    String pages = bookData.optString("number_of_pages", "");

                    final String finalTitle = title;
                    final String finalAuthor = author;
                    final String finalPages = pages;

                    runOnUiThread(() -> {
                        if (!finalTitle.isEmpty()) txtKitapAdi.setText(finalTitle);
                        if (!finalAuthor.isEmpty()) txtYazarAdi.setText(finalAuthor);
                        if (!finalPages.isEmpty()) txtSayfaSayisi.setText(finalPages);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Kitap bilgisi veritabanlarında bulunamadı.", Toast.LENGTH_SHORT).show();
                        if (txtKitapAdi.getText().toString().isEmpty()) {
                            txtKitapAdi.setText("Kitap " + isbn);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Veri alınırken hata oluştu.", Toast.LENGTH_SHORT).show();
                    if (txtKitapAdi.getText().toString().isEmpty()) {
                        txtKitapAdi.setText("Kitap " + isbn);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            internetCoverUrl = null; // İnternet URL'sini temizle
            Glide.with(this).load(selectedImageUri).into(imgPreview);
        }
    }
}