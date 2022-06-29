package com.dnhsolution.restokabmalang.tersimpan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.dnhsolution.restokabmalang.R;

import java.util.List;

public class DetailTersimpanAdapter extends RecyclerView.Adapter<DetailTersimpanAdapter.MyViewHolder> {

    private List<ItemDetailTersimpan> detailTersimpanList;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mId, tvNumItem, tvNama, tvHarga, tvQty, tvTotal;

        public MyViewHolder(View view){
            super(view);
            mId = (TextView) view.findViewById(R.id.mId);
            tvNumItem = (TextView) view.findViewById(R.id.tvNumItem);
            tvNama = (TextView)view.findViewById(R.id.tvNama);
            tvHarga = (TextView)view.findViewById(R.id.tvHarga);
            tvQty = (TextView)view.findViewById(R.id.tvQty);
            tvTotal = (TextView)view.findViewById(R.id.tvTotal);
        }
    }

    public DetailTersimpanAdapter(List<ItemDetailTersimpan> detailTersimpanList, Context context){
        this.detailTersimpanList = detailTersimpanList;
        this.context = context;
    }

    @Override
    public DetailTersimpanAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_d_rekap_harian, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DetailTersimpanAdapter.MyViewHolder holder, int position) {
        final ItemDetailTersimpan detailTersimpan = detailTersimpanList.get(position);
        holder.mId.setText(String.valueOf(detailTersimpan.getId()));
        holder.tvNumItem.setText(String.valueOf(detailTersimpan.getNo()));
        holder.tvNama.setText(detailTersimpan.getNama());
        holder.tvHarga.setText(detailTersimpan.getHarga());
        holder.tvQty.setText(detailTersimpan.getQty());
        holder.tvTotal.setText(detailTersimpan.getTotal());

    }

    @Override
    public int getItemCount() {
        return detailTersimpanList.size();
    }
}
