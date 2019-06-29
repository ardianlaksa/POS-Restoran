package com.dnhsolution.restokabmalang.sistem.master.file_utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.dnhsolution.restokabmalang.R;

class FileListHolder extends RecyclerView.ViewHolder {
    private final TextView tvId,textView,textView1;
    private final ImageView imageView;
    private final View view;
    private final Context context;
    private final ImageButton imageButton;

    static FileListHolder newInstance(View parent, Context context) {
        TextView tvId = parent.findViewById(R.id.tvId);
        ImageView imageView = parent.findViewById(R.id.imageView);
        TextView textView = parent.findViewById(R.id.textView);
        TextView textView1 = parent.findViewById(R.id.textView1);
        ImageButton imageButton = parent.findViewById(R.id.imageButton);
        return new FileListHolder(parent, context, imageView, tvId, textView, textView1, imageButton);
    }

    private FileListHolder(final View parent, final Context context
            ,final ImageView imageView, final TextView tvId, final TextView textView, final TextView textView1
            , final ImageButton imageButton) {
        super(parent);
        this.context = context;
        this.imageView = imageView;
        this.tvId = tvId;
        this.textView = textView;
        this.textView1 = textView1;
        this.imageButton = imageButton;
        view = parent;

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String dId = mId.getText().toString();
//                activity.startActivity(new Intent(activity, PandWisataDetail.class).putExtra("getId",dId));
            }
        });
    }

    void setId(Integer value) {
        tvId.setText(String.valueOf(value));
    }

    void setImage(String text) {
        Bitmap bitmap = BitmapFactory.decodeFile(text);
        if (bitmap == null) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);
        }
    }

    void setItem(CharSequence text) { textView.setText(text);}

    void setDeleteFile(final DeleteFileOnTask listener, final Integer index){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.deleteFileOnTask(index);
//                String dId = mId.getText().toString();
//                activity.startActivity(new Intent(activity, PandWisataDetail.class).putExtra("getId",dId));
            }
        });
    }

    void setSizeFile(CharSequence text) { textView1.setText(text);}
}
