package com.dnhsolution.restokabmalang.tersimpan;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.utilities.DataTersimpanLongClick;

import java.util.List;

public class TersimpanAdater extends RecyclerView.Adapter<TersimpanAdater.MyViewHolder> {

    private List<ItemTersimpan> tersimpanList;
    Context context;
    View view;
    DataTersimpanLongClick onLongClick;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mId, tvNumItem, tvTrx, tvTgl, tvDisc, tvOmzet, tvStatus;

        public MyViewHolder(View view){
            super(view);
            mId = (TextView) view.findViewById(R.id.mId);
            tvNumItem = (TextView) view.findViewById(R.id.tvNumItem);
            tvTrx = (TextView)view.findViewById(R.id.tvNama);
            tvTgl = (TextView)view.findViewById(R.id.tvHarga);
            tvDisc = (TextView)view.findViewById(R.id.tvQty);
            tvOmzet = (TextView)view.findViewById(R.id.tvTotal);
            tvStatus = (TextView)view.findViewById(R.id.tvStatus);
        }
    }

    public TersimpanAdater(List<ItemTersimpan> tersimpanList, Context context, DataTersimpanLongClick onLongClick){
        this.tersimpanList = tersimpanList;
        this.context = context;
        this.onLongClick = onLongClick;
    }

    @Override
    public TersimpanAdater.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_tersimpan, parent, false);
        view = itemView;

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TersimpanAdater.MyViewHolder holder, int position) {
        final ItemTersimpan ItemTersimpan = tersimpanList.get(position);
        holder.mId.setText(String.valueOf(ItemTersimpan.getId()));
        holder.tvNumItem.setText(String.valueOf(ItemTersimpan.getNo()));
        holder.tvTgl.setText(ItemTersimpan.getTanggal_trx());
        holder.tvDisc.setText(ItemTersimpan.getDisc_rp());
        holder.tvOmzet.setText(ItemTersimpan.getOmzet());
        holder.tvTrx.setText(String.valueOf(ItemTersimpan.getId()));
        if(ItemTersimpan.getStatus().equalsIgnoreCase("0")){
            holder.tvStatus.setText("Belum Sinkron");
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
        }else{
            holder.tvStatus.setText("Sudah Sinkron");
            holder.tvStatus.setTextColor(Color.parseColor("#148E19"));
        }

        view.setOnClickListener(v -> {
//            Toast.makeText(context,"adsf",Toast.LENGTH_SHORT).show();
            ((DataTersimpanActivity)context).DialogDetailTrx(ItemTersimpan.getId());
        });

        view.setOnLongClickListener(v -> {
            onLongClick.dataTersimpanLongClick(String.valueOf(ItemTersimpan.getId()));
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tersimpanList.size();
    }
}
