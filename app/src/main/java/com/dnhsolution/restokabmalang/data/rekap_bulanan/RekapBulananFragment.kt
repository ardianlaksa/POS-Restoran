package com.dnhsolution.restokabmalang.data.rekap_bulanan

import android.app.DatePickerDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.data.rekap_bulanan.task.RekapBulananJsonTask
import com.dnhsolution.restokabmalang.databinding.FragmentRekapBulananBinding
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
import java.text.SimpleDateFormat
import java.util.*

class RekapBulananFragment : Fragment(), RekapBulananOnTask {

    interface UploadPdfServiceBulanan {
        @FormUrlEncoded
        @POST("${Url.serverPos}notifEmailPdfBulanan")
        fun sendPosts(
            @Field("idPengguna") idPengguna: String, @Field("idTmpUsaha") idTmpUsaha: String
            , @Field("tgl") tgl: String, @Field("tipeStruk") tipeStruk: String
        ): Call<DefaultPojo>
    }

    interface CetakFullServiceBulanan {
        @FormUrlEncoded
        @POST("${Url.serverPos}cetakFullBulanan")
        fun sendPosts(
            @Field("idPengguna") idPengguna: String, @Field("idTmpUsaha") idTmpUsaha: String
            , @Field("tgl") tgl: String
        ): Call<DefaultResultPojo>
    }

    object CetakFullBulananResultFeedback {

        fun create(): CetakFullServiceBulanan {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(Url.serverBase)
                .build()
            return retrofit.create(CetakFullServiceBulanan::class.java)
        }
    }

    object UploadPdfBulananResultFeedback {

        fun create(): UploadPdfServiceBulanan {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(Url.serverBase)
                .build()
            return retrofit.create(UploadPdfServiceBulanan::class.java)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(params: String) = RekapBulananFragment().apply {
            arguments = Bundle().apply {
                putString("params",params)
            }
        }
    }

    private var sdfFormatDate1: String = ""
    private var sdfFormatDate2: String = ""
    private var idPengguna: String? = null
    private var idTmpUsaha: String? = null
    private lateinit var tipeStruk: String
    private lateinit var tvTotal: TextView
    private lateinit var tvTotalPajak: TextView
    private lateinit var recyclerView: RecyclerView
    private val listBulan: HashMap<String,String>
        get(){
            val bln = HashMap<String,String>()
            bln["01"] = "Jan"
            bln["02"] = "Feb"
            bln["03"] = "Mar"
            bln["04"] = "Apr"
            bln["05"] = "Mei"
            bln["06"] = "Jun"
            bln["07"] = "Jul"
            bln["08"] = "Agu"
            bln["09"] = "Sep"
            bln["10"] = "Okt"
            bln["11"] = "Nov"
            bln["12"] = "Des"
            return bln
        }

    private var params = ""

    private lateinit var spiBln: Spinner
    private lateinit var spiThn: Spinner
    private var tempItemsBulanan = ArrayList<RekapBulananListElement>()
    private var itemsBulanan:ArrayList<RekapBulananListElement>? = null
    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private var spinThnArray = ArrayList<String>()
    private var spinBlnArray = ArrayList<RekapBulananBlnSpinElement>()
    private var adapterList:RekapBulananListAdapter? = null
    private var isOpenedThn = false
    private var isOpenedBln = false
    private var selectedBln = ""
    private var selectedThn = ""
    private var bulan = ""
    private var tahun = ""
    private lateinit var btnCari : Button
    private lateinit var btnReset : Button
    private lateinit var binding: FragmentRekapBulananBinding
    private val myCalendar = Calendar.getInstance()
    private var tanggal1 = ""
    private var tanggal2 = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("params")?.let { params = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRekapBulananBinding.inflate(layoutInflater)
        val view = binding.root
        setHasOptionsMenu(true)
//        val view = inflater.inflate(R.layout.fragment_rekap_bulanan, container, false)
        spiBln = binding.spinBln
        spiThn = binding.spinThn
        recyclerView = binding.recyclerView
        tvTotal = binding.tvTotal
        tvTotalPajak = binding.tvTotalPajak
        btnCari = binding.btnCari
        btnReset = binding.btnReset

        val sharedPreferences = context?.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        tipeStruk = sharedPreferences?.getString(Url.SESSION_TIPE_STRUK, "").toString()
        idTmpUsaha = MainActivity.idTempatUsaha
        idPengguna = MainActivity.idPengguna

        spiBln.setSelection(0)
        spiThn.setSelection(0)

        val date1 = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel1()
        }

