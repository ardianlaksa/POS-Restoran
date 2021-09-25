package com.dnhsolution.restokabmalang.transaksi.selected_produk_list

import android.content.Context
import android.content.Intent
import android.database.SQLException
import android.os.Build
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
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.cetak.MainCetak
import com.dnhsolution.restokabmalang.cetak.MainCetakLokal
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.database.ItemDetailTransaksi
import com.dnhsolution.restokabmalang.database.ItemTransaksi
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable
import com.dnhsolution.restokabmalang.utilities.*
import kotlinx.android.synthetic.main.activity_keranjang.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SelectedProdukListActivity:AppCompatActivity(), KeranjangProdukItemOnTask
    ,View.OnClickListener, KeranjangTransaksiOnTask {

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bProses -> {
//                val valueDiskon = valueDiskon
////                if ( valueDiskon == "") return
////                else showDialog("Konfirmasi","Apakan anda ingin memproses?")
                showDialog("Konfirmasi","Apakan anda ingin memproses?")
            }
        }
    }

    private var produkAdapter: SelectedProdukListAdapter? = null
    private val _tag: String = javaClass.simpleName
    private var idPengguna: String? = null
    private var idTmpUsaha: String? = null
    private var valueDiskon: Int = 0
    private var valueDiskonRupiah: Int = 0
    private var valueDR: Double = 0.0
    private var valueTotalPrice: Int = 0
    private var obyek:ArrayList<ProdukSerializable>? = null
    var databaseHandler: DatabaseHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keranjang)

        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
        idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "")

        databaseHandler = DatabaseHandler(this)
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

//        etDiskon.addTextChangedListener(object : TextWatcher {
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            private var current = ""
//
//            override fun afterTextChanged(s: Editable?) {
//                Toast.makeText(this@SelectedProdukListActivity, s.toString(), Toast.LENGTH_SHORT).show()
////                if (s.toString() == "") return
////                if (s.toString() != current)
////                    etDiskon.removeTextChangedListener(this)
////
////                if(s.toString() == ""){
////                    valueDiskon = 0
////                }else{
////                    valueDiskon = s.toString().toInt()
////                }
////                setTotal()
//
//                etDiskon.addTextChangedListener(this)
//            }
//        })

        etDiskon.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                etDiskon.requestFocus()

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {

                if (etDiskon.getText().toString().length == 0) {
                    valueDiskon = 0
                } else {
                    valueDiskon = editable.toString().toInt()
                }

                setTotal()

            }
        })

        etRupiahDiskon.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                etRupiahDiskon.requestFocus()

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {

                etRupiahDiskon.removeTextChangedListener(this)

                try {
                    var originalString = editable.toString()

                    val longval: Long?
                    if (originalString.contains(".")) {

                        originalString = originalString.replace(".", "")
                    }
                    longval = java.lang.Long.parseLong(originalString)

                    val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
                    formatter.applyPattern("#,###,###,###")
                    val formattedString = formatter.format(longval)

                    //setting text after format to EditText
                    etRupiahDiskon.setText(formattedString.replace(",", "."))
                    etRupiahDiskon.setSelection(etRupiahDiskon.getText().toString().length)
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }


                etRupiahDiskon.addTextChangedListener(this)
                if (etRupiahDiskon.text.toString().isEmpty()) {
                    valueDiskonRupiah = 0
                } else {

                    valueDiskonRupiah = editable.toString().replace(".", "").toInt()
                }

                setTotalRupiah()

            }
        })

