package com.dnhsolution.restokabmalang.transaksihiburan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.dnhsolution.restokabmalang.BuildConfig
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.cetak.AsyncBluetoothEscPosPrint
import com.dnhsolution.restokabmalang.cetak.AsyncEscPosPrint
import com.dnhsolution.restokabmalang.cetak.AsyncEscPosPrinter
import com.dnhsolution.restokabmalang.database.DatabaseHandler
import com.dnhsolution.restokabmalang.databinding.FragmentTransaksiListBinding
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.OnDataFetched
import com.dnhsolution.restokabmalang.utilities.TaskRunner
import com.dnhsolution.restokabmalang.utilities.Url
import com.zj.btsdk.BluetoothService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.set


data class TarifModel(val id:Int, val nama:String, val nominal:Int)

interface TarifServices {
    @GET("pdrd/Android/AndroidJsonPOS_Dev2/getProdukHiburan")
    fun getPosts(@Query("idTmpUsaha") nilai:String): Call<List<ProdukModel>>
}

interface OnQtyChangedListener {
    fun onQtyUpdated()
}

object TarifDataRepository {
    fun create(): TarifServices {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Url.serverBase)
            .build()
        return retrofit.create(TarifServices::class.java)
    }
}

class ProdukListFragment:Fragment()
    , EasyPermissions.PermissionCallbacks, OnDataFetched, OnQtyChangedListener {

    private var sharedPreferences: SharedPreferences? = null
    private var isPrinterReady: Boolean = false
    private lateinit var btService: BluetoothService
    private val _tag = javaClass.simpleName

    private var selectedDevice: BluetoothConnection? = null
    private val PERMISSION_BLUETOOTH = 100
    private val PERMISSION_BLUETOOTH_ADMIN = 101
    private val PERMISSION_BLUETOOTH_CONNECT = 102
    private val PERMISSION_BLUETOOTH_SCAN = 103

    var nama: String = ""
    var bayar: Int = 0
    var subtotal: Int = 0
    var kembalian: Int = 0
    lateinit var tarifList : List<ProdukModel>
    var tarifListAdapter : TarifListAdapter? = null
    var rcBluetoothStatus = 0
    var rbPilihan = ""
    var databaseHandler: DatabaseHandler? = null
    private lateinit var trxAdapter: TransaksiAdapter
    lateinit var backupList: MutableList<ProdukModel>


    internal var ChildView: View? = null
    internal var RecyclerViewClickedItemPos: Int = 0

    private var binding : FragmentTransaksiListBinding?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransaksiListBinding.inflate(layoutInflater, container, false)

        return binding?.root!!
    }

    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = context?.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        val idTmpUsaha = sharedPreferences?.getString(Url.SESSION_ID_TEMPAT_USAHA, "-1").toString()
        val label = sharedPreferences?.getString(Url.setLabel, "Belum disetting")
        databaseHandler = DatabaseHandler(requireContext())

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectNonSdkApiUsage()
                    .penaltyLog()
                    .build()
            )
        }

        binding?.etBayar?.addTextChangedListener(object : TextWatcher {
            var current = ""

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    binding?.etBayar?.removeTextChangedListener(this)

                    val cleanString = s.toString().replace(".", "").replace(",", "")
                    val parsed = cleanString.toLongOrNull()

                    if (parsed != null) {
                        val formatted = NumberFormat.getInstance(Locale("in", "ID")).format(parsed)
                        current = formatted
                        binding?.etBayar?.setText(formatted)
                        binding?.etBayar?.setSelection(formatted.length)
                    } else {
                        current = ""
                        binding?.etBayar?.setText("")
                    }

                    binding?.etBayar?.addTextChangedListener(this)
                }

                bayar = Integer.parseInt(getUnformattedValue(current).toString())

                binding?.tvValueKembalianUang?.text = "Rp. "+formatRibuan((kembalian+bayar))

                if ((kembalian+bayar)>=0){
                    binding?.bCetakStruk!!.visibility=View.VISIBLE
                }else{
                    binding?.bCetakStruk!!.visibility=View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        if(CheckNetwork().checkingNetwork(requireContext())) {
            val loadingDialog = context?.let { showLoadingDialog(it) }
            val postServices = TarifDataRepository.create()
            postServices.getPosts(idTmpUsaha).enqueue(object : Callback<List<ProdukModel>> {

                override fun onFailure(call: Call<List<ProdukModel>>, error: Throwable) {
                    Log.e(_tag, "errornya ${error.message}")
                }

                override fun onResponse(
                    call: Call<List<ProdukModel>>,
                    response: retrofit2.Response<List<ProdukModel>>
                ) {
                    if (response.isSuccessful) {
                        loadingDialog?.dismiss()
                        val data = response.body()
                        Log.d("DataProduk", "onResponse: "+data)
                        data?.let {
                            tarifList = it as ArrayList<ProdukModel>
                            tarifListAdapter = TarifListAdapter(
                                tarifList,
                                context
                            )
                        }
                        backupList = tarifList.map { it.copy() }.toMutableList()
                    }
                }
            })
        }

        binding?.btnListProduk!!.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_daftar_produk, null)

            val dialogBuilder = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)

            val alertDialog = dialogBuilder.create()

            val rvData = dialogView.findViewById<RecyclerView>(R.id.rvData)
            val bSubmit = dialogView.findViewById<Button>(R.id.bSubmit)
            val bCancel = dialogView.findViewById<Button>(R.id.bCancel)

            rvData.layoutManager = GridLayoutManager(context, 2)
            rvData.itemAnimator = DefaultItemAnimator()
            rvData.adapter = tarifListAdapter

            tarifListAdapter?.notifyDataSetChanged()

            backupList.clear()
            backupList = tarifList.map { it.copy() }.toMutableList()

            alertDialog.show()

            bCancel.setOnClickListener{
                for (list in backupList) {
                    val index = tarifList.indexOfFirst { it.id == list.id }
                    if (index != -1) {
                        tarifList[index].ischecked = list.ischecked
                    }
                }
                alertDialog.dismiss()
            }

            bSubmit.setOnClickListener{
                var jml_selected = 0
                for (list in tarifList) {
                    if (list.ischecked == true){
                        jml_selected++
                        if (list.qty == 0){
                            updateQty(list.id, 1, tarifList)
                        }
                    }
                }

                if (jml_selected>0){
                    backupList.clear()
                    binding?.tvKosong!!.visibility=View.GONE
                    binding?.clTransaksi!!.visibility=View.VISIBLE
                    alertDialog.dismiss()
                    setTransaksi()
                    trxAdapter.notifyDataSetChanged()
                    backupList = tarifList.map { it.copy() }.toMutableList()
                }else{
                    resetTransaksi()
                    Toast.makeText(context, "Anda belum memilih produk !", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding?.bCetakStruk!!.setOnClickListener {

            if(CheckNetwork().checkingNetwork(requireContext())){
//                Log.d(_tag, "onViewCreated: "+BluetoothPrintersConnections.selectFirstPaired())
                val params = HashMap<String, String>()
                params["paramsArray"] = createJson()
                println("params[paramsArray] : "+params)
                val runner = TaskRunner()
                runner.executeAsync(
                    TransaksiNetworkTask(
                        this,
                        requireContext(),
                        Url.setKeranjangTransaksi,
                        params
                    )
                )
            }else{

            }
        }
    }


    fun printBluetooth(resultArray: JSONArray) {
        eksekusiCetakStruk(resultArray)
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.BLUETOOTH
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireContext() as Activity,
//                arrayOf(Manifest.permission.BLUETOOTH),
//                PERMISSION_BLUETOOTH
//            )
//        } else if (ContextCompat.checkSelfPermission(
//                requireContext() as Activity,
//                Manifest.permission.BLUETOOTH_ADMIN
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireContext() as Activity,
//                arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
//                PERMISSION_BLUETOOTH_ADMIN
//            )
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
//                requireContext() as Activity,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireContext() as Activity,
//                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
//                PERMISSION_BLUETOOTH_CONNECT
//            )
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
//                requireContext() as Activity,
//                Manifest.permission.BLUETOOTH_SCAN
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireContext() as Activity,
//                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
//                PERMISSION_BLUETOOTH_SCAN
//            )
//        } else {
//            Toast.makeText(context, "Cetak Struk", Toast.LENGTH_SHORT).show()
//            //khusus advan
////            eksekusiCetakStrukAdvan(resultArray)
//            //global
////            eksekusiCetakStruk(resultArray)
//        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun printBluetooth2(platNomor: String,tglMasuk: String,tglKeluar: String,biayaParkir: String,jnsKendaraan: String,idTrx: Int, no_seri: String) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(Manifest.permission.BLUETOOTH),
                PERMISSION_BLUETOOTH
            )
        } else if (ContextCompat.checkSelfPermission(
                requireContext() as Activity,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
                PERMISSION_BLUETOOTH_ADMIN
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                requireContext() as Activity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                PERMISSION_BLUETOOTH_CONNECT
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                requireContext() as Activity,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                PERMISSION_BLUETOOTH_SCAN
            )
        } else {

//            value.forEach {
//                val totalQty = it["totalQty"]
//                val nmProduk = it["nmProduk"]
//                println("$totalQty $nmProduk")
//            }

            //Khsusus Advan
            eksekusiCetakStrukAdvan2(platNomor, tglMasuk,tglKeluar,biayaParkir,jnsKendaraan,idTrx,no_seri)

            //Global
//            AsyncBluetoothEscPosPrint(
//                requireContext(),
//                object : AsyncEscPosPrint.OnPrintFinished() {
//                    override fun onError(
//                        asyncEscPosPrinter: AsyncEscPosPrinter?,
//                        codeException: Int
//                    ) {
//                        Log.e(
//                            "Async.OnPrintFinished",
//                            "AsyncEscPosPrint.OnPrintFinished : An error occurred !"
//                        )
//                    }
//
//                    override fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?) {
//                        Log.i(
//                            "Async.OnPrintFinished",
//                            "AsyncEscPosPrint.OnPrintFinished : Print is finished !"
//                        )
//                    }
//                }
//            ).execute(eksekusiCetakStruk2(selectedDevice, platNomor, tglMasuk,tglKeluar,biayaParkir,jnsKendaraan,idTrx,no_seri))
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun eksekusiCetakStruk(resultArray: JSONArray) {

        val printer = EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32)

        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        val nmTmpUsaha =
            sharedPreferences.getString(Url.SESSION_NAMA_TEMPAT_USAHA, "0")
        val alamat = sharedPreferences.getString(Url.SESSION_ALAMAT, "0")
        val nama_petugas = sharedPreferences.getString(Url.SESSION_NAMA_PETUGAS, "0")
        val tanggal: String? = getDateTime()
        val pegawai = nama_petugas

        val list = (0 until resultArray.length()).map { i ->
            resultArray.getJSONObject(i)
        }
        var idTrx = ""
        for (item in list) {
            idTrx = item.getString("id_trx")
        }
        var text = "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
            requireActivity().resources.getDrawableForDensity(R.drawable.ic_malang_makmur_grayscale,
                DisplayMetrics.DENSITY_LOW, requireActivity().theme
            )) + "</img>\n" +
                "[C]<b>$nmTmpUsaha</b>\n" +
                "[C]$alamat\n" +
                "[L]\n" +
                "[L]Tanggal : $tanggal\n" +
                "[L]Kasir   : $pegawai\n" +
                "[L]No.Trx   : $idTrx\n"

        for (item in list) {
            val nama = item.getString("nama_produk")
            val nomor_seri = item.getString("nomor_seri")
            val qty = item.getString("qty")
            val harga = item.getString("harga")

            text +=
                "[C]--------------------------------\n" +
                "[L]No.Seri : $nomor_seri\n" +
                "[L]$nama\n" +
                "[L]Nominal : $harga\n" +
                "[C]--------------------------------\n"
        }

        text +=
                "[C]Terima Kasih\n" +
                "[C]Atas Kunjungan\n" +
                "[C]Anda\n"

        printer.printFormattedText(text).disconnectPrinter()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun eksekusiCetakStrukAdvan(resultArray: JSONArray) {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        val nmTmpUsaha =
            sharedPreferences.getString(Url.SESSION_NAMA_TEMPAT_USAHA, "0")
        val alamat = sharedPreferences.getString(Url.SESSION_ALAMAT, "0")
        val nama_petugas = sharedPreferences.getString(Url.SESSION_NAMA_PETUGAS, "0")
        val tanggal: String? = getDateTime()
        val pegawai = nama_petugas
        val tipeKendaraan = tarifList[RecyclerViewClickedItemPos].nama

        try {
            val printer = EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32)
            var text = "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
                requireActivity().resources.getDrawableForDensity(R.drawable.ic_malang_makmur_grayscale,
                    DisplayMetrics.DENSITY_LOW, requireActivity().theme
                )) + "</img>\n" +
                    "[L]\n"+
                    "[C]<b>$nmTmpUsaha</b>\n"+
                    "[C]$alamat\n"+
                    "[L]\n"
            text += "[L]Petugas    : $pegawai\n"+
                    "[C]- Terima Kasih -"
            printer.printFormattedText(text).disconnectPrinter()
        } catch (e: java.lang.Exception) {
            Log.d(_tag, "eksekusiCetakStrukAdvan: "+e.message)
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun eksekusiCetakStruk2(printerConnection: DeviceConnection?, plat_nomor: String, tglMasuk: String, tglKeluar: String, biayaParkir: String, jnsKendaraan: String, idTrx: Int, no_seri: String) : AsyncEscPosPrinter {
        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)

        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        val nmTmpUsaha =
            sharedPreferences.getString(Url.SESSION_NAMA_TEMPAT_USAHA, "0")
        val alamat = sharedPreferences.getString(Url.SESSION_ALAMAT, "0")
        val nama_petugas = sharedPreferences.getString(Url.SESSION_NAMA_PETUGAS, "0")
        val pegawai = nama_petugas

        var text = "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
            requireActivity().resources.getDrawableForDensity(R.drawable.ic_malang_makmur_grayscale,
                DisplayMetrics.DENSITY_LOW, requireActivity().theme
            )) + "</img>\n" +
                "[L]\n"+
                "[C]<b>$nmTmpUsaha</b>\n"+
                "[C]$alamat\n"+
                "[L]\n"+
                "[L]No. Trx: $no_seri\n"+
                "[L]Check In : $tglMasuk\n"+
                "[L]Check Out : $tglKeluar\n"+
                "[L]Plat Nomor : $plat_nomor\n"+
                "[L]Tipe Kend. : $jnsKendaraan\n"+
                "[C]--------------------------------\n\n"+
                "[L]Biaya Parkir   : $biayaParkir\n"+
                "[C]--------------------------------\n"
        text += "[L]Petugas        : $pegawai\n"+
                "[C]- Terima Kasih -"

        return printer.addTextToPrint(text)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun eksekusiCetakStrukAdvan2(plat_nomor: String, tglMasuk: String, tglKeluar: String, biayaParkir: String, jnsKendaraan: String, idTrx: Int, no_seri: String) {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        val nmTmpUsaha =
            sharedPreferences.getString(Url.SESSION_NAMA_TEMPAT_USAHA, "0")
        val alamat = sharedPreferences.getString(Url.SESSION_ALAMAT, "0")
        val nama_petugas = sharedPreferences.getString(Url.SESSION_NAMA_PETUGAS, "0")
        val pegawai = nama_petugas

        try {
            val printer = EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32)
            var text = "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
            requireActivity().resources.getDrawableForDensity(R.drawable.ic_malang_makmur_grayscale,
                DisplayMetrics.DENSITY_LOW, requireActivity().theme
            )) + "</img>\n" +
            "[L]\n"+
                    "[C]<b>$nmTmpUsaha</b>\n"+
                    "[C]$alamat\n"+
                    "[L]\n"+
                    "[L]No. Trx: $no_seri\n"+
                    "[L]Check In   : $tglMasuk\n"+
                    "[L]Check Out  : $tglKeluar\n"+
                    "[L]Plat Nomor : $plat_nomor\n"+
                    "[L]Tipe Kend. : $jnsKendaraan\n"+
                    "[C]--------------------------------\n\n"+
                    "[L]Biaya Parkir : $biayaParkir\n"+
                    "[C]--------------------------------\n"
            text += "[L]Petugas      : $pegawai\n"+
                    "[C]- Terima Kasih -"
            printer.printFormattedText(text).disconnectPrinter()
        } catch (e: java.lang.Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getDateTime(): String? {
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    private fun writePrint(align: ByteArray?, msg: String) {
        btService.write(align)
        var space = "   "
        val l = msg.length
        if (l < 31) {
            for (x in 31 - l downTo 0) {
                space = "$space "
            }
        }
        btService.write(msg.replace(" : ", space).toByteArray())
    }

    private fun createJson() : String{
        val rootObject= JSONObject()
        val idTmpUsaha = sharedPreferences?.getString(Url.SESSION_ID_TEMPAT_USAHA, "0")
        val idPengguna = sharedPreferences?.getString(Url.SESSION_ID_PENGGUNA, "0")
        val uuid = sharedPreferences?.getString(Url.SESSION_UUID, "0")
        val tipeStruk = sharedPreferences?.getString(Url.SESSION_TIPE_STRUK, "0")
        val jenis_pajak = sharedPreferences?.getString(Url.SESSION_JENIS_PAJAK, "0")

        rootObject.put("uuid",uuid)
        rootObject.put("idTmptUsaha",idTmpUsaha)
        rootObject.put("jenis_pajak",jenis_pajak)
        rootObject.put("user",idPengguna)
        rootObject.put("disc","0")
        rootObject.put("disc_rp","0")
        rootObject.put("omzet",subtotal)
        rootObject.put("pajakRp","0")
        rootObject.put("pajakPersen","0")
        rootObject.put("bayar",bayar)
        rootObject.put("idHiburanNomor","0")
        rootObject.put("nominalServiceCharge","0")

        val jsonArr = JSONArray()
        for (pn in tarifList) {
            val pnObj = JSONObject()
            if (pn.ischecked==true){
                pnObj.put("idProduk", pn.id)
                pnObj.put("nmProduk", pn.nama)
                pnObj.put("qty", pn.qty)
                pnObj.put("hrgProduk", pn.nominal)
                pnObj.put("isPajak", pn.ispajak)
                pnObj.put("tipeStruk", tipeStruk)
                jsonArr.put(pnObj)
            }
        }
        rootObject.put("produk",jsonArr)

        return rootObject.toString()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        TODO("Not yet implemented")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        TODO("Not yet implemented")
    }

    override fun showProgressBar() {
        binding?.progressBar!!.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        binding?.progressBar!!.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun setDataInPageWithResult(result: Any?) {
        when (result) {
            "" -> {
                Toast.makeText(requireContext(), R.string.empty_data, Toast.LENGTH_SHORT).show()
                return
            }
            "-1" -> {
                Toast.makeText(requireContext(), R.string.time_out, Toast.LENGTH_SHORT).show()
                return
            }
            "cancelled" -> return
        }

        Log.e(tag, "Response from url:$result")

        try {
            val jsonObj = JSONObject(result.toString())
            val success = jsonObj.getString("success")
//            val proses = jsonObj.getString("proses")
            if (success == "1") {
                resetTransaksi()
                val resultArray : JSONArray = jsonObj.getJSONArray("result")
                printBluetooth(resultArray)
                Log.d(_tag, "result_array: "+resultArray)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.error_data, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    private fun getNow(): String? {
        val dateFormat: DateFormat = SimpleDateFormat("dd-mm-yyyy HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    private fun getNow2(): String? {
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    fun showLoadingDialog(context: Context): AlertDialog {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_loading, null)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        return dialog
    }

    fun resetTransaksi() {
        hideKeyboard()
        backupList.clear()
        binding?.tvKosong!!.visibility=View.VISIBLE
        binding?.clTransaksi!!.visibility=View.GONE
        binding?.bCetakStruk!!.visibility=View.GONE
        kembalian = 0
        bayar = 0
        subtotal = 0
        for (list in tarifList) {
            list.ischecked = false
        }
        tarifListAdapter?.notifyDataSetChanged()
    }

    fun updateQty(id: Int, newQty: Int, itemList: List<ProdukModel>) {
        val index = itemList.indexOfFirst { it.id == id }
        if (index != -1) {
            itemList[index].qty = newQty
        }
    }

    fun setTransaksi(){
        binding?.recyclerView!!.layoutManager = LinearLayoutManager(context)
        trxAdapter = TransaksiAdapter(tarifList, this)
        binding?.recyclerView!!.adapter = trxAdapter
        updateTotal()
    }

    fun formatRibuan(angka: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return formatter.format(angka)
    }

//    Jika kamu ingin mengambil angka asli dari input untuk disimpan/hitung:
    fun getUnformattedValue(input: String): Long {
        return input.replace(".","").toLongOrNull() ?: 0L
    }

    override fun onQtyUpdated() {
        updateTotal()
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotal() {
        var total = 0
        for (list in tarifList) {
            if (list.ischecked == true){
                total += (list.qty*list.nominal)
            }
        }
        binding?.tvSubtotal?.text = "Rp. "+formatRibuan(total)
        binding?.tvDiskonTotal?.text = "Rp. "+formatRibuan(total)
        subtotal = total
        kembalian = bayar-subtotal
        binding?.tvValueKembalianUang?.text = "Rp. "+formatRibuan(kembalian)
        if (total==0){
            resetTransaksi()
        }
        kembalian = -subtotal

        if ((kembalian+bayar)>=0){
            binding?.bCetakStruk!!.visibility=View.VISIBLE
        }else{
            binding?.bCetakStruk!!.visibility=View.GONE
        }
    }

    private fun hideKeyboard() {
        view?.let { v ->
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

}

