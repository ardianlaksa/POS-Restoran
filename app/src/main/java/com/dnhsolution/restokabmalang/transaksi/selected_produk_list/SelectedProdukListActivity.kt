package com.dnhsolution.restokabmalang.transaksi.selected_produk_list

import android.app.Activity
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
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.cetak.MainCetak
import com.dnhsolution.restokabmalang.cetak.MainCetakLokal
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.database.ItemDetailTransaksi
import com.dnhsolution.restokabmalang.database.ItemTransaksi
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable
import com.dnhsolution.restokabmalang.utilities.*
import com.dnhsolution.restokabmalang.utilities.dialog.AdapterWizard
import com.dnhsolution.restokabmalang.utilities.dialog.ItemView
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
                showDialog("Konfirmasi","Apakan anda ingin memproses?")
            }
        }
    }

    private var omzetRp: Int = 0
    private var tipeStruk: String? = null
    private var pajakRp: Float = 0F
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

    lateinit var alertDialog: android.app.AlertDialog.Builder
    lateinit var dialog: android.app.AlertDialog

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }


    private var resultLauncherTambah = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val i = result.data
            val args = i?.getBundleExtra("BUNDLE")
            val obyekBundle = args?.getParcelableArrayList<ProdukSerializable?>("ARRAYLIST")
            if(obyek != null && obyekBundle != null) {
                obyekBundle.sortWith(compareByDescending { it.name })
                val numberIterator = obyek!!.iterator()
                var indexIterator = 0
                while (numberIterator.hasNext()) {
                    val integer = numberIterator.next()
                    var found = false
                    for (item1 in obyekBundle) {
                        if (integer.idItem == item1.idItem) {
                            found = true
                        }
                    }
                    if (!found) {
                        numberIterator.remove()
                        produkAdapter?.notifyItemRemoved(indexIterator)
                        produkAdapter?.notifyItemChanged(indexIterator)
                    }
                    indexIterator++
                }

                obyekBundle.forEachIndexed { index, item2 ->

                    var found = false
                    for (item1 in obyek!!) {
                        if (item2.idItem == item1.idItem) {
                            found = true
                        }
                    }
                    if (!found) {
                        obyek!!.add(item2)
                        produkAdapter?.notifyItemInserted(obyek!!.size)
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
            }

            setTotal()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keranjang)

        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
        idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "")
        tipeStruk = sharedPreferences.getString(Url.SESSION_TIPE_STRUK, "")

        databaseHandler = DatabaseHandler(this)
        bProses.setOnClickListener(this)

        bBantuan.setOnClickListener{
            tampilAlertDialogTutorial()
        }

        bTambah.setOnClickListener{
            val favoritedProdukNames = ArrayList<ProdukSerializable>()
            for (Produk in obyek!!) {
                favoritedProdukNames.add(Produk)
            }

            val i = Intent(this, TambahProdukTransaksiActivity::class.java)
            i.putExtra("ARRAYLIST", favoritedProdukNames)
//            startActivity(i)
            resultLauncherTambah.launch(i)

        }

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

        val i = intent
        val args = i.getBundleExtra("BUNDLE")
        val obyekProduk:ArrayList<ProdukSerializable>? = args?.getParcelableArrayList("ARRAYLIST")
        obyekProduk?.sortWith(compareByDescending { it.name })
        if (obyek == null) {
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

    fun tampilAlertDialogTutorial() {
        alertDialog = android.app.AlertDialog.Builder(this)
        val rowList: View = layoutInflater.inflate(R.layout.dialog_tutorial, null)
        var listView = rowList.findViewById<ListView>(R.id.listView)
        val arrayList: ArrayList<ItemView> = ArrayList<ItemView>()
        arrayList.add(ItemView("1", "Icon (+) dan (-) digunakan untuk menambah dan mengurangi jumlah pesanan."))
        arrayList.add(ItemView("2", "Disc digunakan untuk menambahkan diskon."))
        arrayList.add(ItemView("3", "Tombol proses untuk mengirim data transaksi ke server dan melanjutkan proses cetak."))
        val tutorialArrayAdapter = AdapterWizard(this, arrayList)
        listView.setAdapter(tutorialArrayAdapter)
        alertDialog.setView(rowList)
        dialog = alertDialog.create()
        dialog.show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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

            println("d :$tipeStruk")
            if(tipeStruk == "1")
                pajakRp = valueTotalPrice.toFloat()*10/100

            tvPajak.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(pajakRp.toInt().toDouble())

            valueTotalPrice +=pajakRp.toInt()

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

            if(tipeStruk == "1")
                pajakRp = valueTotalPrice.toFloat()*10/100

            tvPajak.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(pajakRp.toInt().toDouble())

            valueTotalPrice +=pajakRp.toInt()

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
        if (etRupiahDiskon.text.toString().isNotEmpty()) {
            if(valueDiskonRupiah <= totalPrice){
                diskonRupiah = valueDiskonRupiah
                this.valueTotalPrice = totalPrice-diskonRupiah

                if(tipeStruk == "1")
                    pajakRp = valueTotalPrice.toFloat()*10/100

                tvPajak.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(pajakRp.toInt().toDouble())

                valueTotalPrice +=pajakRp.toInt()

                valueDR = ((diskonRupiah*100)/totalPrice).toDouble()
                val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
                tvDiskonTotal.text = rupiahValue
            }else{
                val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(totalPrice.toDouble())
                tvDiskonTotal.text = rupiahValue
            }

        }else{
            this.valueTotalPrice = totalPrice

            if(tipeStruk == "1")
                pajakRp = valueTotalPrice.toFloat()*10/100

            tvPajak.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(pajakRp.toInt().toDouble())

            valueTotalPrice +=pajakRp.toInt()

            valueDR = 0.0

            val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
            tvDiskonTotal.text = rupiahValue

            if(!etDiskon.text.toString().isEmpty()){
                valueDiskon = etDiskon.text.toString().toInt()
                setTotal()
            }
        }

    }

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

                val i = Intent(this, TambahProdukTransaksiActivity::class.java)
                i.putExtra("ARRAYLIST", favoritedProdukNames)
                resultLauncherTambah.launch(i)
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
                    val idTrx : String = saveLokal()
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

    private fun showDialogBerhasil(title:String,message:String){
        val builder = AlertDialog.Builder(this)

        // Set the alert dialog title
        builder.setTitle(title)

        // Display a message on alert dialog
        builder.setMessage(message)
        builder.setPositiveButton("Cetak"){_, _ ->
            //                println(createJson())
//            startActivity()
            resultLauncher.launch(Intent(this@SelectedProdukListActivity, MainCetak::class.java))
        }

        builder.setNegativeButton("Batal"){_, _ ->
//            val i = Intent(Intent(this@SelectedProdukListActivity, MainActivity::class.java))
//            startActivity(i)
            setResult(RESULT_OK)
            finish()
        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun showDialogBerhasilLokal(title:String,message:String, idTrx:String){
        val builder = AlertDialog.Builder(this)

        // Set the alert dialog title
        builder.setTitle(title)

        // Display a message on alert dialog
        builder.setMessage(message)
        builder.setPositiveButton("Cetak"){_, _ ->
            val i = Intent(Intent(this@SelectedProdukListActivity, MainCetakLokal::class.java))
            i.putExtra("idTrx", idTrx)
//            startActivity(i)
//            finish()
            resultLauncher.launch(i)
        }

        builder.setNegativeButton("Batal"){_, _ ->
//            val i = Intent(Intent(this@SelectedProdukListActivity, MainActivity::class.java))
//            startActivity(i)
            setResult(RESULT_OK)
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
        rootObject.put("pajakRp",pajakRp.toInt())
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

    private fun saveLokal() : String{
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
            val date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault())
            tglTrx = formatter.format(date)
        }

        try {
            databaseHandler!!.insert_transaksi(
                ItemTransaksi(
                    0, tglTrx, disc, omzet, idPengguna, idTmpUsaha, valueDiskonRupiah.toString()
                    , "0",pajakRp.toInt().toString()
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