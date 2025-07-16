package com.dnhsolution.restokabmalang.transaksihiburan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.R
import java.text.NumberFormat
import java.util.Locale

class TransaksiAdapter(private val itemList: List<ProdukModel>,
                       private val listener: OnQtyChangedListener) :
    RecyclerView.Adapter<TransaksiAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val constraintLayout: ConstraintLayout = view.findViewById(R.id.cl1)
        val tvJudul: TextView = view.findViewById(R.id.tvJudul)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        var tvQty: TextView = view.findViewById(R.id.tvJumlahProduk)
        var tvTotal: TextView = view.findViewById(R.id.tvTotalPrice)
        val bMinus: ImageButton = view.findViewById(R.id.bMinus)
        val bPlus: ImageButton = view.findViewById(R.id.bPlus)
        val vBorder: View = view.findViewById(R.id.vBorderBottom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_keranjang_produk, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.tvJudul.text = item.nama
        holder.tvPrice.text = formatRibuan(item.nominal)
        holder.tvQty.text = item.qty.toString()
        holder.tvTotal.text = formatRibuan((item.qty*item.nominal))

        holder.bPlus.setOnClickListener {
            item.qty++
            notifyItemChanged(position)
            listener.onQtyUpdated()
        }

        holder.bMinus.setOnClickListener {
            if (item.qty > 0) {
                item.qty--
                if (item.qty == 0) {
                    holder.constraintLayout.visibility = View.GONE
                    holder.vBorder.visibility = View.GONE
                    item.ischecked = false
                } else {
                    notifyItemChanged(position)
                }
                listener.onQtyUpdated()
            }
        }

        if (item.ischecked==true){
            holder.constraintLayout.visibility = View.VISIBLE
            holder.vBorder.visibility = View.VISIBLE
        }else{
            holder.constraintLayout.visibility = View.GONE
            holder.vBorder.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun formatRibuan(angka: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return formatter.format(angka)
    }
}