package com.example.kutuphane;

public class Kitap {
    private int id;
    private String ad;
    private String yazar;
    private String sayfa;
    private String kategori;
    private String durum;
    private String resimYolu;
    private String iadeTarihi;
    private float puan;
    private int yorumSayisi;

    public Kitap(int id, String ad, String yazar, String sayfa, String kategori, String durum, String resimYolu, String iadeTarihi) {
        this.id = id;
        this.ad = ad;
        this.yazar = yazar;
        this.sayfa = sayfa;
        this.kategori = kategori;
        this.durum = durum;
        this.resimYolu = resimYolu;
        this.iadeTarihi = iadeTarihi;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getAd() { return ad; }
    public String getYazar() { return yazar; }
    public String getSayfa() { return sayfa; }
    public String getKategori() { return kategori; }
    public String getDurum() { return durum; }
    public String getResimYolu() { return resimYolu; }
    public String getIadeTarihi() { return iadeTarihi; }
    
    public void setIadeTarihi(String iadeTarihi) { this.iadeTarihi = iadeTarihi; }
    public void setDurum(String durum) { this.durum = durum; }
    public void setPuan(float puan) { this.puan = puan; }
    public float getPuan() { return puan; }
    
    public void setYorumSayisi(int yorumSayisi) { this.yorumSayisi = yorumSayisi; }
    public int getYorumSayisi() { return yorumSayisi; }
}