//        etRupiahDiskon.addTextChangedListener(object : TextWatcher {
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            private var current = ""
//
//            override fun afterTextChanged(s: Editable?) {
//
//                Toast.makeText(this@SelectedProdukListActivity, s.toString(), Toast.LENGTH_SHORT).show()
////                if (s.toString() == "") return
////                if (s.toString() != current)
////                    etRupiahDiskon.removeTextChangedListener(this)
////
////                if(s.toString() == ""){
////                    valueDiskonRupiah = 0
////                }else{
////                    valueDiskonRupiah = s.toString().toInt()
////                }
////                setTotalRupiah()
//
//                etRupiahDiskon.addTextChangedListener(this)
//            }
//        })

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val i = intent
        val args = i.getBundleExtra("BUNDLE")
        val obyekProduk:ArrayList<ProdukSerializable>? = args?.getParcelableArrayList("ARRAYLIST")
        if (obyek == null) {
            Log.i(_tag, "onResume")
            obyek = obyekProduk
        }

        // Loop arrayList2 items
        if (obyekProduk != null) {
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
        tvSubtotal.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(totalPrice.toDouble())

        var diskon = 0
//        if (!etDiskon.text.toString().isEmpty()) diskon = etDiskon.text.toString().toInt()
        if (!etDiskon.text.toString().isEmpty()) {
            diskon = valueDiskon
            this.valueTotalPrice = totalPrice-(totalPrice*diskon/100)
            val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
            tvDiskonTotal.text = rupiahValue

            if(!etRupiahDiskon.text.toString().isEmpty()){
                this.valueTotalPrice = totalPrice
                val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
                tvDiskonTotal.text = rupiahValue

                valueDiskonRupiah = etRupiahDiskon.text.toString().toInt()
                setTotalRupiah()
            }

        }else{
            this.valueTotalPrice = totalPrice
            val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
            tvDiskonTotal.text = rupiahValue

            if(!etRupiahDiskon.text.toString().isEmpty()){
                valueDiskonRupiah = etRupiahDiskon.text.toString().toInt()
                setTotalRupiah()
            }
        }


    }

    private fun setTotalRupiah () {
        var totalPrice = 0
        for (valueTotal in obyek!!) {
            totalPrice += valueTotal.totalPrice
        }
        tvSubtotal.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(totalPrice.toDouble())

        var diskonRupiah = 0
//        if (!etDiskon.text.toString().isEmpty()) diskon = etDiskon.text.toString().toInt()
        if (!etRupiahDiskon.text.toString().isEmpty()) {
            if(valueDiskonRupiah <= totalPrice){
                diskonRupiah = valueDiskonRupiah
                this.valueTotalPrice = totalPrice-diskonRupiah
                valueDR = ((diskonRupiah*100)/totalPrice).toDouble()
                val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
                tvDiskonTotal.text = rupiahValue
            }else{
                val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(totalPrice.toDouble())
                tvDiskonTotal.text = rupiahValue
            }

        }else{
            this.valueTotalPrice = totalPrice
            valueDR = 0.0

            val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
            tvDiskonTotal.text = rupiahValue

            if(!etDiskon.text.toString().isEmpty()){
                valueDiskon = etDiskon.text.toString().toInt()
                setTotal()
            }
        }

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
                    params["paramsArray"] = createJson()
                    SelectedProdukListJsonTask(
                        this,
                        params
                    ).execute(Url.setKeranjangTransaksi)

                    showDialogBerhasil("Selamat","Transaksi Anda Berhasil di proses !")

                } else {
                    var idTrx : String = saveLokal()
                    showDialogBerhasilLokal("Selamat","Transaksi Anda Berhasil di proses !", idTrx)
                    //Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
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
        dialog.setCanceledOnTouchOutside(false)
    }

    fun showDialogBerhasil(title:String,message:String){
        val builder = AlertDialog.Builder(this)

        // Set the alert dialog title
        builder.setTitle(title)

        // Display a message on alert dialog
        builder.setMessage(message)
        builder.setPositiveButton("Cetak"){_, _ ->
            //                println(createJson())
            startActivity(Intent(this@SelectedProdukListActivity, MainCetak::class.java))
            finish()
        }

        builder.setNegativeButton("Batal"){_, _ ->
            val i = Intent(Intent(this@SelectedProdukListActivity, MainActivity::class.java))
            startActivity(i)
            finishAffinity()
        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    fun showDialogBerhasilLokal(title:String,message:String, idTrx:String){
        val builder = AlertDialog.Builder(this)

        // Set the alert dialog title
        builder.setTitle(title)

        // Display a message on alert dialog
        builder.setMessage(message)
        builder.setPositiveButton("Cetak"){_, _ ->
            //                println(createJson())
            //        String tema = sharedPreferences.getString(Url.setTema, "0");
//
//        if(tema.equalsIgnoreCase("0")){
//            MainCetak.this.setTheme(R.style.Theme_First);
//        }else if(tema.equalsIgnoreCase("1")){
//            MainCetak.this.setTheme(R.style.Theme_Second);
//        }else if(tema.equalsIgnoreCase("2")){
//            MainCetak.this.setTheme(R.style.Theme_Third);
//        }else if(tema.equalsIgnoreCase("3")){
//            MainCetak.this.setTheme(R.style.Theme_Fourth);
//        }else if(tema.equalsIgnoreCase("4")){
//            MainCetak.this.setTheme(R.style.Theme_Fifth);
//        }else if(tema.equalsIgnoreCase("5")){
//            MainCetak.this.setTheme(R.style.Theme_Sixth);
//        }
            val i = Intent(Intent(this@SelectedProdukListActivity, MainCetakLokal::class.java))
            i.putExtra("idTrx", idTrx)
            startActivity(i)
            finish()
        }

        builder.setNegativeButton("Batal"){_, _ ->
            val i = Intent(Intent(this@SelectedProdukListActivity, MainActivity::class.java))
            startActivity(i)
            finish()

        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun createJson() : String{
        val rootObject= JSONObject()
        rootObject.put("idTmptUsaha",idTmpUsaha)
        rootObject.put("user",idPengguna)
        rootObject.put("disc_rp",valueDiskonRupiah)
        if(!etRupiahDiskon.text.toString().isEmpty()){
            rootObject.put("disc",valueDR)
        }else{
            rootObject.put("disc",valueDiskon)
        }
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

            Log.d("NAMA_BARANG", pn.name.toString())
        }
        rootObject.put("produk",jsonArr)


        return rootObject.toString()
    }

    fun saveLokal() : String{
        var tglTrx :String = ""
        var disc : String = ""
        var omzet : String = ""
        var id_trx : Int = 0

        if(!etRupiahDiskon.text.toString().isEmpty()){
            disc = valueDR.toString()
        }else{
            disc = valueDiskon.toString()
        }
        omzet = valueTotalPrice.toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            tglTrx = current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            tglTrx = formatter.format(date)
        }

        try {
            databaseHandler!!.insert_transaksi(
                ItemTransaksi(
                    0, tglTrx, disc, omzet, idPengguna, idTmpUsaha, valueDiskonRupiah.toString(), "0"
                )
            )

            id_trx = databaseHandler!!.CountMaxIdTrx()

            for (pn in obyek!!) {
                Log.d("detail_transaksi", pn.idItem.toString()+"/"+pn.name+"/"+pn.qty.toString()+"/"+pn.price)
                databaseHandler!!.insert_detail_transaksi(
                    ItemDetailTransaksi(
                        0, id_trx.toString(), pn.idItem.toString(), pn.name, pn.qty.toString(),  pn.price
                    )
                )
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return id_trx.toString()
    }

//    private fun saveLokal() {
//
//        var tglTrx :String = ""
//        var disc : String = ""
//        var omzet : String = ""
//
//        if(!etRupiahDiskon.text.toString().isEmpty()){
//            disc = valueDR.toString()
//        }else{
//            disc = valueDiskon.toString()
//        }
//        omzet = valueTotalPrice.toString()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val current = LocalDateTime.now()
//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//            tglTrx = current.format(formatter)
//        } else {
//            var date = Date()
//            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//            tglTrx = formatter.format(date)
//        }
//
//        try {
//            databaseHandler!!.insert_transaksi(
//                ItemTransaksi(
//                    0, tglTrx, disc, omzet, idPengguna, idTmpUsaha, valueDiskonRupiah.toString(), "0"
//                )
//            )
//
//            var id_trx: Int = databaseHandler!!.CountMaxIdTrx()
//
//            for (pn in obyek!!) {
//                Log.d("detail_transaksi", pn.idItem.toString()+"/"+pn.name+"/"+pn.qty.toString()+"/"+pn.price)
//                databaseHandler!!.insert_detail_transaksi(
//                    ItemDetailTransaksi(
//                        0, id_trx.toString(), pn.idItem.toString(), pn.name, pn.qty.toString(),  pn.price
//                    )
//                )
//            }
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }
//
//    }

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
                //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                Log.d("MESSAGE", message)
        } catch (ex : JSONException) {
            ex.printStackTrace()
            Toast.makeText(this, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }
}