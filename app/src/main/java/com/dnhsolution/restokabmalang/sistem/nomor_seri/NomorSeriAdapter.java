package com.dnhsolution.restokabmalang.sistem.nomor_seri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.dnhsolution.restokabmalang.R;

import java.util.List;

public class NomorSeriAdapter extends RecyclerView.Adapter<NomorSeriAdapter.MyViewHolder> {

    private List<NSModel> nsmodelList;
    Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mId, tvNumItem, tvTglRequest, tvStatus, tvJmlNomorSeri;

        public MyViewHolder(View view){
            super(view);
            mId = view.findViewById(R.id.mId);
            tvNumItem = view.findViewById(R.id.tvNumItem);
            tvTglRequest = view.findViewById(R.id.tvTglRequest);
            tvStatus = view.findViewById(R.id.tvStatus);
            tvJmlNomorSeri = view.findViewById(R.id.tvJmlNomorSeri);
        }
    }

    public NomorSeriAdapter(List<NSModel> nsmodelList, Context context){
        this.nsmodelList = nsmodelList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_nomor_seri, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final NSModel nsmodel = nsmodelList.get(position);
        holder.mId.setText(String.valueOf(nsmodel.getId()));
        holder.tvNumItem.setText(String.valueOf(position+1));
        holder.tvTglRequest.setText(nsmodel.getTgl_request());
        holder.tvStatus.setText(nsmodel.getStatus());
        holder.tvJmlNomorSeri.setText(String.valueOf(nsmodel.getJml_nomor_seri()));
    }

    @Override
    public int getItemCount() {
        return nsmodelList.size();
    }
}