        binding.ivDate1.setOnClickListener{
            DatePickerDialog(
                requireContext(), date1, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        tanggal1 = getCurrentDate()
        binding.etDate1.setText(tanggal1)

        val date2 = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel2()
        }

        binding.ivDate2.setOnClickListener{
            DatePickerDialog(
                requireContext(), date2, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        tanggal2 = getCurrentDate()
        binding.etDate2.setText(getCurrentDate())

//        btnCari.setOnClickListener{
//
//            var totalValue = 0.0
//            var totalPajakValue = 0.0
//            tempItemsBulanan = ArrayList()
//            itemsBulanan!!.forEach { event ->
//                val tgl = (event.tgl).split("-")
//                if (selectedThn == tgl[0] && selectedBln == tgl[1]) {
//                    tempItemsBulanan.add(event)
//                    totalValue += event.omzet
//                    totalPajakValue += event.pajak
//                }
//            }
//
//            tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)
//            tvTotalPajak.text = AddingIDRCurrency().formatIdrCurrency(totalPajakValue)
//
//            adapterList = context?.let {
//                RekapBulananListAdapter(
//                    tempItemsBulanan,
//                    it
//                )
//            }
//
//            recyclerView.adapter = adapterList
//            recyclerView.scheduleLayoutAnimation()
//            btnReset.visibility = View.VISIBLE
//        }
//
//        btnReset.setOnClickListener{
//            spiBln.setSelection(0)
//            spiThn.setSelection(0)
//            selectedThn = "-1"
//            selectedBln = "-1"
//
//            var totalValue = 0.0
//            var totalPajakValue = 0.0
//            tempItemsBulanan = ArrayList()
//            itemsBulanan!!.forEach { event ->
//                tempItemsBulanan.add(event)
//                totalValue += event.omzet
//                totalPajakValue += event.pajak
//            }
//            tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)
//            tvTotalPajak.text = AddingIDRCurrency().formatIdrCurrency(totalPajakValue)
//            adapterList = context?.let {
//                RekapBulananListAdapter(
//                    tempItemsBulanan,
//                    it
//                )
//            }
//            recyclerView.adapter = adapterList
//            recyclerView.scheduleLayoutAnimation()
//            btnReset.visibility = View.GONE
//        }
//
//        spiThn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                if (!isOpenedThn) {
//                    isOpenedThn = true
//                    return
//                }
//
//                selectedThn = spinThnArray[position]
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//            }
//
//        }
//
//        spiBln.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                if (!isOpenedBln) {
//                    isOpenedBln = true
//                    return
//                }
//                selectedBln = spinBlnArray[position].idItem
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//            }
//
//        }

        return view
    }

    private fun updateLabel1() {
        val myFormat = "dd-MM-yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        sdfFormatDate1 = sdf.format(myCalendar.time)
        binding.etDate1.setText(sdfFormatDate1)
        tanggal1 = sdfFormatDate1

        if(tanggal2 == "") return

        val strDate = sdf.parse(tanggal2)
        if (myCalendar.time.after(strDate)) {
            Toast.makeText(requireContext(),R.string.tanggal_tidak_sesuai,Toast.LENGTH_SHORT).show()
            return
        }

        if(CheckNetwork().checkingNetwork(requireContext())) {
            val stringUrl = "${Url.getRekapBulanan}?filterTanggal1=$tanggal1&filterTanggal2=$tanggal2" +
                    "&idTmpUsaha=$idTmpUsaha&tipeStruk=$tipeStruk&idPengguna=$idPengguna"
            Log.i(_tag,stringUrl)
            jsonTask = RekapBulananJsonTask(this).execute(stringUrl)
        } else {
//            Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
            binding.ivIconDataKosong.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_conn_lost,null))
        }
    }

