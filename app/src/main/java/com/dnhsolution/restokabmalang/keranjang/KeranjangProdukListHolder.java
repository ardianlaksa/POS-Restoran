package com.dnhsolution.restokabmalang.keranjang;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.ProdukSerializable;

class KeranjangProdukListHolder extends RecyclerView.ViewHolder {
    private final TextView judul,price,jumlahProduk;
    private final View view;
    private final Activity activity;
    private final ImageButton minus,plus;

    static KeranjangProdukListHolder newInstance(View parent, Activity activity) {
        TextView tvJudul = parent.findViewById(R.id.tvJudul);
        TextView tvPrice = parent.findViewById(R.id.tvPrice);
        TextView tvJumlahProduk = parent.findViewById(R.id.tvJumlahProduk);
        ImageButton bMinus = parent.findViewById(R.id.bMinus);
        ImageButton bPlus = parent.findViewById(R.id.bPlus);
        return new KeranjangProdukListHolder(parent, activity, tvJudul, tvPrice, tvJumlahProduk, bMinus, bPlus);
    }

    private KeranjangProdukListHolder(final View parent, final Activity activity, TextView judul
            , final TextView price, final TextView jumlahProduk, final ImageButton minus, final ImageButton plus) {
        super(parent);
        this.activity = activity;
        this.price = price;
        this.judul = judul;
        this.jumlahProduk = jumlahProduk;
        this.minus = minus;
        this.plus = plus;
        view = parent;

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String dId = mId.getText().toString();
//                activity.startActivity(new Intent(activity, PandWisataDetail.class).putExtra("getId",dId));
            }
        });
    }

    void setValues(final KeranjangProdukItemOnTask onTask, ProdukSerializable obyek,final int position) {
        judul.setText(obyek.getName());
        final String priceValue = obyek.getPrice();
        if (priceValue == null) return;
        price.setText(priceValue);

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int jumlah = Integer.parseInt(jumlahProduk.getText().toString());
                if (jumlah > 1) {
                    jumlah--;
                    jumlahProduk.setText(String.valueOf(jumlah));
                    int priceValueTotal = Integer.parseInt(priceValue)*jumlah;
                    onTask.keranjangProdukItemOnTask(position,priceValueTotal);
                }
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int jumlah = Integer.parseInt(jumlahProduk.getText().toString());
                jumlah++;
                jumlahProduk.setText(String.valueOf(jumlah));
                int priceValueTotal = Integer.parseInt(priceValue)*jumlah;
                onTask.keranjangProdukItemOnTask(position,priceValueTotal);
            }
        });
    }
}
