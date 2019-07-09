package com.dnhsolution.restokabmalang.transaksi.selected_produk_list;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.dnhsolution.restokabmalang.utilities.KeranjangProdukItemOnTask;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable;
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.squareup.picasso.Picasso;

class SelectedProdukListHolder extends RecyclerView.ViewHolder {
    private final ImageView ivItem;
    private final TextView judul,price,jumlahProduk,totalPrice;
    private final View view;
    private final Activity activity;
    private final ImageButton minus,plus;

    static SelectedProdukListHolder newInstance(View parent, Activity activity) {
        ImageView ivItem = parent.findViewById(R.id.ivItem);
        TextView tvJudul = parent.findViewById(R.id.tvJudul);
        TextView tvPrice = parent.findViewById(R.id.tvPrice);
        TextView tvJumlahProduk = parent.findViewById(R.id.tvJumlahProduk);
        ImageButton bMinus = parent.findViewById(R.id.bMinus);
        ImageButton bPlus = parent.findViewById(R.id.bPlus);
        TextView tvTotalPrice = parent.findViewById(R.id.tvTotalPrice);
        return new SelectedProdukListHolder(parent, activity, ivItem, tvJudul, tvPrice, tvJumlahProduk
                , bMinus, bPlus, tvTotalPrice);
    }

    private SelectedProdukListHolder(final View parent, final Activity activity, ImageView ivItem, TextView judul
            , final TextView price, final TextView jumlahProduk, final ImageButton minus, final ImageButton plus,
                                     final TextView totalPrice) {
        super(parent);
        this.activity = activity;
        this.ivItem = ivItem;
        this.price = price;
        this.judul = judul;
        this.jumlahProduk = jumlahProduk;
        this.minus = minus;
        this.plus = plus;
        this.totalPrice = totalPrice;
        view = parent;

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String dId = mId.getText().toString();
//                activity.startActivity(new Intent(activity, PandWisataDetail.class).putExtra("getId",dId));
            }
        });
    }

    void setValues(final KeranjangProdukItemOnTask onTask, final ProdukSerializable obyek, final int position) {
        judul.setText(obyek.getName());
        final String priceValue = obyek.getPrice();
        if (priceValue == null) return;

        String rupiahPrice = new AddingIDRCurrency().formatIdrCurrencyNonKoma(Double.parseDouble(priceValue));
        price.setText(rupiahPrice);
        totalPrice.setText(rupiahPrice);

        Picasso.get().load(Url.serverFoto+obyek.getImgUrl())
                .into(ivItem);

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//            int jumlah = Integer.parseInt(jumlahProduk.getText().toString());
                int jumlah = obyek.getQty();
            if (jumlah > 1) {
                jumlah--;
                jumlahProduk.setText(String.valueOf(jumlah));
                int priceValueTotal = Integer.parseInt(priceValue)*jumlah;
                onTask.keranjangProdukItemOnTask(position,priceValueTotal,jumlah);
                String sPriceValueTotal = new AddingIDRCurrency().formatIdrCurrencyNonKoma(priceValueTotal);
                totalPrice.setText(sPriceValueTotal);
            }
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              int jumlah = Integer.parseInt(jumlahProduk.getText().toString());
            int jumlah = obyek.getQty();
            jumlah++;
            jumlahProduk.setText(String.valueOf(jumlah));
            int priceValueTotal = Integer.parseInt(priceValue)*jumlah;
            onTask.keranjangProdukItemOnTask(position,priceValueTotal,jumlah);
            String sPriceValueTotal = new AddingIDRCurrency().formatIdrCurrencyNonKoma(priceValueTotal);
            totalPrice.setText(sPriceValueTotal);
            }
        });
    }
}
