package com.dnhsolution.restokabmalang.data.rekap_harian

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
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
import java.text.SimpleDateFormat
import java.util.*

interface UploadPdfService {
        @FormUrlEncoded
        @POST("pdrd/Android/AndroidJsonPOS/notifEmailPdf")
        fun sendPosts(
            @Field("idPengguna") idPengguna: String, @Field("idTmpUsaha") idTmpUsaha: String
            , @Field("tgl") tgl: String
        ): Call<DefaultPojo>
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("params")?.let { params = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.fragment_rekap_harian, container, false)
        binding = FragmentRekapHarianBinding.inflate(layoutInflater)
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
            R.id.action_menu_email -> {
                kirimEmail(idPengguna, idTmpUsaha)
                true
            } R.id.action_menu_bantuan -> {
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
                            idTrx,namaUserTrx, 0, 0, disc_rp, omzet, tglTrx, concatProduk)
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
}