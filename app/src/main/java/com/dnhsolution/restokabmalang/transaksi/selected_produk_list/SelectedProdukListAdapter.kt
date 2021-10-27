package com.dnhsolution.restokabmalang.transaksi.selected_produk_list

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.utilities.KeranjangProdukItemOnTask
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable
import com.google.android.material.snackbar.Snackbar

class SelectedProdukListAdapter(itemList: ArrayList<ProdukSerializable>, private val activity: Activity
                                , private val onTask: KeranjangProdukItemOnTask
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mRecentlyDeletedItemPosition: Int = 0
    private var mRecentlyDeletedItem: ProdukSerializable? = null
    var mItemList: ArrayList<ProdukSerializable>? = null

    fun getContext() : Context {
        return activity.applicationContext
    }
    init {
        setHasStableIds(true)
        mItemList = itemList
        Log.d("sptpdb","${mItemList?.size}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_keranjang_produk, parent, false)
        return SelectedProdukListHolder.newInstance(view, activity)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as SelectedProdukListHolder
        val itemText = mItemList!![position]
        holder.setValues(onTask,itemText,position)
    }

    override fun getItemCount(): Int {
        return if (mItemList == null) 0 else mItemList!!.size
    }

    override fun getItemId(position: Int): Long {
        return mItemList?.get(position)?.idItem?.toLong()!!
    }

    fun deleteItem(position: Int) {
        mRecentlyDeletedItem = mItemList?.get(position)
        mRecentlyDeletedItemPosition = position
        mItemList?.removeAt(position)
        notifyItemRemoved(position)
        onTask.keranjangProdukItemOnTask(-1,0,0)
        showUndoSnackbar()
    }

    private fun showUndoSnackbar() {
        val view = activity.findViewById(R.id.coordinator) as CoordinatorLayout
        val snackbar = Snackbar.make(
            view, R.string.text,
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction(R.string.snack_bar_undo, { v -> undoDelete() })
        snackbar.show()
    }

    private fun undoDelete() {
        mRecentlyDeletedItem?.let {
            mItemList?.add(
                mRecentlyDeletedItemPosition,
                it
            )
            onTask.keranjangProdukItemOnTask(-1,0,0)
            notifyItemInserted(mRecentlyDeletedItemPosition)
        }
    }

//    internal fun setFilter(mItem: List<ProdukSerializable>) {
//        mItemList = ArrayList()
//        mItemList!!.addAll(mItem)
//        notifyDataSetChanged()
//    }
}
