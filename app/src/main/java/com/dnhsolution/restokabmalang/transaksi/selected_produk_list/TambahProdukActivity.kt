package com.dnhsolution.restokabmalang.transaksi.selected_produk_list

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dnhsolution.restokabmalang.utilities.ProdukOnTask
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.sistem.produk.ItemProduk
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListAdapter
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListElement
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListJsonTask
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.Url
import kotlinx.android.synthetic.main.activity_produk.*
import kotlinx.android.synthetic.main.fragment_produk_list.gvMainActivity
import org.json.JSONException
import org.json.JSONObject

class TambahProdukActivity:AppCompatActivity(), ProdukOnTask {

    private var valueArgsFromKeranjang: ArrayList<Int>? = null
    private var produkAdapter: ProdukListAdapter? = null
    private var idTmpUsaha: String = "0"

    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private var produks:ArrayList<ProdukListElement> = ArrayList()
    var databaseHandler: DatabaseHandler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produk)

        val i = intent
        valueArgsFromKeranjang = i.getIntegerArrayListExtra("ARRAYLIST")
        Log.i(_tag,"onResume $valueArgsFromKeranjang")

        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, MODE_PRIVATE)
        idTmpUsaha = sharedPreferences?.getString(Url.SESSION_ID_TEMPAT_USAHA, "0").toString()

        databaseHandler = DatabaseHandler(this)

        if(CheckNetwork().checkingNetwork(this)) {
            val stringUrl = "${Url.getProduk}?idTmpUsaha=$idTmpUsaha"
            Log.i(_tag,stringUrl)
            jsonTask = ProdukListJsonTask(this).execute(stringUrl)
        } else {
            getDataLokal()
            //Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
        }

        gvMainActivity.setOnItemClickListener { _, _, position, _ ->
            val produk = produks[position]
            produk.toggleFavorite()
            produkAdapter?.notifyDataSetChanged()
        }

        bTambah.setOnClickListener {
            val arrayProdukSerialization = ArrayList<ProdukSerializable>()
            for (value in produks) {
                if (value.isFavorite) {
                    arrayProdukSerialization.add(
                        ProdukSerializable(
                            value.idItem, value.name, value.price
                            , value.imageUrl, value.price.toInt(), 1, value.status
                        )
                    )
                } else {
                    println("kosong")
                }
            }

            if (arrayProdukSerialization.size > 0) {
                val intent = Intent(this, SelectedProdukListActivity::class.java)
                val args = Bundle()
                args.putSerializable("ARRAYLIST", arrayProdukSerialization)
                intent.putExtra("BUNDLE", args)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
                finish()
            }
        }
    }

    override fun produkOnTask(result: String?) {
        if (result == null) {
            Toast.makeText(this,R.string.error_get_data,Toast.LENGTH_SHORT).show()
            return
        } else if (result == "") {
            Toast.makeText(this,R.string.empty_data,Toast.LENGTH_SHORT).show()
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
                            idBarang,nmBarang, harga, foto, keterangan,"server" )
                    )
                }

                if (produkAdapter != null) produkAdapter?.notifyDataSetChanged()
                else produkAdapter =
                    ProdukListAdapter(this, produks)

                if (gvMainActivity == null) return
                gvMainActivity.adapter = produkAdapter

                if (valueArgsFromKeranjang != null) {
                    for (favoriteId in valueArgsFromKeranjang!!) {
                        for (Produk in produks) {
                            if (Produk.idItem == favoriteId) {
                                Produk.isFavorite = true
                                break
                            }
                        }
                    }
                }

            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDataLokal() {
        produks.clear()
        produkAdapter?.notifyDataSetChanged()

        val jml_data = databaseHandler!!.CountDataProduk()
        if (jml_data == 0) {
            Toast.makeText(this,R.string.empty_data,Toast.LENGTH_SHORT).show()
        }

        val listProduk: List<ItemProduk> = databaseHandler!!.dataProduk2

        for(e in listProduk){
            val idBarang = e.id_barang.toInt()
            val harga = e.harga
            val foto = e.url_image
            val nmBarang = e.nama_barang
            val keterangan = e.keterangan

            produks.add(
                ProdukListElement(
                    idBarang,nmBarang, harga, foto, keterangan, "lokal" )
            )
        }

        if (produkAdapter != null) produkAdapter?.notifyDataSetChanged()
        else produkAdapter =
            ProdukListAdapter(this, produks)

        if (gvMainActivity == null) return
        gvMainActivity.adapter = produkAdapter

        if (valueArgsFromKeranjang != null) {
            for (favoriteId in valueArgsFromKeranjang!!) {
                for (Produk in produks) {
                    if (Produk.idItem == favoriteId) {
                        Produk.isFavorite = true
                        break
                    }
                }
            }
        }
    }

//    override fun onBackPressed() {
//        val arrayProdukSerialization = ArrayList<ProdukSerializable>()
//        for (value in produks) {
//            if (value.isFavorite) {
//                arrayProdukSerialization.add(
//                    ProdukSerializable(
//                        value.idItem, value.name, value.price
//                        , value.imageUrl,value.price.toInt(), 1
//                    )
//                )
//            } else {
//                println("kosong")
//            }
//        }
//
//        if (arrayProdukSerialization.size > 0) {
//            val intent = Intent(this, SelectedProdukListActivity::class.java)
//            val args = Bundle()
//            args.putSerializable("ARRAYLIST", arrayProdukSerialization)
//            intent.putExtra("BUNDLE", args)
//            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//            startActivity(intent)
//            finish()
//        }
//        super.onBackPressed()
//    }
}