package com.dnhsolution.restokabmalang.data.rekap_bulanan;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency;
import com.dnhsolution.restokabmalang.R;

class RekapBulananListHolder extends RecyclerView.ViewHolder {
    private final TextView numItem,omzet,tgl,disc,total;
//    private final View view;
//    private final Context context;

    static RekapBulananListHolder newInstance(View parent, Context context) {
        TextView tvMid = parent.findViewById(R.id.mId);
        TextView tvNumItem = parent.findViewById(R.id.tvNumItem);
        TextView tvOmzet = parent.findViewById(R.id.tvOmzet);
        TextView tvTgl = parent.findViewById(R.id.tvTgl);
        TextView tvDisc = parent.findViewById(R.id.tvDisc);
        TextView tvTotal = parent.findViewById(R.id.tvTotal);
        return new RekapBulananListHolder(parent, context, tvMid, tvNumItem, tvOmzet, tvTgl
                , tvDisc, tvTotal);
    }

    private RekapBulananListHolder(final View parent, final Context context, TextView id
            , final TextView tvNumItem, final TextView tvOmzet, final TextView tvTgl
            , final TextView tvDisc, final TextView tvTotal) {
        super(parent);
//        this.context = context;
//        mId = id;
        numItem = tvNumItem;
        omzet = tvOmzet;
        tgl = tvTgl;
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

    void setValues(String number,RekapBulananListElement itemText) {
        numItem.setText(number);
        String omzetValue = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getOmzet());
        omzet.setText(omzetValue);
        tgl.setText(itemText.getTgl());
        disc.setText(String.valueOf(itemText.getDisc()));
//        String totalValue = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getTotal());
//        total.setText(totalValue);
    }
}
