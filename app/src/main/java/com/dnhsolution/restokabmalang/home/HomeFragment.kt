package com.dnhsolution.restokabmalang.home

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dnhsolution.restokabmalang.ProdukOnTask
import com.dnhsolution.restokabmalang.keranjang.ProdukSerializable
import com.dnhsolution.restokabmalang.utilities.Url
import com.dnhsolution.restokabmalang.keranjang.KeranjangActivity
import kotlinx.android.synthetic.main.home_fragment.*
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.utilities.CheckNetwork

class HomeFragment:Fragment(), ProdukOnTask {

    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private val favoritedBookNamesKey = "favoritedBookNamesKey"
    var produkSerializable: ProdukSerializable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        if(CheckNetwork().checkingNetwork(context!!)) {
            val stringUrl = "${Url.getProduk}?idTmpUsaha=1"
            Log.i(_tag,stringUrl)
            jsonTask = ProdukJsonTask(this).execute(stringUrl)
        } else {
            Toast.makeText(context, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
        }

        val produkAdapter = ProdukAdapter(context, produks)
        gvMainActivity.adapter = produkAdapter

        gvMainActivity.setOnItemClickListener { parent, _, position, id ->
            val produk = produks[position]
            produk.toggleFavorite()
            produkAdapter.notifyDataSetChanged()
        }
    }

    override fun produkOnTask(result: String?) {
        if (result == null) {
            Toast.makeText(context,R.string.error_get_data,Toast.LENGTH_SHORT).show()
            return
        } else if (result == "") {
            Toast.makeText(context,R.string.empty_data,Toast.LENGTH_SHORT).show()
            return
        }

        Log.e("Debug", "Response from url:$result")
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
                                , value.imageResource, value.imageUrl,value.price.toInt(), 1
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
            1, "Judul", "10", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
        ,"Diskripsi"), ProdukElement(
            2, "Judul", "20", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
            ,"Diskripsi"), ProdukElement(
            3, "Judul", "30", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
            ,"Diskripsi"), ProdukElement(
            4, "Judul", "40", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
            ,"Diskripsi"), ProdukElement(
            5, "Judul", "50", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
            ,"Diskripsi"), ProdukElement(
            6, "Judul", "60", R.drawable.img_food_example,
            "${Url.serverPdrd}IMG_20190516_163229_1994000784056139154.jpg"
            ,"Diskripsi"), ProdukElement(
            7, "Judul", "70", R.drawable.img_food_example,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/thegoingtobedbook.jpg"
            ,"Diskripsi"), ProdukElement(
            8, "Judul", "80", R.drawable.img_food_example,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/ohbabygobaby.jpg"
            ,"Diskripsi"), ProdukElement(
            9, "Judul", "90", R.drawable.img_food_example,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/thetoothbook.jpg"
            ,"Diskripsi"), ProdukElement(
            10, "Judul", "100", R.drawable.img_food_example,
            "http://www.raywenderlich.com/wp-content/uploads/2016/03/onefish.jpg"
            ,"Diskripsi")
    )
}