package com.dnhsolution.restokabmalang.transaksi

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class KategoriListViewModel:ViewModel() {

    val items:MutableLiveData<KategoriListlement> by lazy {
        MutableLiveData<KategoriListlement>()
    }

}
