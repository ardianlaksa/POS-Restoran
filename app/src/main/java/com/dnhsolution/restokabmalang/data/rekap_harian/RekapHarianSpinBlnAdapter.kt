package com.dnhsolution.restokabmalang.data.rekap_harian

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.TextView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes

class RekapHarianSpinBlnAdapter(context: Context, @LayoutRes resource: Int, list: ArrayList<String>)
    : ArrayAdapter<String>(context, resource, list){

    private var resource:Int
    private var mInflater:LayoutInflater? = null
    private var list:ArrayList<String>

    init {
        mInflater = LayoutInflater.from(context)
        this.resource = resource
        this.list = list
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val holder: ViewHolder
        val retView: View?

        if(convertView == null){
            retView = mInflater?.inflate(resource, parent, false)
            holder = ViewHolder()

            if (retView != null) {
                holder.tvItem = retView.findViewById(android.R.id.text1) as TextView
                retView.tag = holder
            }

        } else {
            holder = convertView.tag as ViewHolder
            retView = convertView
        }

        val offerData = list.get(position)

        holder.tvItem?.setText(offerData)

        return retView
    }

    internal class ViewHolder {
        var tvItem: TextView? = null
    }

}