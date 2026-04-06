package com.example.kutuphane;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class kutuphanedb extends SQLiteOpenHelper {

    public static final String DB_ADI = "kutuphane.db";
    public static final int DB_VERSIYON = 8; // Versiyon artırıldı


    public kutuphanedb(Context context) {
        super(context, DB_ADI, null, DB_VERSIYON);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE kitaplar (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "kitapadi TEXT," +
                "yazar TEXT," +
                "sayfa TEXT," +
                "kategori TEXT," +
                "durum TEXT DEFAULT 'rafta'," +   // rafta veya odunc_verildi
                "odunc_alan TEXT," +
                "iade_tarihi TEXT," +
                "puan_toplam REAL DEFAULT 0," +
                "yorum_sayisi INTEGER DEFAULT 0," +
                "resim_yolu TEXT)"); // Yeni sütun
                
        db.execSQL("CREATE TABLE uyeler (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "ad TEXT," +
                "sifre TEXT," +
                "rol TEXT DEFAULT 'Uye')");      // Admin veya Uye

        // Varsayılan verileri yüklüyoruz
        varsayilanVerileriYukle(db);
    }

    private void varsayilanVerileriYukle(SQLiteDatabase db) {
        // Varsayılan Admin Kullanıcısı
        db.execSQL("INSERT INTO uyeler (ad, sifre, rol) VALUES ('admin', '1234', 'Admin')");
        db.execSQL("INSERT INTO uyeler (ad, sifre, rol) VALUES ('uye', '1234', 'Uye')");

        // Varsayılan Kitaplar
        db.execSQL("INSERT INTO kitaplar (kitapadi, yazar, sayfa, kategori) VALUES ('Sefiller', 'Victor Hugo', '1200', 'Roman')");
        db.execSQL("INSERT INTO kitaplar (kitapadi, yazar, sayfa, kategori) VALUES ('Nutuk', 'Mustafa Kemal Atatürk', '800', 'Tarih')");
        db.execSQL("INSERT INTO kitaplar (kitapadi, yazar, sayfa, kategori) VALUES ('Kozmos', 'Carl Sagan', '400', 'Bilim')");
        db.execSQL("INSERT INTO kitaplar (kitapadi, yazar, sayfa, kategori) VALUES ('Suç ve Ceza', 'Dostoyevski', '600', 'Roman')");
        db.execSQL("INSERT INTO kitaplar (kitapadi, yazar, sayfa, kategori) VALUES ('Hayvan Çiftliği', 'George Orwell', '150', 'Roman')");
        db.execSQL("INSERT INTO kitaplar (kitapadi, yazar, sayfa, kategori) VALUES ('Sapiens', 'Yuval Noah Harari', '500', 'Bilim')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS kitaplar");
        db.execSQL("DROP TABLE IF EXISTS uyeler");
        onCreate(db);
    }

    public void kitapEkle(String kitapAdi, String yazar, String sayfa, String kategori, String resimYolu) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("kitapadi", kitapAdi);
        cv.put("yazar", yazar);
        cv.put("sayfa", sayfa);
        cv.put("kategori", kategori);
        cv.put("resim_yolu", resimYolu);

        db.insert("kitaplar", null, cv);
    }

    public void kitapSil(String kitapAdi) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("kitaplar", "kitapadi=?", new String[]{kitapAdi});
    }

    public void kitapGuncelle(String eskiAd, String yeniAd, String yeniYazar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("kitapadi", yeniAd);
        cv.put("yazar", yeniYazar);
        
        db.update("kitaplar", cv, "kitapadi=?", new String[]{eskiAd});
    }

    public void uyeEkle(String ad, String sifre, String rol) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("ad", ad);
        cv.put("sifre", sifre);
        cv.put("rol", rol);

        db.insert("uyeler", null, cv);
    }

    public boolean uyeKontrol(String ad, String sifre) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM uyeler WHERE ad=? AND sifre=?", new String[]{ad, sifre});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    public String uyeRolGetir(String ad) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT rol FROM uyeler WHERE ad=?", new String[]{ad});
        String rol = "Uye"; // Varsayılan
        if(cursor.moveToFirst()){
            rol = cursor.getString(0);
        }
        cursor.close();
        return rol;
    }

    public void kitapOduncVer(String kitapAdi, String oduncAlanUye) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            
            // Eger kullanicinin cihazinda bu kolonlar yoksa aninda ekleyelim (Uygulamaniz silinmedigi icin eski DB kalmis olabilir)
            try { db.execSQL("ALTER TABLE kitaplar ADD COLUMN durum TEXT DEFAULT 'rafta';"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE kitaplar ADD COLUMN odunc_alan TEXT;"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE kitaplar ADD COLUMN iade_tarihi TEXT;"); } catch (Exception ignore) {}

            ContentValues cv = new ContentValues();
            cv.put("durum", "odunc_verildi");
            cv.put("odunc_alan", oduncAlanUye);

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            cv.put("iade_tarihi", sdf.format(new java.util.Date(System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000)))); // 15 Gün sonrası

            db.update("kitaplar", cv, "kitapadi=?", new String[]{kitapAdi});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void kitapIadeEt(String kitapAdi) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            
            try { db.execSQL("ALTER TABLE kitaplar ADD COLUMN durum TEXT DEFAULT 'rafta';"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE kitaplar ADD COLUMN odunc_alan TEXT;"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE kitaplar ADD COLUMN iade_tarihi TEXT;"); } catch (Exception ignore) {}

            ContentValues cv = new ContentValues();
            cv.put("durum", "rafta");
            cv.put("odunc_alan", (String) null);
            cv.put("iade_tarihi", (String) null);

            db.update("kitaplar", cv, "kitapadi=?", new String[]{kitapAdi});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void kitapPuanla(String kitapAdi, float puan) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            
            try { db.execSQL("ALTER TABLE kitaplar ADD COLUMN puan_toplam REAL DEFAULT 0;"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE kitaplar ADD COLUMN yorum_sayisi INTEGER DEFAULT 0;"); } catch (Exception ignore) {}

            Cursor cursor = db.rawQuery("SELECT puan_toplam, yorum_sayisi FROM kitaplar WHERE kitapadi=?", new String[]{kitapAdi});
            if(cursor.moveToFirst()) {
                float mevcutPuan = cursor.getFloat(0);
                int mevcutYorum = cursor.getInt(1);
                
                ContentValues cv = new ContentValues();
                cv.put("puan_toplam", mevcutPuan + puan);
                cv.put("yorum_sayisi", mevcutYorum + 1);
                db.update("kitaplar", cv, "kitapadi=?", new String[]{kitapAdi});
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Kitap> kitaplariGetirList() {
        ArrayList<Kitap> liste = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM kitaplar", null);

        int idxId = cursor.getColumnIndex("id");
        int idxAd = cursor.getColumnIndex("kitapadi");
        int idxYazar = cursor.getColumnIndex("yazar");
        int idxSayfa = cursor.getColumnIndex("sayfa");
        int idxKategori = cursor.getColumnIndex("kategori");
        int idxDurum = cursor.getColumnIndex("durum");
        int idxIadeTarihi = cursor.getColumnIndex("iade_tarihi");
        int idxPuan = cursor.getColumnIndex("puan_toplam");
        int idxYorum = cursor.getColumnIndex("yorum_sayisi");
        int idxResim = cursor.getColumnIndex("resim_yolu");

        while(cursor.moveToNext()) {
            int id = idxId != -1 ? cursor.getInt(idxId) : 0;
            String kAd = idxAd != -1 ? cursor.getString(idxAd) : "";
            String kYazar = idxYazar != -1 ? cursor.getString(idxYazar) : "";
            String kSayfa = idxSayfa != -1 ? cursor.getString(idxSayfa) : "";
            String kKategori = idxKategori != -1 ? cursor.getString(idxKategori) : "";
            String kDurum = idxDurum != -1 ? cursor.getString(idxDurum) : "rafta";
            if (kDurum == null || kDurum.trim().isEmpty()) {
                kDurum = "rafta"; // Guarantee it is never null for safe string comparison
            }
            String kIadeTarihi = idxIadeTarihi != -1 ? cursor.getString(idxIadeTarihi) : null;
            String kResim = idxResim != -1 ? cursor.getString(idxResim) : null;

            Kitap kitap = new Kitap(id, kAd, kYazar, kSayfa, kKategori, kDurum, kResim, kIadeTarihi);
            
            float kPuanToplam = idxPuan != -1 ? cursor.getFloat(idxPuan) : 0;
            int kYorumSayisi = idxYorum != -1 ? cursor.getInt(idxYorum) : 0;
            kitap.setPuan(kYorumSayisi > 0 ? kPuanToplam / kYorumSayisi : 0);
            kitap.setYorumSayisi(kYorumSayisi);

            liste.add(kitap);
        }
        cursor.close();
        return liste;
    }
}