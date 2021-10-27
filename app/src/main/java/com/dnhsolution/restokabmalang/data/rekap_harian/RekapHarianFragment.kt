package com.dnhsolution.restokabmalang.data.rekap_harian

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.data.rekap_harian.task.DRekapHarianJsonTask
import com.dnhsolution.restokabmalang.data.rekap_harian.task.RekapHarianJsonTask
import com.dnhsolution.restokabmalang.utilities.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RekapHarianFragment : Fragment(), DRekapHarianOnTask, RekapHarianOnTask, RekapHarianDetailOnTask {

    companion object {
        @JvmStatic
        fun newInstance(params: String) = RekapHarianFragment().apply {
            arguments = Bundle().apply {
                putString("params",params)
            }
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewD: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var spiTgl: Spinner
    private lateinit var etDate: EditText
    private lateinit var ivDate: ImageView

    private val myCalendar = Calendar.getInstance()

    private var itemsHarian:ArrayList<RekapHarianListElement>? = null
    private var itemsDHarian:ArrayList<DRekapHarianListElement>? = null
    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private var jsonTaskDetail: AsyncTask<String, Void, String?>? = null
    private var params = ""
    private var tanggal = ""
    private var idTmpUsaha = "-1"
    private var tempItemsHarian = ArrayList<RekapHarianListElement>()
    private var tempItemsDHarian = ArrayList<DRekapHarianListElement>()
    private var adapterList:RekapHarianListAdapter? = null
    private var adapterListD:DRekapHarianListAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("params")?.let { params = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rekap_harian, container, false)
        spiTgl = view.findViewById(R.id.spinTgl) as Spinner
        tvTotal = view.findViewById(R.id.tvTotal) as TextView
        ivDate = view.findViewById(R.id.ivDate) as ImageView
        etDate = view.findViewById(R.id.etDate) as EditText

        val sharedPreferences = context?.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences?.getString(Url.SESSION_ID_TEMPAT_USAHA, "").toString()

        recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView

        if(CheckNetwork().checkingNetwork(requireContext())) {
            val stringUrl = "${Url.getRekapHarian}?tgl="+getCurrentDate()+"&idTmpUsaha="+idTmpUsaha
            Log.i(_tag,stringUrl)
            jsonTask = RekapHarianJsonTask(this).execute(stringUrl)
        } else {
            Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
        }

        val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        ivDate.setOnClickListener(View.OnClickListener {
            // TODO Auto-generated method stub
            DatePickerDialog(
                requireContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        })

        etDate.setText(getCurrentDate())

        return view
    }

    private fun updateLabel() {
        val myFormat = "dd-MM-yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        etDate.setText(sdf.format(myCalendar.time))
        tanggal = sdf.format(myCalendar.time)

        if(CheckNetwork().checkingNetwork(requireContext())) {
            val stringUrl = "${Url.getRekapHarian}?tgl="+tanggal+"&idTmpUsaha="+idTmpUsaha
            Log.i(_tag,stringUrl)
            jsonTask = RekapHarianJsonTask(this).execute(stringUrl)
        } else {
            Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val current = Date()
        val frmt = SimpleDateFormat("dd-MM-yyyy")
        return frmt.format(current)
    }

    override fun rekapHarianOnTask(result: String?) {
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

            itemsHarian = ArrayList()
            itemsHarian!!.clear()

            if (success == 1) {

                val rArray = jsonObj.getJSONArray("result")
                for (i in 0 until rArray.length()) {

                    val idTrx = rArray.getJSONObject(i).getInt("ID_TRX")
                    val disc_rp = rArray.getJSONObject(i).getInt("DISC_RP")
                    val omzet = rArray.getJSONObject(i).getInt("OMZET")
                    val tglTrx = rArray.getJSONObject(i).getString("TANGGAL_TRX")

                    itemsHarian?.add(
                        RekapHarianListElement(
                            idTrx,"", 0, 0, disc_rp, omzet, tglTrx)
                    )
                }

                itemsHarian.let {

                }
                if (itemsHarian != null && itemsHarian!!.size > 0) {
                    var totalValue = 0.0
                    tempItemsHarian = itemsHarian!!
                    for(ttl in tempItemsHarian) {
                        totalValue += ttl.total
                    }
                    tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)

                    adapterList = context?.let {
                        RekapHarianListAdapter(
                            this,
                            tempItemsHarian,
                            it
                        )
                    }
                    recyclerView.adapter = adapterList
                    recyclerView.layoutManager = LinearLayoutManager(context)
                }

            }else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun dialogDetail(idTrx: String) {
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
                        DRekapHarianListAdapter(
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
}