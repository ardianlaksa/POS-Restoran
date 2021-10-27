package com.dnhsolution.restokabmalang.data.rekap_bulanan

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.utilities.RekapBulananOnTask
import com.dnhsolution.restokabmalang.data.rekap_bulanan.task.RekapBulananJsonTask
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.Url
import kotlinx.android.synthetic.main.fragment_rekap_bulanan.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RekapBulananFragment : Fragment(), RekapBulananOnTask {

    companion object {
        @JvmStatic
        fun newInstance(params: String) = RekapBulananFragment().apply {
            arguments = Bundle().apply {
                putString("params",params)
            }
        }
    }

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
    private var itemsBulanan:ArrayList<RekapBulananListElement>? = null
    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private var tempItemsBulanan = ArrayList<RekapBulananListElement>()
    private var spinThnArray = ArrayList<String>()
    private var spinBlnArray = ArrayList<RekapBulananBlnSpinElement>()
    private var adapterList:RekapBulananListAdapter? = null
    private var isOpenedThn = false
    private var isOpenedBln = false
    private var selectedBln = "0"
    private var selectedThn = "Tahun"
    private var bulan = "1"
    private var tahun = "1"
    private var idTmpUsaha = "-1"
    private lateinit var btnCari : Button
    private lateinit var btnReset : Button

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("params")?.let { params = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rekap_bulanan, container, false)
        spiBln = view.findViewById(R.id.spinBln) as Spinner
        spiThn = view.findViewById(R.id.spinThn) as Spinner
        recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView

        tvTotal = view.findViewById(R.id.tvTotal) as TextView
        tvTotalPajak = view.findViewById(R.id.tvTotalPajak) as TextView
        btnCari = view.findViewById(R.id.btnCari) as Button
        btnReset = view.findViewById(R.id.btnReset) as Button


        val sharedPreferences = context?.getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences?.getString(Url.SESSION_ID_TEMPAT_USAHA, "").toString()
        val tipeStruk = sharedPreferences?.getString(Url.SESSION_TIPE_STRUK, "").toString()

        spiBln.setSelection(0)
        spiThn.setSelection(0)

        if(CheckNetwork().checkingNetwork(requireContext())) {
            val stringUrl = "${Url.getRekapBulanan}?BULAN=$bulan&TAHUN=$tahun&idTmpUsaha=$idTmpUsaha&tipeStruk=$tipeStruk"
            Log.i(_tag,stringUrl)
            jsonTask = RekapBulananJsonTask(this).execute(stringUrl)
        } else {
            Toast.makeText(context, getString(R.string.tidak_terkoneksi_internet), Toast.LENGTH_SHORT).show()
        }

        btnCari.setOnClickListener{

            var totalValue = 0.0
            tempItemsBulanan = ArrayList()
            itemsBulanan!!.forEach { event ->
                val tgl = (event.tgl).split("-")
                println("TAHUN : "+ tgl[2]+", BULAN : "+tgl[0]+", OMZET"+event.omzet)
                if (selectedThn == tgl[2] && selectedBln == tgl[0]) {
                    tempItemsBulanan.add(event)
                    totalValue += event.omzet
                    println("TAHUN : "+ tgl[2]+", BULAN : "+tgl[0]+", OMZET"+event.omzet)
                }
            }

            tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)
            tvTotalPajak.text = "Perbaikan"//AddingIDRCurrency().formatIdrCurrency(totalValue)

            adapterList = context?.let {
                RekapBulananListAdapter(
                    tempItemsBulanan,
                    it
                )
            }
            recyclerView.adapter = adapterList

            btnReset.visibility = View.VISIBLE
        }

        btnReset.setOnClickListener{
            spiBln.setSelection(0)
            spiThn.setSelection(0)

            val today = getCurrentDate().split("-")
            selectedThn = today[2]
            selectedBln = today[0]

            var totalValue = 0.0
            tempItemsBulanan = ArrayList()

            itemsBulanan!!.forEach { event ->
                val tgl = (event.tgl).split("-")
                println("TAHUN : "+ tgl[2]+", BULAN : "+tgl[0]+", OMZET"+event.omzet)
                if (selectedThn == tgl[2] && selectedBln == tgl[0]) {
                    tempItemsBulanan.add(event)
                    totalValue += event.omzet
                    println("TAHUN : "+ tgl[2]+", BULAN : "+tgl[0]+", OMZET"+event.omzet)
                }
            }
            tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)
            tvTotalPajak.text = "Perbaikan"//AddingIDRCurrency().formatIdrCurrency(totalValue)
            adapterList = context?.let {
                RekapBulananListAdapter(
                    tempItemsBulanan,
                    it
                )
            }
            recyclerView.adapter = adapterList
            btnReset.visibility = View.GONE
        }

        spiThn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isOpenedThn) {
                    isOpenedThn = true
                    return
                }

                selectedThn = spinThnArray[position]

               //Toast.makeText(context, selectedThn+", "+selectedBln, Toast.LENGTH_SHORT).show()
