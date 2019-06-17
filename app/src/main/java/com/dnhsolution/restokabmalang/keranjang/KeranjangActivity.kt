package com.dnhsolution.restokabmalang.keranjang

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.ProdukSerializable
import kotlinx.android.synthetic.main.activity_keranjang.*

class KeranjangActivity:AppCompatActivity(),KeranjangProdukItemOnTask,View.OnClickListener {

    override fun onClick(v: View?) {

        when(v?.id) {
            R.id.bProses -> {
                val valueDiskon = etDiskon.text.toString()
                if ( valueDiskon == "") return
                else Toast.makeText(this,"Diskon tidak kosong",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var obyek:ArrayList<ProdukSerializable>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keranjang)

        setSupportActionBar(toolbar)

        bProses.setOnClickListener(this)

        val intent = intent
        val args = intent.getBundleExtra("BUNDLE")
        obyek = args.getSerializable("ARRAYLIST") as ArrayList<ProdukSerializable>
//        val name = obyek.get(1).name

        val produkAdapter = KeranjangProdukListAdapter(obyek, this,this)
        recyclerView.adapter = produkAdapter
        recyclerView?.layoutManager = (LinearLayoutManager(this))

        setTotal()

        etDiskon.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            private var current = ""

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "") return
                if (s.toString() != current)
                    etDiskon.removeTextChangedListener(this)

                setTotal()

                etDiskon.addTextChangedListener(this)
            }

        })
    }

    override fun keranjangProdukItemOnTask(position:Int, totalPrice: Int) {
        obyek[position].totalPrice = totalPrice
        setTotal()
    }

    private fun setTotal () {
        var valueTotalPrice = 0
        for (valueTotal in obyek) {
            valueTotalPrice += valueTotal.totalPrice
        }
        val valueDiskon = etDiskon.text.toString().toInt()
        valueTotalPrice = valueTotalPrice-(valueTotalPrice*valueDiskon/100)
        val rupiahValue = "Rp ${valueTotalPrice.toString()}"
        tvTotal.text = rupiahValue
    }
}