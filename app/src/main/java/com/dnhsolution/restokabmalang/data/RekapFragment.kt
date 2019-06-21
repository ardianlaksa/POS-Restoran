package com.dnhsolution.restokabmalang.data

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.AddingIDRCurrency
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.data.rekap_bulanan.RekapBulananListAdapter
import com.dnhsolution.restokabmalang.data.rekap_bulanan.RekapBulananListElement
import com.dnhsolution.restokabmalang.data.rekap_harian.RekapHarianListElement
import com.dnhsolution.restokabmalang.data.rekap_harian.RekapHarianListAdapter

class RekapFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(params: String) = RekapFragment().apply {
            arguments = Bundle().apply {
                putString("params",params)
            }
        }
    }

    private var params = ""

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments?.getString("params")?.let { params = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rekap_harian, container, false)
        val tvTotal = view.findViewById(R.id.tvTotal) as TextView
        var totalValue = 0.0
        if (params == "0")
            for(ttl in itemsHarian) {
                totalValue += ttl.total
            }
        else
            for(ttl in itemsBulanan) {
                totalValue += ttl.total
            }
        tvTotal.text = AddingIDRCurrency().formatIdrCurrency(totalValue)

        val recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView
        if (params == "0") {
            val adapterList = context?.let {
                RekapHarianListAdapter(
                    itemsHarian,
                    it
                )
            }
            recyclerView.adapter = adapterList
            recyclerView.layoutManager = LinearLayoutManager(context)
        } else {
            val adapterList = context?.let {
                RekapBulananListAdapter(
                    itemsBulanan,
                    it
                )
            }
            recyclerView.adapter = adapterList
            recyclerView.layoutManager = (LinearLayoutManager(context))
        }
        return view
    }

    private val itemsHarian = arrayListOf(
        RekapHarianListElement(
            1, "Ayam Bakar Madu", 16000, 1, 0, 16000
        ),
        RekapHarianListElement(
            1, "Ayam Goreng Crispy", 15000, 1, 0, 15000
        ),
        RekapHarianListElement(
            1, "Es Campur", 8000, 1, 0, 8000
        ),
        RekapHarianListElement(
            1, "Jus Tobat", 8000, 1, 0, 8000
        )
    )

    private val itemsBulanan = arrayListOf(
        RekapBulananListElement(
            1, "1 Januari 2019", 16000, 0, 16000),
        RekapBulananListElement(
            2, "2 Januari 2019", 8000, 0, 8000),
        RekapBulananListElement(
            3, "3 Januari 2019", 100000, 0, 100000),
        RekapBulananListElement(
            4, "4 Januari 2019", 4500, 0, 4500),
        RekapBulananListElement(
            5, "5 Januari 2019", 30000, 0, 30000)
    )
}