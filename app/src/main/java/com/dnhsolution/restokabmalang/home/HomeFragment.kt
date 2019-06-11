package com.dnhsolution.restokabmalang.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.dnhsolution.restokabmalang.ProdukSerializable
import com.dnhsolution.restokabmalang.Url
import com.dnhsolution.restokabmalang.keranjang.KeranjangActivity
import kotlinx.android.synthetic.main.home_fragment.*
import com.dnhsolution.restokabmalang.R

class HomeFragment:Fragment() {

    private val favoritedBookNamesKey = "favoritedBookNamesKey"
    var produkSerializable: ProdukSerializable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val produkAdapter = ProdukAdapter(context, produks)
        gvMainActivity.adapter = produkAdapter

        gvMainActivity.setOnItemClickListener { parent, _, position, id ->
            val produk = produks[position]
            produk.toggleFavorite()
            produkAdapter.notifyDataSetChanged()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val favoritedProdukNames = ArrayList<Int>()
        for (Produk in produks) {
            if (Produk.isFavorite) {
                favoritedProdukNames.add(Produk.idItem)
            }
        }

        outState.putIntegerArrayList(favoritedBookNamesKey, favoritedProdukNames)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater?.inflate(R.menu.menu_lanjut, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_menu_lanjut -> {

                produkSerializable = ProdukSerializable()
                val arrayProdukSerialization = ArrayList<ProdukSerializable>()
                for (value in produks) {
                    if (value.isFavorite) {
                        arrayProdukSerialization.add(
                            ProdukSerializable(
                                value.idItem, value.name, value.price
                                , value.imageResource, value.imageUrl,value.price.toInt()
                            )
                        )
//                        produkSerializable?.idItem = value.idItem
//                        produkSerializable?.name = value.name
//                        produkSerializable?.price = value.price
//                        produkSerializable?.imgResource = value.imageResource
//                        produkSerializable?.imgUrl = value.imageUrl
                    } else {
                        println("kosong")
                    }
                }

                if (arrayProdukSerialization.size > 0) {

                    val intent = Intent(context, KeranjangActivity::class.java)
                    val args = Bundle()
                    args.putSerializable("ARRAYLIST", arrayProdukSerialization)
                    intent.putExtra("BUNDLE", args)
                    startActivity(intent)
                }
                true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        val favoritedBookNames = savedInstanceState?.getIntegerArrayList(favoritedBookNamesKey)

        // warning: typically you should avoid n^2 loops like this, use a Map instead.
        // I'm keeping this because it is more straightforward
        if (favoritedBookNames != null) {
            for (bookName in favoritedBookNames) {
                for (Produk in produks) {
                    if (Produk.idItem == bookName) {
                        Produk.isFavorite = true
                        break
                    }
                }
            }
        }
    }

    private val produks = arrayOf(
        ProdukElement(
            1, "Judul", "10", R.drawable.abc,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
        ), ProdukElement(
            2, "Judul", "20", R.drawable.areyoumymother,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/areyoumymother.jpg"
        ), ProdukElement(
            3, "Judul", "30", R.drawable.whereisbabysbellybutton,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/whereisbabysbellybutton.jpg"
        ), ProdukElement(
            4, "Judul", "40", R.drawable.onthenightyouwereborn,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/onthenightyouwereborn.jpg"
        ), ProdukElement(
            5, "Judul", "50", R.drawable.handhandfingersthumb,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/handhandfingersthumb.jpg"
        ), ProdukElement(
            6, "Judul", "60", R.drawable.theveryhungrycaterpillar,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/theveryhungrycaterpillar.jpg"
        ), ProdukElement(
            7, "Judul", "70", R.drawable.thegoingtobedbook,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/thegoingtobedbook.jpg"
        ), ProdukElement(
            8, "Judul", "80", R.drawable.ohbabygobaby,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/ohbabygobaby.jpg"
        ), ProdukElement(
            9, "Judul", "90", R.drawable.thetoothbook,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/thetoothbook.jpg"
        ), ProdukElement(
            10, "Judul", "100", R.drawable.onefish,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/onefish.jpg"
        )
    )
}