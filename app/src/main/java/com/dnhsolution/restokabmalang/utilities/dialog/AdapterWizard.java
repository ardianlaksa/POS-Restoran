package com.dnhsolution.restokabmalang.utilities.dialog;

import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dnhsolution.restokabmalang.R;

import java.util.ArrayList;

public class AdapterWizard  extends ArrayAdapter<ItemView> {
    // invoke the suitable constructor of the ArrayAdapter class
    public AdapterWizard(@NonNull Context context, ArrayList<ItemView> arrayList) {

        // pass the context and arrayList for the super
        // constructor of the ArrayAdapter class
        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // convertView which is recyclable view
        View currentItemView = convertView;

        // of the recyclable view is null then inflate the custom layout for the same
        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_tutorial, parent, false);
        }

        // get the position of the view from the ArrayAdapter
        ItemView currentNumberPosition = getItem(position);

        // then according to the position of the view assign the desired image for the same
//        ImageView numbersImage = currentItemView.findViewById(R.id.imageView);
//        assert currentNumberPosition != null;
//        numbersImage.setImageResource(currentNumberPosition.getNumbersImageId());

        // then according to the position of the view assign the desired TextView 1 for the same
        TextView textView1 = currentItemView.findViewById(R.id.tvNomor);
        textView1.setText(currentNumberPosition.getNumberInDigit());

        // then according to the position of the view assign the desired TextView 2 for the same
        TextView textView2 = currentItemView.findViewById(R.id.tvIsi);
        textView2.setText(currentNumberPosition.getInText());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textView2.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }


        // then return the recyclable view
        return currentItemView;
    }
}
