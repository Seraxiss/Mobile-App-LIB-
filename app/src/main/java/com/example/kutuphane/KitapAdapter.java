package com.example.kutuphane;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

import android.widget.Filter;
import android.widget.Filterable;
import java.util.List;

public class KitapAdapter extends ArrayAdapter<Kitap> implements Filterable {

    private ArrayList<Kitap> tumKitaplar;
    private ArrayList<Kitap> filtrelenmişKitaplar;

    public KitapAdapter(Context context, ArrayList<Kitap> kitaplar) {
        super(context, 0, kitaplar);
        this.tumKitaplar = new ArrayList<>(kitaplar);
        this.filtrelenmişKitaplar = kitaplar;
    }

    @Override
    public int getCount() {
        return filtrelenmişKitaplar.size();
    }

    @Override
    public Kitap getItem(int position) {
        return filtrelenmişKitaplar.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_kitap, parent, false);
            }

            Kitap kitap = getItem(position);

            ImageView imgCover = convertView.findViewById(R.id.imgBookCover);
            TextView txtAd = convertView.findViewById(R.id.txtKitapAdi);
            TextView txtYazar = convertView.findViewById(R.id.txtYazarAdi);
            TextView txtDetay = convertView.findViewById(R.id.txtDetaylar);
            TextView txtDurum = convertView.findViewById(R.id.txtDurum);
            TextView txtIadeTarihi = convertView.findViewById(R.id.txtIadeTarihi);
            android.widget.RatingBar ratingBar = convertView.findViewById(R.id.ratingBarPuan);
            TextView txtPuan = convertView.findViewById(R.id.txtPuan);

            txtAd.setText(kitap.getAd());
            txtYazar.setText(kitap.getYazar());
            
            txtDetay.setText(kitap.getKategori());
            
            if ("odunc_verildi".equals(kitap.getDurum())) {
                txtDurum.setText("Ödünç Verildi");
                txtDurum.setTextColor(android.graphics.Color.parseColor("#E74C3C")); // Kırmızımsı
                txtIadeTarihi.setVisibility(View.VISIBLE);
                if (kitap.getIadeTarihi() != null && !kitap.getIadeTarihi().isEmpty()) {
                    txtIadeTarihi.setText("İade: " + kitap.getIadeTarihi());
                } else {
                    txtIadeTarihi.setText("");
                }
            } else {
                txtDurum.setText("Rafta");
                txtDurum.setTextColor(android.graphics.Color.parseColor("#27AE60")); // Yeşil
                txtIadeTarihi.setVisibility(View.GONE);
            }
            
            float puan = kitap.getPuan();
            ratingBar.setRating(puan);
            txtPuan.setText(String.format("%.1f", puan));

            String resimYolu = kitap.getResimYolu();
            if (resimYolu != null && !resimYolu.isEmpty()) {
                Glide.with(getContext())
                    .load(resimYolu) // Works for both file:// URIs and http:// URLs
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(imgCover);
            } else {
                imgCover.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            return convertView;
        } catch (Exception e) {
            android.widget.Toast.makeText(getContext(), "Hata Adapter getView: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return new android.view.View(getContext());
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Kitap> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(tumKitaplar);
                } else {
                    String pattern = constraint.toString().toLowerCase().trim();
                    for (Kitap k : tumKitaplar) {
                        boolean matchAd = k.getAd() != null && k.getAd().toLowerCase().contains(pattern);
                        boolean matchYazar = k.getYazar() != null && k.getYazar().toLowerCase().contains(pattern);
                        if (matchAd || matchYazar) {
                            filteredList.add(k);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtrelenmişKitaplar.clear();
                filtrelenmişKitaplar.addAll((ArrayList<Kitap>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}
