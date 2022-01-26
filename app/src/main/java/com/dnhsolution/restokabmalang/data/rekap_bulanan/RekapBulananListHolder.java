package com.dnhsolution.restokabmalang.data.rekap_bulanan;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency;
import com.dnhsolution.restokabmalang.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class RekapBulananListHolder extends RecyclerView.ViewHolder {
    private final TextView numItem,omzet,tgl;

    static RekapBulananListHolder newInstance(View parent, Context context) {
        TextView tvMid = parent.findViewById(R.id.mId);
        TextView tvNumItem = parent.findViewById(R.id.tvNumItem);
        TextView tvOmzet = parent.findViewById(R.id.tvOmzet);
        TextView tvTgl = parent.findViewById(R.id.tvTgl);
        return new RekapBulananListHolder(parent, context, tvMid, tvNumItem, tvOmzet, tvTgl);
    }

    private RekapBulananListHolder(final View parent, final Context context, TextView id
            , final TextView tvNumItem, final TextView tvOmzet, final TextView tvTgl) {
        super(parent);
        numItem = tvNumItem;
        omzet = tvOmzet;
        tgl = tvTgl;

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

        final String OLD_FORMAT = "yyyy-MM-dd";
        final String NEW_FORMAT = "dd-MM-yyyy";

// August 12, 2010
        String oldDateString = itemText.getTgl();
        String newDateString;

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT, Locale.getDefault());
        Date d = null;
        try {
            d = sdf.parse(oldDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern(NEW_FORMAT);
        newDateString = sdf.format(d);
        tgl.setText(newDateString);
//        disc.setText(String.valueOf(itemText.getDisc()));
//        String totalValue = new AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.getTotal());
//        total.setText(totalValue);
    }
}
