package com.dnhsolution.restokabmalang.data.rekap_harian

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.R

class RekapHarianFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rekap_harian, container, false)
        val recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView
        val adapterList = context?.let { SptpdTagihanListAdapter(items, it) }
        recyclerView.adapter = adapterList
        recyclerView.layoutManager = (LinearLayoutManager(context))
        return view
    }

    private val items = arrayListOf(
        RekapHarianListElement(
            1, "Ayam Bakar Madu", 16000,1,0,16000),
        RekapHarianListElement(
            1, "Ayam Goreng Crispy", 15000,1,0,15000),
        RekapHarianListElement(
            1, "Es Campur", 8000,1,0,8000),
        RekapHarianListElement(
            1, "Jus Tobat", 8000,1,0,8000)
    )
}