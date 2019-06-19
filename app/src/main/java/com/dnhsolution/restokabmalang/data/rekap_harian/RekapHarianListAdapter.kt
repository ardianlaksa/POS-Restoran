package com.dnhsolution.restokabmalang.data.rekap_harian

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.R

class RekapHarianListAdapter(itemList: ArrayList<RekapHarianListElement>, private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mItemList: ArrayList<RekapHarianListElement>? = null

    val _tag = javaClass.simpleName

    init {
        mItemList = itemList
        Log.d(_tag,"${mItemList?.size}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_rekap_harian, parent, false)
        return RekapHarianListHolder.newInstance(view, context)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as RekapHarianListHolder
        val itemText = mItemList!![position]
        val numItem = "${position+1}"
        holder.setValues(numItem,itemText)
    }

    override fun getItemCount(): Int {
        return if (mItemList == null) 0 else mItemList!!.size
    }

    internal fun setFilter(mItem: List<RekapHarianListElement>) {
        mItemList = ArrayList()
        mItemList!!.addAll(mItem)
        notifyDataSetChanged()
    }
}
