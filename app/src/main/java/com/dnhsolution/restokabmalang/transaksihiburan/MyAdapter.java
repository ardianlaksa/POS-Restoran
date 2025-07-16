package com.dnhsolution.restokabmalang.transaksihiburan;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dnhsolution.restokabmalang.R;

public class MyAdapter extends BaseAdapter {

    Context mCtx;
    int[] mImg;
    LayoutInflater layoutInflater;
    RadioGroup rgp;
    private RadioButton mSelectedRB;
    private int mSelectedPosition = -1;

    public MyAdapter(Context context, int[] img) {
        this.mCtx = context;
        this.mImg = img;
        rgp = new RadioGroup(context);
        layoutInflater = (LayoutInflater) mCtx
                .getSystemService(LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {

        return mImg.length;
    }

    @Override
    public Object getItem(int position) {

        return null;
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        View view = convertView;
        Holder holder;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_blood_type, null);
            holder = new Holder();
            holder.image = (ImageView) view.findViewById(R.id.imageView);
            holder.radioButton = (RadioButton) view
                    .findViewById(R.id.radiobtn);
            view.setTag(holder);

        } else {

            holder = (Holder) view.getTag();
        }

        holder.radioButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if ((position != mSelectedPosition && mSelectedRB != null)) {
                    mSelectedRB.setChecked(false);
                }

                mSelectedPosition = position;
                mSelectedRB = (RadioButton) v;
            }
        });

        if (mSelectedPosition != position) {
            holder.radioButton.setChecked(false);
        } else {
            holder.radioButton.setChecked(true);
            if (mSelectedRB != null && holder.radioButton != mSelectedRB) {
                mSelectedRB = holder.radioButton;
            }
        }

        return view;
    }

}

class Holder {
    ImageView image;
    RadioButton radioButton;

}
