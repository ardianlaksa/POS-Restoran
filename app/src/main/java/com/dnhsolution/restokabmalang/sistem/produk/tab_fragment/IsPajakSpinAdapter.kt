package com.dnhsolution.restokabmalang.sistem.produk.tab_fragment

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.TextView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import com.dnhsolution.restokabmalang.R

class IsPajakSpinAdapter(context: Context, @LayoutRes resource: Int, list: ArrayList<IsPajakListElement>)
    : ArrayAdapter<IsPajakListElement>(context, resource, list){

    private var resource:Int
    private var mInflater:LayoutInflater? = null
    private var list:ArrayList<IsPajakListElement>

    init {
        mInflater = LayoutInflater.from(context)
        this.resource = resource
        this.list = list
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createItemView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val retView: View?

        if(convertView == null){
            retView = mInflater?.inflate(resource, parent, false)
            holder = ViewHolder()
//             holder.tvItem = retView.findViewById(android.R.id.text1) as TextView
            holder.mId = retView?.findViewById(R.id.mId)
            holder.tvItem = retView?.findViewById(R.id.tvItem)
            retView?.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            retView = convertView
        }

        val offerData = list[position]

        holder.mId?.text = offerData.idItem
        holder.tvItem?.text = offerData.value

        return retView!!
    }

    internal class ViewHolder {
//        var tvItem: TextView? = null
        var mId: TextView? = null
        var tvItem: TextView? = null
    }

}