package com.dnhsolution.restokabmalang.transaksi.keranjang;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dnhsolution.restokabmalang.utilities.KeranjangProdukItemOnTask;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable;
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency;
import com.dnhsolution.restokabmalang.utilities.Url;

class KeranjangProdukListHolder extends RecyclerView.ViewHolder {
    private final ImageView ivItem;
    private final TextView judul,price,jumlahProduk,totalPrice;
    private final View view;
    private final Activity activity;
    private final ImageButton minus,plus;
    private final String _tag = "SelectedProdukList";
    private final View vBorderBottom;

    static KeranjangProdukListHolder newInstance(View parent, Activity activity) {
        ImageView ivItem = parent.findViewById(R.id.ivItem);
        TextView tvJudul = parent.findViewById(R.id.tvJudul);
        TextView tvPrice = parent.findViewById(R.id.tvPrice);
        TextView tvJumlahProduk = parent.findViewById(R.id.tvJumlahProduk);
        ImageButton bMinus = parent.findViewById(R.id.bMinus);
        ImageButton bPlus = parent.findViewById(R.id.bPlus);
        TextView tvTotalPrice = parent.findViewById(R.id.tvTotalPrice);
        View vBorderBottom = parent.findViewById(R.id.vBorderBottom);
        return new KeranjangProdukListHolder(parent, activity, ivItem, tvJudul, tvPrice, tvJumlahProduk
                , bMinus, bPlus, tvTotalPrice, vBorderBottom);
    }

    private KeranjangProdukListHolder(final View parent, final Activity activity, ImageView ivItem, TextView judul
            , final TextView price, final TextView jumlahProduk, final ImageButton minus, final ImageButton plus,
                                      final TextView totalPrice, final View vBorderBottom) {
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
        this.vBorderBottom = vBorderBottom;

        parent.setOnClickListener(v -> {
//                String dId = mId.getText().toString();
//                activity.startActivity(new Intent(activity, PandWisataDetail.class).putExtra("getId",dId));
        });
    }

    void setValues(final KeranjangProdukItemOnTask onTask, final ProdukSerializable obyek,
                   final int position, final int jmlProdukDipesan) {
        int jumlah = obyek.getQty();
        Log.i(_tag, "setValue : "+jumlah+", posisi "+position);
        judul.setText(obyek.getName());
        final String priceValue = obyek.getPrice();
        if (priceValue == null) return;

        if (jmlProdukDipesan-1 == position) vBorderBottom.setVisibility(View.GONE);

        String rupiahPrice = new AddingIDRCurrency().formatIdrCurrencyNonKoma(Double.parseDouble(priceValue));
        price.setText(rupiahPrice);
        totalPrice.setText(rupiahPrice);
        jumlahProduk.setText(String.valueOf(jumlah));

        String url = Url.serverFoto+obyek.getImgUrl();
//        Glide.with(ivItem.getContext()).load(url)
//                .override(512,512)
//                .centerCrop()
//                .fitCenter()
//                .diskCacheStrategy(DiskCacheStrategy.DATA)
//                .listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        Log.e("xmx1","Error "+e.toString());
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        Log.e("xmx1","no Error ");
//                        return false;
//                    }
//                })
//                .into(ivItem);
//        Picasso.get().load(Url.serverFoto+obyek.getImgUrl())
//                .into(ivItem);

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
