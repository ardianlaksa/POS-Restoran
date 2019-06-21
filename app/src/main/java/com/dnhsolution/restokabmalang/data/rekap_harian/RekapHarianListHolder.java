package com.dnhsolution.restokabmalang.data.rekap_harian;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.dnhsolution.restokabmalang.AddingIDRCurrency;
import com.dnhsolution.restokabmalang.R;

class RekapHarianListHolder extends RecyclerView.ViewHolder {
    private final TextView numItem,nama,Harga,qty,disc,total;
//    private final View view;
//    private final Context context;

    static RekapHarianListHolder newInstance(View parent, Context context) {
        TextView tvMid = parent.findViewById(R.id.mId);
        TextView tvNumItem = parent.findViewById(R.id.tvNumItem);
        TextView tvNama = parent.findViewById(R.id.tvNama);
        TextView tvPrice = parent.findViewById(R.id.tvHarga);
        TextView tvQty = parent.findViewById(R.id.tvQty);
        TextView tvDisc = parent.findViewById(R.id.tvDisc);
        TextView tvTotal = parent.findViewById(R.id.tvTotal);
        return new RekapHarianListHolder(parent, context, tvMid, tvNumItem, tvNama, tvPrice, tvQty
                , tvDisc, tvTotal);
    }

    private RekapHarianListHolder(final View parent, final Context context, TextView id
            , final TextView tvNumItem, final TextView tvNama, final TextView tvPrice, final TextView tvQty
            , final TextView tvDisc, final TextView tvTotal) {
        super(parent);
//        this.context = context;
//        mId = id;
        numItem = tvNumItem;
        nama = tvNama;
        Harga = tvPrice;
        qty = tvQty;
        disc = tvDisc;
        total = tvTotal;
//        view = parent;

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String dId = mId.getText().toString();
//                activity.startActivity(new Intent(activity, PandWisataDetail.class).putExtra("getId",dId));
            }
        });
    }

    void setValues(String number,RekapHarianListElement itemText) {
        numItem.setText(number);
        nama.setText(itemText.getName());
        String priceValue = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getHarga());
        Harga.setText(priceValue);
        qty.setText(String.valueOf(itemText.getQty()));
        disc.setText(String.valueOf(itemText.getDisc()));
        String totalValue = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getTotal());
        total.setText(totalValue);
    }
}