    private fun updateLabel2() {
        val myFormat = "dd-MM-yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        sdfFormatDate2 = sdf.format(myCalendar.time)
        binding.etDate2.setText(sdfFormatDate2)
        tanggal2 = sdfFormatDate2

        if(tanggal1 == "") return

        val strDate = sdf.parse(tanggal1)
        if (myCalendar.time.before(strDate)) {
            Toast.makeText(requireContext(),R.string.tanggal_tidak_sesuai,Toast.LENGTH_SHORT).show()
            return
        }

        if(CheckNetwork().checkingNetwork(requireContext())) {
            val stringUrl = "${Url.getRekapBulanan}?filterTanggal1=$tanggal1&filterTanggal2=$tanggal2" +
                    "&idTmpUsaha=$idTmpUsaha&tipeStruk=$tipeStruk&idPengguna=$idPengguna"
            Log.i(_tag,stringUrl)
            jsonTask = RekapBulananJsonTask(this).execute(stringUrl)
        } else {
//            Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
            binding.ivIconDataKosong.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_conn_lost,null))
        }
    }

    override fun rekapBulananOnTask(result: String?) {
        if (result == null) {
//            Toast.makeText(context,R.string.error_get_data,Toast.LENGTH_SHORT).show()
            binding.ivIconDataKosong.visibility = View.VISIBLE
            return
        } else if (result == "") {
//            Toast.makeText(context,R.string.empty_data,Toast.LENGTH_SHORT).show()
            binding.ivIconDataKosong.visibility = View.VISIBLE
            return
        }

        Log.e("$_tag : rekapBulananOnTask", "Response from url:$result")
        try {
            val jsonObj = JSONObject(result)
            val success = jsonObj.getInt("success")
            val message = jsonObj.getString("message")

            tempItemsBulanan = ArrayList()
            itemsBulanan = ArrayList()
            if (success == 1) {
//                itemsBulanan?.clear()


                val rArray = jsonObj.getJSONArray("result")
                for (i in 0 until rArray.length()) {

                    val idTrx = rArray.getJSONObject(i).getInt("ID_TRX")
                    val disc = rArray.getJSONObject(i).getInt("DISC")
                    val omzet = rArray.getJSONObject(i).getInt("OMZET")
                    val tglTrx = rArray.getJSONObject(i).getString("TANGGAL_TRX")
                    val totalPajakIcl = rArray.getJSONObject(i).getInt("TOTAL_PAJAK_ICL")
                    val totalPajakExc = rArray.getJSONObject(i).getInt("TOTAL_PAJAK_EXC")

                    val totalPajak = if(tipeStruk == "1") totalPajakExc
                    else totalPajakIcl

                    itemsBulanan?.add(
                        RekapBulananListElement(
                            idTrx,tglTrx, omzet, disc, totalPajak)
                    )
                }

//                    spinBlnArray.clear()
//                    spinThnArray.clear()
//                    for (index in itemsBulanan!!.indices) {
//                        val stringTgl = itemsBulanan!![index].tgl
//                        if (stringTgl.isNotEmpty()) {
//                            val listTgl = (itemsBulanan!![index].tgl).split("-")
//                            if (!spinThnArray.contains(listTgl[0])) spinThnArray.add(listTgl[0])
//
//                            println("Tahun : "+listTgl[0])
//                            for (itemBulan in listBulan) {
//                                if (itemBulan.key == listTgl[1]) {
//                                    var isAda = false
//                                    spinBlnArray.forEach { event ->
//                                        if (event.idItem == listTgl[1]) {
//                                            isAda = true
//                                            println("Bulan : "+listTgl[1])
//                                        }
//                                    }
//                                    if (!isAda) spinBlnArray.add(RekapBulananBlnSpinElement(listTgl[1], itemBulan.value))
//                                    break
//                                }
//                            }
//                        }
//                    }
//
//                    val spinThnAdapter = context?.let {
//                        RekapBulananThnSpinAdapter(
//                            it,
//                            R.layout.item_spi_bulan,
//                            spinThnArray
//                        )
//                    }
//
//                    spiThn.adapter = spinThnAdapter
//
//                    val spinBlnAdapter = context?.let {
//                        RekapBulananBlnSpinAdapter(
//                            it,
//                            R.layout.item_spi_bulan,
//                            spinBlnArray
//                        )
//                    }
//
//                    spiBln.adapter = spinBlnAdapter

                    var totalValue = 0.0
                    var totalPajakValue = 0.0

                    itemsBulanan!!.forEach { event ->
                        tempItemsBulanan.add(event)
                        totalValue += event.omzet
                        totalPajakValue += event.pajak
                    }

                    tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)
                    tvTotalPajak.text = AddingIDRCurrency().formatIdrCurrency(totalPajakValue)

                    adapterList = context?.let {
                        RekapBulananListAdapter(
                            tempItemsBulanan,
                            it
                        )
                    }

                recyclerView.adapter = adapterList
                recyclerView.scheduleLayoutAnimation()
//                }

                binding.ivIconDataKosong.visibility = View.GONE
            } else {
                tvTotal.text = AddingIDRCurrency().formatIdrCurrency(0.0)
                tvTotalPajak.text = AddingIDRCurrency().formatIdrCurrency(0.0)
                adapterList = context?.let {
                    RekapBulananListAdapter(
                        tempItemsBulanan,
                        it
                    )
                }
                recyclerView.adapter = adapterList
//                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                binding.ivIconDataKosong.visibility = View.VISIBLE
            }
            recyclerView.layoutManager = (LinearLayoutManager(context))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun getCurrentDate(): String {
        val current = Date()
        val frmt = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return frmt.format(current)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_data_harian, menu)
        val item: MenuItem = menu.findItem(R.id.action_menu_print)
        item.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            R.id.action_menu_print -> {
//                cetakFull(idPengguna, idTmpUsaha)
                true
            } R.id.action_menu_email -> {
//                Log.d(_tag,tipeStruk)
                kirimEmail(idPengguna, idTmpUsaha)
                true
            }R.id.action_menu_bantuan -> {
//                tampilAlertDialogTutorial()
                true
            }else -> super.onOptionsItemSelected(item)
        }
    }

    private fun kirimEmail(idPengguna : String?, idTmpUsaha : String?){
        val postServices = UploadPdfBulananResultFeedback.create()
        postServices.sendPosts(idPengguna ?: ""
            ,idTmpUsaha ?: "", "$tanggal1|$tanggal2",tipeStruk
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

//    private fun cetakFull(idPengguna : String?, idTmpUsaha : String?){
//        val postServices = CetakFullResultFeedback.create()
//        postServices.sendPosts(idPengguna ?: ""
//            ,idTmpUsaha ?: "", "$tanggal1|$tanggal2"
//        ).enqueue(object : Callback<DefaultResultPojo> {
//
//            override fun onFailure(call: Call<DefaultResultPojo>, error: Throwable) {
//                Log.e(_tag, "errornya ${error.message}")
//            }
//
//            override fun onResponse(
//                call: Call<DefaultResultPojo>,
//                response: retrofit2.Response<DefaultResultPojo>
//            ) {
//                if (response.isSuccessful) {
//                    val data = response.body()
//                    data?.let {
//                        val feedback = it
//                        println("${feedback.success}, ${feedback.message}, ${feedback.result}")
//                        if(feedback.success == 1) {
////                            Toast.makeText(context,feedback.message, Toast.LENGTH_SHORT).show()
//                            if(feedback.result.size > 0) printBluetooth(feedback.result)
//                            else Toast.makeText(context,R.string.data_kosong, Toast.LENGTH_SHORT).show()
//                        }else {
//                            Toast.makeText(context,feedback.message, Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            }
//        })
//    }
}