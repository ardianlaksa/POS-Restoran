package com.dnhsolution.restokabmalang.data.rekap_harian

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.cetak.AsyncBluetoothEscPosPrint
import com.dnhsolution.restokabmalang.cetak.AsyncEscPosPrint
import com.dnhsolution.restokabmalang.cetak.AsyncEscPosPrinter
import com.dnhsolution.restokabmalang.cetak.MainCetak
import com.dnhsolution.restokabmalang.data.rekap_harian.task.DRekapHarianJsonTask
import com.dnhsolution.restokabmalang.data.rekap_harian.task.RekapHarianJsonTask
import com.dnhsolution.restokabmalang.databinding.FragmentRekapHarianBinding
import com.dnhsolution.restokabmalang.utilities.*
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

interface UploadPdfService {
    @FormUrlEncoded
    @POST("${Url.serverPos}notifEmailPdfHarian")
    fun sendPosts(
        @Field("idPengguna") idPengguna: String, @Field("idTmpUsaha") idTmpUsaha: String
        , @Field("tgl") tgl: String
    ): Call<DefaultPojo>
}

interface CetakFullService {
    @FormUrlEncoded
    @POST("${Url.serverPos}cetakFullHarian")
    fun sendPosts(
        @Field("idPengguna") idPengguna: String, @Field("idTmpUsaha") idTmpUsaha: String
        , @Field("tgl") tgl: String
    ): Call<DefaultResultPojo>
}

object CetakFullResultFeedback {

    fun create(): CetakFullService {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(Url.serverBase)
            .build()
        return retrofit.create(CetakFullService::class.java)
    }
}

object UploadPdfResultFeedback {

    fun create(): UploadPdfService {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(Url.serverBase)
            .build()
        return retrofit.create(UploadPdfService::class.java)
    }
}

class RekapHarianFragment : Fragment(), DRekapHarianOnTask, RekapHarianOnTask, RekapHarianDetailOnTask,
    RekapHarianDetailLongClick {

    companion object {
        @JvmStatic
        fun newInstance(params: String) = RekapHarianFragment().apply {
            arguments = Bundle().apply {
                putString("params",params)
            }
        }
    }

    private var namaPetugas: String = ""
    private var namaTempatUsaha: String = ""
    private var idPengguna: String? = null
    private var idTmpUsaha: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewD: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var spiTgl: Spinner
    private lateinit var etDate: EditText
    private lateinit var ivDate: ImageView
    private lateinit var binding : FragmentRekapHarianBinding
    private val myCalendar = Calendar.getInstance()
    private var itemsHarian:ArrayList<RekapHarianListElement>? = null
    private var itemsDHarian:ArrayList<DRekapHarianListElement>? = null
    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private var jsonTaskDetail: AsyncTask<String, Void, String?>? = null
    private var params = ""
    private var tanggal = ""
    private var tempItemsHarian = ArrayList<RekapHarianListElement>()
    private var tempItemsDHarian = ArrayList<DRekapHarianListElement>()
    private var adapterList:RekapHarianListAdapter? = null
    private var adapterListD:DRekapHarianListAdapter2? = null
    private val IMAGE_DIRECTORY = "/POSRestoran"
    private var selectedDevice: BluetoothConnection? = null
    private val PERMISSION_BLUETOOTH = 100
    private val PERMISSION_BLUETOOTH_ADMIN = 101
    private val PERMISSION_BLUETOOTH_CONNECT = 102
    private val PERMISSION_BLUETOOTH_SCAN = 103

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("params")?.let { params = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.fragment_rekap_harian, container, false)
        binding = FragmentRekapHarianBinding.inflate(layoutInflater)
        namaPetugas = MainActivity.namaPetugas ?: ""
        namaTempatUsaha = MainActivity.namaTempatUsaha ?: ""
        val view = binding.root
        setHasOptionsMenu(true)
        spiTgl = binding.spinTgl
        tvTotal = binding.tvTotal
        ivDate = binding.ivDate
        etDate = binding.etDate
        recyclerView = binding.recyclerView

//        val sharedPreferences = context?.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = MainActivity.idTempatUsaha.toString()
        idPengguna = MainActivity.idPengguna.toString()

        if(CheckNetwork().checkingNetwork(requireContext())) {
            if(tanggal.isEmpty()) tanggal = getCurrentDate()
            val stringUrl = "${Url.getRekapHarian}?tgl="+tanggal+"&idTmpUsaha="+idTmpUsaha+"&idPengguna="+idPengguna
            Log.i(_tag,stringUrl)
            jsonTask = RekapHarianJsonTask(this).execute(stringUrl)
        } else {
//            Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
            binding.ivIconDataKosong.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_conn_lost,null))
        }

        val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        ivDate.setOnClickListener{
            DatePickerDialog(
                requireContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etDate.setText(getCurrentDate())

//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
//            intent.data = uri
//            startActivity(intent)
//        }

        return view
    }

    fun printBluetooth(value: List<java.util.HashMap<String, String>>) {
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

            AsyncBluetoothEscPosPrint(
                requireContext(),
                object : AsyncEscPosPrint.OnPrintFinished() {
                    override fun onError(
                        asyncEscPosPrinter: AsyncEscPosPrinter?,
                        codeException: Int
                    ) {
                        Log.e(
                            "Async.OnPrintFinished",
                            "AsyncEscPosPrint.OnPrintFinished : An error occurred !"
                        )
                    }

                    override fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?) {
                        Log.i(
                            "Async.OnPrintFinished",
                            "AsyncEscPosPrint.OnPrintFinished : Print is finished !"
                        )
                    }
                }
            ).execute(printText(selectedDevice,value))
        }
    }

    private fun printText(printerConnection: DeviceConnection?,value: List<HashMap<String,String>>) : AsyncEscPosPrinter  {
//        val printer = EscPosPrinter(connection, 203, 48f, 32)
        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)

