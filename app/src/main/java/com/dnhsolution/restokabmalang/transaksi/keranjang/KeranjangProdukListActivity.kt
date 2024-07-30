package com.dnhsolution.restokabmalang.transaksi.keranjang

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.cetak.MainCetak
import com.dnhsolution.restokabmalang.cetak.MainCetakLokal
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.database.ItemDetailTransaksi
import com.dnhsolution.restokabmalang.database.ItemTransaksi
import com.dnhsolution.restokabmalang.databinding.ActivityKeranjangBinding
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable
import com.dnhsolution.restokabmalang.transaksi.keranjang.tambah.TambahProdukTransaksiActivity
import com.dnhsolution.restokabmalang.utilities.*
import com.dnhsolution.restokabmalang.utilities.dialog.AdapterWizard
import com.dnhsolution.restokabmalang.utilities.dialog.ItemView
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

class KeranjangProdukListActivity:AppCompatActivity(), KeranjangProdukItemOnTask
    ,View.OnClickListener, KeranjangTransaksiOnTask {

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bProses -> {
                println("asdf $valueTotalPrice $valueDiskonRupiah $valueBayar $isDiskonValid $isBayarValid")
                if(isDiskonValid) {
                    if(isBayarValid){
                        val jumlahObjek = obyek?.size ?: 0
                        if (jumlahObjek > 0) {
                            prosesTrx()
//                            showDialog("Konfirmasi", "Apakan anda ingin memproses?")
                        }
                        else {
                            Toast.makeText(applicationContext, R.string.data_kosong, Toast.LENGTH_SHORT).show()
                        }
                    } else Toast.makeText(applicationContext, R.string.bayar_tidak_valid, Toast.LENGTH_SHORT).show()
                } else Toast.makeText(this,R.string.diskon_tidak_valid,Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var pajakPersen: Int = 0
    private var nominalServiceCharge: Int = 0
    private var serviceCharge: Int = 0
    private var idHiburanNomor: Int? = null
    private lateinit var sharedPreferences: SharedPreferences
    private var isDiskonValid: Boolean = true
    private var isBayarValid: Boolean = true
    private var omzetRp: Int = 0
    private var tipeStruk: String? = null
    private var pajakRp: Float = 0F
    private var produkAdapter: KeranjangProdukListAdapter? = null
    private val _tag: String = javaClass.simpleName
    private var idPengguna: String? = null
    private var uuid: String? = null
    private var idTmpUsaha: String? = null
    private var jenis_pajak: String? = null
    private var valueDiskon: Int = 0
    private var valueDiskonRupiah: Int = 0
    private var valueDR: Double = 0.0
    private var valueBayar: Int = 0
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
                produkAdapter = KeranjangProdukListAdapter(
                    obyek!!,
                    this,
                    this
                )
                setUpRecyclerView(produkAdapter!!)
            }

            setTotal()
        }
    }

    private lateinit var binding : ActivityKeranjangBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeranjangBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
        idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "0")
        uuid = sharedPreferences.getString(Url.SESSION_UUID, "")
        tipeStruk = sharedPreferences.getString(Url.SESSION_TIPE_STRUK, "")
        idHiburanNomor = sharedPreferences.getInt(Url.SESSION_ID_HIBURAN_NOMOR, 0)
        serviceCharge = sharedPreferences.getInt(Url.SESSION_SERVICE_CHARGE, 0)
        jenis_pajak = sharedPreferences.getString(Url.SESSION_JENIS_PAJAK, "")
        pajakPersen = MainActivity.pajakPersen

        if(tipeStruk == "2") binding.llPajak.visibility = View.GONE
