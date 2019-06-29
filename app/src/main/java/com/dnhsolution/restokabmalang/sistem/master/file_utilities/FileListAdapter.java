package com.dnhsolution.restokabmalang.sistem.master.file_utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dnhsolution.restokabmalang.R;

import java.util.ArrayList;

public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final DeleteFileOnTask listener;
    private Context context;
    private ArrayList<FileListElement> mItemList;

    public FileListAdapter(ArrayList<FileListElement> itemList, Context context, DeleteFileOnTask listener) {
        this.mItemList = itemList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_file, parent, false);
        return FileListHolder.newInstance(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        FileListHolder holder = (FileListHolder) viewHolder;
        FileListElement itemText = mItemList.get(position);
        holder.setId(itemText.getId());

        String item = itemText.getItem();
        holder.setImage(item);

        String[] parts = item.split("/");
        final String fileName = parts[parts.length-1];

        holder.setItem(fileName);
        holder.setDeleteFile(listener, position);
        String sizeFile = itemText.getSize()+" KB";
        holder.setSizeFile(sizeFile);
    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    void setFilter(ArrayList<FileListElement> mItem){
        mItemList = new ArrayList<>();
        mItemList.addAll(mItem);
        notifyDataSetChanged();
    }
}
