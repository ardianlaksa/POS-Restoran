package com.dnhsolution.restokabmalang.transaksi.produk_list

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dnhsolution.restokabmalang.utilities.ProdukOnTask
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable
import com.dnhsolution.restokabmalang.utilities.Url
import com.dnhsolution.restokabmalang.transaksi.selected_produk_list.SelectedProdukListActivity
import kotlinx.android.synthetic.main.fragment_produk_list.*
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import org.json.JSONException
import org.json.JSONObject

class ProdukListFragment:Fragment(), ProdukOnTask {

    private var valueArgsFromKeranjang: Int? = null
    private var produkAdapter: ProdukListAdapter? = null
    private var idTmpUsaha: String = "0"

    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private val favoritedBookNamesKey = "favoritedBookNamesKey"
    var produkSerializable: ProdukSerializable? = null
    private var produks:ArrayList<ProdukListElement> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_produk_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        Log.i(_tag,"onCreate")

        val sharedPreferences = context?.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences?.getString(Url.SESSION_ID_TEMPAT_USAHA, "-1").toString()

        if(CheckNetwork().checkingNetwork(context!!)) {
            val stringUrl = "${Url.getProduk}?idTmpUsaha=$idTmpUsaha"
            Log.i(_tag,stringUrl)
            jsonTask = ProdukListJsonTask(this).execute(stringUrl)
        } else {
            Toast.makeText(context, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
        }

        gvMainActivity.setOnItemClickListener { _, _, position, _ ->
            val produk = produks[position]
            produk.toggleFavorite()
            produkAdapter?.notifyDataSetChanged()
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
        try {
            val jsonObj = JSONObject(result)
            val success = jsonObj.getInt("success")
            val message = jsonObj.getString("message")
            if (success == 1) {

                val rArray = jsonObj.getJSONArray("result")
                for (i in 0 until rArray.length()) {

                    val idBarang = rArray.getJSONObject(i).getInt("ID_BARANG")
                    val harga = rArray.getJSONObject(i).getString("HARGA")
                    val foto = rArray.getJSONObject(i).getString("FOTO")
                    val nmBarang = rArray.getJSONObject(i).getString("NM_BARANG")
                    val keterangan = rArray.getJSONObject(i).getString("KETERANGAN")

                    produks.add(
                        ProdukListElement(
                            idBarang,nmBarang, harga, foto, keterangan )
                    )
                }

                if (produkAdapter != null) produkAdapter?.notifyDataSetChanged()
                else produkAdapter =
                    ProdukListAdapter(context, produks)

                if (gvMainActivity == null) return
                gvMainActivity.adapter = produkAdapter

            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_lanjut, menu)
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
                                , value.imageUrl,value.price.toInt(), 1
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

                    val intent = Intent(context, SelectedProdukListActivity::class.java)
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
}