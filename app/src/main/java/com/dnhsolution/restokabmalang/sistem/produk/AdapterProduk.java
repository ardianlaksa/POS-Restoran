package com.dnhsolution.restokabmalang.sistem.produk;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.utilities.CheckNetwork;
import com.dnhsolution.restokabmalang.utilities.Url;

import java.io.File;
import java.util.List;

/**
 * Created by sawrusdino on 09/04/2018.
 */

public class AdapterProduk extends RecyclerView.Adapter<AdapterProduk.MyViewHolder> {

    private List<ItemProduk> itemProdukList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nama;
        public ImageView ivFoto;
        public ImageView ivIcSync;

        public MyViewHolder(View view){
            super(view);
            nama = (TextView) view.findViewById(R.id.tvNama);
            ivFoto = (ImageView)view.findViewById(R.id.ivBerkas);
            ivIcSync = (ImageView)view.findViewById(R.id.ivIcSync);
        }
    }

    public AdapterProduk(List<ItemProduk> itemProduks, Context context){
        this.itemProdukList = itemProduks;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_produk, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final ItemProduk itemProduk = itemProdukList.get(position);
        holder.ivIcSync.setImageResource(itemProduk.getStatus() ? R.drawable.ic_baseline_sync_alt_24_green : R.drawable.ic_baseline_sync_24_yellow);

        holder.nama.setText(itemProduk.getNama_barang());

        if(new CheckNetwork().checkingNetwork(context)){
            Glide.with(holder.ivFoto.getContext()).load(Url.serverFoto+itemProduk.getUrl_image())
                    .centerCrop()
                    .fitCenter()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("xmx1","Error "+e.toString());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.e("xmx1","no Error ");
                            return false;
                        }
                    })
                    .into(holder.ivFoto);
        }else{
            Glide.with(holder.ivFoto.getContext()).load(new File(itemProduk.getUrl_image()).toString())
                    .centerCrop()
                    .fitCenter()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("xmx1","Error "+e.toString());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.e("xmx1","no Error ");
                            return false;
                        }
                    })
                    .into(holder.ivFoto);
        }


    }

    @Override
    public int getItemCount() {
        return itemProdukList.size();
    }
}
