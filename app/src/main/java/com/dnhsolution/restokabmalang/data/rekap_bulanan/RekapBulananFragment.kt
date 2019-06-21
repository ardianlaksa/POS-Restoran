package com.dnhsolution.restokabmalang.data.rekap_bulanan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency
import com.dnhsolution.restokabmalang.R

class RekapBulananFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(params: String) = RekapBulananFragment().apply {
            arguments = Bundle().apply {
                putString("params",params)
            }
        }
    }

    private val listBulan: HashMap<String,String>
        get(){
            val bln = HashMap<String,String>()
            bln["01"] = "Jan"
            bln["02"] = "Feb"
            bln["03"] = "Mar"
            bln["04"] = "Apr"
            bln["05"] = "Mei"
            bln["06"] = "Jun"
            bln["05"] = "Jul"
            bln["08"] = "Agu"
            bln["09"] = "Sep"
            bln["10"] = "Okt"
            bln["11"] = "Nov"
            bln["12"] = "Des"
            return bln
        }

    private var params = ""

    private var tempItemsBulanan = ArrayList<RekapBulananListElement>()
    private var spinThnArray = ArrayList<String>()
    private var spinBlnArray = ArrayList<RekapBulananBlnSpinElement>()
    private var adapterList:RekapBulananListAdapter? = null
    private var isOpenedThn = false
    private var isOpenedBln = false
    private var selectedBln = ""
    private var selectedThn = ""

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments?.getString("params")?.let { params = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rekap_bulanan, container, false)
        val spiBln = view.findViewById(R.id.spinBln) as Spinner
        val spiThn = view.findViewById(R.id.spinThn) as Spinner
        val recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView

        for (index in itemsBulanan.indices) {
            val tgl = (itemsBulanan[index].tgl).split("/")
            if (!spinThnArray.contains(tgl[0])) spinThnArray.add(tgl[0])

            for (itemBulan in listBulan) {
                if (itemBulan.key == tgl[1]) {
                    var isAda = false
                    spinBlnArray.forEach { event ->
                        if (event.idItem == tgl[1]) {
                            isAda = true
                        }
                    }
                    if (!isAda) spinBlnArray.add(RekapBulananBlnSpinElement(tgl[1], itemBulan.value))
                    break
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

        spiThn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isOpenedThn == false) {
                    isOpenedThn = true
                    return
                }

                selectedThn = spinThnArray[position]
                tempItemsBulanan = ArrayList()
                itemsBulanan.forEach { event ->
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
                itemsBulanan.forEach { event ->
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

        val tvTotal = view.findViewById(R.id.tvTotal) as TextView
        var totalValue = 0.0
        for(ttl in itemsBulanan) {
            totalValue += ttl.total
        }
        tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)

        val adapterList = context?.let {
            RekapBulananListAdapter(
                itemsBulanan,
                it
            )
        }
        recyclerView.adapter = adapterList
        recyclerView.layoutManager = (LinearLayoutManager(context))

        return view
    }

    private val itemsBulanan = arrayListOf(
        RekapBulananListElement(
            1, "2019/01/01", 16000, 0, 16000),
        RekapBulananListElement(
            2, "2018/02/02", 8000, 0, 8000),
        RekapBulananListElement(
            3, "2019/01/02", 100000, 0, 100000),
        RekapBulananListElement(
            4, "2016/03/03", 4500, 0, 4500),
        RekapBulananListElement(
            5, "2019/03/04", 30000, 0, 30000)
    )
}