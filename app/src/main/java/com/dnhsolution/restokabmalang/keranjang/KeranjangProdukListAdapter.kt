package com.dnhsolution.restokabmalang.keranjang

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.ProdukSerializable

class KeranjangProdukListAdapter(itemList: ArrayList<ProdukSerializable>, private val activity: Activity
                                 , private val onTask: KeranjangProdukItemOnTask) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mItemList: ArrayList<ProdukSerializable>? = null

    init {
        mItemList = itemList
        Log.d("sptpdb","${mItemList?.size}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_keranjang_produk, parent, false)
        return KeranjangProdukListHolder.newInstance(view, activity)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as KeranjangProdukListHolder
        val itemText = mItemList!![position]
        holder.setValues(onTask,itemText,position)
    }

    override fun getItemCount(): Int {
        return if (mItemList == null) 0 else mItemList!!.size
    }

    internal fun setFilter(mItem: List<ProdukSerializable>) {
        mItemList = ArrayList()
        mItemList!!.addAll(mItem)
        notifyDataSetChanged()
    }
}
