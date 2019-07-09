package com.dnhsolution.restokabmalang.data.rekap_harian

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
import com.dnhsolution.restokabmalang.utilities.RekapHarianOnTask
import com.dnhsolution.restokabmalang.data.rekap_harian.task.RekapHarianJsonTask
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.Url
import org.json.JSONException
import org.json.JSONObject

class RekapHarianFragment : Fragment(), RekapHarianOnTask {

    companion object {
        @JvmStatic
        fun newInstance(params: String) = RekapHarianFragment().apply {
            arguments = Bundle().apply {
                putString("params",params)
            }
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var spiTgl: Spinner

    private var itemsHarian:ArrayList<RekapHarianListElement>? = null
    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private var params = ""
    private var tempItemsHarian = ArrayList<RekapHarianListElement>()
    private var spinTglArray = ArrayList<String>()
    private var adapterList:RekapHarianListAdapter? = null

    private var isOpened = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("params")?.let { params = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rekap_harian, container, false)
        spiTgl = view.findViewById(R.id.spinTgl) as Spinner
        tvTotal = view.findViewById(R.id.tvTotal) as TextView
        recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView

        if(CheckNetwork().checkingNetwork(context!!)) {
            val stringUrl = "${Url.getRekapHarian}?tgl=1"
            Log.i(_tag,stringUrl)
            jsonTask = RekapHarianJsonTask(this).execute(stringUrl)
        } else {
            Toast.makeText(context, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
        }

        spiTgl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isOpened) {
                    isOpened = true
                    return
                }

                tempItemsHarian = ArrayList()

                for (item in itemsHarian!!) {
                    val tgl = spinTglArray[position]
                    if (item.tgl == tgl) tempItemsHarian.add(item)
                }
//                adapterList?.notifyDataSetChanged()
                adapterList = context?.let {
                    RekapHarianListAdapter(
                        tempItemsHarian,
                        it
                    )
                }
                recyclerView.adapter = adapterList
            }
        }

        return view
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
            if (success == 1) {

                itemsHarian = ArrayList()

                val rArray = jsonObj.getJSONArray("result")
                for (i in 0 until rArray.length()) {

                    val idDtx = rArray.getJSONObject(i).getInt("ID_DTX")
                    val nmBarang = rArray.getJSONObject(i).getString("NAMA_BARANG")
                    val qty = rArray.getJSONObject(i).getInt("QTY")
                    val disc = rArray.getJSONObject(i).getInt("DISC")
                    val harga = rArray.getJSONObject(i).getInt("HARGA")
                    val tglTrx = rArray.getJSONObject(i).getString("TANGGAL_TRX")

                    itemsHarian?.add(
                        RekapHarianListElement(
                            idDtx,nmBarang, harga, qty, disc, qty*harga, tglTrx)
                    )
                }

                itemsHarian.let {

                }
                if (itemsHarian != null && itemsHarian!!.size > 0) {
                    for (item in itemsHarian!!) {
                        val tgl = item.tgl
                        if (!spinTglArray.contains(tgl)) spinTglArray.add(tgl)
                    }

                    val spinTglAdapter = context?.let {
                        RekapHarianSpinAdapter(
                            it,
                            android.R.layout.simple_spinner_dropdown_item,
                            spinTglArray
                        )
                    }

                    spiTgl.adapter = spinTglAdapter

                    var totalValue = 0.0

                    tempItemsHarian = itemsHarian!!
                    for(ttl in tempItemsHarian) {
                        totalValue += ttl.total
                    }
                    tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)

                    adapterList = context?.let {
                        RekapHarianListAdapter(
                            tempItemsHarian,
                            it
                        )
                    }
                    recyclerView.adapter = adapterList
                    recyclerView.layoutManager = LinearLayoutManager(context)
                }

            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
        }
    }

//    private val itemsHarian = arrayListOf(
//        RekapHarianListElement(
//            1, "Ayam Bakar Madu", 16000, 1, 0, 16000
//        , "2019/01/01"),
//        RekapHarianListElement(
//            1, "Ayam Goreng Crispy", 15000, 1, 0, 15000
//            , "2019/01/02"),
//        RekapHarianListElement(
//            1, "Es Campur", 8000, 1, 0, 8000
//            , "2019/01/02"),
//        RekapHarianListElement(
//            1, "Jus Tomat", 8000, 1, 0, 8000
//            , "2019/01/03")
//    )
}