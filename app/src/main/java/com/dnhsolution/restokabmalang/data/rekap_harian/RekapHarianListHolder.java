package com.dnhsolution.restokabmalang.data.rekap_harian;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnhsolution.restokabmalang.MainActivity;
import com.dnhsolution.restokabmalang.cetak.MainCetak;
import com.dnhsolution.restokabmalang.data.DataFragment;
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.utilities.RekapHarianDetailOnTask;
import com.dnhsolution.restokabmalang.utilities.Utils;

class RekapHarianListHolder extends RecyclerView.ViewHolder {
    private final TextView numItem,mId,nama,Harga,qty,disc,total;
//    private final View view;
//    private final Context context;

    static RekapHarianListHolder newInstance(View parent, Context context, RekapHarianDetailOnTask onTask) {
        TextView tvMid = parent.findViewById(R.id.mId);
        TextView tvNumItem = parent.findViewById(R.id.tvNumItem);
        TextView tvNama = parent.findViewById(R.id.tvNama);
        TextView tvPrice = parent.findViewById(R.id.tvHarga);
        TextView tvQty = parent.findViewById(R.id.tvQty);
        TextView tvDisc = parent.findViewById(R.id.tvDisc);
        TextView tvTotal = parent.findViewById(R.id.tvTotal);
        return new RekapHarianListHolder(parent, context, tvMid, tvNumItem, tvNama, tvPrice, tvQty
                , tvDisc, tvTotal,onTask);
    }

    private RekapHarianListHolder(final View parent, final Context context, TextView id
            , final TextView tvNumItem, final TextView tvNama, final TextView tvPrice, final TextView tvQty
            , final TextView tvDisc, final TextView tvTotal, final RekapHarianDetailOnTask onTask) {
        super(parent);
//        this.context = context;
        mId = id;
        numItem = tvNumItem;
        nama = tvNama;
        Harga = tvPrice;
        qty = tvQty;
        disc = tvDisc;
        total = tvTotal;
//        view = parent;

        parent.setOnClickListener(v -> {
            if(Utils.isOpenRecently()) return;
            String dId = mId.getText().toString();
            onTask.rekapHarianDetailOnTask(dId);
        });

        parent.setOnLongClickListener(v -> {
            String dId = mId.getText().toString();
            context.startActivity(new Intent(context, MainCetak.class).putExtra("getIdItem", dId));
            return true;
        });
    }

    void setValues(String number,RekapHarianListElement itemText) {
        numItem.setText(number);
        mId.setText(String.valueOf(itemText.getIdItem()));
        nama.setText(itemText.getName());
        Harga.setText(itemText.getTgl());
        String disc_rp = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getDisc());
        qty.setText(disc_rp);
        String omzet = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getTotal());
        total.setText(omzet);
    }
}
