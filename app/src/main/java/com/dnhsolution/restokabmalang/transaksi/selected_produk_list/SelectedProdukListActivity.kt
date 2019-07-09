package com.dnhsolution.restokabmalang.transaksi.selected_produk_list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.dnhsolution.restokabmalang.utilities.KeranjangProdukItemOnTask
import com.dnhsolution.restokabmalang.utilities.KeranjangTransaksiOnTask
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.cetak.MainCetak
import com.dnhsolution.restokabmalang.transaksi.*
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.Url
import kotlinx.android.synthetic.main.activity_keranjang.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SelectedProdukListActivity:AppCompatActivity(), KeranjangProdukItemOnTask
    ,View.OnClickListener, KeranjangTransaksiOnTask {

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bProses -> {
//                val valueDiskon = valueDiskon
//                if ( valueDiskon == "") return
//                else showDialog("Konfirmasi","Apakan anda ingin memproses?")
                showDialog("Konfirmasi","Apakan anda ingin memproses?")
            }
        }
    }

    private var produkAdapter: SelectedProdukListAdapter? = null
    private val _tag: String = javaClass.simpleName
    private var idPengguna: String? = null
    private var idTmpUsaha: String? = null
    private var valueDiskon: Int = 0
    private var valueTotalPrice: Int = 0
    private var obyek:ArrayList<ProdukSerializable>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keranjang)
        setSupportActionBar(toolbar)

        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
        idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "")

        bProses.setOnClickListener(this)

//        val i = intent
//        val args = i.getBundleExtra("BUNDLE")
//        obyek = args.getParcelableArrayList("ARRAYLIST")

//        val name = obyek.get(1).name

//        val produkAdapter = SelectedProdukListAdapter(obyek, this,this)
//        setUpRecyclerView(produkAdapter)

//        recyclerView.adapter = produkAdapter
//        recyclerView?.layoutManager = (LinearLayoutManager(this))

//        setTotal()

        etDiskon.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            private var current = ""

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "") return
                if (s.toString() != current)
                    etDiskon.removeTextChangedListener(this)

                valueDiskon = s.toString().toInt()
                setTotal()

                etDiskon.addTextChangedListener(this)
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val i = intent
        val args = i.getBundleExtra("BUNDLE")
        val obyekProduk:ArrayList<ProdukSerializable> = args.getParcelableArrayList("ARRAYLIST")
        if (obyek == null) {
            Log.i(_tag, "onResume")
            obyek = obyekProduk
        }

        // Loop arrayList2 items
        for (item2 in obyekProduk) {
            // Loop arrayList1 items
            var found = false
            for (item1 in obyek!!) {
                if (item2.idItem == item1.idItem) {
                    found = true
                }
            }
            if (!found) {
                obyek!!.add(item2)
            }
        }

        if (produkAdapter == null) {
            produkAdapter = SelectedProdukListAdapter(
                obyek!!,
                this,
                this
            )
            setUpRecyclerView(produkAdapter!!)
        } else {
            produkAdapter!!.notifyDataSetChanged()
        }

        setTotal()
    }

    private fun setUpRecyclerView(mAdapter: SelectedProdukListAdapter) {
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        val itemTouchHelper = ItemTouchHelper(
            SwipeToDeleteCallback(
                mAdapter
            )
        )
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun keranjangProdukItemOnTask(position:Int, totalPrice: Int, qty: Int) {
        if (position > -1) {
            obyek!![position].totalPrice = totalPrice
            obyek!![position].qty = qty
        }
        setTotal()
    }

    private fun setTotal () {
        var totalPrice = 0
        for (valueTotal in obyek!!) {
            totalPrice += valueTotal.totalPrice
        }
        tvTotal.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(totalPrice.toDouble())

        var diskon = 0
//        if (!etDiskon.text.toString().isEmpty()) diskon = etDiskon.text.toString().toInt()
        if (!etDiskon.text.toString().isEmpty()) diskon = valueDiskon
        this.valueTotalPrice = totalPrice-(totalPrice*diskon/100)
        val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
        tvDiskonTotal.text = rupiahValue
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        super.onCreateOptionsMenu(menu)
//        // Inflate the menu; this adds items to the action bar if it is present.
//        inflater?.inflate(R.menu.menu_lanjut, menu)
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_tambah, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_menu_tambah -> {

                val favoritedProdukNames = ArrayList<Int>()
                for (Produk in obyek!!) {
                    favoritedProdukNames.add(Produk.idItem)
                }

                val i = Intent(this, TambahProdukActivity::class.java)
                i.putExtra("ARRAYLIST", favoritedProdukNames)
                startActivity(i)
                true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    fun showDialog(title:String,message:String){
        val builder = AlertDialog.Builder(this)

        // Set the alert dialog title
        builder.setTitle(title)

        // Display a message on alert dialog
        builder.setMessage(message)
            builder.setPositiveButton("Lanjut"){_, _ ->
//                println(createJson())
                if(CheckNetwork().checkingNetwork(this)) {
                    val params = HashMap<String, String>()
                    params.put("paramsArray",createJson())
                    SelectedProdukListJsonTask(
                        this,
                        params
                    ).execute(Url.setKeranjangTransaksi)
                    startActivity(Intent(this@SelectedProdukListActivity, MainCetak::class.java))
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
                }
            }

        builder.setNegativeButton("Batal"){dialog, _ ->
            // Do something when user press the positive button
            dialog.dismiss()
        }


        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
    }

    fun createJson() : String{
        val rootObject= JSONObject()
        rootObject.put("idTmptUsaha",idTmpUsaha)
        rootObject.put("user",idPengguna)
        rootObject.put("disc",valueDiskon)
        rootObject.put("omzet",valueTotalPrice)

        val jsonArr = JSONArray()

        for (pn in obyek!!) {
            val pnObj = JSONObject()
            pnObj.put("idProduk", pn.idItem)
            pnObj.put("nmProduk", pn.name)
            pnObj.put("nmProduk", pn.name)
            pnObj.put("qty", pn.qty)
            pnObj.put("hrgProduk", pn.price)
            jsonArr.put(pnObj)
        }
        rootObject.put("produk",jsonArr)

        return rootObject.toString()
    }

    override fun keranjangTransaksiOnTask(result: String?) {
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
            if (success == 1)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (ex : JSONException) {
            ex.printStackTrace()
            Toast.makeText(this, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }
}