//                tempItemsBulanan = ArrayList()
//                itemsBulanan!!.forEach { event ->
//                    val tgl = (event.tgl).split("/")
//                    if (selectedThn == tgl[0] && selectedBln == tgl[1]) {
//                        tempItemsBulanan.add(event)
//                    }
//                }
//
//                adapterList = context?.let {
//                    RekapBulananListAdapter(
//                        tempItemsBulanan,
//                        it
//                    )
//                }
//
//                recyclerView.adapter = adapterList
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        spiBln.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isOpenedBln) {
                    isOpenedBln = true
                    return
                }
                selectedBln = spinBlnArray[position].idItem

//                tempItemsBulanan = ArrayList()
//                itemsBulanan!!.forEach { event ->
//                    val tgl = (event.tgl).split("/")
//                    if (selectedBln == tgl[1] && selectedThn == tgl[0]) {
//                        tempItemsBulanan.add(event)
//                    }
//                }
//
//                adapterList = context?.let {
//                    RekapBulananListAdapter(
//                        tempItemsBulanan,
//                        it
//                    )
//                }
//
//                recyclerView.adapter = adapterList
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        return view
    }

    override fun rekapBulananOnTask(result: String?) {
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

            itemsBulanan = ArrayList()
            itemsBulanan!!.clear()

            if (success == 1) {
                val rArray = jsonObj.getJSONArray("result")
                for (i in 0 until rArray.length()) {

                    val idTrx = rArray.getJSONObject(i).getInt("ID_TRX")
                    val disc = rArray.getJSONObject(i).getInt("DISC")
                    val omzet = rArray.getJSONObject(i).getInt("OMZET")
                    val tglTrx = rArray.getJSONObject(i).getString("TANGGAL_TRX")
                    val totalPajak = rArray.getJSONObject(i).getInt("TOTAL_PAJAK")

                    itemsBulanan?.add(
                        RekapBulananListElement(
                            idTrx,tglTrx, omzet, disc, totalPajak)
                    )
                }


                if (itemsBulanan != null && itemsBulanan!!.size > 0) {
                    spinBlnArray.clear()
                    spinThnArray.clear()
//                    spinThnArray.add("Tahun")
//                    spinBlnArray.add(RekapBulananBlnSpinElement("0", "Bulan"))
                    for (index in itemsBulanan!!.indices) {
                        val stringTgl = itemsBulanan!![index].tgl
                        if (stringTgl.isNotEmpty()) {
                            val listTgl = (itemsBulanan!![index].tgl).split("-")
                            if (!spinThnArray.contains(listTgl[2])) spinThnArray.add(listTgl[2])

                            println("Tahun : "+listTgl[2])
                            for (itemBulan in listBulan) {
                                if (itemBulan.key == listTgl[0]) {
                                    var isAda = false
                                    spinBlnArray.forEach { event ->
                                        if (event.idItem == listTgl[0]) {
                                            isAda = true
                                            println("Bulan : "+listTgl[0])
                                        }
                                    }
                                    if (!isAda) spinBlnArray.add(RekapBulananBlnSpinElement(listTgl[0], itemBulan.value))
                                    break
                                }
                            }
                        }
                    }

                    val spinThnAdapter = context?.let {
                        RekapBulananThnSpinAdapter(
                            it,
                            android.R.layout.simple_spinner_dropdown_item,
                            spinThnArray
                        )
                    }

                    spiThn.adapter = spinThnAdapter

                    val spinBlnAdapter = context?.let {
                        RekapBulananBlnSpinAdapter(
                            it,
                            R.layout.item_spi_bulan,
                            spinBlnArray
                        )
                    }

                    spiBln.adapter = spinBlnAdapter

                    var totalValue = 0.0
                    var totalPajakValue = 0.0
                    val today = getCurrentDate().split("-")
                    selectedThn = today[2]
                    selectedBln = today[0]

                    itemsBulanan!!.forEach { event ->
                        val tgl = (event.tgl).split("-")
                        println("TAHUN : "+ tgl[2]+", BULAN : "+tgl[0]+", OMZET"+event.omzet)
                        if (selectedThn == tgl[2] && selectedBln == tgl[0]) {
                            tempItemsBulanan.add(event)
                            totalValue += event.omzet
                            totalPajakValue += event.pajak
                            println("TAHUN : "+ tgl[2]+", BULAN : "+tgl[0]+", OMZET"+event.omzet)
                        }
                    }

                    tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)
                    tvTotalPajak.text = AddingIDRCurrency().formatIdrCurrency(totalPajakValue)

                    val adapterList = context?.let {
                        RekapBulananListAdapter(
                            tempItemsBulanan,
                            it
                        )
                    }

//                    for(ttl in itemsBulanan!!) {
//                        totalValue += ttl.omzet
//                    }
//                    tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)
//
//                    val adapterList = context?.let {
//                        RekapBulananListAdapter(
//                            itemsBulanan!!,
//                            it
//                        )
//                    }
                    recyclerView.adapter = adapterList
                    recyclerView.layoutManager = (LinearLayoutManager(context))
                }

            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            //Toast.makeText(context, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val current = Date()
        val frmt = SimpleDateFormat("MM-dd-yyyy")
        return frmt.format(current)
    }
}