//        Log.d(_tag,"ready")
        val nmTmpUsaha = MainActivity.namaTempatUsaha
        val alamat = MainActivity.alamatTempatUsaha
        val namaPetugas = MainActivity.namaPetugas
//        val tgl = dateTime
        val tgl = tanggal
        var text = "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
            requireActivity().resources.getDrawableForDensity(R.drawable.ic_malang_makmur_grayscale,
                DisplayMetrics.DENSITY_LOW, requireActivity().theme
            )) + "</img>\n" +
                "[L]\n"+
                "[C]<b>$nmTmpUsaha</b>\n"+
                "[C]$alamat\n"+
                "[L]\n"+
                "[L]<b>Rekap Opname Harian</b>\n"+
                "[L]Kasir : $namaPetugas\n"+
                "[L]Tanggal : $tgl\n"+
                "[C]--------------------------------\n\n"

        var dblTotalTotalharga = 0.0
        value.forEach { it ->
            val totalQty = it["totalQty"]
            val nmProduk = it["nmProduk"]
            it["totalHarga"]?.let { a ->
                val dblTotalHarga = a.toDouble()
                val formatTotalOmzet = AddingIDRCurrency().formatIdrCurrencyNonKoma(dblTotalHarga)
//            println("$totalQty $nmProduk")
                text += "[L]- $nmProduk\n" +
                        "[L]  $totalQty [R]$formatTotalOmzet\n"
                dblTotalTotalharga += dblTotalHarga
            }
        }

//        for (i in itemProduk!!.indices) {
//            val nmProduk = itemProduk!![i].getNama_produk()
//            val qty = itemProduk!![i].getQty()
//            val harga = itemProduk!![i].getHarga()
//            val totalHarga = itemProduk!![i].getTotal_harga()
//            text += "[L]$nmProduk\n" +
//                    "[L] $harga x $qty[R]${gantiKetitik(totalHarga)}\n"
//        }
//
//        text += "[C]--------------------------------\n"+
//                "[L]Subtotal[R]${gantiKetitik(tvSubtotal?.text.toString())}\n"+
//                "[L]Disc[R]${tvJmlDisc?.text}\n"
        val formatDblTotalTotalharga = AddingIDRCurrency().formatIdrCurrencyNonKoma(dblTotalTotalharga)
        text += "[C]--------------------------------\n"+
                "[L]Total[R]$formatDblTotalTotalharga"
        return printer.addTextToPrint(text)
    }

