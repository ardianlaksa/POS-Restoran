package com.dnhsolution.restokabmalang.transaksihiburan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dnhsolution.restokabmalang.R;

import java.util.List;

public class TarifListAdapter extends RecyclerView.Adapter<TarifListAdapter.MyViewHolder> {

    private List<ProdukModel> detailTersimpanList;
    Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mId, tvTipeKend, tvNominal;
        public Button btnTipeKend;

        public MyViewHolder(View view){
            super(view);
            mId = view.findViewById(R.id.mId);
            tvTipeKend = view.findViewById(R.id.tvTipeKend);
            tvNominal = view.findViewById(R.id.tvNominal);
            btnTipeKend = view.findViewById(R.id.btnTipeKend);
        }
    }

    public TarifListAdapter(List<ProdukModel> detailTersimpanList, Context context){
        this.detailTersimpanList = detailTersimpanList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_tarif, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final ProdukModel detailTersimpan = detailTersimpanList.get(position);
        holder.mId.setText(String.valueOf(detailTersimpan.getId()));
        holder.tvTipeKend.setText(detailTersimpan.getNama());
        holder.tvNominal.setText(String.valueOf(detailTersimpan.getNominal()));
        holder.btnTipeKend.setText(detailTersimpan.getNama());
        if (detailTersimpan.getIschecked()==false){
            holder.btnTipeKend.setBackgroundTintList(context.getColorStateList(R.color.colorOff));
            holder.btnTipeKend.setTextColor(context.getColor(R.color.colorBlack));
        }else{
            holder.btnTipeKend.setBackgroundTintList(context.getColorStateList(R.color.colorPrimary));
            holder.btnTipeKend.setTextColor(context.getColor(R.color.colorWhite));
        }
        holder.btnTipeKend.setOnClickListener(v -> {
            if (detailTersimpan.getIschecked()==false){
                holder.btnTipeKend.setBackgroundTintList(context.getColorStateList(R.color.colorPrimary));
                holder.btnTipeKend.setTextColor(context.getColor(R.color.colorWhite));
                detailTersimpan.setIschecked(true);
            }else{
                holder.btnTipeKend.setBackgroundTintList(context.getColorStateList(R.color.colorOff));
                holder.btnTipeKend.setTextColor(context.getColor(R.color.colorBlack));
                detailTersimpan.setIschecked(false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return detailTersimpanList.size();
    }
}
