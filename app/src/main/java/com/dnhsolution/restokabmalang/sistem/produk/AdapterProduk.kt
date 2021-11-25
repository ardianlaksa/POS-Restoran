package com.dnhsolution.restokabmalang.sistem.produk

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.dnhsolution.restokabmalang.R
import android.view.ViewGroup
import android.view.LayoutInflater
import com.dnhsolution.restokabmalang.utilities.Url
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListElement
import com.dnhsolution.restokabmalang.utilities.HapusProdukMasterOnTask
import com.dnhsolution.restokabmalang.utilities.Utils
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by sawrusdino on 09/04/2018.
 */

class AdapterProduk(private val itemProdukList: MutableList<ItemProduk>
    , private val itemProdukListNotFiltered: MutableList<ItemProduk>, private val context: Context
    , private val onTask: HapusProdukMasterOnTask) :
    RecyclerView.Adapter<AdapterProduk.MyViewHolder>(), Filterable{

    private var mItemListFull: ArrayList<ItemProduk> = ArrayList(itemProdukList)
    private var mItemListAll: ArrayList<ItemProduk> = ArrayList(itemProdukListNotFiltered)

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val holderView = view
        var nama: TextView = view.findViewById<View>(R.id.tvNama) as TextView
        var ivFoto: ImageView = view.findViewById<View>(R.id.ivBerkas) as ImageView
        var ivIcSync: ImageView = view.findViewById<View>(R.id.ivIcSync) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_produk, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        setValue(holder,position)
    }

    private fun setValue(holder: MyViewHolder,posisi:Int){
        val itemProduk = itemProdukList[posisi]
        holder.ivIcSync.setImageResource(if (itemProduk.status) R.drawable.ic_baseline_sync_alt_24_green else R.drawable.ic_baseline_sync_24_yellow)

//        holder.nama.setText(itemProduk.getNama_barang()+" "+itemProduk.getIsPajak()+" "+itemProduk.getJenisProduk());
        val a = itemProduk.nama_barang
        holder.nama.text = a
        val url = Url.serverFoto + itemProduk.url_image
        Glide.with(holder.ivFoto.context).load(url)
            .override(512, 512)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .centerCrop()
            .fitCenter()
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("xmx1", "Error $e")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("xmx1", "no Error ")
                    return false
                }
            })
            .into(holder.ivFoto)

        holder.holderView.setOnClickListener {
            if(Utils.isOpenRecently()) {
                holder.holderView.isEnabled = false
                return@setOnClickListener
            }
            holder.holderView.isEnabled = true
            onTask.hapusProdukMasterOnTask("1",posisi)
        }

        holder.holderView.setOnLongClickListener {
            onTask.hapusProdukMasterOnTask("2",posisi)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return itemProdukList.size
    }

    override fun getFilter(): Filter {
        return dataPencarian
    }

    private val dataPencarian: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<ItemProduk> = ArrayList()
            if (constraint.isEmpty()) {
                filteredList.addAll(mItemListFull)
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                print("a : $filterPattern ${itemProdukListNotFiltered.size} ")
                for (item in mItemListAll) {
//                    val wpNm = item.value1 + " " + item.value4 + " " + item.value5 + " " + item.value7
                    val wpNmAlmtKelKec = "${item.nama_barang} ${item.keterangan}"
                    if (wpNmAlmtKelKec.lowercase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            itemProdukList.clear()
            val valuesResult = results.values as List<ItemProduk>
            itemProdukList.addAll(valuesResult)
            notifyDataSetChanged()
        }
    }
}