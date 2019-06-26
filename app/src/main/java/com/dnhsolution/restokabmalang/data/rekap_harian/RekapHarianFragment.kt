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
import com.dnhsolution.restokabmalang.data.rekap_harian.task.RekapHarianJsonTask
import com.dnhsolution.restokabmalang.utilities.CheckNetwork
import com.dnhsolution.restokabmalang.utilities.Url

class RekapHarianFragment : Fragment(), RekapHarianOnTask {

    companion object {
        @JvmStatic
        fun newInstance(params: String) = RekapHarianFragment().apply {
            arguments = Bundle().apply {
                putString("params",params)
            }
        }
    }

    private val _tag = javaClass.simpleName
    private var jsonTask: AsyncTask<String, Void, String?>? = null
    private var params = ""
    private var tempItemsHarian = ArrayList<RekapHarianListElement>()
    private var spinTglArray = ArrayList<String>()
    private var adapterList:RekapHarianListAdapter? = null

    private var isOpened = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments?.getString("params")?.let { params = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rekap_harian, container, false)
        val spiTgl = view.findViewById(R.id.spinTgl) as Spinner
        val tvTotal = view.findViewById(R.id.tvTotal) as TextView
        val recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView

        if(CheckNetwork().checkingNetwork(context!!)) {
            val stringUrl = "${Url.getRekapHarian}?value=1"
            Log.i(_tag,stringUrl)
            jsonTask = RekapHarianJsonTask(this).execute(stringUrl)
        } else {
            Toast.makeText(context, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
        }

        for (item in itemsHarian) {
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

        spiTgl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isOpened == false) {
                    isOpened = true
                    return
                }
                tempItemsHarian = ArrayList()
                for (item in itemsHarian) {
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

        var totalValue = 0.0

        tempItemsHarian = itemsHarian
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
    }

    private val itemsHarian = arrayListOf(
        RekapHarianListElement(
            1, "Ayam Bakar Madu", 16000, 1, 0, 16000
        , "2019/01/01"),
        RekapHarianListElement(
            1, "Ayam Goreng Crispy", 15000, 1, 0, 15000
            , "2019/01/02"),
        RekapHarianListElement(
            1, "Es Campur", 8000, 1, 0, 8000
            , "2019/01/02"),
        RekapHarianListElement(
            1, "Jus Tomat", 8000, 1, 0, 8000
            , "2019/01/03")
    )
}