//    private fun printTextHiburanKarcis(printerConnection: DeviceConnection?) : AsyncEscPosPrinter  {
//
//        val nominal = 1000
//        val nomorSeri = "2022-N1-0001-AA-00001"
//        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)
//        val nmTmpUsaha = MainActivity.namaTempatUsaha
//        val alamat = MainActivity.alamatTempatUsaha
//        val namaPetugas = MainActivity.namaPetugas
//        val tanggal = dateTime
//        var text = "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
//            requireActivity().applicationContext.resources.getDrawableForDensity(R.drawable.ic_malang_makmur_grayscale,
//                DisplayMetrics.DENSITY_LOW, requireActivity().theme
//            )) + "</img>\n" +
//                "[L]\n"+
//                "[C]<b>$nmTmpUsaha</b>\n"+
//                "[C]$alamat\n"+
//                "[L]\n"+
//                "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
//            TampilanBarcode().displayBitmap(requireContext(),nomorSeri)) + "</img>\n" +
//                "[L]\n"+
//                "[C]$nomorSeri\n" +
//                "[L]\n"+
//                "[L]Tanggal : $tanggal\n"+
//                "[L]Kasir   : $namaPetugas\n"+
//                "[C]--------------------------------\n" +
//                "[C]Nominal : $nominal\n" +
//                "[L]\n"+
//                "[C]Terima Kasih\n" +
//                "[C]Atas Kunjungan\n" +
//                "[C]Anda"
//
//        return printer.addTextToPrint(text)
//    }

    var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val a = data?.extras