//        if(MainActivity.jenisPajak != "02") binding.llDisc.visibility = View.GONE

        databaseHandler = DatabaseHandler(this)
        binding.bProses.setOnClickListener(this)

        binding.bBantuan.setOnClickListener{
            tampilAlertDialogTutorial()
        }

        binding.bTambah.setOnClickListener{
            val favoritedProdukNames = ArrayList<ProdukSerializable>()
            for (Produk in obyek!!) {
                favoritedProdukNames.add(Produk)
            }

            val i = Intent(this, TambahProdukTransaksiActivity::class.java)
            i.putExtra("ARRAYLIST", favoritedProdukNames)
            resultLauncherTambah.launch(i)

        }

        binding.etRupiahDiskon.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                binding.etRupiahDiskon.requestFocus()
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {

                binding.etRupiahDiskon.removeTextChangedListener(this)

                var totalPrice = 0
                var totalPajak = 0
                for (valueTotal in obyek!!) {
                    val nilai = valueTotal.totalPrice
                    if(valueTotal.isPajak == "1") totalPajak += nilai
                    totalPrice += nilai
                }

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
                    binding.etRupiahDiskon.setText(formattedString.replace(",", "."))
                    binding.etRupiahDiskon.setSelection(binding.etRupiahDiskon.text.toString().length)
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }

                binding.etRupiahDiskon.addTextChangedListener(this)
                valueDiskonRupiah = if (binding.etRupiahDiskon.text.toString().isEmpty()) {
                    0
                } else {
                    editable.toString().replace(".", "").toInt()
                }

                setDiskonDanTotalRupiah(totalPajak,totalPrice)

                val valueKembalianUang = AddingIDRCurrency().formatIdrCurrencyNonKoma((valueBayar-valueTotalPrice).toDouble())
                binding.tvValueKembalianUang.text = valueKembalianUang
            }
        })

        binding.etBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                binding.etBayar.requestFocus()
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {

                binding.etBayar.removeTextChangedListener(this)

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
                    binding.etBayar.setText(formattedString.replace(",", "."))
                    binding.etBayar.setSelection(binding.etBayar.text.toString().length)
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }

                binding.etBayar.addTextChangedListener(this)
                valueBayar = if (binding.etBayar.text.toString().isEmpty()) {
                    0
                } else {
                    editable.toString().replace(".", "").toInt()
                }

                val valueKembalianUang = AddingIDRCurrency().formatIdrCurrencyNonKoma((valueBayar-valueTotalPrice).toDouble())
                binding.tvValueKembalianUang.text = valueKembalianUang

                isDiskonValid = valueTotalPrice >= valueDiskonRupiah
                isBayarValid = valueBayar >= valueTotalPrice

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
            produkAdapter = KeranjangProdukListAdapter(
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

    private fun tampilAlertDialogTutorial() {
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

    private fun setUpRecyclerView(mAdapter: KeranjangProdukListAdapter) {
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val itemTouchHelper = ItemTouchHelper(
            SwipeToDeleteCallback(
                mAdapter
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    override fun keranjangProdukItemOnTask(position:Int, totalPrice: Int, qty: Int) {
        if (position > -1) {
            obyek!![position].totalPrice = totalPrice
            obyek!![position].qty = qty
        }
        var totalHargaKeseluruhan = 0
        var totalPajak = 0
        for (valueTotal in obyek!!) {
            val nilai = valueTotal.totalPrice
            if(valueTotal.isPajak == "1") totalPajak += nilai
            totalHargaKeseluruhan += nilai
        }
        setDiskonDanTotalRupiah(totalPajak,totalHargaKeseluruhan)
    }

    private fun setTotal () {
        var totalPrice = 0
        var totalPajak = 0
        for (valueTotal in obyek!!) {
            val nilai = valueTotal.totalPrice
            if(valueTotal.isPajak == "1") totalPajak += nilai
            totalPrice += nilai
        }
        binding.tvSubtotal.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(totalPrice.toDouble())
        this.valueTotalPrice = totalPrice

        if(tipeStruk == "1")
            pajakRp = totalPajak.toFloat()*pajakPersen/100

        binding.tvPajak.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(pajakRp.toInt().toDouble())

        nominalServiceCharge = valueTotalPrice*serviceCharge/100
        binding.tvServiceCharge.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(nominalServiceCharge.toDouble())

        valueTotalPrice +=pajakRp.toInt()+nominalServiceCharge

        val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
        binding.tvDiskonTotal.text = rupiahValue

        val kembalian = valueBayar-valueTotalPrice
        binding.tvValueKembalianUang.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(kembalian.toDouble())

        isDiskonValid = valueTotalPrice >= valueDiskonRupiah
        isBayarValid = valueBayar >= valueTotalPrice
    }

    private fun setDiskonDanTotalRupiah (ttlPjk: Int, totalPrice: Int) {
        var totalPajak = ttlPjk
        binding.tvSubtotal.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(totalPrice.toDouble())

        val diskonRupiah: Int
        if (binding.etRupiahDiskon.text.toString().isNotEmpty()) {
            when {
                valueDiskonRupiah <= totalPajak -> {
                    diskonRupiah = valueDiskonRupiah
                    this.valueTotalPrice = totalPrice-diskonRupiah
                    totalPajak -= diskonRupiah

                    if(tipeStruk == "1")
                        pajakRp = totalPajak.toFloat()*pajakPersen/100

                    binding.tvPajak.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(pajakRp.toInt().toDouble())

                    nominalServiceCharge = valueTotalPrice*serviceCharge/100
                    binding.tvServiceCharge.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(nominalServiceCharge.toDouble())

                    valueTotalPrice +=pajakRp.toInt()+nominalServiceCharge

                    valueDR = ((diskonRupiah*100)/totalPrice).toDouble()
                    val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
                    binding.tvDiskonTotal.text = rupiahValue

                    val kembalian = valueBayar-valueTotalPrice
                    binding.tvValueKembalianUang.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(kembalian.toDouble())
                }
                else -> {
                    diskonRupiah = valueDiskonRupiah
                    this.valueTotalPrice = totalPrice-diskonRupiah

                    nominalServiceCharge = valueTotalPrice*serviceCharge/100
                    binding.tvServiceCharge.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(nominalServiceCharge.toDouble())

                    valueTotalPrice +=nominalServiceCharge
                    val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble()).replace(",","-")
                    binding.tvDiskonTotal.text = rupiahValue
                    pajakRp = 0F
                    binding.tvPajak.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(0.0)

                    val kembalian = valueBayar-valueTotalPrice
                    binding.tvValueKembalianUang.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(kembalian.toDouble())
                }
            }
        }else{
            this.valueTotalPrice = totalPrice

            if(tipeStruk == "1")
                pajakRp = totalPajak.toFloat()*pajakPersen/100

            binding.tvPajak.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(pajakRp.toInt().toDouble())

            nominalServiceCharge = valueTotalPrice*serviceCharge/100
            binding.tvServiceCharge.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(nominalServiceCharge.toDouble())

            valueTotalPrice +=pajakRp.toInt()+nominalServiceCharge

            valueDR = 0.0

            val rupiahValue = AddingIDRCurrency().formatIdrCurrencyNonKoma(valueTotalPrice.toDouble())
            binding.tvDiskonTotal.text = rupiahValue

            val kembalian = valueBayar-valueTotalPrice
            binding.tvValueKembalianUang.text = AddingIDRCurrency().formatIdrCurrencyNonKoma(kembalian.toDouble())
        }
        isDiskonValid = valueTotalPrice >= valueDiskonRupiah
        isBayarValid = valueBayar >= valueTotalPrice
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
                    println("$_tag $params")
                    KeranjangProdukListJsonTask(
                        this,
                        params
                    ).execute(Url.setKeranjangTransaksi)

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

    fun prosesTrx(){
        if(CheckNetwork().checkingNetwork(this)) {
            val params = HashMap<String, String>()
            params["paramsArray"] = createJson()
            println("$_tag $params")
            KeranjangProdukListJsonTask(
                this,
                params
            ).execute(Url.setKeranjangTransaksi)

        } else {
            val idTrx : String = saveLokal()
            showDialogBerhasilLokal("Selamat","Transaksi Anda Berhasil di proses !", idTrx)
            //Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
        }
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

            val intent = Intent(this@KeranjangProdukListActivity, MainCetak::class.java)
            intent.putExtra("getIdItem", "0")
            intent.putExtra("tipe", "karcis")
            resultLauncher.launch(intent)
//            resultLauncher.launch(Intent(this@KeranjangProdukListActivity, MainCetak::class.java).putExtra("getIdItem", "0"))
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

    private fun showDialogGagal(title:String,message:String){
        val builder = AlertDialog.Builder(this)

        // Set the alert dialog title
        builder.setTitle(title)

        // Display a message on alert dialog
        builder.setMessage(message)
        builder.setPositiveButton("Tutup"){_, _ ->
            dialog.dismiss()
        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
        dialog.getWindow()!!.setLayout(800, 400)
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
            val i = Intent(Intent(this@KeranjangProdukListActivity, MainCetakLokal::class.java))
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
        rootObject.put("uuid",uuid)
        rootObject.put("idTmptUsaha",idTmpUsaha)
        rootObject.put("jenis_pajak",jenis_pajak)
        rootObject.put("user",idPengguna)
        rootObject.put("disc_rp",valueDiskonRupiah)
        rootObject.put("pajakRp",pajakRp.toInt())
        if(binding.etRupiahDiskon.text.toString().isNotEmpty()){
            rootObject.put("disc",valueDR)
        } else{
            rootObject.put("disc",valueDiskon)
        }
        rootObject.put("omzet",valueTotalPrice)
        rootObject.put("pajakPersen",pajakPersen.toString())
        rootObject.put("bayar",valueBayar)
        rootObject.put("idHiburanNomor",idHiburanNomor)
        rootObject.put("nominalServiceCharge",nominalServiceCharge)

        val jsonArr = JSONArray()

        for (pn in obyek!!) {
            val pnObj = JSONObject()
            pnObj.put("idProduk", pn.idItem)
            pnObj.put("nmProduk", pn.name)
            pnObj.put("qty", pn.qty)
            pnObj.put("hrgProduk", pn.price)
            pnObj.put("isPajak", pn.isPajak)
            pnObj.put("tipeStruk", tipeStruk)
            pnObj.put("keterangan", pn.keterangan)
            jsonArr.put(pnObj)

            Log.d("NAMA_BARANG", pn.name.toString())
        }
        rootObject.put("produk",jsonArr)

        return rootObject.toString()
    }

    private fun saveLokal() : String{
//        var tglTrx = ""
        var disc = ""
        var omzet = ""
        var idTrx = 0
        var bayar = ""

        if(binding.etRupiahDiskon.text.toString().isNotEmpty()){
            disc = valueDR.toString()
        }

        if(binding.etBayar.text.toString().isNotEmpty()){
            bayar = valueBayar.toString()
        }

        omzet = valueTotalPrice.toString()

        val tglTrx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            current.format(formatter)
        } else {
            val date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault())
            formatter.format(date)
        }

        try {

            databaseHandler!!.insert_transaksi(
                ItemTransaksi(
                    0, tglTrx, disc, omzet, idPengguna, idTmpUsaha, valueDiskonRupiah.toString()
                    , "0",pajakRp.toInt().toString(),bayar,pajakPersen.toString()
                    ,idHiburanNomor.toString(),nominalServiceCharge.toString()
                )
            )

            idTrx = databaseHandler!!.CountMaxIdTrx()

            for (pn in obyek!!) {
                Log.d("detail_transaksi", pn.idItem.toString()+"/"+pn.name+"/"+pn.qty.toString()+"/"+pn.price)
                databaseHandler!!.insert_detail_transaksi(
                    ItemDetailTransaksi(
                        0, idTrx.toString(), pn.idItem.toString(), pn.name, pn.qty.toString()
                        ,  pn.price, pn.keterangan
                    )
                )
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return idTrx.toString()
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
            if (success == 1) {
                //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                Log.d("MESSAGE", message)

                resultLauncher.launch(
                    Intent(this@KeranjangProdukListActivity, MainCetak::class.java).putExtra("getIdItem", "0"))

//                showDialogBerhasil("Selamat","Transaksi Anda Berhasil di proses !")
            }else{
                showDialogGagal("Mohon Maaf","Transaksi Anda tidak dapat diproses !")
            }
        } catch (ex : JSONException) {
            ex.printStackTrace()
            Toast.makeText(this, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }
}