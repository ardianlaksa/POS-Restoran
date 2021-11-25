package com.dnhsolution.restokabmalang.data.rekap_harian

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.R

class DRekapHarianListAdapter2(itemList: ArrayList<DRekapHarianListElement>, private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mItemList: ArrayList<DRekapHarianListElement>? = null

    val _tag = javaClass.simpleName

    init {
        mItemList = itemList
        Log.d(_tag,"${mItemList?.size}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_d_rekap_harian, parent, false)
        return DRekapHarianListHolder.newInstance(view, context)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as DRekapHarianListHolder
        val itemText = mItemList!![position]
        val numItem = "${position+1}"
        holder.setValues(numItem,itemText)
    }

    override fun getItemCount(): Int {
        return if (mItemList == null) 0 else mItemList!!.size
    }

    internal fun setFilter(mItem: List<DRekapHarianListElement>) {
        mItemList = ArrayList()
        mItemList!!.addAll(mItem)
        notifyDataSetChanged()
    }
}
