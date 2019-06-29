package com.dnhsolution.restokabmalang.data.rekap_bulanan

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.RekapBulananOnTask
import com.dnhsolution.restokabmalang.data.rekap_bulanan.task.RekapBulananJsonTask
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.Url
import org.json.JSONException
import org.json.JSONObject

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
    private lateinit var recyclerView: RecyclerView
    private val listBulan: HashMap<String,String>
        get(){
            val bln = HashMap<String,String>()
            bln["JAN"] = "Jan"
            bln["FEB"] = "Feb"
            bln["MAR"] = "Mar"
            bln["APR"] = "Apr"
            bln["MAY"] = "Mei"
            bln["JUN"] = "Jun"
            bln["JUL"] = "Jul"
            bln["AUG"] = "Agu"
            bln["SEP"] = "Sep"
            bln["OCT"] = "Okt"
            bln["NOV"] = "Nov"
            bln["DES"] = "Des"
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
    private var selectedBln = ""
    private var selectedThn = ""
    private var bulan = "1"
    private var tahun = "1"
    private var idTempatUsaha = "1"

    override fun onAttach(context: Context?) {
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

        if(CheckNetwork().checkingNetwork(context!!)) {
            val stringUrl = "${Url.getRekapBulanan}?BULAN=$bulan&&TAHUN=$tahun&&ID_TEMPAT_USAHA=$idTempatUsaha"
            Log.i(_tag,stringUrl)
            jsonTask = RekapBulananJsonTask(this).execute(stringUrl)
        } else {
            Toast.makeText(context, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
        }

        spiThn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isOpenedThn == false) {
                    isOpenedThn = true
                    return
                }

                selectedThn = spinThnArray[position]
                tempItemsBulanan = ArrayList()
                itemsBulanan!!.forEach { event ->
                    val tgl = (event.tgl).split("/")
                    if (selectedThn == tgl[0] && selectedBln == tgl[1]) {
                        tempItemsBulanan.add(event)
                    }
                }

                adapterList = context?.let {
                    RekapBulananListAdapter(
                        tempItemsBulanan,
                        it
                    )
                }

                recyclerView.adapter = adapterList
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }

        val spinBlnAdapter = context?.let {
            RekapBulananBlnSpinAdapter(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                spinBlnArray
            )
        }

        spiBln.adapter = spinBlnAdapter

        spiBln.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isOpenedBln == false) {
                    isOpenedBln = true
                    return
                }

                selectedBln = spinBlnArray[position].idItem
                tempItemsBulanan = ArrayList()
                itemsBulanan!!.forEach { event ->
                    val tgl = (event.tgl).split("/")
                    if (selectedBln == tgl[1] && selectedThn == tgl[0]) {
                        tempItemsBulanan.add(event)
                    }
                }

                adapterList = context?.let {
                    RekapBulananListAdapter(
                        tempItemsBulanan,
                        it
                    )
                }

                recyclerView.adapter = adapterList
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
            if (success == 1) {

                itemsBulanan = ArrayList()

                val rArray = jsonObj.getJSONArray("result")
                for (i in 0 until rArray.length()) {

                    val idTrx = rArray.getJSONObject(i).getInt("ID_TRX")
                    val disc = rArray.getJSONObject(i).getInt("DISC")
                    val omzet = rArray.getJSONObject(i).getInt("OMZET")
                    val tglTrx = rArray.getJSONObject(i).getString("TANGGAL_TRX")

                    itemsBulanan?.add(
                        RekapBulananListElement(
                            idTrx,tglTrx, omzet, disc)
                    )
                }


                if (itemsBulanan != null && itemsBulanan!!.size > 0) {
                    for (index in itemsBulanan!!.indices) {
                        val stringTgl = itemsBulanan!![index].tgl
                        if (!stringTgl.isEmpty()) {
                            val listTgl = (itemsBulanan!![index].tgl).split("-")
                            if (!spinThnArray.contains(listTgl[2])) spinThnArray.add(listTgl[2])

                            for (itemBulan in listBulan) {
                                if (itemBulan.key == listTgl[1]) {
                                    var isAda = false
                                    spinBlnArray.forEach { event ->
                                        if (event.idItem == listTgl[1]) {
                                            isAda = true
                                        }
                                    }
                                    if (!isAda) spinBlnArray.add(RekapBulananBlnSpinElement(listTgl[1], itemBulan.value))
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

                    var totalValue = 0.0
                    for(ttl in itemsBulanan!!) {
                        totalValue += ttl.omzet
                    }
                    tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)

                    val adapterList = context?.let {
                        RekapBulananListAdapter(
                            itemsBulanan!!,
                            it
                        )
                    }
                    recyclerView.adapter = adapterList
                    recyclerView.layoutManager = (LinearLayoutManager(context))
                }

            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }
}