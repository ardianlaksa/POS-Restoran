package com.dnhsolution.restokabmalang.cetak;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dnhsolution.restokabmalang.R;

import java.util.List;

public class AdapterProduk extends RecyclerView.Adapter<AdapterProduk.Myholder> {
    List<ItemProduk> itemProduks;
    Context context;

    public AdapterProduk(List<ItemProduk> itemProdukList, Context context) {
        this.itemProduks = itemProdukList;
        this.context=context;
    }

    static class Myholder extends RecyclerView.ViewHolder{
        TextView no,nama_item,qty, harga;

        public Myholder(View itemView) {
            super(itemView);

            no = (TextView) itemView.findViewById(R.id.tvNo);
            nama_item = (TextView) itemView.findViewById(R.id.tvNama);
            qty = (TextView) itemView.findViewById(R.id.tvQty);
            harga = (TextView) itemView.findViewById(R.id.tvHarga);
        }
    }


    @NonNull
    @Override
    public Myholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produk_cetak,parent,false);
        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(Myholder holder, int position) {
        final ItemProduk itemProduk = itemProduks.get(position);

        String nomorString = itemProduk.getNo()+".";
        holder.no.setText(nomorString);
        holder.nama_item.setText(itemProduk.getNama_produk());
        String qtyString = "( "+itemProduk.getQty()+" )";
        holder.qty.setText(qtyString);
        holder.harga.setText(itemProduk.getHarga());


    }

    @Override
    public int getItemCount() {
        return itemProduks.size();
    }

}