//            Toast.makeText(requireContext(), "OK",Toast.LENGTH_SHORT).show()
            updateLabel()
        }
    }

    private fun kirimEmail(idPengguna : String?, idTmpUsaha : String?){
        val postServices = UploadPdfResultFeedback.create()
        postServices.sendPosts(idPengguna ?: ""
            ,idTmpUsaha ?: "", tanggal
        ).enqueue(object : Callback<DefaultPojo> {

            override fun onFailure(call: Call<DefaultPojo>, error: Throwable) {
                Log.e(_tag, "errornya ${error.message}")
            }

            override fun onResponse(
                call: Call<DefaultPojo>,
                response: retrofit2.Response<DefaultPojo>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        val feedback = it
                        println("${feedback.success}, ${feedback.message}")
                        if(feedback.success == 1) {
                            Toast.makeText(context,feedback.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun cetakFull(idPengguna : String?, idTmpUsaha : String?){
        val postServices = CetakFullResultFeedback.create()
        postServices.sendPosts(idPengguna ?: ""
            ,idTmpUsaha ?: "", tanggal
        ).enqueue(object : Callback<DefaultResultPojo> {

            override fun onFailure(call: Call<DefaultResultPojo>, error: Throwable) {
                Log.e(_tag, "errornya ${error.message}")
            }

            override fun onResponse(
                call: Call<DefaultResultPojo>,
                response: retrofit2.Response<DefaultResultPojo>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        val feedback = it
                        println("${feedback.success}, ${feedback.message}, ${feedback.result}")
                        if(feedback.success == 1) {
//                            Toast.makeText(context,feedback.message, Toast.LENGTH_SHORT).show()
                            if(feedback.result.size > 0) printBluetooth(feedback.result)
                            else Toast.makeText(context,R.string.data_kosong, Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(context,feedback.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_data_harian, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            R.id.action_menu_print -> {
//                kirimEmail(idPengguna, idTmpUsaha)
                cetakFull(idPengguna, idTmpUsaha)
                true
            } R.id.action_menu_email -> {
                kirimEmail(idPengguna, idTmpUsaha)
                true
            }R.id.action_menu_bantuan -> {
//                tampilAlertDialogTutorial()
                true
            }else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateLabel() {
        val myFormat = "dd-MM-yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        etDate.setText(sdf.format(myCalendar.time))
        tanggal = sdf.format(myCalendar.time)

        if(CheckNetwork().checkingNetwork(requireContext())) {
            val stringUrl = "${Url.getRekapHarian}?tgl="+tanggal+"&idTmpUsaha="+idTmpUsaha+"&idPengguna="+idPengguna
            Log.i(_tag,stringUrl)
            jsonTask = RekapHarianJsonTask(this).execute(stringUrl)
        } else {
            Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val current = Date()
        val frmt = SimpleDateFormat("dd-MM-yyyy",Locale.getDefault())
        return frmt.format(current)
    }

    override fun rekapHarianOnTask(result: String?) {
        if (result == null) {
//            Toast.makeText(context,R.string.error_get_data,Toast.LENGTH_SHORT).show()
            binding.ivIconDataKosong.visibility = View.VISIBLE
            return
        } else if (result == "") {
//            Toast.makeText(context,R.string.empty_data,Toast.LENGTH_SHORT).show()
            binding.ivIconDataKosong.visibility = View.VISIBLE
            return
        }

        Log.e("Debug", "Response from url:$result")
        try {
            val jsonObj = JSONObject(result)
            val success = jsonObj.getInt("success")
            val message = jsonObj.getString("message")

            itemsHarian = ArrayList()
            itemsHarian!!.clear()

            if (success == 1) {

                val rArray = jsonObj.getJSONArray("result")
                for (i in 0 until rArray.length()) {

                    val idTrx = rArray.getJSONObject(i).getInt("ID_TRX")
                    val disc_rp = rArray.getJSONObject(i).getInt("DISC_RP")
                    val omzet = rArray.getJSONObject(i).getInt("OMZET")
                    val tglTrx = rArray.getJSONObject(i).getString("TANGGAL_TRX")
                    val namaUserTrx = rArray.getJSONObject(i).getString("NAME")
                    val concatProduk = rArray.getJSONObject(i).getString("CONCATPRODUK")

                    itemsHarian?.add(
                        RekapHarianListElement(
                            idTrx,namaPetugas, 0, 0, disc_rp, omzet, tglTrx, concatProduk)
                    )
                }

                if (itemsHarian != null && itemsHarian!!.size > 0) {
                    var totalValue = 0.0
                    tempItemsHarian = itemsHarian!!
                    for(ttl in tempItemsHarian) {
                        totalValue += ttl.total
                    }
                    tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)
                }
                binding.ivIconDataKosong.visibility = View.GONE
            }else {
//                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                tempItemsHarian = itemsHarian!!
                tvTotal.text = AddingIDRCurrency().formatIdrCurrency(0.0)
                binding.ivIconDataKosong.visibility = View.VISIBLE
            }

            adapterList = context?.let {
                RekapHarianListAdapter(
                    this, this,
                    tempItemsHarian,
                    it
                )
            }
            recyclerView.adapter = adapterList
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.scheduleLayoutAnimation()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun dialogDetail(idTrx: String) {
        val mBuilder = AlertDialog.Builder(context)
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.dialog_detail_rekap_harian, null)

        val tvNoTrx: TextView = mView.findViewById(R.id.tvNoTrx) as TextView
        recyclerViewD = mView.findViewById(R.id.recyclerView) as RecyclerView

        tvNoTrx.text = idTrx
        mBuilder.setView(mView)
        val dialog = mBuilder.create()
        dialog.show()

        if(CheckNetwork().checkingNetwork(requireContext())) {
            val stringUrl = "${Url.getDRekapHarian}?id_trx="+idTrx
            Log.i(_tag,stringUrl)
            jsonTaskDetail = DRekapHarianJsonTask(this).execute(stringUrl)
        } else {
            Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
        }
    }

    override fun DrekapHarianOnTask(result: String?) {
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

            itemsDHarian = ArrayList()
            itemsDHarian!!.clear()

            if (success == 1) {

                val rArray = jsonObj.getJSONArray("result")
                for (i in 0 until rArray.length()) {

                    val nama = rArray.getJSONObject(i).getString("NAMA_BARANG")
                    val qty = rArray.getJSONObject(i).getInt("QTY")
                    val idDtx = rArray.getJSONObject(i).getInt("ID_DTX")
                    val harga = rArray.getJSONObject(i).getInt("HARGA")
                    val total = rArray.getJSONObject(i).getInt("TOTAL")

                    itemsDHarian?.add(
                        DRekapHarianListElement(
                            idDtx, nama, harga, qty, total)
                    )
                }

                itemsDHarian.let {

                }
                if (itemsDHarian != null && itemsDHarian!!.size > 0) {
//
                    var totalValue = 0.0

                    tempItemsDHarian = itemsDHarian!!
//                    for(ttl in tempItemsDHarian) {
//                        totalValue += ttl.total
//                    }
//                    tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)

                    adapterListD = context?.let {
                        DRekapHarianListAdapter2(
                            tempItemsDHarian,
                            it
                        )
                    }
                    recyclerViewD.adapter = adapterListD
                    recyclerViewD.layoutManager = LinearLayoutManager(context)
                }

            }else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }

    override fun rekapHarianDetailOnTask(result: String?) {
        result?.let { dialogDetail(it) }
    }

    override fun rekapHarianDetailLongClick(result: String?) {
        resultLauncher.launch(Intent(requireContext(), MainCetak::class.java).putExtra("getIdItem", result))
//        requireContext().startActivity(Intent(context, MainCetak::class.java).putExtra("getIdItem", result))
    }

    private val dateTime: String
        get() {
            val dateFormat: DateFormat =
                SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val date = Date()
            return dateFormat.format(date)
        }
}