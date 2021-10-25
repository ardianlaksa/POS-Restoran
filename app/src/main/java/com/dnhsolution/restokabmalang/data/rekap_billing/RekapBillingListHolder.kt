package com.dnhsolution.restokabmalang.data.rekap_billing

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency
import com.dnhsolution.restokabmalang.R
import java.util.HashMap

internal class RekapBillingListHolder private constructor(
    parent: View,
    context: Context,
    id: TextView,
    private val numItem: TextView,
    private val mastah: TextView,
    private val nilaiPajak: TextView,
    private val kodeBilling: TextView,
    private val tglBayar: TextView
) : RecyclerView.ViewHolder(parent) {

    private val listBulan: HashMap<String, String>
        get(){
            val bln = HashMap<String,String>()
            bln["1"] = "Jan"
            bln["2"] = "Feb"
            bln["3"] = "Mar"
            bln["4"] = "Apr"
            bln["5"] = "Mei"
            bln["6"] = "Jun"
            bln["7"] = "Jul"
            bln["8"] = "Agu"
            bln["9"] = "Sep"
            bln["10"] = "Okt"
            bln["11"] = "Nov"
            bln["12"] = "Des"
            return bln
        }

    fun setValues(number: String?, itemText: RekapBillingListElement) {
        numItem.text = number
        val pajakValue =
            AddingIDRCurrency().formatIdrCurrencyNonKoma(itemText.pajakTerutang.toDouble())
        nilaiPajak.text = pajakValue
        val tglBayarValue = itemText.tglBayar
        if (!tglBayarValue.equals("", ignoreCase = true)) tglBayar.text = tglBayarValue
        kodeBilling.text = itemText.kodeBilling
        var mastahValue = itemText.masaPajak + "-" + itemText.tahunMasaPajak
        for(value in listBulan){
            if(value.key == itemText.masaPajak){
                mastahValue = value.value + " " + itemText.tahunMasaPajak
            }
        }
        mastah.text = mastahValue
    }

    companion object {
        fun newInstance(parent: View, context: Context): RekapBillingListHolder {
            val tvMid = parent.findViewById<TextView>(R.id.mId)
            val tvNumItem = parent.findViewById<TextView>(R.id.tvNumItem)
            val tvMastah = parent.findViewById<TextView>(R.id.tvMastah)
            val tvNilaiPajak = parent.findViewById<TextView>(R.id.tvNilaiPajak)
            val tvBilling = parent.findViewById<TextView>(R.id.tvBilling)
            val tvTglBayar = parent.findViewById<TextView>(R.id.tvTglBayar)
            return RekapBillingListHolder(
                parent, context, tvMid, tvNumItem, tvMastah, tvNilaiPajak, tvBilling, tvTglBayar
            )
        }
    }

    init {
        parent.setOnClickListener { v: View? -> }
    }
}