package com.dnhsolution.restokabmalang.utilities

import java.text.DecimalFormat
import java.util.*

class AddingIDRCurrency {

    fun formatIdrCurrency(value: Double) : String {
        val formatter = DecimalFormat("#,###.##")
        var fText = formatter.format(value)
        fText = fText.format(value).replace(".",".")
        fText = fText.replace(",",".")
//        fText = fText.replace("-",",")
        var rupiah = String.format(Locale.getDefault(), "Rp. %s", fText)
        if (!rupiah.contains(",")) rupiah = "$rupiah,00"
        return rupiah
    }

    fun formatIdrCurrencyNonKoma(value: Double) : String {
        val formatter = DecimalFormat("#,###.##")
        var fText = formatter.format(value)
        fText = fText.format(value).replace(".",".")
        fText = fText.replace(",",".")
//        fText = fText.replace("-",",")
        var rupiah = String.format(Locale.getDefault(), "Rp. %s", fText)
        return rupiah
    }
}