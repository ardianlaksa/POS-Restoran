package com.dnhsolution.restokabmalang.data.rekap_harian;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency;

class DRekapHarianListHolder extends RecyclerView.ViewHolder {
    private final TextView numItem,nama,Harga,qty,total;
//    private final View view;
//    private final Context context;

    static DRekapHarianListHolder newInstance(View parent, Context context) {
        TextView tvMid = parent.findViewById(R.id.mId);
        TextView tvNumItem = parent.findViewById(R.id.tvNumItem);
        TextView tvNama = parent.findViewById(R.id.tvNama);
        TextView tvPrice = parent.findViewById(R.id.tvHarga);
        TextView tvQty = parent.findViewById(R.id.tvQty);
        TextView tvTotal = parent.findViewById(R.id.tvTotal);
        return new DRekapHarianListHolder(parent, context, tvMid, tvNumItem, tvNama, tvPrice, tvQty
                , tvTotal);
    }

    private DRekapHarianListHolder(final View parent, final Context context, TextView id
            , final TextView tvNumItem, final TextView tvNama, final TextView tvPrice, final TextView tvQty
            , final TextView tvTotal) {
        super(parent);
//        this.context = context;
//        mId = id;
        numItem = tvNumItem;
        nama = tvNama;
        Harga = tvPrice;
        qty = tvQty;
        total = tvTotal;
//        view = parent;

        parent.setOnClickListener(v -> {
//                String dId = mId.getText().toString();
//                activity.startActivity(new Intent(activity, PandWisataDetail.class).putExtra("getId",dId));
        });
    }

    void setValues(String number,DRekapHarianListElement itemText) {
        numItem.setText(number);
        nama.setText(itemText.getName());
        String harga = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getHarga());
        Harga.setText(harga);
        qty.setText(String.valueOf(itemText.getQty()));
        String omzet = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getTotal());
        total.setText(omzet);

//        String priceValue = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getHarga());
//        Harga.setText(priceValue);
//        qty.setText(String.valueOf(itemText.getQty()));
//        disc.setText(String.valueOf(itemText.getDisc()));
//        String totalValue = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getTotal());
//        total.setText(totalValue);
    }
}
