/*
 * Copyright (c) 2016 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dnhsolution.restokabmalang.transaksi.tab_fragment

import android.content.Context
import android.view.ViewGroup
import android.view.LayoutInflater
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency
import com.dnhsolution.restokabmalang.utilities.Url
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.*
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import java.util.*


class ProdukListAdapter(
    private val mContext: Context,
    private val books: ArrayList<ProdukListElement>
) : BaseAdapter(), Filterable {

    private var mItemListSemua: ArrayList<ProdukListElement> = ArrayList(books)
//    private var mItemListFiltered: ArrayList<ProdukListElement>? = null

    override fun getCount(): Int {
        return books.size
    }

    override fun getItem(p0: Int): Any{
        return books[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    fun resetMasterList(allItems: List<ProdukListElement>) {
        mItemListSemua.clear()
        mItemListSemua.addAll(allItems)
    }

    fun appendToMasterList(newItems: List<ProdukListElement>) {
        mItemListSemua.addAll(newItems)
    }

    override fun getView(position:Int, convertView: View?, parent: ViewGroup?): View {
        var cv = convertView
        val book = books[position]

        // view holder pattern
        if (cv == null) {
            val layoutInflater = LayoutInflater.from(mContext)
            cv = layoutInflater.inflate(R.layout.item_produk, parent, false)
            val imageViewCoverArt = cv.findViewById<ImageView>(R.id.imageview_cover_art)
            val nameTextView = cv.findViewById<TextView>(R.id.textview_book_name)
            val authorTextView = cv.findViewById<TextView>(R.id.textview_book_author)
            val imageViewFavorite = cv.findViewById<ImageView>(R.id.imageview_favorite)
            val descriptionTextView = cv.findViewById<TextView>(R.id.tvDescription)
            val viewHolder: ViewHolder = ViewHolder(
                nameTextView,
                authorTextView,
                imageViewCoverArt,
                imageViewFavorite,
                descriptionTextView
            )
            cv.tag = viewHolder
        }
        val viewHolder = cv?.tag as ViewHolder
        //    viewHolder.imageViewCoverArt.setImageResource(book.getImageResource());
        viewHolder.nameTextView.text = book.name
        val priceValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(book.price.toDouble())
        viewHolder.authorTextView.text = priceValue
        viewHolder.imageViewFavorite.setImageResource(if (book.isFavorite) R.drawable.ic_baseline_check_circle_24_dark_green else R.drawable.ic_baseline_check_circle_24_gray)
        viewHolder.descriptionTextView.text = book.description
        val url = Url.serverFoto + book.imageUrl
//        Glide.with(viewHolder.imageViewCoverArt.context).load(url)
//            .override(512, 512)
//            .centerCrop()
//            .diskCacheStrategy(DiskCacheStrategy.DATA)
//            .fitCenter()
//            .listener(object : RequestListener<Drawable?> {
//                override fun onLoadFailed(
//                    e: GlideException?,
//                    model: Any,
//                    target: Target<Drawable?>,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    Log.e("onLoadFailed", "Error $e")
//                    //                viewHolder.imageViewCoverArt.setImageDrawable(mContext.getResources().getDrawable(R.drawable.img_no_image));
//                    return false
//                }
//
//                override fun onResourceReady(
//                    resource: Drawable?,
//                    model: Any,
//                    target: Target<Drawable?>,
//                    dataSource: DataSource,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    Log.e("onResourceReady", "no Error ")
//                    return false
//                }
//            })
//            .into(viewHolder.imageViewCoverArt)
        //    Log.i("url : ", url);
//    Picasso.get().load(url).fit().centerCrop().into(viewHolder.imageViewCoverArt);
        //Picasso.get().load(url).into(viewHolder.imageViewCoverArt);
        return cv
    }

    private inner class ViewHolder(
        val nameTextView: TextView,
        val authorTextView: TextView,
        val imageViewCoverArt: ImageView,
        val imageViewFavorite: ImageView,
        val descriptionTextView: TextView
    )

    override fun getFilter(): Filter {
        return dataPencarian
    }

    private val dataPencarian: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<ProdukListElement> = ArrayList()
            if (constraint.isEmpty()) {
                filteredList.addAll(mItemListSemua)
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                for (item in mItemListSemua) {
//                    val wpNm = item.value1 + " " + item.value4 + " " + item.value5 + " " + item.value7
                    val wpNmAlmtKelKec = "${item.name} ${item.description}"
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
            books.clear()
            val valuesResult = results.values as List<ProdukListElement>
            books.addAll(valuesResult)
            notifyDataSetChanged()
        }
    }
}